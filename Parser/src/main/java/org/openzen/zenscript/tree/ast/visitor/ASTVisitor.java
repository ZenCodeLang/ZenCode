package org.openzen.zenscript.tree.ast.visitor;

import org.openzen.zenscript.tree.ast.ErrorNode;
import org.openzen.zenscript.tree.ast.FileNode;
import org.openzen.zenscript.tree.ast.definition.*;
import org.openzen.zenscript.tree.ast.definition.member.*;
import org.openzen.zenscript.tree.ast.definition.variant.VariantOptionNode;
import org.openzen.zenscript.tree.ast.definition.variant.VariantOptionTypesNode;
import org.openzen.zenscript.tree.ast.definition.variant.VariantOptionsNode;
import org.openzen.zenscript.tree.ast.expression.*;
import org.openzen.zenscript.tree.ast.function.*;
import org.openzen.zenscript.tree.ast.generic.GenericBoundNode;
import org.openzen.zenscript.tree.ast.generic.TypeArgumentsNode;
import org.openzen.zenscript.tree.ast.generic.TypeParameterNode;
import org.openzen.zenscript.tree.ast.generic.TypeParametersNode;
import org.openzen.zenscript.tree.ast.misc.*;
import org.openzen.zenscript.tree.ast.statement.*;
import org.openzen.zenscript.tree.ast.type.*;

public interface ASTVisitor<C, R> {

	R visitFile(FileNode node, C context);

	R visitError(ErrorNode node, C context);

	R visitAnnotation(AnnotationNode node, C context);

	R visitAlias(AliasNode node, C context);

	R visitClass(ClassNode node, C context);

	R visitEnum(EnumNode node, C context);

	R visitEnumConstant(EnumNode.EnumConstantNode node, C context);

	R visitExpansion(ExpansionNode node, C context);

	R visitFunction(FunctionNode node, C context);

	R visitInterface(InterfaceNode node, C context);

	R visitStruct(StructNode node, C context);

	R visitVariant(VariantNode node, C context);

	R visitVariantOption(VariantOptionNode node, C context);

	R visitVariantOptionTypes(VariantOptionTypesNode node, C context);

	R visitVariantOptionsNode(VariantOptionsNode node, C context);

	R visitBlock(BlockStatementNode node, C context);

	R visitBreak(BreakNode node, C context);

	R visitContinue(ContinueNode node, C context);

	R visitDoWhile(DoWhileNode node, C context);

	R visitElse(ElseNode node, C context);

	R visitFor(ForNode node, C context);

	R visitIf(IfNode node, C context);

	R visitLock(LockNode node, C context);

	R visitReturn(ReturnNode node, C context);

	R visitSwitch(SwitchNode node, C context);

	R visitSwitchCase(SwitchNode.SwitchCaseNode node, C context);

	R visitSwitchCases(SwitchNode.SwitchCasesNode node, C context);

	R visitThrow(ThrowStatementNode node, C context);

	R visitTryCatch(TryCatchNode node, C context);

	R visitTryCatchClause(TryCatchNode.CatchClauseNode node, C context);

	R visitVar(VarStatementNode node, C context);

	R visitWhile(WhileNode node, C context);

	R visitAddAssign(AddAssignNode node, C context);

	R visitAdd(AddNode node, C context);

	R visitAndAnd(AndAndNode node, C context);

	R visitAndAssign(AndAssignNode node, C context);

	R visitAnd(AndNode node, C context);

	R visitArray(ArrayNode node, C context);

	R visitAssign(AssignNode node, C context);

	R visitBEP(BEPNode node, C context);

	R visitBool(BoolNode node, C context);

	R visitBracket(BracketNode node, C context);

	R visitCall(CallNode node, C context);

	R visitCast(CastNode node, C context);

	R visitCatAssign(CatAssignNode node, C context);

	R visitCat(CatNode node, C context);

	R visitCoalesce(CoalesceNode node, C context);

	R visitConditional(ConditionalNode node, C context);

	R visitContains(ContainsNode node, C context);

	R visitDecrement(DecrementNode node, C context);

	R visitDivAssign(DivAssignNode node, C context);

	R visitDiv(DivNode node, C context);

	R visitDollar(DollarNode node, C context);

	R visitEqual(EqualNode node, C context);

	R visitFloat(FloatNode node, C context);

	R visitFunctionExpression(FunctionExpressionNode node, C context);

	R visitGreater(GreaterNode node, C context);

	R visitGreaterThanEqualTo(GreaterThanEqualToNode node, C context);

	R visitIncrement(IncrementNode node, C context);

	R visitIndex(IndexNode node, C context);

	R visitInt(IntNode node, C context);

	R visitInvert(InvertNode node, C context);

	R visitIs(IsNode node, C context);

	R visitLessThanEqualTo(LessThanEqualToNode node, C context);

	R visitLessThan(LessThanNode node, C context);

	R visitLocalVariable(LocalVariableNode node, C context);

	R visitMap(MapNode node, C context);

	R visitMatch(MatchNode node, C context);

	R visitMatchCase(MatchNode.CaseNode node, C context);

	R visitMember(MemberNode node, C context);

	R visitModAssign(ModAssignNode node, C context);

	R visitMod(ModNode node, C context);

	R visitMulAssign(MulAssignNode node, C context);

	R visitMul(MulNode node, C context);

	R visitNeg(NegNode node, C context);

	R visitNew(NewNode node, C context);

	R visitNotContains(NotContainsNode node, C context);

	R visitNotEqual(NotEqualNode node, C context);

	R visitNotIs(NotIsNode node, C context);

	R visitNot(NotNode node, C context);

	R visitNotSame(NotSameNode node, C context);

	R visitNull(NullNode node, C context);

	R visitOrAssign(OrAssignNode node, C context);

	R visitOr(OrNode node, C context);

	R visitOrOr(OrOrNode node, C context);

	R visitOuter(OuterNode node, C context);

	R visitPanic(PanicNode node, C context);

	R visitRange(RangeNode node, C context);

	R visitSame(SameNode node, C context);

	R visitSHLAssign(SHLAssignNode node, C context);

	R visitSHL(SHLNode node, C context);

	R visitSHRAssign(SHRAssignNode node, C context);

	R visitSHR(SHRNode node, C context);

	R visitStringExpression(StringExpressionNode node, C context);

	R visitSubAssign(SubAssignNode node, C context);

	R visitSub(SubNode node, C context);

	R visitSuper(SuperNode node, C context);

	R visitThis(ThisNode node, C context);

	R visitThrow(ThrowNode node, C context);

	R visitTryConvert(TryConvertNode node, C context);

	R visitTryRethrow(TryRethrowNode node, C context);

	R visitTypeExpression(TypeExpressionNode node, C context);

	R visitUSHRAssign(USHRAssignNode node, C context);

	R visitUSHR(USHRNode node, C context);

	R visitVariable(VariableNode node, C context);

	R visitXorAssign(XorAssignNode node, C context);

	R visitXor(XorNode node, C context);

	R visitArrayType(ArrayTypeNode node, C context);

	R visitBasicType(BasicTypeNode node, C context);

	R visitGenericMapType(GenericMapTypeNode node, C context);

	R visitMapType(MapTypeNode node, C context);

	R visitNamedType(NamedTypeNode node, C context);

	R visitFunctionType(FunctionTypeNode node, C context);

	R visitOptionalType(OptionalTypeNode node, C context);

	R visitRangeType(RangeTypeNode node, C context);

	R visitName(NameNode node, C context);

	R visitCallArguments(CallArgumentsNode node, C context);

	R visitFunctionHeader(FunctionHeaderNode node, C context);

	R visitLambdaHeader(LambdaHeaderNode node, C context);

	R visitParameter(ParameterNode node, C context);

	R visitParameters(ParametersNode node, C context);

	R visitGenericBound(GenericBoundNode node, C context);

	R visitTypeArguments(TypeArgumentsNode node, C context);

	R visitTypeParameter(TypeParameterNode node, C context);

	R visitTypeParameters(TypeParametersNode node, C context);

	R visitForKeys(ForKeysNode node, C context);

	R visitLabel(LabelNode node, C context);

	R visitModifier(ModifierNode node, C context);

	R visitModifiers(ModifiersNode node, C context);

	R visitSuperType(SuperTypeNode node, C context);

	R visitSuperTypes(SuperTypesNode node, C context);

	R visitImport(ImportNode node, C context);

	R visitAutoGetter(AutoGetterNode node, C context);

	R visitAutoSetter(AutoSetterNode node, C context);

	R visitAutos(AutosNode node, C context);

	R visitCaster(CasterNode node, C context);

	R visitConstructor(ConstructorNode node, C context);

	R visitDestructor(DestructorNode node, C context);

	R visitField(FieldNode node, C context);

	R visitGetter(GetterNode node, C context);

	R visitImplements(ImplementsNode node, C context);

	R visitInnerDefinition(InnerDefinitionNode node, C context);

	R visitIterator(IteratorNode node, C context);

	R visitMembers(MembersNode node, C context);

	R visitMethod(MethodNode node, C context);

	R visitOperator(OperatorNode node, C context);

	R visitSetter(SetterNode node, C context);

	R visitStaticInitializer(StaticInitializerNode node, C context);

	R visitEmptyFunctionBody(EmptyFunctionBodyNode node, C context);
}
