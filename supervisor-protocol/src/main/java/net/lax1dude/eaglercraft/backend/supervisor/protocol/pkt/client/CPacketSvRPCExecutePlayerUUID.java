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

package net.lax1dude.eaglercraft.backend.supervisor.protocol.pkt.client;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;
import net.lax1dude.eaglercraft.backend.supervisor.protocol.pkt.EaglerSupervisorHandler;
import net.lax1dude.eaglercraft.backend.supervisor.protocol.pkt.EaglerSupervisorPacket;
import net.lax1dude.eaglercraft.backend.supervisor.protocol.util.IInjectedPayload;
import net.lax1dude.eaglercraft.backend.supervisor.protocol.util.IRefCountedHolder;

public class CPacketSvRPCExecutePlayerUUID implements EaglerSupervisorPacket, IRefCountedHolder {

	public UUID requestUUID;
	public int timeout;
	public UUID playerUUID;
	public int nameLength;
	public ByteBuf payload;
	private IInjectedPayload injected;

	public CPacketSvRPCExecutePlayerUUID() {
	}

	public CPacketSvRPCExecutePlayerUUID(UUID requestUUID, int timeout, UUID playerUUID, int nameLength,
			ByteBuf payload) {
		this.requestUUID = requestUUID;
		this.timeout = timeout;
		this.playerUUID = playerUUID;
		this.nameLength = nameLength;
		this.payload = payload;
	}

	public CPacketSvRPCExecutePlayerUUID(UUID requestUUID, int timeout, UUID playerUUID, IInjectedPayload injected) {
		this.requestUUID = requestUUID;
		this.timeout = timeout;
		this.playerUUID = playerUUID;
		this.injected = injected;
	}

	@Override
	public void readPacket(ByteBuf buffer) {
		timeout = EaglerSupervisorPacket.readVarInt(buffer);
		if (timeout > 0) {
			requestUUID = new UUID(buffer.readLong(), buffer.readLong());
		} else {
			requestUUID = null;
		}
		playerUUID = new UUID(buffer.readLong(), buffer.readLong());
		nameLength = buffer.readUnsignedByte();
		payload = buffer.readRetainedSlice(buffer.readUnsignedMedium());
	}

	@Override
	public void writePacket(ByteBuf buffer) {
		EaglerSupervisorPacket.writeVarInt(buffer, timeout);
		if (timeout > 0) {
			buffer.writeLong(requestUUID.getMostSignificantBits());
			buffer.writeLong(requestUUID.getLeastSignificantBits());
		}
		buffer.writeLong(playerUUID.getMostSignificantBits());
		buffer.writeLong(playerUUID.getLeastSignificantBits());
		if (injected != null) {
			buffer.writeIntLE(0);
			int pos = buffer.writerIndex();
			buffer.setByte(pos - 4, injected.writePayload(buffer));
			buffer.setMedium(pos - 3, buffer.writerIndex() - pos);
		} else {
			buffer.writeByte(nameLength);
			int l = payload.readableBytes();
			buffer.writeMedium(l);
			buffer.writeBytes(payload, payload.readerIndex(), l);
		}
	}

	@Override
	public void handlePacket(EaglerSupervisorHandler handler) {
		handler.handleClient(this);
	}

	@Override
	public ReferenceCounted delegate() {
		return payload;
	}

}