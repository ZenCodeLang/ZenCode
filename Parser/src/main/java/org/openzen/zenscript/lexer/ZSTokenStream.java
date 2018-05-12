/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSTokenStream extends TokenStream<ZSToken, ZSTokenType> {
	private static final CompiledDFA DFA = CompiledDFA.createLexerDFA(ZSTokenType.values(), ZSTokenType.class);
	private static final Map<String, ZSTokenType> KEYWORDS = new HashMap<>();
	
	static {
		KEYWORDS.put("import", K_IMPORT);
		KEYWORDS.put("alias", K_ALIAS);
		KEYWORDS.put("class", K_CLASS);
		KEYWORDS.put("interface", K_INTERFACE);
		KEYWORDS.put("enum", K_ENUM);
		KEYWORDS.put("struct", K_STRUCT);
		KEYWORDS.put("expand", K_EXPAND);
		KEYWORDS.put("function", K_FUNCTION);
		
		KEYWORDS.put("abstract", K_ABSTRACT);
		KEYWORDS.put("final", K_FINAL);
		KEYWORDS.put("override", K_OVERRIDE);
		KEYWORDS.put("const", K_CONST);
		KEYWORDS.put("shared", K_SHARED);
		KEYWORDS.put("weak", K_WEAK);
		KEYWORDS.put("private", K_PRIVATE);
		KEYWORDS.put("public", K_PUBLIC);
		KEYWORDS.put("export", K_EXPORT);
		KEYWORDS.put("static", K_STATIC);
		KEYWORDS.put("protected", K_PROTECTED);
		KEYWORDS.put("implicit", K_IMPLICIT);
		KEYWORDS.put("virtual", K_VIRTUAL);
		KEYWORDS.put("extern", K_EXTERN);
		
		KEYWORDS.put("val", K_VAL);
		KEYWORDS.put("var", K_VAR);
		KEYWORDS.put("get", K_GET);
		KEYWORDS.put("implements", K_IMPLEMENTS);
		KEYWORDS.put("set", K_SET);
		
		KEYWORDS.put("void", K_VOID);
		KEYWORDS.put("any", K_ANY);
		KEYWORDS.put("bool", K_BOOL);
		KEYWORDS.put("byte", K_BYTE);
		KEYWORDS.put("ubyte", K_UBYTE);
		KEYWORDS.put("short", K_SHORT);
		KEYWORDS.put("ushort", K_USHORT);
		KEYWORDS.put("int", K_INT);
		KEYWORDS.put("uint", K_UINT);
		KEYWORDS.put("long", K_LONG);
		KEYWORDS.put("ulong", K_ULONG);
		KEYWORDS.put("float", K_FLOAT);
		KEYWORDS.put("double", K_DOUBLE);
		KEYWORDS.put("char", K_CHAR);
		KEYWORDS.put("string", K_STRING);
		
		KEYWORDS.put("if", K_IF);
		KEYWORDS.put("else", K_ELSE);
		KEYWORDS.put("do", K_DO);
		KEYWORDS.put("while", K_WHILE);
		KEYWORDS.put("for", K_FOR);
		KEYWORDS.put("throw", K_THROW);
		KEYWORDS.put("lock", K_LOCK);
		KEYWORDS.put("try", K_TRY);
		KEYWORDS.put("catch", K_CATCH);
		KEYWORDS.put("finally", K_FINALLY);
		KEYWORDS.put("return", K_RETURN);
		KEYWORDS.put("break", K_BREAK);
		KEYWORDS.put("continue", K_CONTINUE);
		KEYWORDS.put("switch", K_SWITCH);
		KEYWORDS.put("case", K_CASE);
		KEYWORDS.put("default", K_DEFAULT);
		
		KEYWORDS.put("in", K_IN);
		KEYWORDS.put("is", K_IS);
		KEYWORDS.put("as", K_AS);
		
		KEYWORDS.put("this", K_THIS);
		KEYWORDS.put("super", K_SUPER);
		KEYWORDS.put("null", K_NULL);
		KEYWORDS.put("true", K_TRUE);
		KEYWORDS.put("false", K_FALSE);
		KEYWORDS.put("new", K_NEW);
	}
	
	private String whitespaceBuffer = null;
	
	public ZSTokenStream(String filename, Reader reader) {
		super(filename, reader, DFA, ZSTokenType.EOF);
	}
	
	public String loadWhitespace() {
		if (whitespaceBuffer == null)
			whitespaceBuffer = peek().whitespaceBefore;
		
		return whitespaceBuffer;
	}
	
	public void reloadWhitespace() {
		whitespaceBuffer = peek().whitespaceBefore;
	}
	
	public String grabWhitespace() {
		String result = loadWhitespace();
		whitespaceBuffer = null;
		return result;
	}
	
	public String grabWhitespaceLine() {
		String whitespace = loadWhitespace();
		if (whitespace.contains("\n")) {
			int index = whitespace.indexOf('\n');
			whitespaceBuffer = whitespace.substring(index + 1);
			return whitespace.substring(0, index);
		} else {
			whitespaceBuffer = "";
			return whitespace;
		}
	}
	
	public void skipWhitespaceNewline() {
		loadWhitespace();
		int index = whitespaceBuffer.indexOf('\n');
		if (index >= 0)
			whitespaceBuffer = whitespaceBuffer.substring(index + 1);
	}
	
	public WhitespaceInfo collectWhitespaceInfoForBlock(String whitespace) {
		return WhitespaceInfo.from(whitespace, "", false);
	}
	
	public WhitespaceInfo collectWhitespaceInfo(String whitespace, boolean skipLineBefore) {
		return WhitespaceInfo.from(whitespace, grabWhitespaceLine(), skipLineBefore);
	}
	
	@Override
	protected ZSToken createToken(CodePosition position, String whitespaceBefore, String value, ZSTokenType tokenType) {
		if (tokenType == T_IDENTIFIER && KEYWORDS.containsKey(value))
			tokenType = KEYWORDS.get(value);
		
		return new ZSToken(position, tokenType, value, whitespaceBefore);
	}

	@Override
	protected void requiredTokenNotFound(CodePosition position, String error, ZSToken token) {
		throw new CompileException(position, CompileExceptionCode.UNEXPECTED_TOKEN, error);
	}

	@Override
	protected ZSToken invalidToken(CodePosition position, String whitespaceBefore, String token) {
		return new ZSToken(position, ZSTokenType.INVALID, token, whitespaceBefore);
	}

	@Override
	protected void ioException(IOException ex) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
