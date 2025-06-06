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

package net.lax1dude.eaglercraft.backend.server.base.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.lax1dude.eaglercraft.backend.rpc.protocol.EaglerBackendRPCProtocol;
import net.lax1dude.eaglercraft.backend.rpc.protocol.pkt.EaglerBackendRPCPacket;
import net.lax1dude.eaglercraft.backend.server.adapter.IPlatformLogger;
import net.lax1dude.eaglercraft.backend.server.base.DataSerializationContext;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.util.ReusableByteArrayInputStream;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.util.ReusableByteArrayOutputStream;

abstract class SerializationContext {

	private final EaglerBackendRPCProtocol protocol;
	private final DataSerializationContext ctx;

	SerializationContext(EaglerBackendRPCProtocol protocol) {
		this.protocol = protocol;
		this.ctx = new DataSerializationContext();
	}

	SerializationContext(EaglerBackendRPCProtocol protocol, DataSerializationContext context) {
		this.protocol = protocol;
		this.ctx = context;
	}

	protected abstract IPlatformLogger logger();

	EaglerBackendRPCProtocol getProtocol() {
		return protocol;
	}

	EaglerBackendRPCPacket deserialize(byte[] packet) throws IOException {
		if (packet.length == 0) {
			throw new IOException("Empty packet recieved");
		}
		if (ctx.aquireInputStream()) {
			try {
				ctx.byteInputStreamSingleton.feedBuffer(packet);
				return protocol.readPacket(ctx.inputStreamSingleton, EaglerBackendRPCProtocol.CLIENT_TO_SERVER);
			} finally {
				ctx.byteInputStreamSingleton.feedBuffer(null);
				ctx.releaseInputStream();
			}
		} else {
			ReusableByteArrayInputStream tmp = new ReusableByteArrayInputStream();
			tmp.feedBuffer(packet);
			return protocol.readPacket(new DataInputStream(tmp), EaglerBackendRPCProtocol.CLIENT_TO_SERVER);
		}
	}

	byte[] serialize(EaglerBackendRPCPacket packet) throws IOException {
		int len = packet.length() + 1;
		byte[] ret;
		if (ctx.aquireOutputStream()) {
			try {
				ctx.byteOutputStreamSingleton.feedBuffer(len == 0 ? ctx.outputTempBuffer : new byte[len]);
				protocol.writePacket(ctx.outputStreamSingleton, EaglerBackendRPCProtocol.SERVER_TO_CLIENT, packet);
				ret = len == 0 ? ctx.byteOutputStreamSingleton.returnBufferCopied()
						: ctx.byteOutputStreamSingleton.returnBuffer();
			} finally {
				ctx.releaseOutputStream();
			}
		} else {
			ReusableByteArrayOutputStream bao = new ReusableByteArrayOutputStream();
			bao.feedBuffer(new byte[len == 0 ? 64 : len]);
			protocol.writePacket(new DataOutputStream(bao), EaglerBackendRPCProtocol.SERVER_TO_CLIENT, packet);
			ret = bao.returnBuffer();
		}
		if (len != 0 && ret.length != len) {
			logger().warn("Packet " + packet.getClass().getSimpleName() + " was the wrong length after serialization, "
					+ ret.length + " != " + len);
		}
		return ret;
	}

}
