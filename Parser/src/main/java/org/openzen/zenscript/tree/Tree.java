package org.openzen.zenscript.tree;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.tree.lexer.DFALexer;
import org.openzen.zenscript.tree.lexer.PositionedToken;
import org.openzen.zenscript.tree.lexer.ZSPosTokenFactory;
import org.openzen.zenscript.tree.visitor.TreeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Tree {

	private TreeKind kind;
	private List<Child> children;

	public Tree(TreeKind kind, List<Child> children) {
		this.kind = kind;
		this.children = children;
	}

	public Tree(TreeKind kind) {
		this.kind = kind;
		this.children = new ArrayList<>();
	}

	public static Tree parse(SourceFile sourceFile) {
		DFALexer dfaLexer = new DFALexer(new ZSPosTokenFactory());
		List<PositionedToken<ZSTokenType, ZSToken>> tokens = dfaLexer.tokenize(sourceFile);
		return parse(tokens);
	}

	public static Tree parse(List<PositionedToken<ZSTokenType, ZSToken>> tokens) {
		TreeParser p = new TreeParser(tokens);
		p.file();
		return p.buildTree();
	}

	void print(StringBuilder buf, int level) {
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < level; i++) {
			indent.append("  ");
		}

		buf.append(indent).append(kind).append("\n");
		for (Child child : children) {
			if (child.isToken()) {
				buf.append(indent).append("  ").append(child.asToken().getType()).append(" \"").append(child.asToken().getContent().replaceAll("\r?\n", "\\\\n")).append("\"\n");
			} else if (child.isTree()) {
				child.asTree().print(buf, level + 1);
			}
		}
	}

	public void reportErrors(List<ParseError> errors) {
		for (Child child : children) {
			if (child.isError()) {
				errors.add(child.asError());
			} else if (child.isTree()) {
				child.asTree().reportErrors(errors);
			}
		}
	}

	public boolean hasChildOfKind(TreeKind kind) {
		return this.children().stream().anyMatch(child -> child.isTree() && child.asTree().kind() == kind);
	}

	public Tree childOfKind(TreeKind kind) {
		for (Child child : this.children()) {
			if (child.isTree() && child.asTree().kind() == kind) {
				return child.asTree();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		print(buf, 0);
		List<ParseError> errors = new ArrayList<>();
		reportErrors(errors);
		errors.forEach(e -> buf.append(e).append("\n"));
		return buf.toString();
	}

	public TreeKind kind() {
		return kind;
	}

	public Child child(int index) {
		return children.get(index);
	}

	public List<Child> children() {
		return children;
	}

	public <C, R> R accept(TreeVisitor<C, R> visitor, C context) {
		switch (this.kind()) {
			case FILE:
				return visitor.visitFile(this, context);
			case ERROR:
				return visitor.visitError(this, context);
			case NAME:
				return visitor.visitName(this, context);
			case ANNOTATION:
				return visitor.visitAnnotation(this, context);
			case IMPORT:
				return visitor.visitImport(this, context);
			case LAMBDA_BODY:
				return visitor.visitLambdaBody(this, context);
			case FUNCTION_HEADER:
				return visitor.visitFunctionHeader(this, context);
			case BLOCK:
				return visitor.visitBlock(this, context);
			case TYPE_BASIC:
				return visitor.visitBasicType(this, context);
			case TYPE_RANGE:
				return visitor.visitRangeType(this, context);
			case TYPE_ARRAY:
				return visitor.visitArrayType(this, context);
			case TYPE_GENERIC_MAP:
				return visitor.visitGenericMapType(this, context);
			case TYPE_MAP:
				return visitor.visitMapType(this, context);
			case TYPE_BRACED:
				return visitor.visitBracedType(this, context);
			case TYPE_FUNCTION:
				return visitor.visitFunctionType(this, context);
			case TYPE_NAMED:
				return visitor.visitNamedTyped(this, context);
			case TYPE_OPTIONAL:
				return visitor.visitOptionalType(this, context);
			case LABEL:
				return visitor.visitLabel(this, context);
			case FOR_NAMES:
				return visitor.visitForNames(this, context);
			case PARAMS:
				return visitor.visitParams(this, context);
			case PARAM:
				return visitor.visitParam(this, context);
			case THROWS:
				return visitor.visitThrows(this, context);
			case DEF_CLASS:
				return visitor.visitClassDefinition(this, context);
			case TYPE_ARGS:
				return visitor.visitTypeArgs(this, context);
			case TYPE_PARAMS:
				return visitor.visitTypeParams(this, context);
			case TYPE_PARAM:
				return visitor.visitTypeParam(this, context);
			case TYPE_BOUND:
				return visitor.visitTypeBound(this, context);
			case SUPER_BOUND:
				return visitor.visitSuperBound(this, context);
			case SUPER_TYPE:
				return visitor.visitSuperType(this, context);
			case SUPER_TYPES:
				return visitor.visitSuperTypes(this, context);
			case DEF_ENUM:
				return visitor.visitEnumDefinition(this, context);
			case ENUM_CONSTANT:
				return visitor.visitEnumConstant(this, context);
			case ENUM_CONSTANT_VALUE:
				return visitor.visitEnumConstantValue(this, context);
			case DEF_INTERFACE:
				return visitor.visitInterfaceDefinition(this, context);
			case DEF_FUNCTION:
				return visitor.visitFunctionDefinition(this, context);
			case DEF_STRUCT:
				return visitor.visitStructDefinition(this, context);
			case DEF_ALIAS:
				return visitor.visitAliasDefinition(this, context);
			case DEF_EXPANSION:
				return visitor.visitExpansionDefinition(this, context);
			case DEF_VARIANT:
				return visitor.visitVariantDefinition(this, context);
			case VARIANT_OPTION:
				return visitor.visitVariantOption(this, context);
			case VARIANT_OPTION_TYPES:
				return visitor.visitVariantOptionTypes(this, context);
			case SWITCH_CASES:
				return visitor.visitSwitchCases(this, context);
			case SWITCH_CASE:
				return visitor.visitSwitchCase(this, context);
			case MODIFIERS:
				return visitor.visitModifiers(this, context);
			case MEMBERS:
				return visitor.visitMembers(this, context);
			case VARIANTS:
				return visitor.visitVariants(this, context);
			case MEMB_CONSTRUCTOR:
				return visitor.visitConstructorMember(this, context);
			case MEMB_FIELD:
				return visitor.visitFieldMember(this, context);
			case TYPE_DECLARATION:
				return visitor.visitTypeDeclaration(this, context);
			case FIELD_AUTO:
				return visitor.visitFieldAuto(this, context);
			case AUTO_GET:
				return visitor.visitAutoGet(this, context);
			case AUTO_SET:
				return visitor.visitAutoSet(this, context);
			case MEMB_CASTER:
				return visitor.visitCasterMember(this, context);
			case MEMB_METHOD:
				return visitor.visitMethodMember(this, context);
			case MEMB_SETTER:
				return visitor.visitSetterMember(this, context);
			case MEMB_GETTER:
				return visitor.visitGetterMember(this, context);
			case MEMB_IMPLEMENTS:
				return visitor.visitImplementsMember(this, context);
			case MEMB_OPERATOR:
				return visitor.visitOperatorMember(this, context);
			case MEMB_DESTRUCTOR:
				return visitor.visitDestructorMember(this, context);
			case MEMB_ITERATOR:
				return visitor.visitIteratorMember(this, context);
			case MEMB_STATIC_INITIALIZER:
				return visitor.visitStaticInitializerMember(this, context);
			case STMT_RETURN:
				return visitor.visitReturnStatement(this, context);
			case STMT_VAR:
				return visitor.visitVarStatement(this, context);
			case STMT_VAL:
				return visitor.visitValStatement(this, context);
			case STMT_IF:
				return visitor.visitIfStatement(this, context);
			case STMT_ELSE:
				return visitor.visitElseStatement(this, context);
			case STMT_FOR:
				return visitor.visitForStatement(this, context);
			case STMT_DO_WHILE:
				return visitor.visitDoWhileStatement(this, context);
			case STMT_WHILE:
				return visitor.visitWhileStatement(this, context);
			case STMT_LOCK:
				return visitor.visitLockStatement(this, context);
			case STMT_THROW:
				return visitor.visitThrowStatement(this, context);
			case STMT_TRY:
				return visitor.visitTryStatement(this, context);
			case STMT_CONTINUE:
				return visitor.visitContinueStatement(this, context);
			case STMT_BREAK:
				return visitor.visitBreakStatement(this, context);
			case STMT_SWITCH:
				return visitor.visitSwitchStatement(this, context);
			case EXPR_ASSIGN:
				return visitor.visitAssignExpression(this, context);
			case EXPR_ADD_ASSIGN:
				return visitor.visitAddAssignExpression(this, context);
			case EXPR_SUB_ASSIGN:
				return visitor.visitSubAssignExpression(this, context);
			case EXPR_CAT_ASSIGN:
				return visitor.visitCatAssignExpression(this, context);
			case EXPR_MUL_ASSIGN:
				return visitor.visitMulAssignExpression(this, context);
			case EXPR_DIV_ASSIGN:
				return visitor.visitDivAssignExpression(this, context);
			case EXPR_MOD_ASSIGN:
				return visitor.visitModAssignExpression(this, context);
			case EXPR_OR_ASSIGN:
				return visitor.visitOrAssignExpression(this, context);
			case EXPR_AND_ASSIGN:
				return visitor.visitAndAssignExpression(this, context);
			case EXPR_XOR_ASSIGN:
				return visitor.visitXorAssignExpression(this, context);
			case EXPR_SHL_ASSIGN:
				return visitor.visitSHLAssignExpression(this, context);
			case EXPR_SHR_ASSIGN:
				return visitor.visitSHRAssignExpression(this, context);
			case EXPR_USHR_ASSIGN:
				return visitor.visitUSHRAssignExpression(this, context);
			case EXPR_CONDITIONAL:
				return visitor.visitConditionalExpression(this, context);
			case EXPR_OR_OR:
				return visitor.visitOrOrExpression(this, context);
			case EXPR_COALESCE:
				return visitor.visitCoalesceExpression(this, context);
			case EXPR_AND_AND:
				return visitor.visitAndAndExpression(this, context);
			case EXPR_OR:
				return visitor.visitOrExpression(this, context);
			case EXPR_XOR:
				return visitor.visitXorExpression(this, context);
			case EXPR_AND:
				return visitor.visitAndExpression(this, context);
			case EXPR_EQ:
				return visitor.visitEqualExpression(this, context);
			case EXPR_SAME:
				return visitor.visitSameExpression(this, context);
			case EXPR_NE:
				return visitor.visitNotEqualExpression(this, context);
			case EXPR_NOT_SAME:
				return visitor.visitNotSameExpression(this, context);
			case EXPR_LT:
				return visitor.visitLessThanExpression(this, context);
			case EXPR_LE:
				return visitor.visitLessThanEqualToExpression(this, context);
			case EXPR_GT:
				return visitor.visitGreaterThanExpression(this, context);
			case EXPR_GE:
				return visitor.visitGreaterThanEqualToExpression(this, context);
			case EXPR_CONTAINS:
				return visitor.visitContainsExpression(this, context);
			case EXPR_IS:
				return visitor.visitIsExpression(this, context);
			case EXPR_NOT_IN:
				return visitor.visitNotInExpression(this, context);
			case EXPR_NOT_IS:
				return visitor.visitNotIsExpression(this, context);
			case EXPR_SHL:
				return visitor.visitSHLExpression(this, context);
			case EXPR_SHR:
				return visitor.visitSHRExpression(this, context);
			case EXPR_USHR:
				return visitor.visitUSHRExpression(this, context);
			case EXPR_ADD:
				return visitor.visitAddExpression(this, context);
			case EXPR_SUB:
				return visitor.visitSubExpression(this, context);
			case EXPR_CAT:
				return visitor.visitCatExpression(this, context);
			case EXPR_MUL:
				return visitor.visitMulExpression(this, context);
			case EXPR_DIV:
				return visitor.visitDivExpression(this, context);
			case EXPR_MOD:
				return visitor.visitModExpression(this, context);
			case EXPR_NOT:
				return visitor.visitNotExpression(this, context);
			case EXPR_NEG:
				return visitor.visitNegExpression(this, context);
			case EXPR_INVERT:
				return visitor.visitInvertExpression(this, context);
			case EXPR_TRY_CONVERT:
				return visitor.visitTryConvertExpression(this, context);
			case EXPR_TRY_RETHROW:
				return visitor.visitTryRethrowExpression(this, context);
			case EXPR_MEMBER:
				return visitor.visitMemberExpression(this, context);
			case EXPR_OUTER:
				return visitor.visitOuterExpression(this, context);
			case EXPR_RANGE:
				return visitor.visitRangeExpression(this, context);
			case EXPR_INDEX:
				return visitor.visitIndexExpression(this, context);
			case INDEX_KEY:
				return visitor.visitIndexKey(this, context);
			case EXPR_CALL:
				return visitor.visitCallExpression(this, context);
			case EXPR_CAST:
				return visitor.visitCastExpression(this, context);
			case EXPR_INCREMENT:
				return visitor.visitIncrementExpression(this, context);
			case EXPR_DECREMENT:
				return visitor.visitDecrementExpression(this, context);
			case EXPR_FUNCTION:
				return visitor.visitFunctionExpression(this, context);
			case EXPR_INT:
				return visitor.visitIntExpression(this, context);
			case EXPR_PREFIXED_INT:
				return visitor.visitPrefixedIntExpression(this, context);
			case EXPR_FLOAT:
				return visitor.visitFloatExpression(this, context);
			case EXPR_STRING:
				return visitor.visitStringExpression(this, context);
			case EXPR_VARIABLE:
				return visitor.visitVariableExpression(this, context);
			case EXPR_LOCAL_VARIABLE:
				return visitor.visitLocalVariableExpression(this, context);
			case EXPR_THIS:
				return visitor.visitThisExpression(this, context);
			case EXPR_SUPER:
				return visitor.visitSuperExpression(this, context);
			case EXPR_DOLLAR:
				return visitor.visitDollarExpression(this, context);
			case EXPR_ARRAY:
				return visitor.visitArrayExpression(this, context);
			case EXPR_MAP:
				return visitor.visitMapExpression(this, context);
			case MAP_KEY:
				return visitor.visitMapKey(this, context);
			case MAP_VALUE:
				return visitor.visitMapValue(this, context);
			case EXPR_BOOL:
				return visitor.visitBoolExpression(this, context);
			case EXPR_NULL:
				return visitor.visitNullExpression(this, context);
			case EXPR_BRACKET:
				return visitor.visitBracketExpression(this, context);
			case EXPR_NEW:
				return visitor.visitNewExpression(this, context);
			case EXPR_THROW:
				return visitor.visitThrowExpression(this, context);
			case EXPR_PANIC:
				return visitor.visitPanicExpression(this, context);
			case EXPR_MATCH:
				return visitor.visitMatchExpression(this, context);
			case MATCH_KEY:
				return visitor.visitMatchKey(this, context);
			case EXPR_BEP:
				return visitor.visitBEP(this, context);
			case EXPR_TYPE:
				return visitor.visitTypeExpression(this, context);
			case CALL_ARGUMENTS:
				return visitor.visitCallArguments(this, context);
			case CALL_ARGUMENT:
				return visitor.visitCallArgument(this, context);
			case T_COMMENT_SCRIPT:
				return visitor.visitScriptComment(this, context);
			case T_COMMENT_SINGLELINE:
				return visitor.visitComment(this, context);
			case T_COMMENT_MULTILINE:
				return visitor.visitMultilineComment(this, context);
			case T_WHITESPACE:
				return visitor.visitWhitespace(this, context);
		}
		throw new IllegalStateException(this.kind.toString() + " is not a valid kind for a Tree");
	}

	public List<Tree> treeChildren() {
		return children.stream().filter(Child::isTree).map(Child::asTree).collect(Collectors.toList());
	}

	public List<Child> filterWhitespaceChildren() {
		return children.stream().filter(child -> !(child.isToken() && child.asToken().getType().isWhitespace())).collect(Collectors.toList());
	}

	public CodePosition position(SourceFile file) {
		CodePosition position = null;
		for (Child child : children()) {
			CodePosition newPos = null;
			if (child.isTree() && child.asTree().kind() != TreeKind.ERROR) {
				newPos = child.asTree().position(file);
			} else if (child.isToken() && child.asToken().getType() != ZSTokenType.T_WHITESPACE) {
				newPos = child.asToken().position();
			} else if (child.isError()) {
//				newPos = child.asError().position();
			}
			if (newPos != null) {
				if (position == null) {
					position = newPos;
				} else {
					position = position.merge(newPos);
				}
			}
		}

		return position;
	}
}
