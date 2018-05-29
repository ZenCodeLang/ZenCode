/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openzen.drawablegui.listeners.ListenerHandle;
import org.openzen.drawablegui.listeners.ListenerList;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;

/**
 *
 * @author Hoofdgebruiker
 */
public class TokenModel {
	private final ListenerList<Listener> listeners = new ListenerList<>();
	
	private final String filename;
	private final int spacesPerTab;
	private final List<TokenLine> lines = new ArrayList<>();
	
	public TokenModel(String filename, int spacesPerTab) {
		this.filename = filename;
		this.spacesPerTab = spacesPerTab;
	}
	
	public ListenerHandle<Listener> addListener(Listener listener) {
		return listeners.add(listener);
	}
	
	public int getLineCount() {
		return lines.size();
	}
	
	public TokenLine getLine(int line) {
		return lines.get(line);
	}
	
	public List<TokenLine> getLines() {
		return Collections.unmodifiableList(lines);
	}
	
	public int getLineLength(int line) {
		return lines.get(line).length();
	}
	
	public void set(Iterator<ZSToken> tokens) {
		lines.clear();
		lines.add(new TokenLine());
		
		insertTokens(0, 0, tokens);
	}
	
	public void deleteNewline(int lineIndex) {
		TokenLine line = getLine(lineIndex);
		merge(line, lineIndex, getLine(lineIndex + 1));
		lines.remove(lineIndex + 1);
		
		listeners.accept(listener -> listener.onLineDeleted(lineIndex + 1));
	}
	
	public void deleteCharacter(int lineIndex, int offset) {
		TokenLine line = getLine(lineIndex);
		int tokenOffset = 0;
		for (int i = 0; i < line.getTokenCount(); i++) {
			ZSToken token = line.getToken(i);
			if (tokenOffset + token.content.length() > offset) {
				if (token.content.length() == 1) {
					line.remove(i);
					i--; // make sure previous token is reparsed too
				} else {
					token = token.delete(offset - tokenOffset, 1);
					line.replace(i, token);
				}

				reparse(lineIndex, i, lineIndex, i + 1);
				return;
			}
			tokenOffset += token.content.length();
		}
	}
	
	public void insert(int lineIndex, int offset, String value) {
		TokenLine line = lines.get(lineIndex);
		int tokenOffset = 0;
		if (line.isEmpty()) {
			line.add(new ZSToken(ZSTokenType.INVALID, value));
			reparse(lineIndex, 0, lineIndex, 1);
			return;
		}

		for (int i = 0; i < line.getTokenCount(); i++) {
			ZSToken token = line.getToken(i);
			if (tokenOffset + token.content.length() > offset) {
				token = token.insert(offset - tokenOffset, value);
				line.replace(i, token);
				reparse(lineIndex, i, lineIndex, i + 1);
				return;
			}
			tokenOffset += token.content.length();
		}

		ZSToken token = line.getLastToken();
		token = new ZSToken(token.type, token.content + value);
		line.replace(line.getTokenCount() - 1, token);
		reparse(lineIndex, line.getTokenCount() - 1, lineIndex, line.getTokenCount());
	}
	
	private void reparse(int fromLine, int fromToken, int toLine, int toToken) {
		TokenReparser reparser = new TokenReparser(filename, lines, fromLine, fromToken, toLine, toToken, spacesPerTab);
		List<ZSToken> tokens = reparser.reparse();
		replaceTokens(fromLine, fromToken, reparser.getLine(), reparser.getToken(), tokens);
	}
	
	private void replaceTokens(int fromLine, int fromToken, int toLine, int toToken, List<ZSToken> tokens) {
		removeTokens(fromLine, fromToken, toLine, toToken);
		insertTokens(fromLine, fromToken, tokens.iterator());
	}
	
	private void removeTokens(int fromLine, int fromToken, int toLine, int toToken) {
		if (toLine > fromLine) {
			TokenLine fromLineObject = lines.get(fromLine);
			fromLineObject.removeRange(fromToken, fromLineObject.getTokenCount());
			
			listeners.accept(listener -> listener.onLineChanged(fromLine));
			
			TokenLine toLineObject = lines.get(toLine);
			for (int i = toToken - 1; i >= 0; i--)
				toLineObject.remove(i);
			
			listeners.accept(listener -> listener.onLineChanged(toLine));
			
			merge(lines.get(fromLine), fromLine, lines.remove(toLine));
			for (int i = toLine - 1; i > fromLine; i--) {
				lines.remove(i);
				
				int ix = i;
				listeners.accept(listener -> listener.onLineDeleted(ix));
			}
		} else {
			TokenLine line = lines.get(fromLine);
			for (int i = toToken - 1; i >= fromToken; i--)
				line.remove(i);
			
			listeners.accept(listener -> listener.onLineChanged(fromLine));
		}
	}
	
	private void insertTokens(int line, int tokenIndex, Iterator<ZSToken> tokens) {
		TokenLine currentLine = lines.get(line);
		Set<Integer> insertedLines = new HashSet<>();
		Set<Integer> modifiedLines = new HashSet<>();
		while (tokens.hasNext()) {
			ZSToken token = tokens.next();
			if (token.type.multiline && token.content.indexOf('\n') >= 0) {
				TokenLine newLine = new TokenLine();
				if (tokenIndex < currentLine.getTokenCount()) {
					for (int i = currentLine.getTokenCount() - 1; i >= tokenIndex; i--) {
						newLine.insert(0, currentLine.remove(i));
					}
				}
				
				tokenIndex = 0;
				if (!token.content.equals("\n")) {
					String[] parts = token.content.split("\r?\n");
					if (!parts[0].isEmpty())
						currentLine.add(new ZSToken(token.type, parts[0]));
					if (!parts[parts.length - 1].isEmpty()) {
						newLine.insert(0, new ZSToken(token.type, parts[parts.length - 1]));
						tokenIndex++;
					}
					
					for (int i = 1; i < parts.length - 1; i++) {
						TokenLine intermediate = new TokenLine();
						if (!parts[i].isEmpty())
							intermediate.add(new ZSToken(token.type, parts[i]));
						lines.add(++line, intermediate);
					}
				}
				
				currentLine = newLine;
				lines.add(++line, currentLine);
				insertedLines.add(line);
			} else if (token.type != ZSTokenType.T_WHITESPACE_CARRIAGE_RETURN) {
				currentLine.insert(tokenIndex++, token);
				modifiedLines.add(line);
			}
		}
		
		listeners.accept(listener -> {
			for (Integer inserted : insertedLines)
				listener.onLineInserted(inserted);
			for (Integer modified : modifiedLines)
				if (!insertedLines.contains(modified))
					listener.onLineChanged(modified);
		});
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0)
				result.append("\n");
			
			TokenLine line = lines.get(i);
			for (ZSToken token : line.getTokens()) {
				result.append(token.content);
			}
		}
		return result.toString();
	}
		
	private void merge(TokenLine line, int lineIndex, TokenLine other) {
		if (line.isEmpty()) {
			line.addAll(other.getTokens());
			return;
		}

		int fromToken = line.getTokenCount() - 1;
		int toToken = !other.isEmpty() ? fromToken + 2 : fromToken + 1;
		line.addAll(other.getTokens());
		reparse(lineIndex, fromToken, lineIndex, toToken);
	}
	
	public interface Listener {
		void onLineInserted(int index);
		
		void onLineChanged(int index);
		
		void onLineDeleted(int index);
	}
}
