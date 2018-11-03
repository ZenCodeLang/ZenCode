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
	T_COMMENT_SINGLELINE("//[^\n]*", true),
	T_COMMENT_MULTILINE("/\\*([^\\*]|(\\*+([^\\*/])))*\\*+/", true, true),
	T_WHITESPACE_SPACE(true, " ", " "),
	T_WHITESPACE_TAB(true, "\t", "\t"),
	T_WHITESPACE_NEWLINE(true, "\n", "\n", true),
	T_WHITESPACE_CARRIAGE_RETURN(true, "\r", "\r"),
	T_IDENTIFIER("@?[a-zA-Z_][a-zA-Z_0-9]*"),
	T_LOCAL_IDENTIFIER("$[a-zA-Z_][a-zA-Z_0-9]*"),
	T_FLOAT("\\-?(0|[1-9][0-9]*)\\.[0-9]+([eE][\\+\\-]?[0-9]+)?[a-zA-Z_]*"),
	T_INT("\\-?(0|[1-9][0-9_]*)[a-zA-Z_]*"),
	T_PREFIXED_INT("\\-?(0b|0x|0o|0B|0X|0O)(0|[1-9a-fA-F][0-9a-fA-F_]*)[a-zA-Z_]*"),
	T_STRING_DQ("\"([^\"\\\\\\n]|\\\\([\'\"\\\\/bfnrt&]|u[0-9a-fA-F]{4}))*\""),
	T_STRING_DQ_WYSIWYG("@\"[^\"]\""),
	T_STRING_SQ("\'([^\'\\\\\\n]|\\\\([\'\"\\\\/bfnrt&]|u[0-9a-fA-F]{4}))*\'"),
	T_STRING_SQ_WYSIWYG("@\'[^\']\'"),
	T_AOPEN("\\{", "{"),
	T_ACLOSE("\\}", "}"),
	T_SQOPEN("\\[", "["),
	T_SQCLOSE("\\]", "]"),
	T_DOT3("\\.\\.\\.", "..."),
	T_DOT2("\\.\\.", ".."),
	T_DOT("\\.", "."),
	T_COMMA(",", ","),
	T_INCREMENT("\\+\\+", "++"),
	T_ADDASSIGN("\\+=", "+="),
	T_ADD("\\+", "+"),
	T_DECREMENT("\\-\\-", "--"),
	T_SUBASSIGN("\\-=", "-="),
	T_SUB("\\-", "-"),
	T_CATASSIGN("~=", "~="),
	T_CAT("~", "~"),
	T_MULASSIGN("\\*=", "*="),
	T_MUL("\\*", "*"),
	T_DIVASSIGN("/=", "/="),
	T_DIV("/", "/"),
	T_MODASSIGN("%=", "%="),
	T_MOD("%", "%"),
	T_ORASSIGN("\\|=", "|="),
	T_OROR("\\|\\|", "||"),
	T_OR("\\|", "|"),
	T_ANDASSIGN("&=", "&="),
	T_ANDAND("&&", "&&"),
	T_AND("&", "&"),
	T_XORASSIGN("\\^=", "^="),
	T_XOR("\\^", "^"),
	T_COALESCE("\\?\\?", "??"),
	T_OPTCALL("\\?\\.", "?."),
	T_QUEST("\\?", "?"),
	T_COLON(":", ":"),
	T_BROPEN("\\(", "("),
	T_BRCLOSE("\\)", ")"),
	T_SEMICOLON(";", ";"),
	T_LESSEQ("<=", "<="),
	T_SHLASSIGN("<<=", "<<="),
	T_SHL("<<", "<<"),
	T_LESS("<", "<"),
	T_GREATEREQ(">=", ">="),
	T_USHR(">>>", ">>>"),
	T_USHRASSIGN(">>>=", ">>>="),
	T_SHRASSIGN(">>=", ">>="),
	T_SHR(">>", ">>"),
	T_GREATER(">", ">"),
	T_LAMBDA("=>", "=>"),
	T_EQUAL3("===", "==="),
	T_EQUAL2("==", "=="),
	T_ASSIGN("=", "="),
	T_NOTEQUAL2("!==", "!=="),
	T_NOTEQUAL("!=", "!="),
	T_NOT("!", "!"),
	T_DOLLAR("$", "$"),
	T_BACKTICK("`", "`"),
	
	K_IMPORT(true, "import"),
	K_ALIAS(true, "alias"),
	K_CLASS(true, "class"),
	K_FUNCTION(true, "function"),
	K_INTERFACE(true, "interface"),
	K_ENUM(true, "enum"),
	K_STRUCT(true, "struct"),
	K_EXPAND(true, "expand"),
	K_VARIANT(true, "variant"),
	
	K_ABSTRACT(true, "abstract"),
	K_FINAL(true, "final"),
	K_OVERRIDE(true, "override"),
	K_CONST(true, "const"),
	K_PRIVATE(true, "private"),
	K_PUBLIC(true, "public"),
	K_EXPORT(true, "export"),
	K_INTERNAL(true, "internal"),
	K_STATIC(true, "static"),
	K_PROTECTED(true, "protected"),
	K_IMPLICIT(true, "implicit"),
	K_VIRTUAL(true, "virtual"),
	K_EXTERN(true, "extern"),
	K_IMMUTABLE(true, "immutable"),
	
	K_VAL(true, "val"),
	K_VAR(true, "var"),
	K_GET(true, "get"),
	K_IMPLEMENTS(true, "implements"),
	K_SET(true, "set"),
	
	K_VOID(true, "void"),
	K_BOOL(true, "bool"),
	K_BYTE(true, "byte"),
	K_SBYTE(true, "sbyte"),
	K_SHORT(true, "short"),
	K_USHORT(true, "ushort"),
	K_INT(true, "int"),
	K_UINT(true, "uint"),
	K_LONG(true, "long"),
	K_ULONG(true, "ulong"),
	K_USIZE(true, "usize"),
	K_FLOAT(true, "float"),
	K_DOUBLE(true, "double"),
	K_CHAR(true, "char"),
	K_STRING(true, "string"),
	
	K_IF(true, "if"),
	K_ELSE(true, "else"),
	K_DO(true, "do"),
	K_WHILE(true, "while"),
	K_FOR(true, "for"),
	K_THROW(true, "throw"),
	K_PANIC(true, "panic"),
	K_LOCK(true, "lock"),
	K_TRY(true, "try"),
	K_CATCH(true, "catch"),
	K_FINALLY(true, "finally"),
	K_RETURN(true, "return"),
	K_BREAK(true, "break"),
	K_CONTINUE(true, "continue"),
	K_SWITCH(true, "switch"),
	K_CASE(true, "case"),
	K_DEFAULT(true, "default"),
	
	K_IN(true, "in"),
	K_IS(true, "is"),
	K_AS(true, "as"),
	K_MATCH(true, "match"),
	K_THROWS(true, "throws"),
	
	K_SUPER(true, "super"),
	K_THIS(true, "this"),
	K_NULL(true, "null"),
	K_TRUE(true, "true"),
	K_FALSE(true, "false"),
	K_NEW(true, "new"),
	
	INVALID,
	EOF
	;
		
	private final String regexp;
	private final boolean whitespace;
	
	public final ZSToken flyweight;
	public final boolean isKeyword;
	public final boolean multiline;
	
	ZSTokenType() {
		this.regexp = null;
		this.whitespace = false;
		this.isKeyword = false;
		this.flyweight = null;
		this.multiline = false;
	}
	
	ZSTokenType(String regexp) {
		this.regexp = regexp;
		this.whitespace = false;
		this.isKeyword = false;
		this.flyweight = null;
		this.multiline = false;
	}
	
	ZSTokenType(String regexp, boolean whitespace) {
		this.regexp = regexp;
		this.whitespace = whitespace;
		this.isKeyword = false;
		this.flyweight = null;
		this.multiline = false;
	}
	
	ZSTokenType(String regexp, boolean whitespace, boolean multiline) {
		this.regexp = regexp;
		this.whitespace = whitespace;
		this.isKeyword = false;
		this.flyweight = null;
		this.multiline = multiline;
	}
	
	ZSTokenType(String regexp, String content) {
		this.regexp = regexp;
		this.whitespace = false;
		this.isKeyword = false;
		this.flyweight = new ZSToken(this, content, content);
		this.multiline = false;
	}
	
	ZSTokenType(boolean isWhitespace, String regexp, String content) {
		this.regexp = regexp;
		this.whitespace = isWhitespace;
		this.isKeyword = false;
		this.flyweight = new ZSToken(this, content, content);
		this.multiline = false;
	}
	
	ZSTokenType(boolean isWhitespace, String regexp, String content, boolean multiline) {
		this.regexp = regexp;
		this.whitespace = isWhitespace;
		this.isKeyword = false;
		this.flyweight = new ZSToken(this, content, content);
		this.multiline = multiline;
	}
	
	ZSTokenType(boolean isKeyword, String content) {
		this.regexp = null;
		this.whitespace = false;
		this.isKeyword = isKeyword;
		this.flyweight = new ZSToken(this, content, content);
		this.multiline = false;
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
