package org.openzen.zenscript.tree.lexer;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.lexer.*;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZSTokenParser extends LLParserTokenStream {
	private static final CompiledDFA<ZSTokenType> DFA = CompiledDFA.createLexerDFA(ZSTokenType.values(), ZSTokenType.class);
	public final BracketExpressionParser bracketParser;
	private final List<ParseException> parseErrors = new ArrayList<>();

	public ZSTokenParser(TokenStream<ZSTokenType, ZSToken> parser, BracketExpressionParser bracketParser) throws ParseException {
		super(parser);

		this.bracketParser = bracketParser;
	}

	public static TokenParser<ZSToken, ZSTokenType> createRaw(SourceFile file, CharReader reader) {
		return new TokenParser<>(
				file,
				reader,
				DFA,
				ZSTokenType.EOF,
				ZSTokenType.INVALID,
				new ZSTokenFactory());
	}

	public static ZSTokenParser create(SourceFile file, BracketExpressionParser bracketParser) throws IOException, ParseException {
		return new ZSTokenParser(createRaw(file, new ReaderCharReader(file.open())), bracketParser);
	}

	public SourceFile getFile() {
		return getPosition().file;
	}

	public void logError(ParseException error) {
		parseErrors.add(error);
	}

	public List<ParseException> getErrors() {
		return parseErrors;
	}

	@FunctionalInterface
	public interface ParseAttempt<U> {
		U apply(ZSTokenParser parser) throws ParseException;
	}
}
