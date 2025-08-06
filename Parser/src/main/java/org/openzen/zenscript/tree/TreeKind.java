package org.openzen.zenscript.tree;

public enum TreeKind {

	FILE,
	ERROR,
	NAME,
	ANNOTATION,
	IMPORT,
	LAMBDA_BODY,
	FUNCTION_HEADER,
	BLOCK,
	TYPE_BASIC,
	TYPE_RANGE,
	TYPE_ARRAY,
	TYPE_GENERIC_MAP,
	TYPE_MAP,
	TYPE_OPTIONAL,
	TYPE_BRACED,
	TYPE_FUNCTION,
	TYPE_NAMED,
	LABEL,
	FOR_NAMES,
	PARAMS,
	PARAM,
	THROWS,
	DEF_CLASS,
	TYPE_PARAMS,
	TYPE_ARGS,
	TYPE_PARAM,
	TYPE_BOUND,
	SUPER_BOUND,
	SUPER_TYPE,
	SUPER_TYPES,
	DEF_ENUM,
	ENUM_CONSTANT,
	ENUM_CONSTANT_VALUE,
	DEF_INTERFACE,
	DEF_FUNCTION,
	DEF_STRUCT,
	DEF_ALIAS,
	DEF_EXPANSION,
	DEF_VARIANT,
	VARIANTS,
	VARIANT_OPTION,
	VARIANT_OPTION_TYPES,
	SWITCH_CASES,
	SWITCH_CASE,
	MODIFIERS,
	MEMBERS,
	MEMB_CONSTRUCTOR,
	MEMB_FIELD,
	TYPE_DECLARATION,
	FIELD_AUTO,
	AUTO_GET,
	AUTO_SET,
	MEMB_CASTER,
	MEMB_METHOD,
	MEMB_SETTER,
	MEMB_GETTER,
	MEMB_IMPLEMENTS,
	MEMB_OPERATOR,
	MEMB_DESTRUCTOR,
	MEMB_ITERATOR,
	MEMB_STATIC_INITIALIZER,

	STMT_RETURN,
	STMT_VAR,
	STMT_VAL,
	STMT_IF,
	STMT_ELSE,
	STMT_FOR,
	STMT_DO_WHILE,
	STMT_WHILE,
	STMT_LOCK,
	STMT_THROW,
	STMT_TRY,
	STMT_CONTINUE,
	STMT_BREAK,
	STMT_SWITCH,

	EXPR_ASSIGN,
	EXPR_ADD_ASSIGN,
	EXPR_SUB_ASSIGN,
	EXPR_CAT_ASSIGN,
	EXPR_MUL_ASSIGN,
	EXPR_DIV_ASSIGN,
	EXPR_MOD_ASSIGN,
	EXPR_OR_ASSIGN,
	EXPR_AND_ASSIGN,
	EXPR_XOR_ASSIGN,
	EXPR_SHL_ASSIGN,
	EXPR_SHR_ASSIGN,
	EXPR_USHR_ASSIGN,
	EXPR_CONDITIONAL,
	EXPR_OR_OR,
	EXPR_COALESCE,
	EXPR_AND_AND,
	EXPR_OR,
	EXPR_XOR,
	EXPR_AND,
	EXPR_EQ,
	EXPR_SAME,
	EXPR_NE,
	EXPR_NOT_SAME,
	EXPR_LT,
	EXPR_LE,
	EXPR_GT,
	EXPR_GE,
	EXPR_CONTAINS,
	EXPR_IS,
	EXPR_NOT_IN,
	EXPR_NOT_IS,
	EXPR_SHL,
	EXPR_SHR,
	EXPR_USHR,
	EXPR_ADD,
	EXPR_SUB,
	EXPR_CAT,
	EXPR_MUL,
	EXPR_DIV,
	EXPR_MOD,
	EXPR_NOT,
	EXPR_NEG,
	EXPR_INVERT,
	EXPR_TRY_CONVERT,
	EXPR_TRY_RETHROW,
	EXPR_MEMBER,
	EXPR_OUTER,
	EXPR_RANGE,
	EXPR_INDEX,
	//TODO do we want this?
	INDEX_KEY,
	EXPR_CALL,
	EXPR_CAST,
	EXPR_INCREMENT,
	EXPR_DECREMENT,
	EXPR_FUNCTION,
	EXPR_INT,
	EXPR_PREFIXED_INT,
	EXPR_FLOAT,
	EXPR_STRING,
	EXPR_VARIABLE,
	EXPR_LOCAL_VARIABLE,
	EXPR_THIS,
	EXPR_SUPER,
	EXPR_DOLLAR,
	EXPR_ARRAY,
	EXPR_MAP,
	MAP_KEY,
	MAP_VALUE,
	EXPR_BOOL,
	EXPR_NULL,
	// ()
	EXPR_BRACKET,
	EXPR_NEW,
	EXPR_THROW,
	EXPR_PANIC,
	EXPR_MATCH,
	MATCH_KEY,
	EXPR_BEP,
	EXPR_TYPE,

	CALL_ARGUMENTS,
	CALL_ARGUMENT,

	//TODO remove?
	T_COMMENT_SCRIPT,
	T_COMMENT_SINGLELINE,
	T_COMMENT_MULTILINE,
	T_WHITESPACE;

	public boolean isDefinitionMember() {
		switch (this) {
			case MEMB_CONSTRUCTOR:
			case MEMB_FIELD:
			case MEMB_CASTER:
			case MEMB_METHOD:
			case MEMB_SETTER:
			case MEMB_GETTER:
			case MEMB_IMPLEMENTS:
			case MEMB_OPERATOR:
			case MEMB_DESTRUCTOR:
			case MEMB_ITERATOR:
			case MEMB_STATIC_INITIALIZER:
				return true;
			default:
				return false;
		}

	}

	public boolean isStatement() {
		switch (this) {
			case BLOCK:
			case STMT_RETURN:
			case STMT_VAR:
			case STMT_VAL:
			case STMT_IF:
			case STMT_ELSE:
			case STMT_FOR:
			case STMT_DO_WHILE:
			case STMT_WHILE:
			case STMT_LOCK:
			case STMT_THROW:
			case STMT_TRY:
			case STMT_CONTINUE:
			case STMT_BREAK:
			case STMT_SWITCH:
				return true;
			default:
				return false;
		}
	}

	public boolean isType() {
		switch (this) {
			case TYPE_BASIC:
			case TYPE_RANGE:
			case TYPE_ARRAY:
			case TYPE_GENERIC_MAP:
			case TYPE_MAP:
			case TYPE_OPTIONAL:
			case TYPE_BRACED:
			case TYPE_FUNCTION:
			case TYPE_NAMED:
				return true;
		}
		return false;
	}

	public boolean isExpression() {
		switch (this) {
			case EXPR_ASSIGN:
			case EXPR_ADD_ASSIGN:
			case EXPR_SUB_ASSIGN:
			case EXPR_CAT_ASSIGN:
			case EXPR_MUL_ASSIGN:
			case EXPR_DIV_ASSIGN:
			case EXPR_MOD_ASSIGN:
			case EXPR_OR_ASSIGN:
			case EXPR_AND_ASSIGN:
			case EXPR_XOR_ASSIGN:
			case EXPR_SHL_ASSIGN:
			case EXPR_SHR_ASSIGN:
			case EXPR_USHR_ASSIGN:
			case EXPR_CONDITIONAL:
			case EXPR_OR_OR:
			case EXPR_COALESCE:
			case EXPR_AND_AND:
			case EXPR_OR:
			case EXPR_XOR:
			case EXPR_AND:
			case EXPR_EQ:
			case EXPR_SAME:
			case EXPR_NE:
			case EXPR_NOT_SAME:
			case EXPR_LT:
			case EXPR_LE:
			case EXPR_GT:
			case EXPR_GE:
			case EXPR_CONTAINS:
			case EXPR_IS:
			case EXPR_NOT_IN:
			case EXPR_NOT_IS:
			case EXPR_SHL:
			case EXPR_SHR:
			case EXPR_USHR:
			case EXPR_ADD:
			case EXPR_SUB:
			case EXPR_CAT:
			case EXPR_MUL:
			case EXPR_DIV:
			case EXPR_MOD:
			case EXPR_NOT:
			case EXPR_NEG:
			case EXPR_INVERT:
			case EXPR_TRY_CONVERT:
			case EXPR_TRY_RETHROW:
			case EXPR_MEMBER:
			case EXPR_OUTER:
			case EXPR_RANGE:
			case EXPR_INDEX:
			case EXPR_CALL:
			case EXPR_CAST:
			case EXPR_INCREMENT:
			case EXPR_DECREMENT:
			case EXPR_FUNCTION:
			case EXPR_INT:
			case EXPR_PREFIXED_INT:
			case EXPR_FLOAT:
			case EXPR_STRING:
			case EXPR_VARIABLE:
			case EXPR_LOCAL_VARIABLE:
			case EXPR_THIS:
			case EXPR_SUPER:
			case EXPR_DOLLAR:
			case EXPR_ARRAY:
			case EXPR_MAP:
			case EXPR_BOOL:
			case EXPR_NULL:
			case EXPR_BRACKET:
			case EXPR_NEW:
			case EXPR_THROW:
			case EXPR_PANIC:
			case EXPR_MATCH:
			case EXPR_BEP:
			case EXPR_TYPE:
				return true;
			default:
				return false;
		}
	}

//	public boolean isToken() {
//		switch (this) {
//			case T_COMMENT_SCRIPT:
//			case T_COMMENT_SINGLELINE:
//			case T_COMMENT_MULTILINE:
//			case T_WHITESPACE:
//			case T_IDENTIFIER:
//			case T_LOCAL_IDENTIFIER:
//			case T_FLOAT:
//			case T_PREFIXED_INT:
//			case T_INT:
//			case T_STRING_DQ:
//			case T_STRING_DQ_WYSIWYG:
//			case T_STRING_SQ:
//			case T_STRING_SQ_WYSIWYG:
//			case T_AOPEN:
//			case T_ACLOSE:
//			case T_SQOPEN:
//			case T_SQCLOSE:
//			case T_DOT3:
//			case T_DOT2:
//			case T_DOT:
//			case T_COMMA:
//			case T_INCREMENT:
//			case T_ADDASSIGN:
//			case T_ADD:
//			case T_DECREMENT:
//			case T_SUBASSIGN:
//			case T_SUB:
//			case T_CATASSIGN:
//			case T_CAT:
//			case T_MULASSIGN:
//			case T_MUL:
//			case T_DIVASSIGN:
//			case T_DIV:
//			case T_MODASSIGN:
//			case T_MOD:
//			case T_ORASSIGN:
//			case T_OROR:
//			case T_OR:
//			case T_ANDASSIGN:
//			case T_ANDAND:
//			case T_AND:
//			case T_XORASSIGN:
//			case T_XOR:
//			case T_COALESCE:
//			case T_OPTCALL:
//			case T_QUEST:
//			case T_COLON:
//			case T_BROPEN:
//			case T_BRCLOSE:
//			case T_SEMICOLON:
//			case T_LESSEQ:
//			case T_SHLASSIGN:
//			case T_SHL:
//			case T_LESS:
//			case T_GREATEREQ:
//			case T_USHR:
//			case T_USHRASSIGN:
//			case T_SHRASSIGN:
//			case T_SHR:
//			case T_GREATER:
//			case T_LAMBDA:
//			case T_EQUAL3:
//			case T_EQUAL2:
//			case T_ASSIGN:
//			case T_NOTEQUAL2:
//			case T_NOTEQUAL:
//			case T_NOT:
//			case T_DOLLAR:
//			case T_BACKTICK:
//			case K_IMPORT:
//			case K_ALIAS:
//			case K_CLASS:
//			case K_FUNCTION:
//			case K_INTERFACE:
//			case K_ENUM:
//			case K_STRUCT:
//			case K_EXPAND:
//			case K_VARIANT:
//			case K_ABSTRACT:
//			case K_FINAL:
//			case K_OVERRIDE:
//			case K_CONST:
//			case K_PRIVATE:
//			case K_PUBLIC:
//			case K_EXPORT:
//			case K_INTERNAL:
//			case K_STATIC:
//			case K_PROTECTED:
//			case K_IMPLICIT:
//			case K_VIRTUAL:
//			case K_EXTERN:
//			case K_IMMUTABLE:
//			case K_VAL:
//			case K_VAR:
//			case K_GET:
//			case K_IMPLEMENTS:
//			case K_SET:
//			case K_VOID:
//			case K_BOOL:
//			case K_BYTE:
//			case K_SBYTE:
//			case K_SHORT:
//			case K_USHORT:
//			case K_INT:
//			case K_UINT:
//			case K_LONG:
//			case K_ULONG:
//			case K_USIZE:
//			case K_FLOAT:
//			case K_DOUBLE:
//			case K_CHAR:
//			case K_STRING:
//			case K_IF:
//			case K_ELSE:
//			case K_DO:
//			case K_WHILE:
//			case K_FOR:
//			case K_THROW:
//			case K_PANIC:
//			case K_LOCK:
//			case K_TRY:
//			case K_CATCH:
//			case K_FINALLY:
//			case K_RETURN:
//			case K_BREAK:
//			case K_CONTINUE:
//			case K_SWITCH:
//			case K_CASE:
//			case K_DEFAULT:
//			case K_IN:
//			case K_IS:
//			case K_AS:
//			case K_MATCH:
//			case K_THROWS:
//			case K_SUPER:
//			case K_THIS:
//			case K_NULL:
//			case K_TRUE:
//			case K_FALSE:
//			case K_NEW:
//			case INVALID:
//				return true;
//		}
//		return false;
//	}

//	public static TreeKind fromTokenType(ZSTokenType type) {
//
//		switch (type) {
//			case T_COMMENT_SCRIPT:
//				return TreeKind.T_COMMENT_SCRIPT;
//			case T_COMMENT_SINGLELINE:
//				return TreeKind.T_COMMENT_SINGLELINE;
//			case T_COMMENT_MULTILINE:
//				return TreeKind.T_COMMENT_MULTILINE;
//			case T_WHITESPACE:
//				return TreeKind.T_WHITESPACE;
//			case T_IDENTIFIER:
//				return TreeKind.T_IDENTIFIER;
//			case T_LOCAL_IDENTIFIER:
//				return TreeKind.T_LOCAL_IDENTIFIER;
//			case T_FLOAT:
//				return TreeKind.T_FLOAT;
//			case T_PREFIXED_INT:
//				return TreeKind.T_PREFIXED_INT;
//			case T_INT:
//				return TreeKind.T_INT;
//			case T_STRING_DQ:
//				return TreeKind.T_STRING_DQ;
//			case T_STRING_DQ_WYSIWYG:
//				return TreeKind.T_STRING_DQ_WYSIWYG;
//			case T_STRING_SQ:
//				return TreeKind.T_STRING_SQ;
//			case T_STRING_SQ_WYSIWYG:
//				return TreeKind.T_STRING_SQ_WYSIWYG;
//			case T_AOPEN:
//				return TreeKind.T_AOPEN;
//			case T_ACLOSE:
//				return TreeKind.T_ACLOSE;
//			case T_SQOPEN:
//				return TreeKind.T_SQOPEN;
//			case T_SQCLOSE:
//				return TreeKind.T_SQCLOSE;
//			case T_DOT3:
//				return TreeKind.T_DOT3;
//			case T_DOT2:
//				return TreeKind.T_DOT2;
//			case T_DOT:
//				return TreeKind.T_DOT;
//			case T_COMMA:
//				return TreeKind.T_COMMA;
//			case T_INCREMENT:
//				return TreeKind.T_INCREMENT;
//			case T_ADDASSIGN:
//				return TreeKind.T_ADDASSIGN;
//			case T_ADD:
//				return TreeKind.T_ADD;
//			case T_DECREMENT:
//				return TreeKind.T_DECREMENT;
//			case T_SUBASSIGN:
//				return TreeKind.T_SUBASSIGN;
//			case T_SUB:
//				return TreeKind.T_SUB;
//			case T_CATASSIGN:
//				return TreeKind.T_CATASSIGN;
//			case T_CAT:
//				return TreeKind.T_CAT;
//			case T_MULASSIGN:
//				return TreeKind.T_MULASSIGN;
//			case T_MUL:
//				return TreeKind.T_MUL;
//			case T_DIVASSIGN:
//				return TreeKind.T_DIVASSIGN;
//			case T_DIV:
//				return TreeKind.T_DIV;
//			case T_MODASSIGN:
//				return TreeKind.T_MODASSIGN;
//			case T_MOD:
//				return TreeKind.T_MOD;
//			case T_ORASSIGN:
//				return TreeKind.T_ORASSIGN;
//			case T_OROR:
//				return TreeKind.T_OROR;
//			case T_OR:
//				return TreeKind.T_OR;
//			case T_ANDASSIGN:
//				return TreeKind.T_ANDASSIGN;
//			case T_ANDAND:
//				return TreeKind.T_ANDAND;
//			case T_AND:
//				return TreeKind.T_AND;
//			case T_XORASSIGN:
//				return TreeKind.T_XORASSIGN;
//			case T_XOR:
//				return TreeKind.T_XOR;
//			case T_COALESCE:
//				return TreeKind.T_COALESCE;
//			case T_OPTCALL:
//				return TreeKind.T_OPTCALL;
//			case T_QUEST:
//				return TreeKind.T_QUEST;
//			case T_COLON:
//				return TreeKind.T_COLON;
//			case T_BROPEN:
//				return TreeKind.T_BROPEN;
//			case T_BRCLOSE:
//				return TreeKind.T_BRCLOSE;
//			case T_SEMICOLON:
//				return TreeKind.T_SEMICOLON;
//			case T_LESSEQ:
//				return TreeKind.T_LESSEQ;
//			case T_SHLASSIGN:
//				return TreeKind.T_SHLASSIGN;
//			case T_SHL:
//				return TreeKind.T_SHL;
//			case T_LESS:
//				return TreeKind.T_LESS;
//			case T_GREATEREQ:
//				return TreeKind.T_GREATEREQ;
//			case T_USHR:
//				return TreeKind.T_USHR;
//			case T_USHRASSIGN:
//				return TreeKind.T_USHRASSIGN;
//			case T_SHRASSIGN:
//				return TreeKind.T_SHRASSIGN;
//			case T_SHR:
//				return TreeKind.T_SHR;
//			case T_GREATER:
//				return TreeKind.T_GREATER;
//			case T_LAMBDA:
//				return TreeKind.T_LAMBDA;
//			case T_EQUAL3:
//				return TreeKind.T_EQUAL3;
//			case T_EQUAL2:
//				return TreeKind.T_EQUAL2;
//			case T_ASSIGN:
//				return TreeKind.T_ASSIGN;
//			case T_NOTEQUAL2:
//				return TreeKind.T_NOTEQUAL2;
//			case T_NOTEQUAL:
//				return TreeKind.T_NOTEQUAL;
//			case T_NOT:
//				return TreeKind.T_NOT;
//			case T_DOLLAR:
//				return TreeKind.T_DOLLAR;
//			case T_BACKTICK:
//				return TreeKind.T_BACKTICK;
//			case K_IMPORT:
//				return TreeKind.K_IMPORT;
//			case K_ALIAS:
//				return TreeKind.K_ALIAS;
//			case K_CLASS:
//				return TreeKind.K_CLASS;
//			case K_FUNCTION:
//				return TreeKind.K_FUNCTION;
//			case K_INTERFACE:
//				return TreeKind.K_INTERFACE;
//			case K_ENUM:
//				return TreeKind.K_ENUM;
//			case K_STRUCT:
//				return TreeKind.K_STRUCT;
//			case K_EXPAND:
//				return TreeKind.K_EXPAND;
//			case K_VARIANT:
//				return TreeKind.K_VARIANT;
//			case K_ABSTRACT:
//				return TreeKind.K_ABSTRACT;
//			case K_FINAL:
//				return TreeKind.K_FINAL;
//			case K_OVERRIDE:
//				return TreeKind.K_OVERRIDE;
//			case K_CONST:
//				return TreeKind.K_CONST;
//			case K_PRIVATE:
//				return TreeKind.K_PRIVATE;
//			case K_PUBLIC:
//				return TreeKind.K_PUBLIC;
//			case K_EXPORT:
//				return TreeKind.K_EXPORT;
//			case K_INTERNAL:
//				return TreeKind.K_INTERNAL;
//			case K_STATIC:
//				return TreeKind.K_STATIC;
//			case K_PROTECTED:
//				return TreeKind.K_PROTECTED;
//			case K_IMPLICIT:
//				return TreeKind.K_IMPLICIT;
//			case K_VIRTUAL:
//				return TreeKind.K_VIRTUAL;
//			case K_EXTERN:
//				return TreeKind.K_EXTERN;
//			case K_IMMUTABLE:
//				return TreeKind.K_IMMUTABLE;
//			case K_VAL:
//				return TreeKind.K_VAL;
//			case K_VAR:
//				return TreeKind.K_VAR;
//			case K_GET:
//				return TreeKind.K_GET;
//			case K_IMPLEMENTS:
//				return TreeKind.K_IMPLEMENTS;
//			case K_SET:
//				return TreeKind.K_SET;
//			case K_VOID:
//				return TreeKind.K_VOID;
//			case K_BOOL:
//				return TreeKind.K_BOOL;
//			case K_BYTE:
//				return TreeKind.K_BYTE;
//			case K_SBYTE:
//				return TreeKind.K_SBYTE;
//			case K_SHORT:
//				return TreeKind.K_SHORT;
//			case K_USHORT:
//				return TreeKind.K_USHORT;
//			case K_INT:
//				return TreeKind.K_INT;
//			case K_UINT:
//				return TreeKind.K_UINT;
//			case K_LONG:
//				return TreeKind.K_LONG;
//			case K_ULONG:
//				return TreeKind.K_ULONG;
//			case K_USIZE:
//				return TreeKind.K_USIZE;
//			case K_FLOAT:
//				return TreeKind.K_FLOAT;
//			case K_DOUBLE:
//				return TreeKind.K_DOUBLE;
//			case K_CHAR:
//				return TreeKind.K_CHAR;
//			case K_STRING:
//				return TreeKind.K_STRING;
//			case K_IF:
//				return TreeKind.K_IF;
//			case K_ELSE:
//				return TreeKind.K_ELSE;
//			case K_DO:
//				return TreeKind.K_DO;
//			case K_WHILE:
//				return TreeKind.K_WHILE;
//			case K_FOR:
//				return TreeKind.K_FOR;
//			case K_THROW:
//				return TreeKind.K_THROW;
//			case K_PANIC:
//				return TreeKind.K_PANIC;
//			case K_LOCK:
//				return TreeKind.K_LOCK;
//			case K_TRY:
//				return TreeKind.K_TRY;
//			case K_CATCH:
//				return TreeKind.K_CATCH;
//			case K_FINALLY:
//				return TreeKind.K_FINALLY;
//			case K_RETURN:
//				return TreeKind.K_RETURN;
//			case K_BREAK:
//				return TreeKind.K_BREAK;
//			case K_CONTINUE:
//				return TreeKind.K_CONTINUE;
//			case K_SWITCH:
//				return TreeKind.K_SWITCH;
//			case K_CASE:
//				return TreeKind.K_CASE;
//			case K_DEFAULT:
//				return TreeKind.K_DEFAULT;
//			case K_IN:
//				return TreeKind.K_IN;
//			case K_IS:
//				return TreeKind.K_IS;
//			case K_AS:
//				return TreeKind.K_AS;
//			case K_MATCH:
//				return TreeKind.K_MATCH;
//			case K_THROWS:
//				return TreeKind.K_THROWS;
//			case K_SUPER:
//				return TreeKind.K_SUPER;
//			case K_THIS:
//				return TreeKind.K_THIS;
//			case K_NULL:
//				return TreeKind.K_NULL;
//			case K_TRUE:
//				return TreeKind.K_TRUE;
//			case K_FALSE:
//				return TreeKind.K_FALSE;
//			case K_NEW:
//				return TreeKind.K_NEW;
//			case INVALID:
//				return TreeKind.INVALID;
//		}
//		throw new IllegalArgumentException();
//	}
}
