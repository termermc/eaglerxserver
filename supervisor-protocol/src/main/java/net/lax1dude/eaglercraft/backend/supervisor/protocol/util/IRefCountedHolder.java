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

package net.lax1dude.eaglercraft.backend.supervisor.protocol.util;

import io.netty.util.ReferenceCounted;

public interface IRefCountedHolder extends ReferenceCounted {

	ReferenceCounted delegate();

	default int refCnt() {
		ReferenceCounted d = delegate();
		return d != null ? d.refCnt() : 0;
	}

	default ReferenceCounted retain() {
		ReferenceCounted d = delegate();
		if (d != null) {
			d.retain();
		}
		return this;
	}

	default ReferenceCounted retain(int increment) {
		ReferenceCounted d = delegate();
		if (d != null) {
			d.retain(increment);
		}
		return this;
	}

	default ReferenceCounted touch() {
		ReferenceCounted d = delegate();
		if (d != null) {
			d.touch();
		}
		return this;
	}

	default ReferenceCounted touch(Object hint) {
		ReferenceCounted d = delegate();
		if (d != null) {
			d.touch(hint);
		}
		return this;
	}

	default boolean release() {
		ReferenceCounted d = delegate();
		return d != null && d.release();
	}

	default boolean release(int decrement) {
		ReferenceCounted d = delegate();
		return d != null && d.release(decrement);
	}

}
