/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;

/**
 *
 * @author Hoofdgebruiker
 */
public final class TokenLine {
	private final List<ZSToken> tokens = new ArrayList<>();
	private int length = 0;
	
	public List<ZSToken> getTokens() {
		return Collections.unmodifiableList(tokens);
	}
	
	public boolean isEmpty() {
		return tokens.isEmpty();
	}
	
	public int getTokenCount() {
		return tokens.size();
	}

	public int length() {
		return length;
	}
	
	public ZSToken getToken(int index) {
		return tokens.get(index);
	}
	
	public ZSToken getLastToken() {
		return tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
	}
	
	public String getIndent() {
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			ZSToken token = tokens.get(i);
			if (token.type != ZSTokenType.T_WHITESPACE_SPACE && token.type != ZSTokenType.T_WHITESPACE_TAB)
				break;
			
			indent.append(token.content);
		}
		return indent.toString();
	}
	
	/**
	 * Adds a temporary token. This token must be relexed later using the relexer.
	 * 
	 * @param token 
	 */
	public void addTemporary(ZSToken token) {
		tokens.add(token);
		length += token.content.length();
	}

	public void add(ZSToken token) {
		insert(tokens.size(), token);
	}

	public void addAll(Iterable<ZSToken> tokens) {
		for (ZSToken token : tokens) {
			add(token);
		}
	}

	public int insert(int index, ZSToken token) {
		if (token.type != ZSTokenType.T_WHITESPACE_TAB && token.content.indexOf('\t') >= 0) {
			int start = 0;
			int tokens = 0;
			for (int i = 0; i < token.content.length(); i++) {
				if (token.content.charAt(i) == '\t') {
					if (i > start) {
						insert(index++, new ZSToken(token.type, token.content.substring(start, i)));
						tokens++;
					}
					insert(index++, ZSTokenType.T_WHITESPACE_TAB.flyweight);
					tokens++;
					start = i + 1;
				}
			}
			
			if (start < token.content.length()) {
				insert(index++, new ZSToken(token.type, token.content.substring(start)));
				tokens++;
			}
			
			return tokens;
		}
		
		tokens.add(index, token);
		length += token.content.length();
		return 1;
	}

	public void replace(int index, ZSToken token) {
		ZSToken old = tokens.set(index, token);
		length += token.content.length() - old.content.length();
	}

	public ZSToken remove(int index) {
		ZSToken result = tokens.remove(index);
		length -= result.content.length();
		return result;
	}
	
	public void removeRange(int from, int to) {
		for (int i = to - 1; i >= from; i--)
			remove(i);
	}
}
