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

package net.lax1dude.eaglercraft.backend.rpc.api.data;

import javax.annotation.Nonnull;

import net.lax1dude.eaglercraft.backend.rpc.api.EnumSubscribeEvents;
import net.lax1dude.eaglercraft.backend.rpc.api.IRPCEvent;
import net.lax1dude.eaglercraft.backend.rpc.api.voice.EnumVoiceState;

public final class VoiceChangeEvent implements IRPCEvent {

	@Nonnull
	public static VoiceChangeEvent create(@Nonnull EnumVoiceState oldState, @Nonnull EnumVoiceState newState) {
		if (oldState == null) {
			throw new NullPointerException("oldState");
		}
		if (newState == null) {
			throw new NullPointerException("newState");
		}
		return new VoiceChangeEvent(oldState, newState);
	}

	private final EnumVoiceState oldState;
	private final EnumVoiceState newState;

	private VoiceChangeEvent(EnumVoiceState oldState, EnumVoiceState newState) {
		this.oldState = oldState;
		this.newState = newState;
	}

	@Nonnull
	public EnumVoiceState getOldState() {
		return oldState;
	}

	@Nonnull
	public EnumVoiceState getNewState() {
		return newState;
	}

	@Nonnull
	@Override
	public EnumSubscribeEvents getEventType() {
		return EnumSubscribeEvents.EVENT_VOICE_CHANGE;
	}

}
