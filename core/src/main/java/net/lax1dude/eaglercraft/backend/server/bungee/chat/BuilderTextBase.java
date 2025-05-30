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

package net.lax1dude.eaglercraft.backend.server.bungee.chat;

import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.backend.server.adapter.IPlatformComponentBuilder.IBuilderClickEvent;
import net.lax1dude.eaglercraft.backend.server.adapter.IPlatformComponentBuilder.IBuilderComponentText;
import net.lax1dude.eaglercraft.backend.server.adapter.IPlatformComponentBuilder.IBuilderComponentTranslation;
import net.lax1dude.eaglercraft.backend.server.adapter.IPlatformComponentBuilder.IBuilderHoverEvent;
import net.lax1dude.eaglercraft.backend.server.adapter.IPlatformComponentBuilder.IBuilderStyle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

abstract class BuilderTextBase<ParentType> implements IBuilderComponentText<ParentType>, IAppendCallback {

	BuilderStyle<IBuilderComponentText<ParentType>> style;
	BuilderClick<IBuilderComponentText<ParentType>> click;
	BuilderHover<IBuilderComponentText<ParentType>> hover;
	String text;
	String insertion;
	List<BaseComponent> buildChildren;

	@Override
	public IBuilderStyle<IBuilderComponentText<ParentType>> beginStyle() {
		return style = new BuilderStyle<>(this);
	}

	@Override
	public IBuilderClickEvent<IBuilderComponentText<ParentType>> beginClickEvent() {
		return click = new BuilderClick<>(this);
	}

	@Override
	public IBuilderHoverEvent<IBuilderComponentText<ParentType>> beginHoverEvent() {
		return hover = new BuilderHover<>(this);
	}

	@Override
	public IBuilderComponentText<IBuilderComponentText<ParentType>> appendTextComponent() {
		return new BuilderTextChild<>(this);
	}

	@Override
	public IBuilderComponentTranslation<IBuilderComponentText<ParentType>> appendTranslationComponent() {
		return new BuilderTranslationChild<>(this);
	}

	@Override
	public IBuilderComponentText<ParentType> insertion(String txt) {
		this.insertion = txt;
		return this;
	}

	@Override
	public IBuilderComponentText<ParentType> text(String txt) {
		this.text = txt;
		return this;
	}

	@Override
	public void append(BaseComponent comp) {
		if (buildChildren == null) {
			buildChildren = new ArrayList<>(4);
		}
		buildChildren.add(comp);
	}

	protected BaseComponent build() {
		TextComponent ret = text != null ? new TextComponent(text) : new TextComponent();
		if (insertion != null) {
			ret.setInsertion(insertion);
		}
		if (style != null) {
			style.applyTo(ret);
		}
		if (click != null) {
			click.applyTo(ret);
		}
		if (hover != null) {
			hover.applyTo(ret);
		}
		if (buildChildren != null) {
			ret.setExtra(buildChildren);
		}
		return ret;
	}

}
