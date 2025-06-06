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

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.netty.util.ReferenceCountUtil;
import net.lax1dude.eaglercraft.backend.server.api.skins.IProfileResolver;
import net.lax1dude.eaglercraft.backend.server.api.skins.TexturesProperty;
import net.lax1dude.eaglercraft.backend.server.api.skins.TexturesResult;
import net.lax1dude.eaglercraft.backend.server.base.EaglerXServer;
import net.lax1dude.eaglercraft.backend.server.base.pipeline.BufferUtils;
import net.lax1dude.eaglercraft.backend.server.util.CharSequenceReader;
import net.lax1dude.eaglercraft.backend.server.util.Util;
import net.lax1dude.eaglercraft.backend.skin_cache.IHTTPClient;

public class ProfileResolver implements IProfileResolver {

	private final EaglerXServer<?> server;
	private final IHTTPClient httpClient;

	public ProfileResolver(EaglerXServer<?> server, IHTTPClient httpClient) {
		this.server = server;
		this.httpClient = httpClient;
	}

	@Override
	public void resolveVanillaUUIDFromUsername(String username, Consumer<UUID> callback) {
		if (username == null) {
			throw new NullPointerException("username");
		}
		if (callback == null) {
			throw new NullPointerException("callback");
		}
		httpClient.asyncRequest("GET", URI.create("https://api.mojang.com/users/profiles/minecraft/"
				+ URLEncoder.encode(username, StandardCharsets.UTF_8)), (response) -> {
			if (response == null) {
				callback.accept(null);
			} else if (response.exception != null) {
				server.logger().error("Exception loading vanilla profile UUID of \"" + username + "\"!",
						response.exception);
				callback.accept(null);
			} else {
				try {
					if (response.code != 200) {
						callback.accept(null);
					} else if (response.data == null) {
						callback.accept(null);
					} else {
						UUID uuid;
						try {
							JsonObject json = EaglerXServer.GSON_PRETTY.fromJson(
									new CharSequenceReader(BufferUtils.readCharSequence(response.data,
											response.data.readableBytes(), StandardCharsets.UTF_8)),
									JsonObject.class);
							uuid = Util.createUUIDFromUndashed(json.get("id").getAsString());
						} catch (Exception t) {
							callback.accept(null);
							return;
						}
						callback.accept(uuid);
					}
				} finally {
					ReferenceCountUtil.release(response.data);
				}
			}
		});
	}

	@Override
	public void resolveVanillaTexturesFromUUID(UUID uuid, Consumer<TexturesProperty> callback) {
		if (uuid == null) {
			throw new NullPointerException("uuid");
		}
		if (callback == null) {
			throw new NullPointerException("callback");
		}
		httpClient.asyncRequest("GET", URI.create("https://sessionserver.mojang.com/session/minecraft/profile/"
				+ Util.toUUIDStringUndashed(uuid) + "?unsigned=false"), (response) -> {
			if (response == null) {
				callback.accept(null);
			} else if (response.exception != null) {
				server.logger().error("Exception loading profile " + uuid + "!", response.exception);
				callback.accept(null);
			} else {
				try {
					if (response.code != 200) {
						callback.accept(null);
					} else if (response.data == null) {
						callback.accept(null);
					} else {
						TexturesProperty result = null;
						try {
							JsonObject json = EaglerXServer.GSON_PRETTY.fromJson(
									new CharSequenceReader(BufferUtils.readCharSequence(response.data,
											response.data.readableBytes(), StandardCharsets.UTF_8)),
									JsonObject.class);
							JsonElement propsElement = json.get("properties");
							if (propsElement != null) {
								JsonArray properties = propsElement.getAsJsonArray();
								if (properties.size() > 0) {
									for (int i = 0, l = properties.size(); i < l; ++i) {
										JsonElement prop = properties.get(i);
										if (prop.isJsonObject()) {
											JsonObject propObj = prop.getAsJsonObject();
											if (propObj.get("name").getAsString().equals("textures")) {
												JsonElement value = propObj.get("value");
												JsonElement signature = propObj.get("signature");
												if (value != null && signature != null) {
													String v = value.getAsString();
													String s = signature.getAsString();
													if (v != null && s != null) {
														result = TexturesProperty.create(v, s);
														break;
													}
												}
											}
										}
									}
								}
							}
						} catch (Exception t) {
							callback.accept(null);
							return;
						}
						callback.accept(result);
					}
				} finally {
					ReferenceCountUtil.release(response.data);
				}
			}
		});
	}

	@Override
	public TexturesResult decodeVanillaTextures(String propertyValue) {
		if (propertyValue == null) {
			throw new NullPointerException("propertyValue");
		}
		return GameProfileUtil.extractSkinAndCape(propertyValue);
	}

}
