/*
 * Copyright (c) 2025 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package net.lax1dude.eaglercraft.backend.server.base.voice;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.lax1dude.eaglercraft.backend.server.api.IEaglerPlayer;
import net.lax1dude.eaglercraft.backend.server.api.collect.ObjectIndexedContainer;
import net.lax1dude.eaglercraft.backend.server.api.voice.EnumVoiceState;
import net.lax1dude.eaglercraft.backend.server.api.voice.IVoiceChannel;
import net.lax1dude.eaglercraft.backend.server.base.EaglerPlayerInstance;
import net.lax1dude.eaglercraft.backend.server.base.collect.ObjectArrayList;
import net.lax1dude.eaglercraft.backend.server.base.collect.ObjectObjectHashMap;
import net.lax1dude.eaglercraft.backend.server.util.Collectors3;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.GameMessagePacket;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalConnectAnnounceV4EAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalConnectV3EAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalConnectV4EAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalDescEAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalDisconnectPeerEAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalGlobalEAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketVoiceSignalICEEAG;

class VoiceChannel<PlayerObject> implements IVoiceChannel {

	public static final long REQUEST_TIMEOUT = 2000l;

	final VoiceServiceLocal<PlayerObject> owner;
	final ConcurrentMap<UUID, Context> connectedPlayers = new ConcurrentHashMap<>();

	VoiceChannel(VoiceServiceLocal<PlayerObject> owner) {
		this.owner = owner;
	}

	void addToChannel(VoiceManagerLocal<PlayerObject> mgr) {
		Context oldContext = mgr.xchgActiveChannel(null);
		boolean connect = false;
		if (oldContext != null) {
			connect = oldContext.finish(false);
		}
		Context newContext = new Context(mgr);
		oldContext = mgr.xchgActiveChannel(newContext);
		if (oldContext != null) {
			connect = oldContext.finish(true);
		}
		if (connect) {
			newContext.handleVoiceSignalPacketTypeConnect();
		}
	}

	void removeFromChannel(VoiceManagerLocal<PlayerObject> mgr, boolean dead) {
		Context oldContext = mgr.xchgActiveChannel(null);
		if (oldContext != null) {
			oldContext.finish(dead);
		}
	}

	interface IVoiceState {
		int expired(long nanos);
	}

	static class PendingState implements IVoiceState {

		final long expiry;

		PendingState(long createdAt) {
			expiry = createdAt + (REQUEST_TIMEOUT * 1000000l);
		}

		@Override
		public int expired(long nanos) {
			return nanos > expiry ? 1 : 0;
		}

	}

	static final IVoiceState ESTABLISHED = (l) -> -1;

	class Context extends ObjectObjectHashMap<Context, IVoiceState> {

		final VoiceManagerLocal<PlayerObject> mgr;
		final UUID selfUUID;
		long lastFlush = 0;
		boolean expirable = false;

		private Context(VoiceManagerLocal<PlayerObject> mgr) {
			this.mgr = mgr;
			this.selfUUID = mgr.player.getUniqueId();
		}

		void handleVoiceSignalPacketTypeConnect() {
			if (!mgr.ratelimitCon()) {
				return;
			}
			boolean empty = connectedPlayers.isEmpty();
			if (connectedPlayers.putIfAbsent(selfUUID, this) != null) {
				return;
			}
			mgr.onStateChanged(EnumVoiceState.ENABLED);
			if (empty) {
				return;
			}
			Object[] allPlayers = connectedPlayers.values().toArray();
			int len = allPlayers.length;
			SPacketVoiceSignalGlobalEAG.UserData[] userDatas = new SPacketVoiceSignalGlobalEAG.UserData[len];
			for (int i = 0; i < len; ++i) {
				Context ctx = (Context) allPlayers[i];
				userDatas[i] = new SPacketVoiceSignalGlobalEAG.UserData(ctx.selfUUID.getMostSignificantBits(),
						ctx.selfUUID.getLeastSignificantBits(), ctx.mgr.player.getUsername());
			}
			GameMessagePacket packetToBroadcast = new SPacketVoiceSignalGlobalEAG(Arrays.asList(userDatas));
			for (int i = 0; i < len; ++i) {
				((Context) allPlayers[i]).mgr.player.sendEaglerMessage(packetToBroadcast);
			}
			EaglerPlayerInstance<PlayerObject> self = mgr.player;
			boolean selfV3 = self.getEaglerProtocol().ver <= 3;
			GameMessagePacket v3p = null;
			GameMessagePacket v4p = null;
			for (int i = 0; i < len; ++i) {
				Context ctx = (Context) allPlayers[i];
				if (ctx != this) {
					EaglerPlayerInstance<PlayerObject> ctxPlayer = ctx.mgr.player;
					if (ctxPlayer.getEaglerProtocol().ver <= 3) {
						ctxPlayer.sendEaglerMessage(v3p == null
								? (v3p = new SPacketVoiceSignalConnectV3EAG(selfUUID.getMostSignificantBits(),
										selfUUID.getLeastSignificantBits(), true, false))
								: v3p);
					} else {
						ctxPlayer
								.sendEaglerMessage(v4p == null
										? (v4p = new SPacketVoiceSignalConnectAnnounceV4EAG(
												selfUUID.getMostSignificantBits(), selfUUID.getLeastSignificantBits()))
										: v4p);
					}
					if (selfV3) {
						self.sendEaglerMessage(new SPacketVoiceSignalConnectV3EAG(ctx.selfUUID.getMostSignificantBits(),
								ctx.selfUUID.getLeastSignificantBits(), true, false));
					} else {
						self.sendEaglerMessage(new SPacketVoiceSignalConnectAnnounceV4EAG(
								ctx.selfUUID.getMostSignificantBits(), ctx.selfUUID.getLeastSignificantBits()));
					}
				}
			}
		}

		private boolean putRequest(Context other) {
			long nanos = System.nanoTime();
			if (putIfAbsent(other, new PendingState(nanos))) {
				if (!expirable) {
					lastFlush = nanos;
					expirable = true;
				}
				return true;
			} else {
				return false;
			}
		}

		private IVoiceState checkState(Context other) {
			long nanos = System.nanoTime();
			if (expirable) {
				if (nanos - lastFlush > (30l * 1000l * 1000l * 1000l)) {
					expirable = false;
					this.removeAll((k, v) -> {
						int i = v.expired(nanos);
						if (i == 1) {
							return true;
						} else if (i == 0) {
							expirable = true;
						}
						return false;
					});
					return get(other);
				}
			}
			IVoiceState state = get(other);
			if (state != null && state.expired(nanos) == 1) {
				remove(other);
				return null;
			}
			return state;
		}

		void handleVoiceSignalPacketTypeRequest(UUID player) {
			if (!mgr.ratelimitReqV5()) {
				return;
			}
			Context other = connectedPlayers.get(player);
			if (other != null && other != this) {
				IVoiceState newState = null;
				synchronized (other) {
					IVoiceState otherState = other.checkState(this);
					if (otherState == ESTABLISHED) {
						return;
					} else if (otherState != null) {
						newState = ESTABLISHED;
						other.put(this, newState);
					}
				}
				synchronized (this) {
					if (newState == null) {
						putRequest(other);
						return;
					} else {
						put(other, newState);
					}
				}
				EaglerPlayerInstance<PlayerObject> otherPlayer = other.mgr.player;
				if (otherPlayer.getEaglerProtocol().ver <= 3) {
					otherPlayer.sendEaglerMessage(new SPacketVoiceSignalConnectV3EAG(selfUUID.getMostSignificantBits(),
							selfUUID.getLeastSignificantBits(), false, false));
				} else {
					otherPlayer.sendEaglerMessage(new SPacketVoiceSignalConnectV4EAG(selfUUID.getMostSignificantBits(),
							selfUUID.getLeastSignificantBits(), false));
				}
				EaglerPlayerInstance<PlayerObject> self = mgr.player;
				if (self.getEaglerProtocol().ver <= 3) {
					self.sendEaglerMessage(new SPacketVoiceSignalConnectV3EAG(player.getMostSignificantBits(),
							player.getLeastSignificantBits(), false, true));
				} else {
					self.sendEaglerMessage(new SPacketVoiceSignalConnectV4EAG(player.getMostSignificantBits(),
							player.getLeastSignificantBits(), true));
				}
			}
		}

		void handleVoiceSignalPacketTypeICE(UUID player, byte[] str) {
			if (!mgr.ratelimitICE()) {
				return;
			}
			Context other = connectedPlayers.get(player);
			if (other != null && other != this) {
				synchronized (this) {
					if (checkState(other) != ESTABLISHED) {
						return;
					}
				}
				other.mgr.player.sendEaglerMessage(new SPacketVoiceSignalICEEAG(selfUUID.getMostSignificantBits(),
						selfUUID.getLeastSignificantBits(), str));
			}
		}

		void handleVoiceSignalPacketTypeDesc(UUID player, byte[] str) {
			if (!mgr.ratelimitICE()) {
				return;
			}
			Context other = connectedPlayers.get(player);
			if (other != null && other != this) {
				synchronized (this) {
					if (checkState(other) != ESTABLISHED) {
						return;
					}
				}
				other.mgr.player.sendEaglerMessage(new SPacketVoiceSignalDescEAG(selfUUID.getMostSignificantBits(),
						selfUUID.getLeastSignificantBits(), str));
			}
		}

		void handleVoiceSignalPacketTypeDisconnectPeer(UUID player) {
			Context other = connectedPlayers.get(player);
			if (other != null && other != this) {
				IVoiceState state;
				synchronized (this) {
					state = remove(other);
				}
				if (state == ESTABLISHED) {
					synchronized (other) {
						other.remove(this);
					}
					other.mgr.player.sendEaglerMessage(new SPacketVoiceSignalDisconnectPeerEAG(
							selfUUID.getMostSignificantBits(), selfUUID.getLeastSignificantBits()));
					mgr.player.sendEaglerMessage(new SPacketVoiceSignalDisconnectPeerEAG(
							player.getMostSignificantBits(), player.getLeastSignificantBits()));
				}
			}
		}

		void handleVoiceSignalPacketTypeDisconnect() {
			handleRemove(true);
		}

		boolean finish(boolean dead) {
			return handleRemove(dead);
		}

		private boolean handleRemove(boolean dead) {
			if (connectedPlayers.remove(selfUUID) == null) {
				return false;
			}
			mgr.onStateChanged(EnumVoiceState.DISABLED);
			Object[] allPlayers = connectedPlayers.values().toArray();
			int len = allPlayers.length;
			ObjectIndexedContainer<Context> toNotify = null;
			synchronized (this) {
				if (size() > 0) {
					toNotify = new ObjectArrayList<>(keys());
					clear();
				}
				expirable = false;
			}
			if (toNotify != null) {
				int cnt = toNotify.size();
				GameMessagePacket pkt = new SPacketVoiceSignalDisconnectPeerEAG(selfUUID.getMostSignificantBits(),
						selfUUID.getLeastSignificantBits());
				for (int i = 0; i < cnt; ++i) {
					Context ctx = toNotify.get(i);
					IVoiceState voice;
					synchronized (ctx) {
						voice = ctx.remove(this);
					}
					if (voice != null) {
						if (!dead) {
							UUID uuid = ctx.selfUUID;
							mgr.player.sendEaglerMessage(new SPacketVoiceSignalDisconnectPeerEAG(
									uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
						}
						if (voice == ESTABLISHED) {
							ctx.mgr.player.sendEaglerMessage(pkt);
						}
					}
				}
			}
			if (len > 0) {
				SPacketVoiceSignalGlobalEAG.UserData[] userDatas = new SPacketVoiceSignalGlobalEAG.UserData[len];
				for (int i = 0; i < len; ++i) {
					Context ctx = (Context) allPlayers[i];
					EaglerPlayerInstance<PlayerObject> ctxPlayer = ctx.mgr.player;
					UUID ctxUUID = ctxPlayer.getUniqueId();
					userDatas[i] = new SPacketVoiceSignalGlobalEAG.UserData(ctxUUID.getMostSignificantBits(),
							ctxUUID.getLeastSignificantBits(), ctxPlayer.getUsername());
				}
				GameMessagePacket packetToBroadcast = new SPacketVoiceSignalGlobalEAG(Arrays.asList(userDatas));
				for (int i = 0; i < len; ++i) {
					((Context) allPlayers[i]).mgr.player.sendEaglerMessage(packetToBroadcast);
				}
			}
			return true;
		}

		boolean isConnected() {
			return connectedPlayers.containsKey(selfUUID);
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object o) {
			return this == o;
		}

	}

	Collection<IEaglerPlayer<PlayerObject>> listConnectedPlayers() {
		return connectedPlayers.values().stream().map((ctx) -> ctx.mgr.player).collect(Collectors3.toImmutableList());
	}

	@Override
	public boolean isManaged() {
		return false;
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

}
