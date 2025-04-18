package org.openzen.zenscript.tree.lexer;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.lexer.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DFALexer {
	private static final CompiledDFA<ZSTokenType> DFA = CompiledDFA.createLexerDFA(ZSTokenType.values(), ZSTokenType.class);
	private final List<ParseException> parseErrors = new ArrayList<>();
	private final PositionalTokenFactory<ZSTokenType, ZSToken> factory;

	public DFALexer(PositionalTokenFactory<ZSTokenType, ZSToken> factory) {
		this.factory = factory;
	}

	public List<PositionedToken<ZSTokenType, ZSToken>> tokenize(SourceFile file) {
		List<PositionedToken<ZSTokenType, ZSToken>> tokens = new ArrayList<>();
		try {
			CountingCharReader reader = new CountingCharReader(new ReaderCharReader(file.open()), file);

			// While not at eof
			while (reader.peek() >= 0) {
				// TODO keep the position of the start, and then get the position from the end, use CountingCharReader
				int state = 0;
				StringBuilder content = new StringBuilder();
				CodePosition startPosition = reader.getPosition();
				while (DFA.transitions[state].containsKey(Math.min(reader.peek(), NFA.UNICODE_PLACEHOLDER))) {
					int c = reader.next();
					content.append((char) c);
					state = DFA.transitions[state].get(Math.min(c, NFA.UNICODE_PLACEHOLDER));
				}
				CodePosition endPosition = startPosition.until(reader.getPosition());

				ZSTokenType finalState = DFA.finals[state];

				//TODO code position doesn't deal with multiline...
				if (finalState != null) {
					if (state == 0) {

						tokens.add(factory.create(endPosition, ZSTokenType.INVALID, content.toString()));
						break;
					}
					tokens.add(factory.create(endPosition, finalState, content.toString()));
				} else {
					if (reader.peek() < 0 && content.length() == 0) {
						tokens.add(factory.create(endPosition, ZSTokenType.EOF, "")); // happens on comments at the end of files
						break;
					}

					if (content.length() == 0) {
						content.append((char) reader.next());
					}
					tokens.add(factory.create(endPosition, ZSTokenType.INVALID, content.toString()));
				}
			}
			tokens.add(factory.create(reader.getPosition(), ZSTokenType.EOF, ""));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return tokens;
	}

}
