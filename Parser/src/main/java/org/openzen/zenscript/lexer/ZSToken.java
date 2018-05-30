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
		if (content.isEmpty() && type != ZSTokenType.EOF)
			throw new IllegalArgumentException("Token must not be empty!");
		
		this.type = type;
		this.content = content;
	}
	
	public ZSToken(ZSTokenType type, String content, String displayContent) {
		if (content.isEmpty())
			throw new IllegalArgumentException("Token must not be empty!");
		
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
	
	@Override
	public String toString() {
		return type + ":" + content;
	}
	
	public ZSToken delete(int offset, int characters) {
		return new ZSToken(
				ZSTokenType.INVALID,
				content.substring(0, offset) + content.substring(offset + 1));
	}
	
	public Pair deleteAndSplit(int offset, int characters) {
		ZSToken first = new ZSToken(
				ZSTokenType.INVALID,
				content.substring(0, offset));
		ZSToken second = new ZSToken(
				ZSTokenType.INVALID,
				content.substring(offset));
		return new Pair(first, second);
	}
	
	public ZSToken insert(int offset, String value) {
		return new ZSToken(
				ZSTokenType.INVALID,
				content.substring(0, offset) + value + content.substring(offset));
	}
	
	public Pair split(int offset) {
		ZSToken first = new ZSToken(
				ZSTokenType.INVALID,
				content.substring(0, offset));
		ZSToken second = new ZSToken(
				ZSTokenType.INVALID,
				content.substring(offset));
		return new Pair(first, second);
	}
	
	public static class Pair {
		public final ZSToken first;
		public final ZSToken second;
		
		public Pair(ZSToken first, ZSToken second) {
			this.first = first;
			this.second = second;
		}
	}
}

