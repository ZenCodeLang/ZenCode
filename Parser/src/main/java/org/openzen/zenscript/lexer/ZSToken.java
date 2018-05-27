/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSToken implements Token<ZSTokenType> {
	public final ZSTokenType type;
	public final String content;
	
	public ZSToken(ZSTokenType type, String content) {
		this.type = type;
		this.content = content;
	}

	@Override
	public ZSTokenType getType() {
		return type;
	}

	@Override
	public String getContent() {
		return content;
	}
}
