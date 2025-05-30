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

package net.lax1dude.eaglercraft.backend.rpc.base.local;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import net.lax1dude.eaglercraft.backend.rpc.api.IBasePlayerRPC;
import net.lax1dude.eaglercraft.backend.rpc.api.IEaglerPlayerRPC;
import net.lax1dude.eaglercraft.backend.rpc.api.IRPCCloseHandler;
import net.lax1dude.eaglercraft.backend.rpc.api.IRPCFuture;
import net.lax1dude.eaglercraft.backend.rpc.api.data.TexturesData;
import net.lax1dude.eaglercraft.backend.rpc.api.skins.EnumPresetCapes;
import net.lax1dude.eaglercraft.backend.rpc.api.skins.EnumPresetSkins;
import net.lax1dude.eaglercraft.backend.rpc.api.skins.EnumSkinModel;
import net.lax1dude.eaglercraft.backend.rpc.api.skins.IEaglerPlayerCape;
import net.lax1dude.eaglercraft.backend.rpc.api.skins.IEaglerPlayerSkin;
import net.lax1dude.eaglercraft.backend.rpc.api.voice.IVoiceManager;
import net.lax1dude.eaglercraft.backend.rpc.base.RPCBiConsumerFuture;
import net.lax1dude.eaglercraft.backend.rpc.base.RPCConsumerFuture;
import net.lax1dude.eaglercraft.backend.rpc.base.RPCImmediateFuture;
import net.lax1dude.eaglercraft.backend.rpc.base.SchedulerExecutors;
import net.lax1dude.eaglercraft.backend.server.api.skins.ISkinManagerBase;

public class BasePlayerRPCLocal<PlayerObject> implements IBasePlayerRPC<PlayerObject> {

	protected final IRPCFuture<IBasePlayerRPC<PlayerObject>> future;
	protected final PlayerInstanceLocal<PlayerObject> owner;
	protected final net.lax1dude.eaglercraft.backend.server.api.IBasePlayer<PlayerObject> delegate;
	protected final SchedulerExecutors schedulerExecutors;
	protected Set<IRPCCloseHandler> closeListeners;
	protected int dummyTimeout;

	BasePlayerRPCLocal(PlayerInstanceLocal<PlayerObject> player,
			net.lax1dude.eaglercraft.backend.server.api.IBasePlayer<PlayerObject> delegate) {
		this.schedulerExecutors = player.server.schedulerExecutors();
		this.future = RPCImmediateFuture.create(schedulerExecutors, this);
		this.owner = player;
		this.delegate = delegate;
		this.dummyTimeout = player.server.getBaseRequestTimeout();
	}

	@Override
	public EaglerXBackendRPCLocal<PlayerObject> getServerAPI() {
		return owner.server;
	}

	@Override
	public PlayerInstanceLocal<PlayerObject> getPlayer() {
		return owner;
	}

	@Override
	public boolean isEaglerPlayer() {
		return false;
	}

	@Override
	public IEaglerPlayerRPC<PlayerObject> asEaglerPlayer() {
		return null;
	}

	@Override
	public boolean isOpen() {
		return owner.player.isConnected();
	}

	@Override
	public int getRPCProtocolVersion() {
		return -1;
	}

	@Override
	public int getMinecraftProtocolVersion() {
		return delegate.getMinecraftProtocol();
	}

	@Override
	public int getSupervisorNodeId() {
		return -1;
	}

	@Override
	public synchronized void addCloseListener(IRPCCloseHandler handler) {
		if (handler == null) {
			throw new NullPointerException("handler");
		}
		if (closeListeners == null) {
			closeListeners = Collections.newSetFromMap(new HashMap<>(4));
		}
		closeListeners.add(handler);
	}

	@Override
	public synchronized void removeCloseListener(IRPCCloseHandler handler) {
		if (handler == null) {
			throw new NullPointerException("handler");
		}
		if (closeListeners != null && closeListeners.remove(handler) && closeListeners.isEmpty()) {
			closeListeners = null;
		}
	}

	void fireCloseListeners() {
		Object[] handlers;
		synchronized (this) {
			if (closeListeners == null) {
				return;
			}
			handlers = closeListeners.toArray();
		}
		for (int i = 0; i < handlers.length; ++i) {
			IRPCCloseHandler handler = (IRPCCloseHandler) handlers[i];
			try {
				handler.handleClosed();
			} catch (Exception ex) {
				owner.logger().error("Caught exception while calling RPC close listener", ex);
			}
		}
	}

	@Override
	public void setBaseRequestTimeout(int seconds) {
		dummyTimeout = seconds;
	}

	@Override
	public int getBaseRequestTimeout() {
		return dummyTimeout;
	}

	@Override
	public IRPCFuture<IEaglerPlayerSkin> getPlayerSkin() {
		ISkinManagerBase<PlayerObject> skinMgr = delegate.getSkinManager();
		net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin skin;
		skin = skinMgr.getPlayerSkinIfLoaded();
		if (skin != null) {
			return RPCImmediateFuture.create(schedulerExecutors, SkinTypesHelper.wrap(skin));
		} else {
			RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, IEaglerPlayerSkin> consumerFuture = new RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, IEaglerPlayerSkin>(
					schedulerExecutors) {
				@Override
				public void accept(net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin t) {
					set(SkinTypesHelper.wrap(skin));
				}
			};
			skinMgr.resolvePlayerSkin(consumerFuture);
			return consumerFuture;
		}
	}

	@Override
	public IRPCFuture<IEaglerPlayerSkin> getPlayerSkin(int timeoutSec) {
		return getPlayerSkin();
	}

	@Override
	public void changePlayerSkin(IEaglerPlayerSkin skin, boolean notifyOthers) {
		if (skin == null) {
			throw new NullPointerException("skin");
		}
		delegate.getSkinManager().changePlayerSkin(SkinTypesHelper.unwrap(skin), notifyOthers);
	}

	@Override
	public void changePlayerSkin(EnumPresetSkins skin, boolean notifyOthers) {
		if (skin == null) {
			throw new NullPointerException("skin");
		}
		delegate.getSkinManager().changePlayerSkin(SkinTypesHelper.unwrap(skin), notifyOthers);
	}

	@Override
	public IRPCFuture<IEaglerPlayerCape> getPlayerCape() {
		ISkinManagerBase<PlayerObject> skinMgr = delegate.getSkinManager();
		net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape cape;
		cape = skinMgr.getPlayerCapeIfLoaded();
		if (cape != null) {
			return RPCImmediateFuture.create(schedulerExecutors, SkinTypesHelper.wrap(cape));
		} else {
			RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape, IEaglerPlayerCape> consumerFuture = new RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape, IEaglerPlayerCape>(
					schedulerExecutors) {
				@Override
				public void accept(net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape cape) {
					set(SkinTypesHelper.wrap(cape));
				}
			};
			skinMgr.resolvePlayerCape(consumerFuture);
			return consumerFuture;
		}
	}

	@Override
	public IRPCFuture<IEaglerPlayerCape> getPlayerCape(int timeoutSec) {
		return getPlayerCape();
	}

	@Override
	public void changePlayerCape(IEaglerPlayerCape cape, boolean notifyOthers) {
		if (cape == null) {
			throw new NullPointerException("cape");
		}
		delegate.getSkinManager().changePlayerCape(SkinTypesHelper.unwrap(cape), notifyOthers);
	}

	@Override
	public void changePlayerCape(EnumPresetCapes cape, boolean notifyOthers) {
		if (cape == null) {
			throw new NullPointerException("cape");
		}
		delegate.getSkinManager().changePlayerCape(SkinTypesHelper.unwrap(cape), notifyOthers);
	}

	@Override
	public IRPCFuture<TexturesData> getPlayerTextures() {
		ISkinManagerBase<PlayerObject> skinMgr = delegate.getSkinManager();
		net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin skin;
		net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape cape;
		skin = skinMgr.getPlayerSkinIfLoaded();
		cape = skinMgr.getPlayerCapeIfLoaded();
		if (skin != null && cape != null) {
			return RPCImmediateFuture.create(schedulerExecutors,
					TexturesData.create(SkinTypesHelper.wrap(skin), SkinTypesHelper.wrap(cape)));
		} else {
			RPCBiConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape, TexturesData> consumerFuture = new RPCBiConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape, TexturesData>(
					schedulerExecutors) {
				@Override
				public void accept(net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin skin,
						net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape cape) {
					set(TexturesData.create(SkinTypesHelper.wrap(skin), SkinTypesHelper.wrap(cape)));
				}
			};
			skinMgr.resolvePlayerTextures(consumerFuture);
			return consumerFuture;
		}
	}

	@Override
	public IRPCFuture<TexturesData> getPlayerTextures(int timeoutSec) {
		return getPlayerTextures();
	}

	@Override
	public void changePlayerTextures(IEaglerPlayerSkin skin, IEaglerPlayerCape cape, boolean notifyOthers) {
		if (skin == null) {
			throw new NullPointerException("skin");
		}
		if (cape == null) {
			throw new NullPointerException("cape");
		}
		delegate.getSkinManager().changePlayerTextures(SkinTypesHelper.unwrap(skin), SkinTypesHelper.unwrap(cape),
				notifyOthers);
	}

	@Override
	public void changePlayerTextures(EnumPresetSkins skin, EnumPresetCapes cape, boolean notifyOthers) {
		if (skin == null) {
			throw new NullPointerException("skin");
		}
		if (cape == null) {
			throw new NullPointerException("cape");
		}
		delegate.getSkinManager().changePlayerTextures(SkinTypesHelper.unwrap(skin), SkinTypesHelper.unwrap(cape),
				notifyOthers);
	}

	@Override
	public void resetPlayerSkin(boolean notifyOthers) {
		delegate.getSkinManager().resetPlayerSkin(notifyOthers);
	}

	@Override
	public void resetPlayerCape(boolean notifyOthers) {
		delegate.getSkinManager().resetPlayerCape(notifyOthers);
	}

	@Override
	public void resetPlayerTextures(boolean notifyOthers) {
		delegate.getSkinManager().resetPlayerTextures(notifyOthers);
	}

	@Override
	public IRPCFuture<UUID> getProfileUUID() {
		return RPCImmediateFuture.create(schedulerExecutors, delegate.getUniqueId());
	}

	@Override
	public IRPCFuture<UUID> getProfileUUID(int timeoutSec) {
		return getProfileUUID();
	}

	@Override
	public IRPCFuture<String> getMinecraftBrand() {
		return RPCImmediateFuture.create(schedulerExecutors, delegate.getMinecraftBrand());
	}

	@Override
	public IRPCFuture<String> getMinecraftBrand(int timeoutSec) {
		return getMinecraftBrand();
	}

	@Override
	public IRPCFuture<UUID> getBrandUUID() {
		return RPCImmediateFuture.create(schedulerExecutors, delegate.getEaglerBrandUUID());
	}

	@Override
	public IRPCFuture<UUID> getBrandUUID(int timeoutSec) {
		return getBrandUUID();
	}

	@Override
	public IRPCFuture<IEaglerPlayerSkin> getSkinByURL(String url) {
		if (url == null) {
			throw new NullPointerException("url");
		}
		RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, IEaglerPlayerSkin> consumerFuture = new RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, IEaglerPlayerSkin>(
				schedulerExecutors) {
			@Override
			public void accept(net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin skin) {
				set(SkinTypesHelper.wrap(skin));
			}
		};
		delegate.getServerAPI().getSkinService().loadCacheSkinFromURL(url, consumerFuture);
		return consumerFuture;
	}

	@Override
	public IRPCFuture<IEaglerPlayerSkin> getSkinByURL(String url, int timeoutSec) {
		return getSkinByURL(url);
	}

	@Override
	public IRPCFuture<IEaglerPlayerSkin> getSkinByURL(String url, EnumSkinModel modelId) {
		if (url == null) {
			throw new NullPointerException("url");
		}
		if (modelId == null) {
			throw new NullPointerException("modelId");
		}
		RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, IEaglerPlayerSkin> consumerFuture = new RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin, IEaglerPlayerSkin>(
				schedulerExecutors) {
			@Override
			public void accept(net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin skin) {
				set(SkinTypesHelper.wrap(skin));
			}
		};
		delegate.getServerAPI().getSkinService().loadCacheSkinFromURL(url, SkinTypesHelper.unwrap(modelId),
				consumerFuture);
		return consumerFuture;
	}

	@Override
	public IRPCFuture<IEaglerPlayerSkin> getSkinByURL(String url, int timeoutSec, EnumSkinModel modelId) {
		return getSkinByURL(url, modelId);
	}

	@Override
	public IRPCFuture<IEaglerPlayerCape> getCapeByURL(String url) {
		if (url == null) {
			throw new NullPointerException("url");
		}
		RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape, IEaglerPlayerCape> consumerFuture = new RPCConsumerFuture<net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape, IEaglerPlayerCape>(
				schedulerExecutors) {
			@Override
			public void accept(net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape skin) {
				set(SkinTypesHelper.wrap(skin));
			}
		};
		delegate.getServerAPI().getSkinService().loadCacheCapeFromURL(url, consumerFuture);
		return consumerFuture;
	}

	@Override
	public IRPCFuture<IEaglerPlayerCape> getCapeByURL(String url, int timeoutSec) {
		return getCapeByURL(url);
	}

	@Override
	public void sendRawCustomPayloadPacket(String channel, byte[] data) {
		owner.player.sendData(channel, data);
	}

	public boolean isVoiceCapable() {
		return false;
	}

	public IVoiceManager<PlayerObject> getVoiceManager() {
		return null;
	}

}
