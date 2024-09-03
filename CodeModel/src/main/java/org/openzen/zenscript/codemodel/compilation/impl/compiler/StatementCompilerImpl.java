package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.WrappedCompilingExpression;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class StatementCompilerImpl implements StatementCompiler {
	private final CompileContext context;
	private final ExpressionCompiler expressionCompiler;
	private final LocalType localType;
	private final LocalSymbols locals;
	private final FunctionHeader functionHeader;
	private final TypeID thrownType;
	private final TypeBuilder types;

	public StatementCompilerImpl(CompileContext context, LocalType localType, TypeBuilder types, FunctionHeader functionHeader, LocalSymbols locals, TypeID thrownType) {
		this.context = context;
		this.localType = localType;
		this.functionHeader = functionHeader;
		this.types = types.withGeneric(functionHeader.typeParameters);
		this.thrownType = thrownType;
		ExpressionCompiler expressionCompiler = new ExpressionCompilerImpl(context, localType, this.types, functionHeader.thrownType, locals, functionHeader);

		final ExpressionCompiler expressionCompiler1 = expressionCompiler;
		CompilingExpression dollar = Stream.of(functionHeader.parameters)
				.filter(p -> p.name.equals("$"))
				.findFirst()
				.map(p -> new WrappedCompilingExpression(expressionCompiler1, new GetFunctionParameterExpression(CodePosition.UNKNOWN, p)))
				.orElse(null);
		if (dollar != null) {
			locals = locals.withDollar(dollar);
			expressionCompiler = new ExpressionCompilerImpl(context, localType, this.types, functionHeader.thrownType, locals, functionHeader);
		}

		this.expressionCompiler = expressionCompiler;
		this.locals = locals;
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
		return new StatementCompilerImpl(context, localType, types, functionHeader, locals.forBlock(), thrownType);
	}

	@Override
	public StatementCompiler forLoop(CompilingLoopStatement loop) {
		return new StatementCompilerImpl(context, localType, types, functionHeader, locals.forLoop(loop, loop.getLabels().toArray(new String[0])), thrownType);
	}

	@Override
	public StatementCompiler forCatch(CompilingVariable exceptionVariable) {
		LocalSymbols locals = this.locals.forBlock();
		locals.add(exceptionVariable);
		return new StatementCompilerImpl(context, localType, types, functionHeader.withThrownType(exceptionVariable.getActualType()), locals, null);
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
	public Optional<TypeID> getThrownType() {
		return thrownType == null ? Optional.ofNullable(functionHeader).map(header -> header.thrownType) : Optional.of(thrownType);
	}

	@Override
	public void addLocalVariable(CompilingVariable variable) {
		locals.add(variable);
	}
}
