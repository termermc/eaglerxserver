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

package net.lax1dude.eaglercraft.backend.server.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import com.google.common.collect.ImmutableList;

import io.netty.channel.Channel;
import net.lax1dude.eaglercraft.backend.server.adapter.IEaglerXServerListener;
import net.lax1dude.eaglercraft.backend.server.api.IEaglerListenerInfo;
import net.lax1dude.eaglercraft.backend.server.api.ITLSManager;
import net.lax1dude.eaglercraft.backend.server.api.attribute.IAttributeKey;
import net.lax1dude.eaglercraft.backend.server.base.EaglerAttributeManager.EaglerAttributeHolder;
import net.lax1dude.eaglercraft.backend.server.base.config.ConfigDataListener;
import net.lax1dude.eaglercraft.backend.server.base.pipeline.WebSocketEaglerInitialHandler;
import net.lax1dude.eaglercraft.backend.server.util.RateLimiterExclusions;

public class EaglerListener implements IEaglerListenerInfo, IEaglerXServerListener {

	private final EaglerXServer<?> server;
	private final SocketAddress address;
	private final ConfigDataListener listenerConf;
	private final EaglerAttributeHolder attrHolder;
	private final boolean sslPluginManaged;
	private final ISSLContextProvider sslContext;
	private final byte[] legacyRedirectAddressBuf;
	private byte[] cachedServerIcon;
	private List<String> cachedServerMOTD;
	private CompoundRateLimiterMap rateLimiter;

	EaglerListener(EaglerXServer<?> server, ConfigDataListener listenerConf) throws SSLException, IOException {
		this(server, listenerConf.getInjectAddress(), listenerConf);
	}

	EaglerListener(EaglerXServer<?> server, SocketAddress address, ConfigDataListener listenerConf)
			throws SSLException, IOException {
		this.server = server;
		this.address = address;
		this.listenerConf = listenerConf;
		this.attrHolder = server.getEaglerAttribManager().createEaglerHolder();
		if (listenerConf.isEnableTLS()) {
			this.sslPluginManaged = listenerConf.isTLSManagedByExternalPlugin();
			if (this.sslPluginManaged) {
				this.sslContext = new SSLContextHolderPlugin(this);
			} else {
				this.sslContext = server.getCertificateManager().createHolder(
						new File(listenerConf.getTLSPublicChainFile()), new File(listenerConf.getTLSPrivateKeyFile()),
						listenerConf.getTLSPrivateKeyPassword(), listenerConf.isTLSAutoRefreshCert());
			}
		} else {
			this.sslPluginManaged = false;
			this.sslContext = null;
		}
		if (listenerConf.getRedirectLegacyClientsTo() != null) {
			this.legacyRedirectAddressBuf = WebSocketEaglerInitialHandler
					.prepareRedirectAddr(listenerConf.getRedirectLegacyClientsTo());
		} else {
			this.legacyRedirectAddressBuf = null;
		}
		cachedServerMOTD = listenerConf.getServerMOTD();
		String iconName = listenerConf.getServerIcon();
		if (iconName != null && !iconName.isEmpty()) {
			try {
				cachedServerIcon = server.getServerIconLoader().loadServerIcon(new File(iconName));
			} catch (FileNotFoundException ex) {
				server.logger().error("Could not load server icon: " + iconName + " (not found)");
				cachedServerIcon = null;
			} catch (IOException ex) {
				server.logger().error("Could not load server icon: " + iconName, ex);
				cachedServerIcon = null;
			}
		} else {
			cachedServerIcon = null;
		}
		rateLimiter = CompoundRateLimiterMap.create(listenerConf.getLimitIP(), listenerConf.getLimitLogin(),
				listenerConf.getLimitMOTD(), listenerConf.getLimitQuery(), listenerConf.getLimitHTTP(),
				RateLimiterExclusions.create(listenerConf.getLimitExclusions(), server.logger()));
	}

	public ISSLContextProvider getSSLContext() {
		return sslContext;
	}

	@Override
	public <T> T get(IAttributeKey<T> key) {
		return attrHolder.get(key);
	}

	@Override
	public <T> void set(IAttributeKey<T> key, T value) {
		attrHolder.set(key, value);
	}

	@Override
	public String getName() {
		return listenerConf.getListenerName();
	}

	@Override
	public SocketAddress getAddress() {
		return address;
	}

	@Override
	public boolean isDualStack() {
		return listenerConf.isDualStack();
	}

	@Override
	public boolean isTLSEnabled() {
		return listenerConf.isEnableTLS();
	}

	@Override
	public boolean isTLSRequired() {
		return listenerConf.isRequireTLS();
	}

	@Override
	public boolean isTLSManagedByPlugin() {
		return sslPluginManaged;
	}

	@Override
	public ITLSManager getTLSManager() throws IllegalStateException {
		if (!listenerConf.isEnableTLS()) {
			throw new IllegalStateException("TLS is not enabled on this listener!");
		}
		if (!sslPluginManaged) {
			throw new IllegalStateException(
					"TLS manager is disabled for this listener! (Set 'tls_managed_by_external_plugin' to true)");
		}
		return (ITLSManager) sslContext;
	}

	@Override
	public byte[] getServerIcon() {
		return cachedServerIcon;
	}

	@Override
	public void setServerIcon(byte[] pixels) {
		if (pixels != null && pixels.length != 16384) {
			throw new IllegalArgumentException("Server icon is the wrong length, should be 16384");
		}
		cachedServerIcon = pixels;
	}

	@Override
	public List<String> getServerMOTD() {
		return cachedServerMOTD;
	}

	@Override
	public void setServerMOTD(List<String> motd) {
		if (motd == null || motd.size() == 0) {
			cachedServerMOTD = Collections.emptyList();
		} else if (motd.size() == 1) {
			cachedServerMOTD = ImmutableList.of(motd.get(0));
		} else {
			cachedServerMOTD = ImmutableList.of(motd.get(0), motd.get(1));
		}
	}

	@Override
	public boolean isForwardIP() {
		return listenerConf.isForwardIP();
	}

	@Override
	public boolean matchListenerAddress(SocketAddress addr) {
		if (addr.equals(listenerConf.getInjectAddress())) {
			return true;
		} else if ((addr instanceof InetSocketAddress addr2)
				&& (listenerConf.getInjectAddress() instanceof InetSocketAddress addr3) && isAllZeros(addr2)
				&& isAllZeros(addr3)) {
			return addr2.getPort() == addr3.getPort();
		} else {
			return false;
		}
	}

	private boolean isAllZeros(InetSocketAddress addr) {
		InetAddress addr2 = addr.getAddress();
		if (addr2 instanceof Inet4Address addr3) {
			byte[] octets = addr3.getAddress();
			return (octets[0] | octets[1] | octets[2] | octets[3]) == 0;
		} else if (addr2 instanceof Inet6Address addr3) {
			byte[] octets = addr3.getAddress();
			return (octets[0] | octets[1] | octets[2] | octets[3] | octets[4] | octets[5] | octets[6] | octets[7]
					| octets[8] | octets[9] | octets[10] | octets[11] | octets[12] | octets[13] | octets[14]
					| octets[15]) == 0;
		} else {
			return false;
		}
	}

	@Override
	public void reportVelocityInjected(Channel channel) {
		server.logger().info("Listener \"" + listenerConf.getListenerName() + "\" injected into channel " + channel
				+ " successfully (Velocity method)");
	}

	@Override
	public void reportPaperMCInjected() {
		server.logger().info("Default listener injected into server channel successfully (PaperMC method)");
	}

	@Override
	public void reportNettyInjected(Channel channel) {
		server.logger().info("Listener \"" + listenerConf.getListenerName() + "\" injected into channel " + channel
				+ " successfully (Generic Netty method)");
	}

	public byte[] getLegacyRedirectAddressBuf() {
		return legacyRedirectAddressBuf;
	}

	public boolean isAllowMOTD() {
		return listenerConf.isAllowMOTD();
	}

	public boolean isAllowQuery() {
		return listenerConf.isAllowQuery();
	}

	public boolean isShowMOTDPlayerList() {
		return listenerConf.isShowMOTDPlayerList();
	}

	public ConfigDataListener getConfigData() {
		return listenerConf;
	}

	public CompoundRateLimiterMap getRateLimiter() {
		return rateLimiter;
	}

}
