package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.Optional;

public class StatementCompilerImpl implements StatementCompiler {
	private final CompileContext context;
	private final ExpressionCompiler expressionCompiler;
	private final LocalType localType;
	private final LocalSymbols locals;
	private final FunctionHeader functionHeader;
	private final TypeBuilder types;

	public StatementCompilerImpl(CompileContext context, LocalType localType, TypeBuilder types, FunctionHeader functionHeader, LocalSymbols locals) {
		this.context = context;
		this.localType = localType;
		this.functionHeader = functionHeader;
		this.locals = locals;
		this.types = types.withGeneric(functionHeader.typeParameters);
		expressionCompiler = new ExpressionCompilerImpl(context, localType, this.types, functionHeader.thrownType, locals, functionHeader);
	}

	@Override
	public Expression compile(CompilableExpression expression) {
		return expression.compile(expressionCompiler).eval();
	}

	@Override
	public Expression compile(CompilableExpression expression, TypeID type) {
		return expression
				.compile(expressionCompiler)
				.cast(CastedEval.implicit(expressionCompiler, expression.getPosition(), type))
				.value;
	}

	@Override
	public ExpressionCompiler expressions() {
		return expressionCompiler;
	}

	@Override
	public TypeBuilder types() {
		return types;
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return context.resolve(type);
	}

	@Override
	public StatementCompiler forBlock() {
		return new StatementCompilerImpl(context, localType, types, functionHeader, locals.forBlock());
	}

	@Override
	public StatementCompiler forLoop(CompilingLoopStatement loop) {
		return new StatementCompilerImpl(context, localType, types, functionHeader, locals.forLoop(loop, loop.getLabels().toArray(new String[0])));
	}

	@Override
	public StatementCompiler forCatch(CompilingVariable exceptionVariable) {
		LocalSymbols locals = this.locals.forBlock();
		locals.add(exceptionVariable);
		return new StatementCompilerImpl(context, localType, types, functionHeader.withThrownType(exceptionVariable.getActualType()), locals);
	}

	@Override
	public Optional<CompilingLoopStatement> getLoop(String name) {
		return locals.findLoop(name);
	}

	@Override
	public Optional<FunctionHeader> getFunctionHeader() {
		return Optional.ofNullable(functionHeader);
	}

	@Override
	public void addLocalVariable(CompilingVariable variable) {
		locals.add(variable);
	}
}
