/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSToken implements Token<ZSTokenType> {
	public final CodePosition position;
	public final ZSTokenType type;
	public final String content;
	public final String whitespaceBefore;
	
	public ZSToken(CodePosition position, ZSTokenType type, String content, String whitespaceBefore) {
		this.position = position;
		this.type = type;
		this.content = content;
		this.whitespaceBefore = whitespaceBefore;
	}
	
	@Override
	public CodePosition getPosition() {
		return position;
	}

	@Override
	public ZSTokenType getType() {
		return type;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public String getWhitespaceBefore() {
		return whitespaceBefore;
	}
}
