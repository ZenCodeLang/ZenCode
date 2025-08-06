package org.openzen.zenscript.tree.visitor;

import org.openzen.zenscript.tree.Child;
import org.openzen.zenscript.tree.Tree;

import java.util.function.Consumer;

public interface TreeVisitor<C, R> {

	default R visitChildren(Tree tree, C context) {
		for (Child child : tree.children()) {
			if (child.isTree()) {
				return child.asTree().accept(this, context);
			}
		}
		return null;
	}

	default void visitChildren(Tree tree, C context, Consumer<R> childConsumer) {
		for (Child child : tree.children()) {
			if (child.isTree()) {
				childConsumer.accept(child.asTree().accept(this, context));
			}
		}
	}

	R visitFile(Tree tree, C context);

	R visitError(Tree tree, C context);

	R visitName(Tree tree, C context);

	R visitAnnotation(Tree tree, C context);

	R visitImport(Tree tree, C context);

	R visitLambdaBody(Tree tree, C context);

	R visitFunctionHeader(Tree tree, C context);

	R visitBlock(Tree tree, C context);

	R visitBasicType(Tree tree, C context);

	R visitRangeType(Tree tree, C context);

	R visitArrayType(Tree tree, C context);

	R visitGenericMapType(Tree tree, C context);

	R visitMapType(Tree tree, C context);

	R visitOptionalType(Tree tree, C context);

	R visitBracedType(Tree tree, C context);

	R visitFunctionType(Tree tree, C context);

	R visitNamedTyped(Tree tree, C context);

	R visitLabel(Tree tree, C context);

	R visitForNames(Tree tree, C context);

	R visitParams(Tree tree, C context);

	R visitParam(Tree tree, C context);

	R visitThrows(Tree tree, C context);

	R visitClassDefinition(Tree tree, C context);

	R visitTypeArgs(Tree tree, C context);

	R visitTypeParams(Tree tree, C context);

	R visitTypeParam(Tree tree, C context);

	R visitTypeBound(Tree tree, C context);

	R visitSuperBound(Tree tree, C context);

	R visitSuperType(Tree tree, C context);

	R visitSuperTypes(Tree tree, C context);

	R visitEnumDefinition(Tree tree, C context);

	R visitEnumConstant(Tree tree, C context);

	R visitEnumConstantValue(Tree tree, C context);

	R visitInterfaceDefinition(Tree tree, C context);

	R visitFunctionDefinition(Tree tree, C context);

	R visitStructDefinition(Tree tree, C context);

	R visitAliasDefinition(Tree tree, C context);

	R visitExpansionDefinition(Tree tree, C context);

	R visitVariantDefinition(Tree tree, C context);

	R visitVariantOption(Tree tree, C context);

	R visitVariantOptionTypes(Tree tree, C context);

	R visitSwitchCases(Tree tree, C context);

	R visitSwitchCase(Tree tree, C context);

	R visitModifiers(Tree tree, C context);

	R visitMembers(Tree tree, C context);

	R visitConstructorMember(Tree tree, C context);

	R visitFieldMember(Tree tree, C context);

	R visitTypeDeclaration(Tree tree, C context);

	R visitFieldAuto(Tree tree, C context);

	R visitAutoGet(Tree tree, C context);

	R visitAutoSet(Tree tree, C context);

	R visitCasterMember(Tree tree, C context);

	R visitMethodMember(Tree tree, C context);

	R visitSetterMember(Tree tree, C context);

	R visitGetterMember(Tree tree, C context);

	R visitImplementsMember(Tree tree, C context);

	R visitOperatorMember(Tree tree, C context);

	R visitDestructorMember(Tree tree, C context);

	R visitIteratorMember(Tree tree, C context);

	R visitStaticInitializerMember(Tree tree, C context);

	R visitReturnStatement(Tree tree, C context);

	R visitVarStatement(Tree tree, C context);

	R visitValStatement(Tree tree, C context);

	R visitIfStatement(Tree tree, C context);

	R visitElseStatement(Tree tree, C context);

	R visitForStatement(Tree tree, C context);

	R visitDoWhileStatement(Tree tree, C context);

	R visitWhileStatement(Tree tree, C context);

	R visitLockStatement(Tree tree, C context);

	R visitThrowStatement(Tree tree, C context);

	R visitTryStatement(Tree tree, C context);

	R visitContinueStatement(Tree tree, C context);

	R visitBreakStatement(Tree tree, C context);

	R visitSwitchStatement(Tree tree, C context);

	R visitAssignExpression(Tree tree, C context);

	R visitAddAssignExpression(Tree tree, C context);

	R visitSubAssignExpression(Tree tree, C context);

	R visitCatAssignExpression(Tree tree, C context);

	R visitMulAssignExpression(Tree tree, C context);

	R visitDivAssignExpression(Tree tree, C context);

	R visitModAssignExpression(Tree tree, C context);

	R visitOrAssignExpression(Tree tree, C context);

	R visitAndAssignExpression(Tree tree, C context);

	R visitXorAssignExpression(Tree tree, C context);

	R visitSHLAssignExpression(Tree tree, C context);

	R visitSHRAssignExpression(Tree tree, C context);

	R visitUSHRAssignExpression(Tree tree, C context);

	R visitConditionalExpression(Tree tree, C context);

	R visitOrOrExpression(Tree tree, C context);

	R visitCoalesceExpression(Tree tree, C context);

	R visitAndAndExpression(Tree tree, C context);

	R visitOrExpression(Tree tree, C context);

	R visitXorExpression(Tree tree, C context);

	R visitAndExpression(Tree tree, C context);

	R visitEqualExpression(Tree tree, C context);

	R visitSameExpression(Tree tree, C context);

	R visitNotEqualExpression(Tree tree, C context);

	R visitNotSameExpression(Tree tree, C context);

	R visitLessThanExpression(Tree tree, C context);

	R visitLessThanEqualToExpression(Tree tree, C context);

	R visitGreaterThanExpression(Tree tree, C context);

	R visitGreaterThanEqualToExpression(Tree tree, C context);

	R visitContainsExpression(Tree tree, C context);

	R visitIsExpression(Tree tree, C context);

	R visitNotInExpression(Tree tree, C context);

	R visitNotIsExpression(Tree tree, C context);

	R visitSHLExpression(Tree tree, C context);

	R visitSHRExpression(Tree tree, C context);

	R visitUSHRExpression(Tree tree, C context);

	R visitAddExpression(Tree tree, C context);

	R visitSubExpression(Tree tree, C context);

	R visitCatExpression(Tree tree, C context);

	R visitMulExpression(Tree tree, C context);

	R visitDivExpression(Tree tree, C context);

	R visitModExpression(Tree tree, C context);

	R visitNotExpression(Tree tree, C context);

	R visitNegExpression(Tree tree, C context);

	R visitInvertExpression(Tree tree, C context);

	R visitTryConvertExpression(Tree tree, C context);

	R visitTryRethrowExpression(Tree tree, C context);

	R visitMemberExpression(Tree tree, C context);

	R visitOuterExpression(Tree tree, C context);

	R visitRangeExpression(Tree tree, C context);

	R visitIndexExpression(Tree tree, C context);

	R visitIndexKey(Tree tree, C context);

	R visitCallExpression(Tree tree, C context);

	R visitCastExpression(Tree tree, C context);

	R visitIncrementExpression(Tree tree, C context);

	R visitDecrementExpression(Tree tree, C context);

	R visitFunctionExpression(Tree tree, C context);

	R visitIntExpression(Tree tree, C context);

	R visitPrefixedIntExpression(Tree tree, C context);

	R visitFloatExpression(Tree tree, C context);

	R visitStringExpression(Tree tree, C context);

	R visitVariableExpression(Tree tree, C context);

	R visitLocalVariableExpression(Tree tree, C context);

	R visitThisExpression(Tree tree, C context);

	R visitSuperExpression(Tree tree, C context);

	R visitDollarExpression(Tree tree, C context);

	R visitArrayExpression(Tree tree, C context);

	R visitMapExpression(Tree tree, C context);

	R visitMapKey(Tree tree, C context);

	R visitMapValue(Tree tree, C context);

	R visitBoolExpression(Tree tree, C context);

	R visitNullExpression(Tree tree, C context);

	R visitBracketExpression(Tree tree, C context);

	R visitNewExpression(Tree tree, C context);

	R visitThrowExpression(Tree tree, C context);

	R visitPanicExpression(Tree tree, C context);

	R visitMatchExpression(Tree tree, C context);

	R visitMatchKey(Tree tree, C context);

	R visitBEP(Tree tree, C context);

	R visitTypeExpression(Tree tree, C context);

	R visitCallArguments(Tree tree, C context);

	R visitCallArgument(Tree tree, C context);

	R visitComment(Tree tree, C context);

	R visitMultilineComment(Tree tree, C context);

	R visitScriptComment(Tree tree, C context);

	R visitWhitespace(Tree tree, C context);

	R visitVariants(Tree tree, C context);
}
