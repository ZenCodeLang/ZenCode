package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StatementCompilerImpl implements StatementCompiler {
	private final ExpressionCompiler expressionCompiler;
	private final TypeBuilder types;
	private final LocalSymbols locals;
	private final FunctionHeader functionHeader;

	public StatementCompilerImpl(TypeBuilder types, FunctionHeader functionHeader, LocalSymbols locals) {
		this.types = types;
		this.functionHeader = functionHeader;
		this.locals = locals;
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
	public SwitchValue compileSwitchValue(CompilableExpression expression, TypeID type) {
		return expression.asSwitchValue(type, expressionCompiler);
	}

	@Override
	public TypeBuilder types() {
		return types;
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		List<ExpansionSymbol> expansions = null;
	}

	@Override
	public StatementCompiler forBlock() {
		return new StatementCompilerImpl(types, functionHeader, new LocalSymbols(locals));
	}

	@Override
	public StatementCompiler forLoop(LoopStatement loop) {
		return new StatementCompilerImpl(types, functionHeader, new LocalSymbols(locals, loop));
	}

	@Override
	public StatementCompiler forForeach(ForeachStatement statement) {
		LocalSymbols locals = new LocalSymbols(
				this.locals,
				statement,
				Arrays.stream(statement.loopVariables).map(v -> v.name).toArray(String[]::new));
		return new StatementCompilerImpl(types, functionHeader, locals);
	}

	@Override
	public StatementCompiler forSwitch(SwitchStatement statement) {
		return new StatementCompilerImpl(types, functionHeader, new LocalSymbols(locals, statement));
	}

	@Override
	public StatementCompiler forCatch(VarStatement exceptionVariable) {
		LocalSymbols locals = new LocalSymbols(this.locals);
		locals.add(exceptionVariable);
		return new StatementCompilerImpl(types, functionHeader, locals);
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
