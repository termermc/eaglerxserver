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

package net.lax1dude.eaglercraft.backend.rpc.base.remote.message;

import net.lax1dude.eaglercraft.backend.rpc.adapter.IBackendRPCMessageChannel;
import net.lax1dude.eaglercraft.backend.rpc.adapter.IBackendRPCMessageHandler;

public class BackendRPCMessageChannel<PlayerObject> implements IBackendRPCMessageChannel<PlayerObject> {

	private final String legacyName;
	private final String modernName;
	private final IBackendRPCMessageHandler<PlayerObject> handler;

	public BackendRPCMessageChannel(String legacyName, String modernName,
			IBackendRPCMessageHandler<PlayerObject> handler) {
		this.legacyName = legacyName;
		this.modernName = modernName;
		this.handler = handler;
	}

	@Override
	public String getLegacyName() {
		return legacyName;
	}

	@Override
	public String getModernName() {
		return modernName;
	}

	@Override
	public IBackendRPCMessageHandler<PlayerObject> getHandler() {
		return handler;
	}

}
