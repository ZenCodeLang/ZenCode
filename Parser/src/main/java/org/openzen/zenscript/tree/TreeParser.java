package org.openzen.zenscript.tree;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenSet;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.lexer.PositionedToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.openzen.zenscript.lexer.ZSTokenType.EOF;
import static org.openzen.zenscript.lexer.ZSTokenType.T_IDENTIFIER;

public class TreeParser {
	private final List<PositionedToken<ZSTokenType, ZSToken>> tokens;
	private int pos;
	private int fuel;
	private final List<Event> events;

	public TreeParser(List<PositionedToken<ZSTokenType, ZSToken>> tokens) {
		this.tokens = tokens;
		this.pos = 0;
		this.fuel = 256;
		this.events = new ArrayList<>();
	}

	public TreeParser subTree() {
		return new TreeParser(tokens.subList(pos, tokens.size()));
	}

	public void mergeIn(TreeParser other) {
		this.pos += other.pos;
		this.events.addAll(other.events);
		this.fuel = 256;
	}

	public void replace(Function<PositionedToken<ZSTokenType, ZSToken>, List<PositionedToken<ZSTokenType, ZSToken>>> replacer) {
		PositionedToken<ZSTokenType, ZSToken> token = this.tokens.get(pos);
		List<PositionedToken<ZSTokenType, ZSToken>> apply = replacer.apply(token);
		if (!apply.isEmpty()) {
			this.tokens.set(pos, apply.get(0));
			this.tokens.addAll(pos + 1, apply.subList(1, apply.size()));
		}
	}

	public void file() {
		MarkOpened open = this.open();
		// If the file starts with whitespace, just skip it for now
		whitespace();
		while (!eof()) {
			if (!Import.parse(this)) {
				if (!Definition.parse(this)) {
					Statement.parse(this);
				}
			}
		}
		close(open, TreeKind.FILE);
	}

	public void whitespace() {
		ZSTokenType nth = nth(0);
		if (nth.isWhitespace()) {
			advance();
			whitespace();
		}
	}

	//TODO remove?
	public void name() {
		name(ZSTokenSet.empty());
	}

	public void name(ZSTokenSet recovery) {
		if (this.at(T_IDENTIFIER)) {
			MarkOpened open = this.open();
			this.expect(T_IDENTIFIER);
			this.close(open, TreeKind.NAME);
			whitespace();
		} else {
			this.recover("expected a name", recovery);
		}
	}

	public Tree buildTree() {
		List<PositionedToken<ZSTokenType, ZSToken>> tokensIterator = new ArrayList<>(tokens);

		// Remove the final CLOSE event
		if (events.get(events.size() - 1) != Event.CLOSE) {
			throw new IllegalStateException("Parser produced unclosed events");
		}
		List<Event> eventsToProcess = new ArrayList<>(events.subList(0, events.size() - 1));

		List<Tree> stack = new ArrayList<>();
		int tokenIndex = 0;

		for (Event event : eventsToProcess) {
			if (event instanceof Event.Open) {
				Event.Open openEvent = (Event.Open) event;
				stack.add(new Tree(openEvent.kind));
			} else if (event == Event.CLOSE) {
				Tree tree = stack.remove(stack.size() - 1);
				stack.get(stack.size() - 1).children().add(Child.ofTree(tree));
			} else if (event == Event.ADVANCE) {
				PositionedToken<ZSTokenType, ZSToken> token = tokensIterator.get(tokenIndex++);
				stack.get(stack.size() - 1).children().add(Child.ofToken(token));
			} else if (event instanceof Event.Error) {
				Event.Error errorEvent = (Event.Error) event;
				stack.get(stack.size() - 1).children().add(Child.ofError(errorEvent.position, errorEvent.message));
			} else {
				throw new IllegalStateException("Unknown event type: " + event.getClass().getName());
			}
		}

		Tree tree = stack.remove(0);
//		assert stack.isEmpty();
//		assert tokenIndex == tokensIterator.size();
		return tree;
	}

	public MarkOpened open() {
		MarkOpened mark = new MarkOpened(events.size());
		events.add(Event.open(TreeKind.ERROR));
		return mark;
	}

	public MarkOpened openBefore(MarkClosed m) {
		if (m == null) {
			return open();
		}
		MarkOpened mark = new MarkOpened(m.index);
		events.add(m.index, Event.open(TreeKind.ERROR));
		return mark;
	}

	public MarkClosed close(MarkOpened m, TreeKind kind) {
		events.set(m.index, Event.open(kind));
		events.add(Event.CLOSE);
		return new MarkClosed(m.index);
	}

	public void advance() {
		if (eof()) {
			return;
		}
		assert !eof();
		fuel = 256;
		events.add(Event.ADVANCE);
		pos++;
	}

	public void error(String message) {
		int reportingPos = pos - 1;
//		while(this.tokens.get(reportingPos).getType().isWhitespace() && reportingPos > 0) {
//			reportingPos--;
//		}
		events.add(Event.error(message, this.tokens.get(reportingPos).position().withLength(1)));
	}

	public void advanceWithError(String error) {
		MarkOpened m = open();
		// TODO proper Error reporting
		error(error);
		advance();
		close(m, TreeKind.ERROR);
	}


	public boolean recover(String error, ZSTokenSet recoveryTokens) {
		if (this.atAny(recoveryTokens)) {
			this.error(error);
			return true;
		}

		MarkOpened open = this.open();
		this.error(error);
		this.advance();
		this.whitespace();
		this.close(open, TreeKind.ERROR);

		return false;
	}

	public boolean eof() {
		//TODO I am not certain on this
		return tokens.get(pos).getType() == EOF;
	}

	public ZSTokenType nth(int lookahead) {
		if (fuel == 0) {
			this.events.add(Event.CLOSE);
			throw new RuntimeException("parser is stuck at " + tokens.get(pos + lookahead).getType() + "\n" + this.buildTree().toString());
		}
		fuel--;

		if (pos + lookahead >= tokens.size()) {
			return ZSTokenType.EOF;
		} else {
			return tokens.get(pos + lookahead).getType();
		}
	}

	public ZSToken nthToken(int lookahead) {
		if (fuel == 0) {
			this.events.add(Event.CLOSE);
			throw new RuntimeException("parser is stuck\n" + this.buildTree().toString());
		}
		fuel--;

		if (pos + lookahead >= tokens.size()) {
			return null;
		} else {
			return tokens.get(pos + lookahead).token();
		}
	}

	//TODO REMOVE
	public ZSTokenType nth(int lookahead, ZSTokenType... lookingFor) {
		if (fuel == 0) {
			this.events.add(Event.CLOSE);
			throw new RuntimeException("parser is stuck looking for " + Arrays.toString(lookingFor) + "\n" + this.buildTree().toString());
		}
		fuel--;

		if (pos + lookahead >= tokens.size()) {
			return ZSTokenType.EOF;
		} else {
			return tokens.get(pos + lookahead).getType();
		}
	}

	public boolean at(ZSTokenType kind) {
		return nth(0, kind) == kind;
	}

	public boolean atAny(ZSTokenSet set) {
		return set.contains(nth(0));
	}

	public boolean atAny(ZSTokenType... kinds) {
		ZSTokenType current = nth(0, kinds);
		for (ZSTokenType kind : kinds) {
			if (current == kind) {
				return true;
			}
		}
		return false;
	}

	public void whileAt(ZSTokenType kinds, Runnable runnable) {
		while (atAny(kinds) && !eof()) {
			runnable.run();
		}
	}

	public void whileAt(ZSTokenType[] kinds, Runnable runnable) {
		while (atAny(kinds) && !eof()) {
			runnable.run();
		}
	}

	public void whileNotAt(ZSTokenType kinds, Runnable runnable) {
		while (!atAny(kinds) && !eof()) {
			runnable.run();
		}
	}

	public void whileNotAt(ZSTokenType[] kinds, Runnable runnable) {
		while (!atAny(kinds) && !eof()) {
			runnable.run();
		}
	}

	public boolean eat(ZSTokenType... kinds) {
		if (atAny(kinds)) {
			ZSTokenType eaten = nth(0);
			advance();
			return true;
		} else {
			return false;
		}
	}

	public void eatAny() {
		ZSTokenType eaten = nth(0);
		advance();
	}

	public boolean expect(ZSTokenType... kinds) {
		if (eat(kinds)) {
			return true;
		}
		//TODO better error reporting
		error("expected " + Arrays.toString(kinds));
//		advanceWithError("expected " + Arrays.toString(kinds));
		// Error reporting
//		System.out.println("expected " + Arrays.toString(kinds));
		return false;
	}


	public interface Event {
		static Open open(TreeKind kind) {
			return new Open(kind);
		}

		static Token token(ZSTokenType type) {
			return new Token(type);
		}

		static Error error(String message,/* TODO do I need this?*/ CodePosition position) {
			return new Error(message, position);
		}

		Advance ADVANCE = new Advance();
		Close CLOSE = new Close();

		class Error implements Event {
			private final String message;
			private final CodePosition position;

			public Error(String message, CodePosition position) {
				this.message = message;
				this.position = position;
			}

			public String message() {
				return message;
			}
		}

		class Open implements Event {
			private TreeKind kind;

			public Open(TreeKind kind) {
				this.kind = kind;
			}

			public void kind(TreeKind kind) {
				this.kind = kind;
			}

			public TreeKind kind() {
				return kind;
			}

			@Override
			public String toString() {
				final StringBuilder sb = new StringBuilder("Open{");
				sb.append("kind=").append(kind);
				sb.append('}');
				return sb.toString();
			}
		}

		class Token implements Event {
			private final ZSTokenType type;

			public Token(ZSTokenType kind) {
				this.type = kind;
			}

			public ZSTokenType type() {
				return type;
			}

		}

		class Advance implements Event {

			@Override
			public String toString() {
				final StringBuilder sb = new StringBuilder("Advance{");
				sb.append('}');
				return sb.toString();
			}
		}

		class Close implements Event {
			@Override
			public String toString() {
				final StringBuilder sb = new StringBuilder("Close{");
				sb.append('}');
				return sb.toString();
			}
		}
	}

	public static class MarkOpened {
		int index;

		MarkOpened(int index) {
			this.index = index;
		}
	}

	public static class MarkClosed {
		int index;

		MarkClosed(int index) {
			this.index = index;
		}
	}
}
