package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

		@Override
		public void collect(SSAVariableCollector collector) {
			receiver.collect(collector);
			for (CompilingExpression argument : arguments)
				argument.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			receiver.linkVariables(linker);
			for (CompilingExpression argument : arguments)
				argument.linkVariables(linker);
		}
	}

	@Override
	public CompilingSwitchValue compileSwitchValue(ExpressionCompiler compiler) {
		if (!(receiver instanceof ParsedExpressionVariable)) {
			return type -> new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
		}

		List<CompilingVariable> bindings = new ArrayList<>();
		for (CompilableExpression argument : arguments.arguments) {
			Optional<CompilableLambdaHeader.Parameter> lambdaHeader = argument.asLambdaHeaderParameter();
			if (lambdaHeader.isPresent()) {
				bindings.add(new CompilingVariable(new VariableID(), lambdaHeader.get().getName(), null, true));
			} else {
				return type -> new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
			}
		}

		return new CompilingSwitchValue() {
            @Override
            public SwitchValue as(TypeID type) {
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

                    return maybeSwitchMember.get().toSwitchValue(bindings);
                } else {
                    return new ErrorSwitchValue(position, CompileErrors.invalidSwitchCaseExpression());
                }
            }

			@Override
			public List<CompilingVariable> getBindings() {
				return bindings;
			}
        };
	}
}
