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

import listeners.ListenerHandle;
import listeners.ListenerList;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.TokenStream;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;

/**
 * @author Hoofdgebruiker
 */
public class TokenModel {
	private final ListenerList<Listener> listeners = new ListenerList<>();

	private final SourceFile file;
	private final int spacesPerTab;
	private final List<TokenLine> lines = new ArrayList<>();
	private long version = 0;

	public TokenModel(SourceFile file, int spacesPerTab) {
		this.file = file;
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

	public ZSToken getTokenAt(Position position) {
		TokenLine line = lines.get(position.line);
		return position.token >= line.getTokenCount() ? null : line.getToken(position.token);
	}

	/**
	 * Version can be used to check if the token model has been updated. If the
	 * version is unchanged, the contents of the token model will be unchanged
	 * too.
	 *
	 * @return version number
	 */
	public long getVersion() {
		return version;
	}

	public Position getPosition(int line, int offset) {
		if (line < 0)
			line = 0;
		if (line >= lines.size())
			return new Position(lines.size() - 1, lines.get(lines.size() - 1).getTokenCount(), 0);

		int token = 0;
		int tokenOffset = 0;
		TokenLine tokenLine = getLine(line);
		while (token < tokenLine.getTokenCount()) {
			ZSToken t = tokenLine.getToken(token);
			if (tokenOffset + t.content.length() > offset)
				return new Position(line, token, offset - tokenOffset);

			tokenOffset += t.content.length();
			token++;
		}
		return new Position(line, tokenLine.getTokenCount(), 0);
	}

	public String extract(SourcePosition from, SourcePosition to) {
		Position fromT = from.asTokenPosition();
		Position toT = to.asTokenPosition();
		if (from.line == to.line) {
			StringBuilder result = new StringBuilder();
			ZSToken fromToken = getTokenAt(fromT);
			if (fromToken != null)
				result.append(fromToken.getContent().substring(fromT.offset));

			TokenLine line = getLine(from.line);
			for (int i = fromT.token + 1; i < toT.token; i++)
				result.append(line.getToken(i).content);

			ZSToken toToken = getTokenAt(toT);
			if (toToken != null)
				result.append(toToken.content.substring(0, toT.offset));

			return result.toString();
		} else {
			StringBuilder result = new StringBuilder();
			ZSToken fromToken = getTokenAt(fromT);
			if (fromToken != null)
				result.append(fromToken.getContent().substring(fromT.offset));

			TokenLine fromLine = getLine(from.line);
			for (int i = fromT.token + 1; i < fromLine.getTokenCount(); i++)
				result.append(fromLine.getToken(i).content);

			for (int i = fromT.line + 1; i < toT.line; i++) {
				result.append("\n");
				for (ZSToken t : getLine(i).getTokens()) {
					result.append(t.content);
				}
			}

			result.append("\n");

			TokenLine toLine = getLine(to.line);
			for (int i = 0; i < toT.token; i++)
				result.append(toLine.getToken(i).content);

			ZSToken toToken = getTokenAt(toT);
			if (toToken != null)
				result.append(toToken.content.substring(0, toT.offset));

			return result.toString();
		}
	}

	public void set(TokenStream<ZSTokenType, ZSToken> tokens) {
		lines.clear();
		lines.add(new TokenLine());

		insertTokens(0, 0, new TokenIterator(tokens));
	}

	public void deleteNewline(int lineIndex) {
		TokenLine line = getLine(lineIndex);
		merge(line, lineIndex, getLine(lineIndex + 1));
		lines.remove(lineIndex + 1);

		version++;
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
					if (i > 0)
						i--; // make sure previous token is reparsed too
				} else {
					token = token.delete(offset - tokenOffset, 1);
					line.replace(i, token);
				}

				relex(lineIndex, i, lineIndex, i + 1);
				return;
			}
			tokenOffset += token.content.length();
		}
	}

	public void delete(SourcePosition from, SourcePosition to) {
		Position fromT = from.asTokenPosition();
		Position toT = to.asTokenPosition();

		ZSToken fromToken = getTokenAt(fromT);
		ZSToken toToken = getTokenAt(toT);

		String remainder = "";
		if (fromToken != null)
			remainder = fromToken.content.substring(0, fromT.offset);
		if (toToken != null && toT.offset > 0)
			remainder += toToken.content.substring(toT.offset);

		removeTokens(fromT.line, fromT.token, toT.line, toT.offset > 0 ? toT.token + 1 : toT.token);
		if (!remainder.isEmpty())
			getLine(fromT.line).insert(fromT.token, new ZSToken(ZSTokenType.INVALID, remainder));

		relex(fromT.line, Math.max(0, fromT.token - 1), fromT.line, Math.min(lines.get(fromT.line).getTokenCount(), fromT.token + 1));
	}

	public void insert(SourcePosition position, String value) {
		TokenLine line = lines.get(position.line);
		Position tokenPosition = position.asTokenPosition();
		ZSToken token = getTokenAt(tokenPosition);
		if (token == null) {
			line.addTemporary(new ZSToken(ZSTokenType.INVALID, value));
		} else {
			token = token.insert(tokenPosition.offset, value);
			line.replace(tokenPosition.token, token);
		}
		relex(tokenPosition.line, Math.max(0, tokenPosition.token - 1), tokenPosition.line, tokenPosition.token + 1);
	}

	private void relex(int fromLine, int fromToken, int toLine, int toToken) {
		try {
			TokenRelexer reparser = new TokenRelexer(file, lines, fromLine, fromToken, toLine, toToken);
			List<ZSToken> tokens = reparser.relex();
			replaceTokens(fromLine, fromToken, reparser.getLine(), reparser.getToken(), tokens);
		} catch (ParseException ex) {
			// TODO: signal this somewhere?
		}
	}

	private void replaceTokens(int fromLine, int fromToken, int toLine, int toToken, List<ZSToken> tokens) {
		removeTokens(fromLine, fromToken, toLine, toToken);
		insertTokens(fromLine, fromToken, tokens.iterator());
	}

	private void removeTokens(int fromLine, int fromToken, int toLine, int toToken) {
		if (toLine > fromLine) {
			TokenLine fromLineObject = lines.get(fromLine);
			fromLineObject.removeRange(fromToken, fromLineObject.getTokenCount());

			TokenLine toLineObject = lines.get(toLine);
			for (int i = toToken - 1; i >= 0; i--)
				toLineObject.remove(i);

			merge(lines.get(fromLine), fromLine, lines.remove(toLine));

			version++;
			listeners.accept(listener -> listener.onLineChanged(fromLine));
			listeners.accept(listener -> listener.onLineDeleted(toLine));

			for (int i = toLine - 1; i > fromLine; i--) {
				lines.remove(i);

				int ix = i;

				version++;
				listeners.accept(listener -> listener.onLineDeleted(ix));
			}
		} else {
			TokenLine line = lines.get(fromLine);
			for (int i = toToken - 1; i >= fromToken; i--)
				line.remove(i);

			version++;
			listeners.accept(listener -> listener.onLineChanged(fromLine));
		}
	}

	private void insertTokens(int line, int tokenIndex, Iterator<ZSToken> tokens) {
		TokenLine currentLine = lines.get(line);
		Set<Integer> insertedLines = new HashSet<>();
		Set<Integer> modifiedLines = new HashSet<>();
		modifiedLines.add(line);
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
						tokenIndex += newLine.insert(0, new ZSToken(token.type, parts[parts.length - 1]));
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

		version++;
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
		relex(lineIndex, fromToken, lineIndex, toToken);
	}

	public interface Listener {
		void onLineInserted(int index);

		void onLineChanged(int index);

		void onLineDeleted(int index);
	}

	private static class TokenIterator implements Iterator<ZSToken> {
		private final TokenStream<ZSTokenType, ZSToken> stream;
		private ZSToken next;

		public TokenIterator(TokenStream<ZSTokenType, ZSToken> stream) {
			this.stream = stream;
			try {
				next = stream.next();
			} catch (ParseException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public boolean hasNext() {
			return next.type != stream.getEOF();
		}

		@Override
		public ZSToken next() {
			ZSToken token = next;
			try {
				next = stream.next();
			} catch (ParseException ex) {
				throw new RuntimeException(ex);
			}
			return token;
		}
	}

	public static class Position {
		public final int line;
		public final int token;
		public final int offset;

		public Position(int line, int token, int offset) {
			if (line < 0)
				throw new IllegalArgumentException("line cannot be negative");
			if (token < 0)
				throw new IllegalArgumentException("token cannot be negative");
			if (offset < 0)
				throw new IllegalArgumentException("offset cannot be negative");

			this.line = line;
			this.token = token;
			this.offset = offset;
		}
	}
}
