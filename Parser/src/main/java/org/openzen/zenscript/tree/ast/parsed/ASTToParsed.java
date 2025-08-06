package org.openzen.zenscript.tree.ast.parsed;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zencode.shared.StringExpansion;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilableLambdaHeader;
import org.openzen.zenscript.codemodel.compilation.CompileContext;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.expression.ModificationExpression;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.ParsedFile;
import org.openzen.zenscript.parser.ParsedImport;
import org.openzen.zenscript.parser.definitions.*;
import org.openzen.zenscript.parser.expression.*;
import org.openzen.zenscript.parser.member.*;
import org.openzen.zenscript.parser.statements.*;
import org.openzen.zenscript.parser.type.*;
import org.openzen.zenscript.tree.ast.ASTNode;
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
import org.openzen.zenscript.tree.ast.visitor.ASTVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ASTToParsed implements ASTVisitor<ASTToParsed.ParsedContext, Parsed<?>> {
	public static final ASTToParsed INSTANCE = new ASTToParsed();

	@Override
	public Parsed<ParsedFile> visitFile(FileNode node, ParsedContext context) {
		List<ParsedImport> imports = new ArrayList<>();
		List<ParsedDefinition> definitions = new ArrayList<>();
		List<ParsedStatement> statements = new ArrayList<>();

		for (ASTNode child : node.children()) {
			Parsed<?> parsed = child.accept(this, context);
			if (parsed == null) {
				continue;
			}
			if (child instanceof DefinitionNode) {
				definitions.add(parsed.cast());
			} else if (child instanceof ImportNode) {
				imports.add(parsed.cast());
			} else if (child instanceof StatementNode) {
				statements.add(parsed.cast());
			} else if (child instanceof ExpressionNode) {
				statements.add(new ParsedStatementExpression(child.position(), ParsedAnnotation.NONE, null, parsed.cast()));
			}
		}

		ParsedFile parsedFile = new ParsedFile(context.pkg, context.file, imports, definitions, statements);
		return new Parsed<>(parsedFile);
	}

	private ParsedStatement visitStatement(ASTNode node, ParsedContext context) {
		if (node == null) {
			return null;
		}
		if (node instanceof StatementNode) {
			return node.accept(this, context).cast();
		} else if (node instanceof ExpressionNode) {
			return new ParsedStatementExpression(node.position(), ParsedAnnotation.NONE, null, node.accept(this, context).cast());
		} else {
			throw new IllegalArgumentException("Node is not a statement or expression: " + node);
		}

	}

	@Override
	public Parsed<?> visitError(ErrorNode node, ParsedContext context) {
		throw new UnsupportedOperationException("ErrorNode should not be visited");
	}

	@Override
	public Parsed<?> visitAnnotation(AnnotationNode node, ParsedContext context) {
		return null;
	}

	@Override
	public Parsed<ParsedAlias> visitAlias(AliasNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE;
		String name = node.name().accept(this, context).cast();
		List<ParsedTypeParameter> parameters = visitTypeParameters(node.typeParameters(), context).cast();
		IParsedType type = this.visitType(node.type(), context).cast();
		return new Parsed<>(new ParsedAlias(position, modifiers, annotations, name, parameters, type));
	}

	@Override
	public Parsed<ParsedClass> visitClass(ClassNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		List<ParsedTypeParameter> typeParameters = visitTypeParameters(node.typeParameters(), context).cast();
		IParsedType superType = node.superType() == null ? null : this.visitType(node.superType().type(), context).cast();
		ParsedClass parsed = new ParsedClass(position, modifiers, annotations, name, typeParameters, superType);

		List<ParsedDefinitionMember> members = this.visitMembers(node.members(), context).cast();
		for (ParsedDefinitionMember member : members) {
			parsed.addMember(member);
		}
		return new Parsed<>(parsed);
	}

	@Override
	public Parsed<ParsedEnum> visitEnum(EnumNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		IParsedType asType = node.asType() == null ? null : this.visitType(node.asType(), context).cast();
		ParsedEnum parsed = new ParsedEnum(position, modifiers, annotations, name, asType);
		int ordinal = 0;
		for (EnumNode.EnumConstantNode constant : node.constants()) {
			CodePosition constantPos = node.position();
			String constantName = node.name().accept(this, context).cast();
			List<CompilableExpression> arguments = constant.arguments().arguments().stream().<CompilableExpression>map(exp -> exp.accept(this, context).cast()).collect(Collectors.toList());
			CompilableExpression value = constant.value() == null ? null : constant.value().accept(this, context).cast();
			parsed.addEnumValue(new ParsedEnumConstant(constantPos, constantName, ordinal++, arguments, value));
		}
		List<ParsedDefinitionMember> members = this.visitMembers(node.members(), context).cast();
		for (ParsedDefinitionMember member : members) {
			parsed.addMember(member);
		}
		return new Parsed<>(parsed);
	}

	@Override
	public Parsed<ParsedEnumConstant> visitEnumConstant(EnumNode.EnumConstantNode node, ParsedContext context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Parsed<ParsedExpansion> visitExpansion(ExpansionNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		List<ParsedTypeParameter> typeParameters = visitTypeParameters(node.typeParameters(), context).cast();
		IParsedType target = this.visitType(node.type(), context).cast();
		ParsedExpansion parsed = new ParsedExpansion(position, modifiers, annotations, typeParameters, target);

		List<ParsedDefinitionMember> members = this.visitMembers(node.members(), context).cast();
		for (ParsedDefinitionMember member : members) {
			parsed.addMember(member);
		}
		return new Parsed<>(parsed);
	}

	@Override
	public Parsed<ParsedFunction> visitFunction(FunctionNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		ParsedFunctionHeader header = node.header().accept(this, context).cast();
		ParsedFunctionBody parsedFunctionBody = this.visitFunctionBody(node.body(), context);
		return new Parsed<>(new ParsedFunction(position, modifiers, annotations, name, header, parsedFunctionBody));
	}

	@Override
	public Parsed<ParsedInterface> visitInterface(InterfaceNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		List<ParsedTypeParameter> typeParameters = visitTypeParameters(node.typeParameters(), context).cast();
		List<IParsedType> superType = node.superTypes() == null ? Collections.emptyList() : node.superTypes().accept(this, context).cast();
		ParsedInterface parsed = new ParsedInterface(position, modifiers, annotations, name, typeParameters, superType);
		List<ParsedDefinitionMember> members = this.visitMembers(node.members(), context).cast();
		for (ParsedDefinitionMember member : members) {
			parsed.addMember(member);
		}
		return new Parsed<>(parsed);
	}

	@Override
	public Parsed<ParsedStruct> visitStruct(StructNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		List<ParsedTypeParameter> typeParameters = visitTypeParameters(node.typeParameters(), context).cast();
		ParsedStruct parsed = new ParsedStruct(position, modifiers, annotations, name, typeParameters);

		List<ParsedDefinitionMember> members = this.visitMembers(node.members(), context).cast();
		for (ParsedDefinitionMember member : members) {
			parsed.addMember(member);
		}
		return new Parsed<>(parsed);
	}

	@Override
	public Parsed<ParsedVariant> visitVariant(VariantNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		List<ParsedTypeParameter> typeParameters = visitTypeParameters(node.typeParameters(), context).cast();
		ParsedVariant parsed = new ParsedVariant(position, modifiers, annotations, name, typeParameters);
		int ordinal = 0;
		for (VariantOptionNode option : node.variants().variants()) {
			CodePosition optionPos = node.position();
			String optionName = node.name().accept(this, context).cast();
			List<IParsedType> types = option.types().accept(this, context).cast();
			parsed.addVariant(new ParsedVariantOption(optionPos, optionName, ordinal++, types));
		}
		List<ParsedDefinitionMember> members = this.visitMembers(node.members(), context).cast();
		for (ParsedDefinitionMember member : members) {
			parsed.addMember(member);
		}
		return new Parsed<>(parsed);
	}

	@Override
	public Parsed<?> visitVariantOption(VariantOptionNode node, ParsedContext context) {
		throw new UnsupportedOperationException("VariantOptionNode should not be visited");
	}

	@Override
	public Parsed<List<IParsedType>> visitVariantOptionTypes(VariantOptionTypesNode node, ParsedContext context) {
		return new Parsed<>(node.types().stream().<IParsedType>map(typeNode -> this.visitType(typeNode, context).cast()).collect(Collectors.toList()));
	}

	@Override
	public Parsed<?> visitVariantOptionsNode(VariantOptionsNode node, ParsedContext context) {
		throw new UnsupportedOperationException("VariantOptionsNode should not be visited");
	}

	@Override
	public Parsed<ParsedStatementBlock> visitBlock(BlockStatementNode node, ParsedContext context) {
		CodePosition position = node.position();

		List<ParsedStatement> statements = node.nodes().stream()
				.map(statementNode -> this.visitStatement(statementNode, context))
				.collect(Collectors.toList());
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		return new Parsed<>(new ParsedStatementBlock(position, annotations, null, null, statements));
	}

	@Override
	public Parsed<ParsedStatementBreak> visitBreak(BreakNode node, ParsedContext context) {
		CodePosition position = node.position();
		String name = node.name() == null ? null : node.name().accept(this, context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		return new Parsed<>(new ParsedStatementBreak(position, annotations, null, name));
	}

	@Override
	public Parsed<ParsedStatementContinue> visitContinue(ContinueNode node, ParsedContext context) {
		CodePosition position = node.position();
		String name = node.name() == null ? null : node.name().accept(this, context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		return new Parsed<>(new ParsedStatementContinue(position, annotations, null, name));
	}

	@Override
	public Parsed<ParsedStatementDoWhile> visitDoWhile(DoWhileNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String label = node.label() == null ? null : node.label().label().identifier().getContent();
		ParsedStatement content = this.visitStatement(node.body(), context);
		CompilableExpression condition = node.condition() == null ? null : node.condition().accept(this, context).cast();
		return new Parsed<>(new ParsedStatementDoWhile(position, annotations, null, label, content, condition));
	}

	@Override
	public Parsed<ParsedStatement> visitElse(ElseNode node, ParsedContext context) {
		return new Parsed<>(this.visitStatement(node.body(), context));
	}

	@Override
	public Parsed<ParsedStatementForeach> visitFor(ForNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE;
		String[] forNames = node.keys().accept(this, context).cast();
		CompilableExpression list = node.value().accept(this, context).cast();
		ParsedStatement body = this.visitStatement(node.body(), context);
		return new Parsed<>(new ParsedStatementForeach(position, annotations, null, forNames, list, body));
	}

	@Override
	public Parsed<ParsedStatementIf> visitIf(IfNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE;
		CompilableExpression condition = node.condition() == null ? null : node.condition().accept(this, context).cast();
		ParsedStatement onThen = this.visitStatement(node.body(), context);
		ParsedStatement onElse = node.elseNode() == null ? null : node.elseNode().accept(this, context).cast();
		return new Parsed<>(new ParsedStatementIf(position, annotations, null, condition, onThen, onElse));
	}

	@Override
	public Parsed<ParsedStatementLock> visitLock(LockNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		CompilableExpression object = node.object().accept(this, context).cast();
		ParsedStatement content = this.visitStatement(node.body(), context);
		return new Parsed<>(new ParsedStatementLock(position, annotations, null, object, content));
	}

	@Override
	public Parsed<ParsedStatementReturn> visitReturn(ReturnNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.expression() == null ? null : node.expression().accept(this, context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		return new Parsed<>(new ParsedStatementReturn(position, annotations, null, expression));
	}

	@Override
	public Parsed<ParsedStatementSwitch> visitSwitch(SwitchNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.label() == null ? null : node.label().accept(this, context).cast();
		CompilableExpression value = node.expression().accept(this, context).cast();
		List<ParsedSwitchCase> cases = node.cases().accept(this, context).cast();
		return new Parsed<>(new ParsedStatementSwitch(position, annotations, null, name, value, cases));
	}

	@Override
	public Parsed<ParsedSwitchCase> visitSwitchCase(SwitchNode.SwitchCaseNode node, ParsedContext context) {
		CompilableExpression value = node.expression().accept(this, context).cast();
		List<ParsedStatement> statements = node.statements().stream()
				.map(statementNode -> this.visitStatement(statementNode, context))
				.collect(Collectors.toList());
		ParsedSwitchCase parsedSwitchCase = new ParsedSwitchCase(value);
		parsedSwitchCase.statements.addAll(statements);
		return new Parsed<>(parsedSwitchCase);
	}

	@Override
	public Parsed<List<ParsedSwitchCase>> visitSwitchCases(SwitchNode.SwitchCasesNode node, ParsedContext context) {
		return new Parsed<>(node.cases().stream().<ParsedSwitchCase>map(caseNode -> caseNode.accept(this, context).cast()).collect(Collectors.toList()));
	}

	@Override
	public Parsed<ParsedStatementThrow> visitThrow(ThrowStatementNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.value().accept(this, context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		return new Parsed<>(new ParsedStatementThrow(position, annotations, null, expression));
	}

	@Override
	public Parsed<?> visitTryCatch(TryCatchNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String resourceName = node.resourceName() == null ? null : node.resourceName().accept(this, context).cast();
		CompilableExpression resource = node.resourceInitializer() == null ? null : node.resourceInitializer().accept(this, context).cast();
		ParsedStatement statement = this.visitStatement(node.body(), context);
		List<ParsedCatchClause> catchClauses = node.catchClauses().stream()
				.<ParsedCatchClause>map(catchNode -> catchNode.accept(this, context).cast())
				.collect(Collectors.toList());
		ParsedStatement finallyClause = this.visitStatement(node.finallyClause(), context);
		return new Parsed<>(new ParsedStatementTryCatch(position, annotations, null, resourceName, resource, statement, catchClauses, finallyClause));
	}

	@Override
	public Parsed<ParsedCatchClause> visitTryCatchClause(TryCatchNode.CatchClauseNode node, ParsedContext context) {
		CodePosition position = node.position();
		String exceptionName = node.exceptionName().accept(this, context).cast();
		ParsedStatement body = this.visitStatement(node.body(), context);
		return new Parsed<>(new ParsedCatchClause(position, exceptionName, body));
	}

	@Override
	public Parsed<ParsedStatementVar> visitVar(VarStatementNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE;
		WhitespaceInfo whitespace = null;
		String name = node.name().accept(this, context).cast();
		IParsedType type = node.type() == null ? null : this.visitType(node.type(), context).thing();
		CompilableExpression initializer = node.initializer() == null ? null : node.initializer().accept(this, context).cast();
		boolean isFinal = node.isFinal();
		return new Parsed<>(new ParsedStatementVar(position, annotations, whitespace, name, type, initializer, isFinal));
	}

	@Override
	public Parsed<ParsedStatementWhile> visitWhile(WhileNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String label = node.label() == null ? null : node.label().label().identifier().getContent();
		CompilableExpression condition = node.condition() == null ? null : node.condition().accept(this, context).cast();
		ParsedStatement content = this.visitStatement(node.body(), context);

		return new Parsed<>(new ParsedStatementWhile(position, annotations, null, label, condition, content));
	}

	@Override
	public Parsed<ParsedExpressionBinary> visitAddAssign(AddAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.ADDASSIGN, context);
	}

	@Override
	public Parsed<ParsedExpressionBinary> visitAdd(AddNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.ADD, context);
	}

	@Override
	public Parsed<ParsedExpressionAndAnd> visitAndAnd(AndAndNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression left = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression right = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionAndAnd(position, left, right));
	}

	@Override
	public Parsed<ParsedExpressionBinary> visitAndAssign(AndAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.ANDASSIGN, context);
	}

	@Override
	public Parsed<ParsedExpressionBinary> visitAnd(AndNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.AND, context);
	}

	@Override
	public Parsed<ParsedExpressionArray> visitArray(ArrayNode node, ParsedContext context) {
		CodePosition position = node.position();
		List<CompilableExpression> contents = node.contents().stream()
				.<CompilableExpression>map(expressionNode -> expressionNode.accept(this, context).cast())
				.collect(Collectors.toList());
		return new Parsed<>(new ParsedExpressionArray(position, contents));
	}

	@Override
	public Parsed<ParsedExpressionAssign> visitAssign(AssignNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression left = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression right = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionAssign(position, left, right));
	}

	private Parsed<ParsedExpressionBinary> visitBinary(CodePosition position, ExpressionNode left, ExpressionNode right, OperatorType type, ParsedContext context) {
		CompilableExpression leftExpr = left == null ? null : left.accept(this, context).cast();
		CompilableExpression rightExpr = right == null ? null : right.accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionBinary(position, leftExpr, rightExpr, type));
	}

	@Override
	public Parsed<CompilableExpression> visitBEP(BEPNode node, ParsedContext context) {
		return null;
	}

	@Override
	public Parsed<ParsedExpressionBool> visitBool(BoolNode node, ParsedContext context) {
		return new Parsed<>(new ParsedExpressionBool(
				node.position(),
				node.value()));
	}

	@Override
	public Parsed<ParsedExpressionBracket> visitBracket(BracketNode node, ParsedContext context) {
		CodePosition position = node.position();
		List<CompilableExpression> expressions = node.expressions().stream()
				.<CompilableExpression>map(expressionNode -> expressionNode.accept(this, context).cast())
				.collect(Collectors.toList());
		return new Parsed<>(new ParsedExpressionBracket(position, expressions));
	}

	@Override
	public Parsed<ParsedExpressionCall> visitCall(CallNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression receiver = node.receiver().accept(this, context).cast();
		ParsedCallArguments arguments = node.arguments().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCall(position, receiver, arguments));
	}

	@Override
	public Parsed<ParsedExpressionCast> visitCast(CastNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.value().accept(this, context).cast();
		IParsedType type = this.visitType(node.type(), context).cast();
		boolean optional = node.optional();
		return new Parsed<>(new ParsedExpressionCast(position, expression, type, optional));
	}

	@Override
	public Parsed<?> visitCatAssign(CatAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.CATASSIGN, context);
	}

	@Override
	public Parsed<?> visitCat(CatNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.CAT, context);
	}

	@Override
	public Parsed<ParsedExpressionCoalesce> visitCoalesce(CoalesceNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCoalesce(position, leftExpr, rightExpr));
	}

	@Override
	public Parsed<ParsedExpressionConditional> visitConditional(ConditionalNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression condition = node.condition().accept(this, context).cast();
		CompilableExpression onIf = node.ifElse().accept(this, context).cast();
		CompilableExpression onElse = node.ifElse().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionConditional(position, condition, onIf, onElse));
	}

	@Override
	public Parsed<?> visitContains(ContainsNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression left = node.left().accept(this, context).cast();
		CompilableExpression right = node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionBinary(position, right, left, OperatorType.CONTAINS));

	}

	@Override
	public Parsed<?> visitDecrement(DecrementNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedModificationExpression(position, expression, node.pre() ? ModificationExpression.Modification.PreDecrement : ModificationExpression.Modification.PostDecrement));
	}

	@Override
	public Parsed<?> visitDivAssign(DivAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.DIVASSIGN, context);
	}

	@Override
	public Parsed<?> visitDiv(DivNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.DIV, context);
	}

	@Override
	public Parsed<?> visitDollar(DollarNode node, ParsedContext context) {
		return new Parsed<>(new ParsedDollarExpression(node.position()));
	}

	@Override
	public Parsed<?> visitEqual(EqualNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCompare(position, leftExpr, rightExpr, CompareType.EQ));
	}

	@Override
	public Parsed<ParsedExpressionFloat> visitFloat(FloatNode node, ParsedContext context) {
		return new Parsed<>(new ParsedExpressionFloat(node.position(), node.value(), node.suffix()));
	}

	@Override
	public Parsed<ParsedExpressionFunction> visitFunctionExpression(FunctionExpressionNode node, ParsedContext context) {
		//TODO the whole CompilableLambdaHeader class seems like a mess right now
		return null;
	}

	@Override
	public Parsed<?> visitGreater(GreaterNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCompare(position, leftExpr, rightExpr, CompareType.GT));
	}

	@Override
	public Parsed<?> visitGreaterThanEqualTo(GreaterThanEqualToNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCompare(position, leftExpr, rightExpr, CompareType.GE));
	}

	@Override
	public Parsed<?> visitIncrement(IncrementNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedModificationExpression(position, expression, node.pre() ? ModificationExpression.Modification.PreIncrement : ModificationExpression.Modification.PostIncrement));
	}

	@Override
	public Parsed<?> visitIndex(IndexNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression value = node.value().accept(this, context).cast();
		List<CompilableExpression> collect = node.indices().stream().<CompilableExpression>map(index -> index.accept(this, context).cast()).collect(Collectors.toList());
		return new Parsed<>(new ParsedExpressionIndex(position, value, collect));
	}

	@Override
	public Parsed<ParsedExpressionInt> visitInt(IntNode node, ParsedContext context) {
		return new Parsed<>(new ParsedExpressionInt(
				node.position(),
				node.negative(), node.value(), node.suffix()));
	}

	@Override
	public Parsed<ParsedExpressionUnary> visitInvert(InvertNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expr = node.expression() == null ? null : node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionUnary(position, expr, OperatorType.INVERT));
	}

	@Override
	public Parsed<ParsedExpressionIs> visitIs(IsNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expr = node.expression().accept(this, context).cast();
		IParsedType type = node.type().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionIs(position, expr, type));
	}

	@Override
	public Parsed<?> visitLessThanEqualTo(LessThanEqualToNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCompare(position, leftExpr, rightExpr, CompareType.LE));
	}

	@Override
	public Parsed<?> visitLessThan(LessThanNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCompare(position, leftExpr, rightExpr, CompareType.LT));
	}

	@Override
	public Parsed<ParsedLocalVariableExpression> visitLocalVariable(LocalVariableNode node, ParsedContext context) {
		CodePosition position = node.position();
		String name = node.name().getContent();
		return new Parsed<>(new ParsedLocalVariableExpression(position, name));
	}

	@Override
	public Parsed<ParsedExpressionMap> visitMap(MapNode node, ParsedContext context) {
		CodePosition position = node.position();
		List<CompilableExpression> keys = node.keys().stream()
				.<CompilableExpression>map(keyNode -> keyNode == null ? null : keyNode.accept(this, context).cast())
				.collect(Collectors.toList());
		List<CompilableExpression> values = node.keys().stream()
				.<CompilableExpression>map(keyNode -> keyNode == null ? null : keyNode.accept(this, context).cast())
				.collect(Collectors.toList());
		return new Parsed<>(new ParsedExpressionMap(position, keys, values));
	}

	@Override
	public Parsed<ParsedMatchExpression> visitMatch(MatchNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression value = node.value().accept(this, context).cast();
		List<ParsedMatchExpression.Case> cases = node.cases().stream().<ParsedMatchExpression.Case>map(caseNode -> caseNode.accept(this, context).cast()).collect(Collectors.toList());
		return new Parsed<>(new ParsedMatchExpression(position, value, cases));
	}

	@Override
	public Parsed<ParsedMatchExpression.Case> visitMatchCase(MatchNode.CaseNode node, ParsedContext context) {
		CompilableExpression name = node.name().accept(this, context).cast();
		CompilableExpression value = node.name().accept(this, context).cast();
		return new Parsed<>(new ParsedMatchExpression.Case(name, value));
	}

	@Override
	public Parsed<ParsedExpression> visitMember(MemberNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedExpression value = node.expression().accept(this, context).cast();
		NameNode member = node.member();

		if (member == null) {
			return new Parsed<>(value);
		}
		//TODO the member contents may be quoted, we should unquote them
		return new Parsed<>(new ParsedExpressionMember(position, value, member.identifier().getContent(), null));
	}

	@Override
	public Parsed<?> visitModAssign(ModAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.MODASSIGN, context);
	}

	@Override
	public Parsed<?> visitMod(ModNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.MOD, context);
	}

	@Override
	public Parsed<?> visitMulAssign(MulAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.MULASSIGN, context);
	}

	@Override
	public Parsed<?> visitMul(MulNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.MUL, context);
	}

	@Override
	public Parsed<ParsedExpressionUnary> visitNeg(NegNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expr = node.expression() == null ? null : node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionUnary(position, expr, OperatorType.NEG));
	}

	@Override
	public Parsed<ParsedNewExpression> visitNew(NewNode node, ParsedContext context) {
		CodePosition position = node.position();
		Parsed<IParsedType> type = this.visitType(node.type(), context);
		ParsedCallArguments arguments = node.arguments().accept(this, context).cast();
		return new Parsed<>(new ParsedNewExpression(position, type.cast(), arguments));
	}

	@Override
	public Parsed<?> visitNotContains(NotContainsNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression left = node.left().accept(this, context).cast();
		CompilableExpression right = node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionUnary(position, new ParsedExpressionBinary(position, right, left, OperatorType.CONTAINS), OperatorType.NOT));
	}

	@Override
	public Parsed<?> visitNotEqual(NotEqualNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression leftExpr = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression rightExpr = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionCompare(position, leftExpr, rightExpr, CompareType.NE));
	}

	@Override
	public Parsed<?> visitNotIs(NotIsNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expr = node.expression().accept(this, context).cast();
		IParsedType type = this.visitType(node.type(), context).cast();
		return new Parsed<>(new ParsedExpressionUnary(position, new ParsedExpressionIs(position, expr, type), OperatorType.NOT));
	}

	@Override
	public Parsed<ParsedExpressionUnary> visitNot(NotNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expr = node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionUnary(position, expr, OperatorType.NOT));
	}

	@Override
	public Parsed<?> visitNotSame(NotSameNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.NOTSAME, context);
	}

	@Override
	public Parsed<ParsedExpressionNull> visitNull(NullNode node, ParsedContext context) {
		return new Parsed<>(new ParsedExpressionNull(node.position()));
	}

	@Override
	public Parsed<?> visitOrAssign(OrAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.ORASSIGN, context);
	}

	@Override
	public Parsed<?> visitOr(OrNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.OR, context);
	}

	@Override
	public Parsed<ParsedExpressionOrOr> visitOrOr(OrOrNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression left = node.left() == null ? null : node.left().accept(this, context).cast();
		CompilableExpression right = node.right() == null ? null : node.right().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionOrOr(position, left, right));
	}

	@Override
	public Parsed<ParsedExpressionOuter> visitOuter(OuterNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionOuter(position, expression));
	}

	@Override
	public Parsed<ParsedPanicExpression> visitPanic(PanicNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.value().accept(this, context).cast();
		return new Parsed<>(new ParsedPanicExpression(position, expression));
	}

	@Override
	public Parsed<?> visitRange(RangeNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression start = node.from() == null ? null : node.from().accept(this, context).cast();
		CompilableExpression end = node.to() == null ? null : node.to().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionRange(position, start, end));
	}

	@Override
	public Parsed<?> visitSame(SameNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SAME, context);
	}

	@Override
	public Parsed<?> visitSHLAssign(SHLAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SHLASSIGN, context);
	}

	@Override
	public Parsed<?> visitSHL(SHLNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SHL, context);
	}

	@Override
	public Parsed<?> visitSHRAssign(SHRAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SHRASSIGN, context);
	}

	@Override
	public Parsed<?> visitSHR(SHRNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SHR, context);
	}

	@Override
	public Parsed<ParsedExpressionString> visitStringExpression(StringExpressionNode node, ParsedContext context) {
		String quoted = node.content().getContent();
		return new Parsed<>(new ParsedExpressionString(
				node.position(),
				StringExpansion.unescape(quoted).orElse(error -> "INVALID_STRING"),
				quoted.charAt(0) == '\''));
	}

	@Override
	public Parsed<?> visitSubAssign(SubAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SUBASSIGN, context);
	}

	@Override
	public Parsed<?> visitSub(SubNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.SUB, context);
	}

	@Override
	public Parsed<ParsedExpressionSuper> visitSuper(SuperNode node, ParsedContext context) {
		return new Parsed<>(new ParsedExpressionSuper(node.position()));
	}

	@Override
	public Parsed<ParsedExpressionThis> visitThis(ThisNode node, ParsedContext context) {
		CodePosition position = node.position();
		return new Parsed<>(new ParsedExpressionThis(position));
	}

	@Override
	public Parsed<ParsedThrowExpression> visitThrow(ThrowNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.value().accept(this, context).cast();
		return new Parsed<>(new ParsedThrowExpression(position, expression));
	}

	@Override
	public Parsed<ParsedTryConvertExpression> visitTryConvert(TryConvertNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedTryConvertExpression(position, expression));
	}

	@Override
	public Parsed<ParsedTryRethrowExpression> visitTryRethrow(TryRethrowNode node, ParsedContext context) {
		CodePosition position = node.position();
		CompilableExpression expression = node.expression().accept(this, context).cast();
		return new Parsed<>(new ParsedTryRethrowExpression(position, expression));
	}

	@Override
	public Parsed<ParsedTypeExpression> visitTypeExpression(TypeExpressionNode node, ParsedContext context) {
		CodePosition position = node.position();
		IParsedType type = this.visitType(node.type(), context).cast();
		return new Parsed<>(new ParsedTypeExpression(position, type));
	}

	@Override
	public Parsed<?> visitUSHRAssign(USHRAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.USHRASSIGN, context);
	}

	@Override
	public Parsed<?> visitUSHR(USHRNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.USHR, context);
	}

	@Override
	public Parsed<ParsedExpressionVariable> visitVariable(VariableNode node, ParsedContext context) {
		CodePosition position = node.position();
		String name = node.name().accept(this, context).cast();
		return new Parsed<>(new ParsedExpressionVariable(position, name, null));
	}

	@Override
	public Parsed<ParsedExpressionBinary> visitXorAssign(XorAssignNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.XORASSIGN, context);
	}

	@Override
	public Parsed<ParsedExpressionBinary> visitXor(XorNode node, ParsedContext context) {
		return visitBinary(node.position(), node.left(), node.right(), OperatorType.XOR, context);
	}

	@Override
	public Parsed<ParsedArrayType> visitArrayType(ArrayTypeNode node, ParsedContext context) {
		IParsedType baseType = this.visitType(node.elementType(), context).cast();
		//TODO handle dimensions
		return new Parsed<>(new ParsedArrayType(baseType, node.dimensions()));
	}

	@Override
	public Parsed<ParsedBasicType> visitBasicType(BasicTypeNode node, ParsedContext context) {
		ParsedBasicType result = null;
		switch (node.type().getType()) {
			case K_VOID:
				result = ParsedBasicType.VOID;
				break;
			case K_BOOL:
				result = ParsedBasicType.BOOL;
				break;
			case K_BYTE:
				result = ParsedBasicType.BYTE;
				break;
			case K_SBYTE:
				result = ParsedBasicType.SBYTE;
				break;
			case K_SHORT:
				result = ParsedBasicType.SHORT;
				break;
			case K_USHORT:
				result = ParsedBasicType.USHORT;
				break;
			case K_INT:
				result = ParsedBasicType.INT;
				break;
			case K_UINT:
				result = ParsedBasicType.UINT;
				break;
			case K_LONG:
				result = ParsedBasicType.LONG;
				break;
			case K_ULONG:
				result = ParsedBasicType.ULONG;
				break;
			case K_USIZE:
				result = ParsedBasicType.USIZE;
				break;
			case K_FLOAT:
				result = ParsedBasicType.FLOAT;
				break;
			case K_DOUBLE:
				result = ParsedBasicType.DOUBLE;
				break;
			case K_CHAR:
				result = ParsedBasicType.CHAR;
				break;
			case K_STRING: {
				result = ParsedBasicType.STRING;
				break;
			}
		}
		return new Parsed<>(result);
	}

	@Override
	public Parsed<ParsedGenericMapType> visitGenericMapType(GenericMapTypeNode node, ParsedContext context) {
		ParsedTypeParameter key = node.key().accept(this, context).cast();
		IParsedType value = node.value().accept(this, context).cast();
		return new Parsed<>(new ParsedGenericMapType(key, value));
	}

	@Override
	public Parsed<ParsedMapType> visitMapType(MapTypeNode node, ParsedContext context) {
		IParsedType keyType = this.visitType(node.key(), context).cast();
		IParsedType valueType = this.visitType(node.value(), context).cast();

		return new Parsed<>(new ParsedMapType(keyType, valueType));
	}

	@Override
	public Parsed<ParsedNamedType> visitNamedType(NamedTypeNode node, ParsedContext context) {
		CodePosition position = node.position();
		List<ParsedNamedType.ParsedNamePart> nameParts = node.names().stream()
				.map(nameNode -> new ParsedNamedType.ParsedNamePart(nameNode.name().accept(this, context).cast(), nameNode.typeArguments() == null ? null : nameNode.typeArguments().accept(this, context).cast()))
				.collect(Collectors.toList());
		return new Parsed<>(new ParsedNamedType(position, nameParts));
	}

	@Override
	public Parsed<ParsedFunctionType> visitFunctionType(FunctionTypeNode node, ParsedContext context) {
		ParsedFunctionHeader header = node.header().accept(this, context).cast();
		return new Parsed<>(new ParsedFunctionType(header));
	}

	@Override
	public Parsed<ParsedOptionalType> visitOptionalType(OptionalTypeNode node, ParsedContext context) {
		IParsedType type = this.visitType(node.baseType(), context).cast();
		return new Parsed<>(new ParsedOptionalType(type));
	}

	@Override
	public Parsed<ParsedRangeType> visitRangeType(RangeTypeNode node, ParsedContext context) {
		CodePosition position = node.position();
		IParsedType from = this.visitType(node.from(), context).cast();
		IParsedType to = this.visitType(node.to(), context).cast();
		return new Parsed<>(new ParsedRangeType(position, from, to));
	}

	@Override
	public Parsed<String> visitName(NameNode node, ParsedContext context) {
		return new Parsed<>(node.identifier().getContent());
	}

	@Override
	public Parsed<ParsedCallArguments> visitCallArguments(CallArgumentsNode node, ParsedContext context) {
		List<CompilableExpression> collect = node.arguments().stream().<CompilableExpression>map(expressionNode -> expressionNode.accept(this, context).cast()).collect(Collectors.toList());
		return new Parsed<>(new ParsedCallArguments(collect));
	}

	@Override
	public Parsed<ParsedFunctionHeader> visitFunctionHeader(FunctionHeaderNode node, ParsedContext context) {
		CodePosition position = node.position();
		List<ParsedTypeParameter> genericParams = this.visitTypeParameters(node.genericParameters(), context).cast();
		List<ParsedFunctionParameter> params = this.visitParameters(node.parameters(), context).cast();
		IParsedType returnType = node.returnType() == null ? ParsedBasicType.UNDETERMINED : this.visitType(node.returnType(), context).cast();
		IParsedType thrownType = node.thrownType() == null ? null : this.visitType(node.thrownType(), context).cast();
		return new Parsed<>(new ParsedFunctionHeader(position, genericParams, params, returnType, thrownType));
	}

	@Override
	public Parsed<CompilableLambdaHeader> visitLambdaHeader(LambdaHeaderNode node, ParsedContext context) {
		//TODO the whole CompilableLambdaHeader class seems like a mess right now
		return null;
	}

	@Override
	public Parsed<ParsedFunctionParameter> visitParameter(ParameterNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		String name = node.name().accept(this, context).cast();
		IParsedType type = this.visitType(node.type(), context).cast();
		CompilableExpression expression = node.defaultValue() == null ? null : node.defaultValue().accept(this, context).cast();
		boolean variadic = node.variadic();
		return new Parsed<>(new ParsedFunctionParameter(position, annotations, name, type, expression, variadic));
	}

	@Override
	public Parsed<List<ParsedFunctionParameter>> visitParameters(ParametersNode node, ParsedContext context) {
		List<ParsedFunctionParameter> parameters = new ArrayList<>();
		for (ParameterNode parameter : node.parameters()) {
			parameters.add(parameter.accept(this, context).cast());
		}
		return new Parsed<>(parameters);
	}

	@Override
	public Parsed<ParsedGenericBound> visitGenericBound(GenericBoundNode node, ParsedContext context) {
		Parsed<IParsedType> type = this.visitType(node, context);
		if (node.isSuper()) {
			return new Parsed<>(new ParsedSuperBound(type.cast()));
		} else {
			return new Parsed<>(new ParsedTypeBound(node.position(), type.cast()));
		}
	}

	private Parsed<IParsedType> visitType(ASTNode node, ParsedContext context) {
		if (node == null) {
			return new Parsed<>(ParsedBasicType.UNDETERMINED);
		}
		return (Parsed<IParsedType>) node.accept(this, context);
	}

	@Override
	public Parsed<List<IParsedType>> visitTypeArguments(TypeArgumentsNode node, ParsedContext context) {
		return new Parsed<>(node.types().stream().<IParsedType>map(typeNode -> this.visitType(typeNode, context).cast()).collect(Collectors.toList()));
	}

	@Override
	public Parsed<ParsedTypeParameter> visitTypeParameter(TypeParameterNode node, ParsedContext context) {
		CodePosition position = node.position();
		String name = node.name().accept(this, context).cast();
		List<ParsedGenericBound> bounds = node.bounds().stream().<ParsedGenericBound>map(genericBoundNode -> this.visitGenericBound(genericBoundNode, context).cast()).collect(Collectors.toList());
		return new Parsed<>(new ParsedTypeParameter(position, name, bounds));
	}

	@Override
	public Parsed<List<ParsedTypeParameter>> visitTypeParameters(TypeParametersNode node, ParsedContext context) {
		if (node == null || node.typeParameters() == null) {
			//TODO remove this check, it should never be null
			return new Parsed<>(new ArrayList<>());
		}
		return new Parsed<>(node.typeParameters().stream().<ParsedTypeParameter>map(typeNode -> this.visitTypeParameter(typeNode, context).cast()).collect(Collectors.toList()));
	}

	@Override
	public Parsed<String[]> visitForKeys(ForKeysNode node, ParsedContext context) {
		return new Parsed<>(node.keys().stream().map(nameNode -> nameNode.identifier().getContent()).toArray(String[]::new));
	}

	@Override
	public Parsed<String> visitLabel(LabelNode node, ParsedContext context) {
		return this.visitName(node.label(), context);
	}

	@Override
	public Parsed<Modifiers> visitModifier(ModifierNode node, ParsedContext context) {
		Modifiers modifiers = Modifiers.NONE;
		switch (node.modifier().getType()) {
			case K_ABSTRACT:
				modifiers = modifiers.withAbstract();
				break;
			case K_FINAL:
				modifiers = modifiers.withFinal();
				break;
			case K_OVERRIDE:
				modifiers = modifiers.withOverride();
				break;
			case K_CONST:
				modifiers = modifiers.withConst();
				break;
			case K_PRIVATE:
				modifiers = modifiers.withPrivate();
				break;
			case K_PUBLIC:
				modifiers = modifiers.withPublic();
				break;
			case K_INTERNAL:
				modifiers = modifiers.withInternal();
				break;
			case K_STATIC:
				modifiers = modifiers.withStatic();
				break;
			case K_PROTECTED:
				modifiers = modifiers.withProtected();
				break;
			case K_IMPLICIT:
				modifiers = modifiers.withImplicit();
				break;
			case K_VIRTUAL:
				modifiers = modifiers.withVirtual();
				break;
			case K_EXTERN:
				modifiers = modifiers.withExtern();
				break;
		}
		return new Parsed<>(modifiers);
	}

	@Override
	public Parsed<Modifiers> visitModifiers(ModifiersNode node, ParsedContext context) {
		Modifiers modifiers = Modifiers.NONE;

		if (node == null || node.modifiers() == null) {
			return new Parsed<>(modifiers);
		}
		for (ModifierNode modifier : node.modifiers()) {
			switch (modifier.modifier().getType()) {
				case K_ABSTRACT:
					modifiers = modifiers.withAbstract();
					break;
				case K_FINAL:
					modifiers = modifiers.withFinal();
					break;
				case K_OVERRIDE:
					modifiers = modifiers.withOverride();
					break;
				case K_CONST:
					modifiers = modifiers.withConst();
					break;
				case K_PRIVATE:
					modifiers = modifiers.withPrivate();
					break;
				case K_PUBLIC:
					modifiers = modifiers.withPublic();
					break;
				case K_INTERNAL:
					modifiers = modifiers.withInternal();
					break;
				case K_STATIC:
					modifiers = modifiers.withStatic();
					break;
				case K_PROTECTED:
					modifiers = modifiers.withProtected();
					break;
				case K_IMPLICIT:
					modifiers = modifiers.withImplicit();
					break;
				case K_VIRTUAL:
					modifiers = modifiers.withVirtual();
					break;
				case K_EXTERN:
					modifiers = modifiers.withExtern();
					break;
			}
		}
		return new Parsed<>(modifiers);
	}

	@Override
	public Parsed<ParsedExpressionSuper> visitSuperType(SuperTypeNode node, ParsedContext context) {
		return new Parsed<>(new ParsedExpressionSuper(node.position()));
	}

	@Override
	public Parsed<List<IParsedType>> visitSuperTypes(SuperTypesNode node, ParsedContext context) {
		List<IParsedType> types = node.superTypes().stream()
				.<IParsedType>map(typeNode -> this.visitType(typeNode, context).cast())
				.collect(Collectors.toList());
		return new Parsed<>(types);
	}

	@Override
	public Parsed<ParsedImport> visitImport(ImportNode node, ParsedContext context) {
		CodePosition position = node.position();
		boolean relative = node.relative();
		List<String> name = node.names().stream().map(nameNode -> nameNode.identifier().getContent()).collect(Collectors.toList());
		String rename = node.alias() == null ? null : node.alias().identifier().getContent();
		if(name.isEmpty()) {
			return null;
		}
		return new Parsed<>(new ParsedImport(position, relative, name, rename));
	}

	@Override
	public Parsed<Modifiers> visitAutoGetter(AutoGetterNode node, ParsedContext context) {
		return this.visitModifiers(node.modifiers(), context);
	}

	@Override
	public Parsed<Modifiers> visitAutoSetter(AutoSetterNode node, ParsedContext context) {
		return this.visitModifiers(node.modifiers(), context);
	}

	@Override
	public Parsed<?> visitAutos(AutosNode node, ParsedContext context) {
		throw new UnsupportedOperationException("AutosNode should not be visited");
	}

	@Override
	public Parsed<ParsedCaster> visitCaster(CasterNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		IParsedType type = this.visitType(node.type(), context).cast();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);
		return new Parsed<>(new ParsedCaster(position, modifiers, annotations, type, body));
	}

	@Override
	public Parsed<ParsedConstructor> visitConstructor(ConstructorNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedFunctionHeader header = node.header().accept(this, context).cast();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);

		return new Parsed<>(new ParsedConstructor(position, modifiers, annotations, header, body));
	}

	@Override
	public Parsed<ParsedOperator> visitDestructor(DestructorNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedFunctionHeader header = node.header().accept(this, context).cast();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);

		return new Parsed<>(new ParsedOperator(position, modifiers, annotations, OperatorType.DESTRUCTOR, header, body));
	}

	@Override
	public Parsed<ParsedField> visitField(FieldNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		String name = node.name().accept(this, context).cast();
		IParsedType type = this.visitType(node.type(), context).thing();
		CompilableExpression expression = node.expression() == null ? null : node.expression().accept(this, context).cast();
		boolean isFinal = node.isFinal();
		Modifiers autoGetter = null;
		Modifiers autoSetter = null;
		if (node.autos() != null) {
			if (node.autos().autoGetter() != null) {
				autoGetter = node.autos().autoGetter().accept(this, context).cast();
			}
			if (node.autos().autoSetter() != null) {
				autoSetter = node.autos().autoSetter().accept(this, context).cast();
			}
		}
		ParsedField field = new ParsedField(position, modifiers, annotations, name, type, expression, isFinal, autoGetter, autoSetter);
		return new Parsed<>(field);
	}

	public ParsedFunctionBody visitFunctionBody(ASTNode node, ParsedContext context) {
		if (node instanceof ExpressionNode) {
			return new ParsedLambdaFunctionBody(node.position(), node.accept(this, context).cast());
		}
		if (node instanceof BlockStatementNode) {
			return new ParsedStatementsFunctionBody(node.accept(this, context).cast());
		}
		if (node instanceof EmptyFunctionBodyNode) {
			return new ParsedEmptyFunctionBody(node.position());
		}
		throw new IllegalStateException();
	}

	@Override
	public Parsed<ParsedGetter> visitGetter(GetterNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		String name = node.name().accept(this, context).cast();
		IParsedType type = this.visitType(node.type(), context).thing();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);

		return new Parsed<>(new ParsedGetter(position, modifiers, annotations, name, type, body));
	}

	@Override
	public Parsed<ParsedImplementation> visitImplements(ImplementsNode node, ParsedContext context) {
		CodePosition position = node.position();
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		IParsedType type = this.visitType(node.type(), context).thing();
		ParsedImplementation parsedImplementation = new ParsedImplementation(position, modifiers, annotations, type);
		node.members().stream().<ParsedDefinitionMember>map(defNode -> defNode.accept(this, context).cast()).forEach(parsedImplementation::addMember);
		return new Parsed<>(parsedImplementation);
	}

	@Override
	public Parsed<ParsedInnerDefinition> visitInnerDefinition(InnerDefinitionNode node, ParsedContext context) {
		ParsedDefinition definition = node.definition().accept(this, context).cast();
		return new Parsed<>(new ParsedInnerDefinition(definition));
	}

	@Override
	public Parsed<ParsedIterator> visitIterator(IteratorNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		List<IParsedType> loopVariableTypes = node.loopVariableTypes().stream().<IParsedType>map(typeNode -> this.visitType(typeNode, context).cast()).collect(Collectors.toList());
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);
		return new Parsed<>(new ParsedIterator(position, modifiers, annotations, loopVariableTypes, body));
	}

	@Override
	public Parsed<List<ParsedDefinitionMember>> visitMembers(MembersNode node, ParsedContext context) {
		List<ParsedDefinitionMember> members = new ArrayList<>();
		for (DefinitionMemberNode member : node.members()) {
			Parsed<?> accept = member.accept(this, context);
			if (accept == null) {
				continue;
			}
			members.add(accept.cast());
		}
		return new Parsed<>(members);
	}

	@Override
	public Parsed<ParsedMethod> visitMethod(MethodNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		String name = node.name().accept(this, context).cast();
		ParsedFunctionHeader header = node.header().accept(this, context).cast();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);

		return new Parsed<>(new ParsedMethod(position, modifiers, annotations, name, header, body));
	}

	@Override
	public Parsed<ParsedOperator> visitOperator(OperatorNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		ParsedFunctionHeader header = node.header().accept(this, context).cast();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);

		return new Parsed<>(new ParsedOperator(position, modifiers, annotations, OperatorType.DESTRUCTOR, header, body));
	}

	@Override
	public Parsed<ParsedSetter> visitSetter(SetterNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		Modifiers modifiers = visitModifiers(node.modifiers(), context).cast();
		String name = node.name().accept(this, context).cast();
		IParsedType type = this.visitType(node.type(), context).thing();
		ParsedFunctionBody body = visitFunctionBody(node.body(), context);

		return new Parsed<>(new ParsedSetter(position, modifiers, annotations, name, type, body));
	}

	@Override
	public Parsed<ParsedStaticInitializer> visitStaticInitializer(StaticInitializerNode node, ParsedContext context) {
		CodePosition position = node.position();
		ParsedAnnotation[] annotations = ParsedAnnotation.NONE; //TODO: handle annotations
		ParsedStatement body = this.visitStatement(node.body(), context);
		return new Parsed<>(new ParsedStaticInitializer(position, annotations, body));
	}

	@Override
	public Parsed<ParsedEmptyFunctionBody> visitEmptyFunctionBody(EmptyFunctionBodyNode node, ParsedContext context) {
		return new Parsed<>(new ParsedEmptyFunctionBody(node.position()));
	}

	public static class ParsedContext {
		public final SourceFile file;
		public final CompileContext context;
		public final CompilingPackage pkg;

		public ParsedContext(SourceFile file, CompileContext context, CompilingPackage pkg) {
			this.file = file;
			this.context = context;
			this.pkg = pkg;
		}
	}
}
