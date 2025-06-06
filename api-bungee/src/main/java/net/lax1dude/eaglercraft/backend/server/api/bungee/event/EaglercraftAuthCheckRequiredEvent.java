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

package net.lax1dude.eaglercraft.backend.server.api.bungee.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.lax1dude.eaglercraft.backend.server.api.IEaglerPendingConnection;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerXServerAPI;
import net.lax1dude.eaglercraft.backend.server.api.event.IEaglercraftAuthCheckRequiredEvent;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.AsyncEvent;

public final class EaglercraftAuthCheckRequiredEvent
		extends AsyncEvent<IEaglercraftAuthCheckRequiredEvent<ProxiedPlayer, BaseComponent>>
		implements IEaglercraftAuthCheckRequiredEvent<ProxiedPlayer, BaseComponent> {

	private final IEaglerXServerAPI<ProxiedPlayer> api;
	private final IEaglerPendingConnection pendingConnection;
	private final boolean clientSolicitingPassword;
	private final byte[] authUsername;
	private boolean nicknameSelectionEnabled;
	private byte[] saltingData;
	private byte authType;
	private EnumAuthResponse authRequired;
	private String authMessage = "enter the code:";
	private BaseComponent kickMessage;
	private boolean cookieAuth;

	public EaglercraftAuthCheckRequiredEvent(@Nonnull IEaglerXServerAPI<ProxiedPlayer> api,
			@Nonnull IEaglerPendingConnection pendingConnection, boolean clientSolicitingPassword,
			@Nonnull byte[] authUsername,
			@Nonnull Callback<IEaglercraftAuthCheckRequiredEvent<ProxiedPlayer, BaseComponent>> cb) {
		super(cb);
		this.api = api;
		this.pendingConnection = pendingConnection;
		this.clientSolicitingPassword = clientSolicitingPassword;
		this.authUsername = authUsername;
	}

	@Nonnull
	@Override
	public IEaglerXServerAPI<ProxiedPlayer> getServerAPI() {
		return api;
	}

	@Nonnull
	@Override
	public IEaglerPendingConnection getPendingConnection() {
		return pendingConnection;
	}

	@Override
	public boolean isClientSolicitingPassword() {
		return clientSolicitingPassword;
	}

	@Nonnull
	@Override
	public byte[] getAuthUsername() {
		return authUsername;
	}

	@Override
	public boolean isNicknameSelectionEnabled() {
		return nicknameSelectionEnabled;
	}

	@Override
	public void setNicknameSelectionEnabled(boolean enable) {
		nicknameSelectionEnabled = enable;
	}

	@Nullable
	@Override
	public byte[] getSaltingData() {
		return saltingData;
	}

	@Override
	public void setSaltingData(@Nullable byte[] saltingData) {
		this.saltingData = saltingData;
	}

	@Override
	public byte getUseAuthTypeRaw() {
		return authType;
	}

	@Override
	public void setUseAuthTypeRaw(byte authType) {
		this.authType = authType;
	}

	@Nullable
	@Override
	public EnumAuthResponse getAuthRequired() {
		return authRequired;
	}

	@Override
	public void setAuthRequired(@Nullable EnumAuthResponse authRequired) {
		this.authRequired = authRequired;
	}

	@Nullable
	@Override
	public String getAuthMessage() {
		return authMessage;
	}

	@Override
	public void setAuthMessage(@Nullable String authMessage) {
		if (authMessage == null) {
			throw new NullPointerException("authMessage");
		}
		this.authMessage = authMessage;
	}

	@Override
	public boolean getEnableCookieAuth() {
		return cookieAuth;
	}

	@Override
	public void setEnableCookieAuth(boolean enable) {
		this.cookieAuth = enable;
	}

	@Nullable
	@Override
	public BaseComponent getKickMessage() {
		return kickMessage;
	}

	@Override
	public void setKickMessage(@Nullable BaseComponent kickMessage) {
		this.kickMessage = kickMessage;
	}

	@Override
	public void setKickMessage(@Nullable String kickMessage) {
		this.kickMessage = kickMessage != null ? new TextComponent(kickMessage) : null;
	}

}
