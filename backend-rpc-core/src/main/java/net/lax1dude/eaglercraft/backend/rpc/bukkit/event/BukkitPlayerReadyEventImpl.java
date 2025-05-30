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

package net.lax1dude.eaglercraft.backend.rpc.bukkit.event;

import org.bukkit.entity.Player;

import net.lax1dude.eaglercraft.backend.rpc.api.IEaglerPlayer;
import net.lax1dude.eaglercraft.backend.rpc.api.IEaglerXBackendRPC;
import net.lax1dude.eaglercraft.backend.rpc.api.bukkit.event.EaglercraftPlayerReadyEvent;

class BukkitPlayerReadyEventImpl extends EaglercraftPlayerReadyEvent {

	private final IEaglerXBackendRPC<Player> api;
	private final IEaglerPlayer<Player> player;

	BukkitPlayerReadyEventImpl(IEaglerXBackendRPC<Player> api, IEaglerPlayer<Player> player) {
		this.api = api;
		this.player = player;
	}

	@Override
	public IEaglerXBackendRPC<Player> getServerAPI() {
		return api;
	}

	@Override
	public IEaglerPlayer<Player> getPlayer() {
		return player;
	}

}
