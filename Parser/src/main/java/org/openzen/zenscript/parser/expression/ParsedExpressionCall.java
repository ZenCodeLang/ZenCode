package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ParsedExpressionCall extends ParsedExpression {
	private final CompilableExpression receiver;
	private final ParsedCallArguments arguments;

	public ParsedExpressionCall(CodePosition position, CompilableExpression receiver, ParsedCallArguments arguments) {
		super(position);

		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, receiver.compile(compiler), arguments.compile(compiler));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression receiver;
		private final CompilingExpression[] arguments;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression receiver, CompilingExpression[] arguments) {
			super(compiler, position);
			this.receiver = receiver;
			this.arguments = arguments;
		}

		@Override
		public Expression eval() {
			return receiver.call()
					.map(call -> call.call(position, arguments))
					.orElseGet(() -> compiler.at(position).invalid(CompileErrors.cannotCall()));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return receiver.call()
					.map(call -> call.casted(position, cast, arguments))
					.orElseGet(() -> cast.invalid(CompileErrors.cannotCall()));
		}
	}

	@Override
	public SwitchValue asSwitchValue(TypeID type, ExpressionCompiler compiler) {
		if (!(receiver instanceof ParsedExpressionVariable))
			return new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());

		String name = ((ParsedExpressionVariable) receiver).name;
		ResolvedType resolved = compiler.resolve(type);
		Optional<ResolvedType.SwitchMember> maybeSwitchMember = resolved.findSwitchMember(name);
		if (maybeSwitchMember.isPresent()) {
			String[] values = new String[arguments.arguments.size()];
			for (int i = 0; i < values.length; i++) {
				CompilableExpression argument = arguments.arguments.get(i);
				Optional<CompilableLambdaHeader.Parameter> lambdaHeader = argument.asLambdaHeaderParameter();
				if (lambdaHeader.isPresent()) {
					values[i] = lambdaHeader.get().getName();
				} else {
					return new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
				}
			}

			return maybeSwitchMember.get().toSwitchValue(values);
		} else {
			return new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
		}
	}
}
