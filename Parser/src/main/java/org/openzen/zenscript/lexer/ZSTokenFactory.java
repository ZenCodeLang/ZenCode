/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.lexer;

import java.util.HashMap;
import java.util.Map;
import static org.openzen.zenscript.lexer.ZSTokenType.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSTokenFactory implements TokenFactory<ZSToken, ZSTokenType> {
	public static final ZSTokenFactory INSTANCE = new ZSTokenFactory();
	
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
		KEYWORDS.put("variant", K_VARIANT);
		
		KEYWORDS.put("abstract", K_ABSTRACT);
		KEYWORDS.put("final", K_FINAL);
		KEYWORDS.put("override", K_OVERRIDE);
		KEYWORDS.put("const", K_CONST);
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
		KEYWORDS.put("sbyte", K_SBYTE);
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
		KEYWORDS.put("match", K_MATCH);
		KEYWORDS.put("throws", K_THROWS);
		
		KEYWORDS.put("this", K_THIS);
		KEYWORDS.put("super", K_SUPER);
		KEYWORDS.put("null", K_NULL);
		KEYWORDS.put("true", K_TRUE);
		KEYWORDS.put("false", K_FALSE);
		KEYWORDS.put("new", K_NEW);
	}

	@Override
	public ZSToken create(ZSTokenType type, String content) {
		if (type == T_IDENTIFIER && KEYWORDS.containsKey(content))
			type = KEYWORDS.get(content);
		
		return new ZSToken(type, content);
	}
}
