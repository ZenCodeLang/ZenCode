package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;

public class ParsedExpressionBool extends ParsedExpression {
	private final boolean value;

	public ParsedExpressionBool(CodePosition position, boolean value) {
		super(position);

		this.value = value;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final boolean value;

		public Compiling(ExpressionCompiler compiler, CodePosition position, boolean value) {
			super(compiler, position);
			this.value = value;
		}

		@Override
		public Expression as(TypeID type) {
			return compiler.at(position, type).constant(value);
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return compiler.matchType(BasicTypeID.BOOL, returnType);
		}

		@Override
		public InferredType inferType() {
			return InferredType.success(BasicTypeID.BOOL);
		}
	}
}
