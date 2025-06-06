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

package net.lax1dude.eaglercraft.backend.server.base.supervisor;

import java.util.UUID;
import java.util.function.Consumer;

import net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape;
import net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin;
import net.lax1dude.eaglercraft.backend.server.api.supervisor.ISupervisorResolver;

public interface ISupervisorResolverImpl extends ISupervisorResolver {

	public static final UUID UNAVAILABLE = UUID.randomUUID();

	void resolvePlayerSkinKeyed(UUID requester, UUID playerUUID, Consumer<IEaglerPlayerSkin> callback);

	void resolvePlayerCapeKeyed(UUID requester, UUID playerUUID, Consumer<IEaglerPlayerCape> callback);

	void resolvePlayerBrandKeyed(UUID requester, UUID playerUUID, Consumer<UUID> callback);

	void resolveForeignSkinKeyed(UUID requester, int modelId, String skinURL, Consumer<IEaglerPlayerSkin> callback);

	void resolveForeignCapeKeyed(UUID requester, String capeURL, Consumer<IEaglerPlayerCape> callback);

}
