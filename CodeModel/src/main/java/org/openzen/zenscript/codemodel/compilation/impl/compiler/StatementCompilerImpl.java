package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
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

	public StatementCompilerImpl(CompileContext context, LocalType localType, FunctionHeader functionHeader, LocalSymbols locals) {
		this.context = context;
		this.localType = localType;
		this.functionHeader = functionHeader;
		this.locals = locals;
		expressionCompiler = new ExpressionCompilerImpl(context, localType, functionHeader.thrownType, locals, functionHeader);
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
	public SwitchValue compileSwitchValue(CompilableExpression expression, TypeID type) {
		return expression.asSwitchValue(type, expressionCompiler);
	}

	@Override
	public TypeBuilder types() {
		return context;
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return context.resolve(type);
	}

	@Override
	public StatementCompiler forBlock() {
		return new StatementCompilerImpl(context, localType, functionHeader, locals.forBlock());
	}

	@Override
	public StatementCompiler forLoop(LoopStatement loop) {
		return new StatementCompilerImpl(context, localType, functionHeader, locals.forLoop(loop, loop.label));
	}

	@Override
	public StatementCompiler forForeach(ForeachStatement statement) {
		LocalSymbols locals = this.locals.forLoop(
				statement,
				Arrays.stream(statement.loopVariables).map(v -> v.name).toArray(String[]::new));
		return new StatementCompilerImpl(context, localType, functionHeader, locals);
	}

	@Override
	public StatementCompiler forSwitch(SwitchStatement statement) {
		return new StatementCompilerImpl(context, localType, functionHeader, locals.forLoop(statement, statement.label));
	}

	@Override
	public StatementCompiler forCatch(VarStatement exceptionVariable) {
		LocalSymbols locals = this.locals.forBlock();
		locals.add(exceptionVariable);
		return new StatementCompilerImpl(context, localType, functionHeader, locals);
	}

	@Override
	public Optional<LoopStatement> getLoop(String name) {
		return locals.findLoop(name);
	}

	@Override
	public Optional<FunctionHeader> getFunctionHeader() {
		return Optional.ofNullable(functionHeader);
	}

	@Override
	public void addLocalVariable(VarStatement variable) {
		locals.add(variable);
	}
}
