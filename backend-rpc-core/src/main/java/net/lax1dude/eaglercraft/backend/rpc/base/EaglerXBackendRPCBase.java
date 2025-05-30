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

package net.lax1dude.eaglercraft.backend.rpc.base;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import net.lax1dude.eaglercraft.backend.rpc.adapter.IBackendRPCImpl;
import net.lax1dude.eaglercraft.backend.rpc.adapter.IPlatform;
import net.lax1dude.eaglercraft.backend.rpc.adapter.IPlatform.Init;
import net.lax1dude.eaglercraft.backend.rpc.adapter.IPlatformLogger;
import net.lax1dude.eaglercraft.backend.rpc.adapter.IPlatformPlayer;
import net.lax1dude.eaglercraft.backend.rpc.api.EnumPlatformType;
import net.lax1dude.eaglercraft.backend.rpc.api.IBasePlayer;
import net.lax1dude.eaglercraft.backend.rpc.api.IEaglerXBackendRPC;
import net.lax1dude.eaglercraft.backend.rpc.api.IScheduler;
import net.lax1dude.eaglercraft.backend.rpc.api.internal.factory.IEaglerRPCFactory;
import net.lax1dude.eaglercraft.backend.rpc.base.local.EaglerXBackendRPCLocal;
import net.lax1dude.eaglercraft.backend.rpc.base.remote.EaglerXBackendRPCRemote;

public abstract class EaglerXBackendRPCBase<PlayerObject> extends RPCAttributeHolder
		implements IBackendRPCImpl<PlayerObject>, IEaglerRPCFactory, IEaglerXBackendRPC<PlayerObject> {

	private boolean hasStartedLoading;
	protected IPlatform<PlayerObject> platform;
	protected EnumPlatformType platformType;
	protected Class<PlayerObject> playerClass;
	protected Set<Class<?>> playerClassSet;
	protected SchedulerExecutors schedulerExecutors;
	protected FutureTimeoutLoop timeoutLoop;
	protected int baseRequestTimeout = 10;

	public static <PlayerObject> EaglerXBackendRPCBase<PlayerObject> init() {
		if (detectAPI()) {
			return new EaglerXBackendRPCLocal<>();
		} else {
			return new EaglerXBackendRPCRemote<>();
		}
	}

	private static boolean detectAPI() {
		try {
			Class.forName("net.lax1dude.eaglercraft.backend.server.api.IEaglerXServerAPI");
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	@Override
	public final void load(Init<PlayerObject> platf) {
		if (hasStartedLoading) {
			throw new IllegalStateException("EaglerXBackendRPC is already loading");
		}
		hasStartedLoading = true;

		platform = platf.getPlatform();
		playerClass = platform.getPlayerClass();
		playerClassSet = Collections.singleton(playerClass);

		schedulerExecutors = new SchedulerExecutors(platform.getScheduler(), platform.logger());

		platformType = switch (platform.getType()) {
		case BUKKIT -> EnumPlatformType.BUKKIT;
		default -> throw new IllegalStateException();
		};

		load0(platf);

		platform.eventDispatcher().setAPI(this);
		APIFactoryImpl.INSTANCE.initialize(playerClass, this);
	}

	protected abstract void load0(Init<PlayerObject> platf);

	protected EaglerXBackendRPCBase() {
	}

	@Override
	public EnumPlatformType getPlatformType() {
		return platformType;
	}

	@Override
	public Class<PlayerObject> getPlayerClass() {
		return playerClass;
	}

	@Override
	public Set<Class<?>> getPlayerTypes() {
		return playerClassSet;
	}

	@Override
	public <T> IEaglerXBackendRPC<T> getAPI(Class<T> playerClass) {
		if (!playerClass.isAssignableFrom(this.playerClass)) {
			throw new ClassCastException(
					"Class " + this.playerClass.getName() + " cannot be cast to " + playerClass.getName());
		}
		return (IEaglerXBackendRPC<T>) this;
	}

	@Override
	public IEaglerXBackendRPC<?> getDefaultAPI() {
		return this;
	}

	@Override
	public IEaglerRPCFactory getFactory() {
		return this;
	}

	public IPlatform<PlayerObject> getPlatform() {
		return platform;
	}

	@Override
	public boolean isPlayer(PlayerObject player) {
		if (player == null) {
			throw new NullPointerException("player");
		}
		return platform.getPlayer(player) != null;
	}

	@Override
	public boolean isPlayerByName(String playerName) {
		if (playerName == null) {
			throw new NullPointerException("playerName");
		}
		return platform.getPlayer(playerName) != null;
	}

	@Override
	public boolean isPlayerByUUID(UUID playerUUID) {
		if (playerUUID == null) {
			throw new NullPointerException("playerUUID");
		}
		return platform.getPlayer(playerUUID) != null;
	}

	@Override
	public boolean isEaglerPlayer(PlayerObject player) {
		if (player == null) {
			throw new NullPointerException("player");
		}
		IPlatformPlayer<PlayerObject> platformPlayer = platform.getPlayer(player);
		if (platformPlayer != null) {
			return platformPlayer.<IBasePlayer<PlayerObject>>getAttachment().isEaglerPlayer();
		}
		return false;
	}

	@Override
	public boolean isEaglerPlayerByName(String playerName) {
		if (playerName == null) {
			throw new NullPointerException("playerName");
		}
		IPlatformPlayer<PlayerObject> platformPlayer = platform.getPlayer(playerName);
		if (platformPlayer != null) {
			return platformPlayer.<IBasePlayer<PlayerObject>>getAttachment().isEaglerPlayer();
		}
		return false;
	}

	@Override
	public boolean isEaglerPlayerByUUID(UUID playerUUID) {
		if (playerUUID == null) {
			throw new NullPointerException("playerUUID");
		}
		IPlatformPlayer<PlayerObject> platformPlayer = platform.getPlayer(playerUUID);
		if (platformPlayer != null) {
			return platformPlayer.<IBasePlayer<PlayerObject>>getAttachment().isEaglerPlayer();
		}
		return false;
	}

	@Override
	public IScheduler getScheduler() {
		return platform.getScheduler();
	}

	@Override
	public void setBaseRequestTimeout(int seconds) {
		baseRequestTimeout = seconds;
	}

	@Override
	public int getBaseRequestTimeout() {
		return baseRequestTimeout;
	}

	public IPlatformLogger logger() {
		return platform.logger();
	}

	public SchedulerExecutors schedulerExecutors() {
		return schedulerExecutors;
	}

	public FutureTimeoutLoop timeoutLoop() {
		return timeoutLoop;
	}

	public <V> RPCActiveFuture<V> createFuture(int expiresAfter) {
		long now = System.nanoTime();
		RPCActiveFuture<V> ret = new RPCActiveFuture<V>(schedulerExecutors, now + expiresAfter * 1000000000l);
		timeoutLoop.addFuture(now, ret);
		return ret;
	}

	protected void createTimeoutLoop(long resolution) {
		timeoutLoop = new FutureTimeoutLoop(platform.getScheduler(), resolution);
	}

	protected void cancelTimeoutLoop() {
		timeoutLoop.cancelAll();
	}

}
