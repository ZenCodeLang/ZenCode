package org.openzen.zenscript.tree.lexer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.TokenStream;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;

import java.util.LinkedList;
import java.util.Stack;

public class LLParserTokenStream implements TokenStream<ZSTokenType, ZSToken> {
	private final TokenStream<ZSTokenType, ZSToken> stream;
	private ZSToken next;
	private CodePosition position;
	private CodePosition positionBeforeWhitespace;
	private String whitespace;
	private final LinkedList<PositionedToken> tokenMemory = new LinkedList<>();
	private final Stack<Integer> marks = new Stack<>();

	private int tokenMemoryOffset = 0;
	private int tokenMemoryCurrent = 0;

	public LLParserTokenStream(TokenStream<ZSTokenType, ZSToken> stream) throws ParseException {
		this.stream = stream;
		advance();
	}

	public void pushMark() {
		marks.push(tokenMemoryCurrent);
	}

	public void popMark() {
		marks.pop();

		if (marks.isEmpty()) {
			tokenMemoryOffset = tokenMemoryCurrent;
			tokenMemory.clear();
		}
	}

	public void reset() {
		tokenMemoryCurrent = marks.pop();
	}

	public ZSToken peek() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
			return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset).token;
		} else {
			return next;
		}
	}

	public void replace(ZSToken other) {
		next = other;
	}

	@Override
	public ZSToken next() throws ParseException {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
			return tokenMemory.get((tokenMemoryCurrent++) - tokenMemoryOffset).token;
		} else {
			ZSToken result = next;
			advance();
			if (marks.isEmpty()) {
				tokenMemoryOffset++;
			} else {
				tokenMemory.add(new PositionedToken(getPosition(), result));
			}
			tokenMemoryCurrent++;
			return result;
		}
	}

	@Override
	public CodePosition getPosition() {
		if (tokenMemoryCurrent < tokenMemoryOffset + tokenMemory.size()) {
			return tokenMemory.get((tokenMemoryCurrent) - tokenMemoryOffset).position;
		} else {
			return position;
		}
	}

	@Override
	public ZSTokenType getEOF() {
		return stream.getEOF();
	}

	private void advance() throws ParseException {
		position = stream.getPosition();
		next = stream.next();
	}

	public boolean isNext(ZSTokenType type) {
		return peek().getType() == type;
	}

	public ZSToken optional(ZSTokenType type) throws ParseException {
		if (peek().getType() == type) {
			return next();
		} else {
			return null;
		}
	}

	public ZSToken required(ZSTokenType type, String error) throws ParseException {
		ZSToken t = peek();
		if (t.getType() == type) {
			return next();
		} else {
			throw new ParseException(getPosition().withLength(t.getContent().length()), error);
		}
	}

	public ZSToken required(ZSTokenType type, CodePosition reportPosition, String error) throws ParseException {
		ZSToken t = peek();
		if (t.getType() == type) {
			return next();
		} else {
			throw new ParseException(reportPosition.withLength(t.getContent().length()), error);
		}
	}

	public boolean hasNext() {
		return peek().getType() != getEOF();
	}

	public void recoverUntilTokenOrNewline(ZSTokenType type) throws ParseException {
		CodePosition last = getPosition();
		while (peek().getType() != type && getPosition().fromLine > last.fromLine) next();
	}

	/**
	 * Moves further until peek type is provided type
	 */
	public void recoverUntilBeforeToken(ZSTokenType type) throws ParseException {
		while (peek().getType() != type && peek().getType() != getEOF()) next();
	}

	/**
	 * Moves further until pointer is standing on the given type
	 */
	public void recoverUntilOnToken(ZSTokenType type) throws ParseException {
		recoverUntilBeforeToken(type);
		next();
	}

	public void logError(ParseException error) throws ParseException {
		throw error;
	}

	private class PositionedToken {
		public final CodePosition position;
		public final ZSToken token;

		public PositionedToken(CodePosition position, ZSToken token) {
			this.position = position;
			this.token = token;
		}
	}
}
