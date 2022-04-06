package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;

public class ParsedExpressionCall extends ParsedExpression {
	private final ParsedExpression receiver;
	private final ParsedCallArguments arguments;

	public ParsedExpressionCall(CodePosition position, ParsedExpression receiver, ParsedCallArguments arguments) {
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
		public Expression as(TypeID type) {
			return receiver.call()
					.map(call -> call.call(type, arguments))
					.orElseGet(() -> compiler.at(position, type).invalid(CompileExceptionCode.CALL_NO_VALID_METHOD, "Cannot call this expression"));
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return receiver.call()
					.map(call -> call.matches(returnType, arguments))
					.orElse(TypeMatch.NONE);
		}

		@Override
		public InferredType inferType() {
			return receiver.call()
					.map(call -> call.inferReturnType(arguments))
					.orElseGet(() -> InferredType.failure(CompileExceptionCode.CALL_NO_VALID_METHOD, "Cannot call this expression"));
		}
	}

	@Override
	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
		if (!(receiver instanceof ParsedExpressionVariable))
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");

		String name = ((ParsedExpressionVariable) receiver).name;
		TypeMembers members = scope.getTypeMembers(type);
		if (type.isVariant()) {
			VariantOptionRef option = members.getVariantOption(name);
			if (option == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant option does not exist: " + name);

			String[] values = new String[arguments.arguments.size()];
			for (int i = 0; i < values.length; i++) {
				try {
					ParsedExpression argument = arguments.arguments.get(i);
					ParsedFunctionParameter lambdaHeader = argument.toLambdaParameter();
					values[i] = lambdaHeader.name;
				} catch (ParseException ex) {
					throw new CompileException(ex.position, CompileExceptionCode.INVALID_SWITCH_CASE, ex.getMessage());
				}
			}

			return new VariantOptionSwitchValue(option, values);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
		}
	}
}
