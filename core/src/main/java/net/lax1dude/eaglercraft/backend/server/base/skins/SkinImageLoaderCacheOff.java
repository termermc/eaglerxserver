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

package net.lax1dude.eaglercraft.backend.server.base.skins;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import net.lax1dude.eaglercraft.backend.server.api.skins.EnumPresetCapes;
import net.lax1dude.eaglercraft.backend.server.api.skins.EnumPresetSkins;
import net.lax1dude.eaglercraft.backend.server.api.skins.EnumSkinModel;
import net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerCape;
import net.lax1dude.eaglercraft.backend.server.api.skins.IEaglerPlayerSkin;
import net.lax1dude.eaglercraft.backend.server.api.skins.ISkinImageLoader;

class SkinImageLoaderCacheOff implements ISkinImageLoader {

	static final ISkinImageLoader INSTANCE = new SkinImageLoaderCacheOff();

	SkinImageLoaderCacheOff() {
	}

	@Override
	public IEaglerPlayerSkin loadPresetSkin(int presetSkin) {
		return SkinImageLoaderImpl.loadPresetSkin(presetSkin);
	}

	@Override
	public IEaglerPlayerSkin loadPresetSkin(EnumPresetSkins presetSkin) {
		return SkinImageLoaderImpl.loadPresetSkin(presetSkin);
	}

	@Override
	public IEaglerPlayerSkin loadPresetSkin(UUID playerUUID) {
		return SkinImageLoaderImpl.loadPresetSkin(playerUUID);
	}

	@Override
	public IEaglerPlayerCape loadPresetNoCape() {
		return SkinImageLoaderImpl.loadPresetNoCape();
	}

	@Override
	public IEaglerPlayerCape loadPresetCape(int presetCape) {
		return SkinImageLoaderImpl.loadPresetCape(presetCape);
	}

	@Override
	public IEaglerPlayerCape loadPresetCape(EnumPresetCapes presetCape) {
		return SkinImageLoaderImpl.loadPresetCape(presetCape);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_ARGB8I_64x64(int[] pixelsARGB8, EnumSkinModel modelId) {
		return SkinImageLoaderImpl.loadSkinImageData64x64(pixelsARGB8, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_ARGB8I_64x64(int[] pixelsARGB8I, int modelIdRaw) {
		return SkinImageLoaderImpl.loadSkinImageData64x64(pixelsARGB8I, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_ABGR8_64x64(byte[] pixelsRGBA8, EnumSkinModel modelId) {
		return SkinImageLoaderImpl.loadSkinImageData64x64(pixelsRGBA8, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_ABGR8_64x64(byte[] pixelsRGBA8, int modelIdRaw) {
		return SkinImageLoaderImpl.loadSkinImageData64x64(pixelsRGBA8, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_eagler(byte[] pixelsEagler, EnumSkinModel modelId) {
		return SkinImageLoaderImpl.loadSkinImageData64x64Eagler(pixelsEagler, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_eagler(byte[] pixelsEagler, int modelIdRaw) {
		return SkinImageLoaderImpl.loadSkinImageData64x64Eagler(pixelsEagler, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_ARGB8I_64x32(int[] pixelsARGB8, EnumSkinModel modelId) {
		return SkinImageLoaderImpl.loadSkinImageData64x32(pixelsARGB8, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData_ARGB8I_64x32(int[] pixelsARGB8, int modelIdRaw) {
		return SkinImageLoaderImpl.loadSkinImageData64x32(pixelsARGB8, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData(BufferedImage image, EnumSkinModel modelId) {
		return SkinImageLoaderImpl.loadSkinImageData(image, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData(BufferedImage image, int modelIdRaw) {
		return SkinImageLoaderImpl.loadSkinImageData(image, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData(InputStream inputStream, EnumSkinModel modelId) throws IOException {
		return SkinImageLoaderImpl.loadSkinImageData(inputStream, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData(InputStream inputStream, int modelIdRaw) throws IOException {
		return SkinImageLoaderImpl.loadSkinImageData(inputStream, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData(File imageFile, EnumSkinModel modelId) throws IOException {
		return SkinImageLoaderImpl.loadSkinImageData(imageFile, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin loadSkinImageData(File imageFile, int modelIdRaw) throws IOException {
		return SkinImageLoaderImpl.loadSkinImageData(imageFile, modelIdRaw);
	}

	@Override
	public IEaglerPlayerSkin rewriteCustomSkinModelId(IEaglerPlayerSkin skin, EnumSkinModel modelId) {
		return SkinImageLoaderImpl.rewriteCustomSkinModelId(skin, modelId.getId());
	}

	@Override
	public IEaglerPlayerSkin rewriteCustomSkinModelId(IEaglerPlayerSkin skin, int modelIdRaw) {
		return SkinImageLoaderImpl.rewriteCustomSkinModelId(skin, modelIdRaw);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData_ARGB8I_64x32(int[] pixelsARGB8) {
		return SkinImageLoaderImpl.loadCapeImageData64x32(pixelsARGB8);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData_ARGB8I_32x32(int[] pixelsARGB8) {
		return SkinImageLoaderImpl.loadCapeImageData32x32(pixelsARGB8);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData_ABGR8_32x32(byte[] pixelsRGBA8) {
		return SkinImageLoaderImpl.loadCapeImageData32x32(pixelsRGBA8);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData_eagler(byte[] pixelsEagler) {
		return SkinImageLoaderImpl.loadCapeImageData23x17Eagler(pixelsEagler);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData(BufferedImage image) {
		return SkinImageLoaderImpl.loadCapeImageData(image);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData(InputStream inputStream) throws IOException {
		return SkinImageLoaderImpl.loadCapeImageData(inputStream);
	}

	@Override
	public IEaglerPlayerCape loadCapeImageData(File imageFile) throws IOException {
		return SkinImageLoaderImpl.loadCapeImageData(imageFile);
	}

}
