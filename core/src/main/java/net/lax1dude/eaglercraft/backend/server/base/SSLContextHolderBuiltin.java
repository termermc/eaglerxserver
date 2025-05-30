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

import java.io.ByteArrayInputStream;

import javax.net.ssl.SSLException;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

public class SSLContextHolderBuiltin implements ISSLContextProvider {

	protected final String name;
	protected final String password;
	protected byte[] pubKey;
	protected byte[] privKey;
	protected SslContext ctx;

	protected SSLContextHolderBuiltin(String name, String password) {
		this.name = name;
		this.password = password;
	}

	@Override
	public SslHandler newHandler(ByteBufAllocator alloc) {
		return ctx.newHandler(alloc);
	}

	protected void refresh() throws SSLException {
		ctx = SslContextBuilder.forServer(new ByteArrayInputStream(pubKey), new ByteArrayInputStream(privKey), password)
				.build();
	}

}
