package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.expression.ExpressionBuilder;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;

import java.util.Optional;

public class ExpressionCompilerImpl implements ExpressionCompiler {
	public ExpressionBuilder at(CodePosition position) {
		return new ExpressionBuilderImpl(position);
	}

	public TypeMatch match(TypeID value, TypeID result) {
		if (value == result)
			return TypeMatch.EXACT;
		else if (resolve(result).findConstructor(c -> c.isImplicit() && c.isCompatible(result, value)).isPresent())
			return TypeMatch.IMPLICIT;
		else if (resolve(value).findImplicitCast(result).isPresent())
			return TypeMatch.IMPLICIT;
		else
			return TypeMatch.NONE;
	}

	private class ExpressionBuilderImpl implements ExpressionBuilder {
		private final CodePosition position;

		public ExpressionBuilderImpl(CodePosition position) {
			this.position = position;
		}

		public InvalidExpression invalid(CompileExceptionCode code, String message) {
			return new InvalidExpression(position, BasicTypeID.INVALID, code, message);
		}

		public Optional<CompilingExpression> resolve(GenericName name) {

		}

		public CompilingExpression is(Expression value) {

		}
	}
}
