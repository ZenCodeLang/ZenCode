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
public enum ZSTokenType implements TokenType {
	T_COMMENT_SCRIPT("#[^\n]*[\n\\e]", true),
	T_COMMENT_SINGLELINE("//[^\n]*[\n\\e]", true),
	T_COMMENT_MULTILINE("/\\*([^\\*]|(\\*+([^\\*/])))*\\*+/", true),
	T_WHITESPACE("[ \t\r\n]*", true),
	T_IDENTIFIER("[a-zA-Z_][a-zA-Z_0-9]*"),
	T_FLOAT("\\-?(0|[1-9][0-9]*)\\.[0-9]+([eE][\\+\\-]?[0-9]+)?"),
	T_INT("\\-?(0|[1-9][0-9]*)"),
	T_STRING_SQ("\"([^\"\\\\]|\\\\([\'\"\\\\/bfnrt&]|u[0-9a-fA-F]{4}))*\""),
	T_STRING_DQ("\'([^\'\\\\]|\\\\([\'\"\\\\/bfnrt&]|u[0-9a-fA-F]{4}))*\'"),
	T_AOPEN("\\{"),
	T_ACLOSE("\\}"),
	T_SQOPEN("\\["),
	T_SQCLOSE("\\]"),
	T_DOT3("\\.\\.\\."),
	T_DOT2("\\.\\."),
	T_DOT("\\."),
	T_COMMA(","),
	T_INCREMENT("\\+\\+"),
	T_ADDASSIGN("\\+="),
	T_ADD("\\+"),
	T_DECREMENT("\\-\\-"),
	T_SUBASSIGN("\\-="),
	T_SUB("\\-"),
	T_CATASSIGN("~="),
	T_CAT("~"),
	T_MULASSIGN("\\*="),
	T_MUL("\\*"),
	T_DIVASSIGN("/="),
	T_DIV("/"),
	T_MODASSIGN("%="),
	T_MOD("%"),
	T_ORASSIGN("\\|="),
	T_OROR("\\|\\|"),
	T_OR("\\|"),
	T_ANDASSIGN("&="),
	T_ANDAND("&&"),
	T_AND("&"),
	T_XORASSIGN("\\^="),
	T_XOR("\\^"),
	T_COALESCE("\\?\\?"),
	T_QUEST("\\?"),
	T_COLON(":"),
	T_BROPEN("\\("),
	T_BRCLOSE("\\)"),
	T_SEMICOLON(";"),
	T_LESSEQ("<="),
	T_SHLASSIGN("<<="),
	T_SHL("<<"),
	T_LESS("<"),
	T_GREATEREQ(">="),
	T_USHR(">>>"),
	T_USHRASSIGN(">>>="),
	T_SHRASSIGN(">>="),
	T_SHR(">>"),
	T_GREATER(">"),
	T_LAMBDA("=>"),
	T_EQUAL3("==="),
	T_EQUAL2("=="),
	T_ASSIGN("="),
	T_NOTEQUAL2("!=="),
	T_NOTEQUAL("!="),
	T_NOT("!"),
	T_DOLLAR("$"),
	
	K_IMPORT,
	K_ALIAS,
	K_CLASS,
	K_FUNCTION,
	K_INTERFACE,
	K_ENUM,
	K_STRUCT,
	K_EXPAND,
	K_VARIANT,
	
	K_ABSTRACT,
	K_FINAL,
	K_OVERRIDE,
	K_CONST,
	K_PRIVATE,
	K_PUBLIC,
	K_EXPORT,
	K_STATIC,
	K_PROTECTED,
	K_IMPLICIT,
	K_VIRTUAL,
	K_EXTERN,
	
	K_VAL,
	K_VAR,
	K_GET,
	K_IMPLEMENTS,
	K_SET,
	
	K_VOID,
	K_ANY,
	K_BOOL,
	K_BYTE,
	K_SBYTE,
	K_SHORT,
	K_USHORT,
	K_INT,
	K_UINT,
	K_LONG,
	K_ULONG,
	K_FLOAT,
	K_DOUBLE,
	K_CHAR,
	K_STRING,
	
	K_IF,
	K_ELSE,
	K_DO,
	K_WHILE,
	K_FOR,
	K_THROW,
	K_LOCK,
	K_TRY,
	K_CATCH,
	K_FINALLY,
	K_RETURN,
	K_BREAK,
	K_CONTINUE,
	K_SWITCH,
	K_CASE,
	K_DEFAULT,
	
	K_IN,
	K_IS,
	K_AS,
	K_MATCH,
	K_THROWS,
	
	K_SUPER,
	K_THIS,
	K_NULL,
	K_TRUE,
	K_FALSE,
	K_NEW,
	
	INVALID,
	EOF
	;
		
	private final String regexp;
	private final boolean whitespace;
	
	private ZSTokenType() {
		this.regexp = null;
		this.whitespace = false;
	}
	
	private ZSTokenType(String regexp) {
		this.regexp = regexp;
		this.whitespace = false;
	}
	
	private ZSTokenType(String regexp, boolean whitespace) {
		this.regexp = regexp;
		this.whitespace = whitespace;
	}

	@Override
	public String getRegexp() {
		return regexp;
	}

	@Override
	public boolean isWhitespace() {
		return whitespace;
	}
}
