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

package net.lax1dude.eaglercraft.backend.server.api;

import java.net.SocketAddress;

import javax.annotation.Nonnull;

import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

public interface INettyChannel {

	@Nonnull
	SocketAddress getSocketAddress();

	@Nonnull
	NettyUnsafe netty();

	public interface NettyUnsafe {

		@Nonnull
		Channel getChannel();

		default void writePacket(@Nonnull Object packet) {
			if (packet == null) {
				throw new NullPointerException("packet");
			}
			Channel channel = getChannel();
			if (channel.isActive()) {
				channel.writeAndFlush(packet, channel.voidPromise());
			} else {
				ReferenceCountUtil.release(packet);
			}
		}

	}

}
