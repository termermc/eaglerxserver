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

package net.lax1dude.eaglercraft.backend.server.bukkit.event;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import io.netty.handler.codec.http.FullHttpRequest;
import net.lax1dude.eaglercraft.backend.server.adapter.event.IEventDispatchAdapter;
import net.lax1dude.eaglercraft.backend.server.adapter.event.IEventDispatchCallback;
import net.lax1dude.eaglercraft.backend.server.adapter.event.IRegisterSkinDelegate;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerConnection;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerLoginConnection;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerPendingConnection;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerPlayer;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerXServerAPI;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftAuthCheckRequiredEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftAuthCookieEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftAuthPasswordEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftClientBrandEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftDestroyPlayerEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftInitializePlayerEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftLoginEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftMOTDEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftRegisterSkinEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftRevokeSessionQueryEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftVoiceChangeEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftWebSocketOpenEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftWebViewChannelEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftWebViewChannelEvent.EnumEventType;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftWebViewMessageEvent;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftWebViewMessageEvent.EnumMessageType;
import net.lax1dude.eaglercraft.backend.server.api.query.IMOTDConnection;
import net.lax1dude.eaglercraft.backend.server.api.query.IQueryConnection;
import net.lax1dude.eaglercraft.backend.server.api.voice.EnumVoiceState;
import net.md_5.bungee.api.chat.BaseComponent;

public class BukkitEventDispatchAdapter implements IEventDispatchAdapter<Player, BaseComponent> {

	private IEaglerXServerAPI<Player> api;
	private final Plugin platformPlugin;
	private final Server server;
	private final PluginManager eventMgr;
	private final BukkitScheduler scheduler;

	public BukkitEventDispatchAdapter(Plugin platformPlugin, Server server, PluginManager eventMgr,
			BukkitScheduler scheduler) {
		this.platformPlugin = platformPlugin;
		this.server = server;
		this.eventMgr = eventMgr;
		this.scheduler = scheduler;
	}

	private <I, T extends Event> void fireSync(T event, IEventDispatchCallback<I> cont) {
		boolean async = event.isAsynchronous();
		if (async != server.isPrimaryThread()) {
			try {
				eventMgr.callEvent(event);
			} catch (Throwable t) {
				if (cont != null) {
					cont.complete(null, t);
				}
				return;
			}
			if (cont != null) {
				cont.complete((I) event, null);
			}
		} else {
			Runnable runnable = () -> {
				try {
					eventMgr.callEvent(event);
				} catch (Throwable t) {
					if (cont != null) {
						cont.complete(null, t);
					}
					return;
				}
				if (cont != null) {
					cont.complete((I) event, null);
				}
			};
			if (async) {
				scheduler.runTaskAsynchronously(platformPlugin, runnable);
			} else {
				scheduler.runTask(platformPlugin, runnable);
			}
		}
	}

	private <I, T extends Event> void fireAsync(T event, IEventDispatchCallback<I> cont) {
		scheduler.runTaskAsynchronously(platformPlugin, () -> {
			try {
				eventMgr.callEvent(event);
			} catch (Throwable t) {
				if (cont != null) {
					cont.complete(null, t);
				}
				return;
			}
			if (cont != null) {
				cont.complete((I) event, null);
			}
		});
	}

	@Override
	public void setAPI(IEaglerXServerAPI<Player> api) {
		this.api = api;
	}

	@Override
	public void dispatchAuthCheckRequired(IEaglerPendingConnection pendingConnection, boolean clientSolicitingPassword,
			byte[] authUsername,
			IEventDispatchCallback<IEaglercraftAuthCheckRequiredEvent<Player, BaseComponent>> onComplete) {
		fireAsync(new BukkitAuthCheckRequiredEventImpl(api, pendingConnection, clientSolicitingPassword, authUsername),
				onComplete);
	}

	@Override
	public void dispatchAuthCookieEvent(IEaglerLoginConnection loginConnection, byte[] authUsername,
			boolean nicknameSelectionEnabled, boolean cookiesEnabled, byte[] cookieData, String requestedUsername,
			String profileUsername, UUID profileUUID, byte authType, String authMessage, String authRequestedServer,
			IEventDispatchCallback<IEaglercraftAuthCookieEvent<Player, BaseComponent>> onComplete) {
		fireAsync(new BukkitAuthCookieEventImpl(api, loginConnection, authUsername, nicknameSelectionEnabled,
				cookiesEnabled, cookieData, requestedUsername, profileUsername, profileUUID, authType, authMessage,
				authRequestedServer), onComplete);
	}

	@Override
	public void dispatchAuthPasswordEvent(IEaglerLoginConnection loginConnection, byte[] authUsername,
			boolean nicknameSelectionEnabled, byte[] authSaltingData, byte[] authPasswordData, boolean cookiesEnabled,
			byte[] cookieData, String requestedUsername, String profileUsername, UUID profileUUID, byte authType,
			String authMessage, String authRequestedServer,
			IEventDispatchCallback<IEaglercraftAuthPasswordEvent<Player, BaseComponent>> onComplete) {
		fireAsync(new BukkitAuthPasswordEventImpl(api, loginConnection, authUsername, nicknameSelectionEnabled,
				authSaltingData, authPasswordData, cookiesEnabled, cookieData, requestedUsername, profileUsername,
				profileUUID, authType, authMessage, authRequestedServer), onComplete);
	}

	@Override
	public void dispatchClientBrandEvent(IEaglerPendingConnection pendingConnection,
			IEventDispatchCallback<IEaglercraftClientBrandEvent<Player, BaseComponent>> onComplete) {
		fireAsync(new BukkitClientBrandEventImpl(api, pendingConnection), onComplete);
	}

	@Override
	public void dispatchLoginEvent(IEaglerLoginConnection loginConnection, boolean redirectSupport,
			String requestedServer, IEventDispatchCallback<IEaglercraftLoginEvent<Player, BaseComponent>> onComplete) {
		fireAsync(new BukkitLoginEventImpl(api, loginConnection, redirectSupport, requestedServer), onComplete);
	}

	@Override
	public void dispatchInitializePlayerEvent(IEaglerPlayer<Player> player, Map<String, byte[]> extraProfileData,
			IEventDispatchCallback<IEaglercraftInitializePlayerEvent<Player>> onComplete) {
		fireSync(new BukkitInitializePlayerEventImpl(api, player, extraProfileData), onComplete);
	}

	@Override
	public void dispatchDestroyPlayerEvent(IEaglerPlayer<Player> player,
			IEventDispatchCallback<IEaglercraftDestroyPlayerEvent<Player>> onComplete) {
		fireSync(new BukkitDestroyPlayerEventImpl(api, player), onComplete);
	}

	@Override
	public void dispatchMOTDEvent(IMOTDConnection connection,
			IEventDispatchCallback<IEaglercraftMOTDEvent<Player>> onComplete) {
		fireSync(new BukkitMOTDEventImpl(api, connection), onComplete);
	}

	@Override
	public void dispatchRegisterSkinEvent(IEaglerLoginConnection loginConnection, IRegisterSkinDelegate delegate,
			IEventDispatchCallback<IEaglercraftRegisterSkinEvent<Player>> onComplete) {
		fireAsync(new BukkitRegisterSkinEventImpl(api, loginConnection, delegate), onComplete);
	}

	@Override
	public void dispatchRevokeSessionQueryEvent(IQueryConnection query, byte[] cookieData,
			IEventDispatchCallback<IEaglercraftRevokeSessionQueryEvent<Player>> onComplete) {
		fireAsync(new BukkitRevokeSessionQueryEventImpl(api, query, cookieData), onComplete);
	}

	@Override
	public void dispatchVoiceChangeEvent(IEaglerPlayer<Player> player, EnumVoiceState voiceStateOld,
			EnumVoiceState voiceStateNew, IEventDispatchCallback<IEaglercraftVoiceChangeEvent<Player>> onComplete) {
		fireSync(new BukkitVoiceChangeEventImpl(api, player, voiceStateOld, voiceStateNew), onComplete);
	}

	@Override
	public void dispatchWebSocketOpenEvent(IEaglerConnection connection, FullHttpRequest request,
			IEventDispatchCallback<IEaglercraftWebSocketOpenEvent<Player>> onComplete) {
		fireSync(new BukkitWebSocketOpenEventImpl(api, connection, request), onComplete);
	}

	@Override
	public void dispatchWebViewChannelEvent(IEaglerPlayer<Player> player, EnumEventType type, String channel,
			IEventDispatchCallback<IEaglercraftWebViewChannelEvent<Player>> onComplete) {
		fireSync(new BukkitWebViewChannelEventImpl(api, player, type, channel), onComplete);
	}

	@Override
	public void dispatchWebViewMessageEvent(IEaglerPlayer<Player> player, String channel, EnumMessageType type,
			byte[] data, IEventDispatchCallback<IEaglercraftWebViewMessageEvent<Player>> onComplete) {
		fireSync(new BukkitWebViewMessageEventImpl(api, player, channel, type, data), onComplete);
	}

}
