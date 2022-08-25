package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedExpressionCast extends ParsedExpression {
	private final CompilableExpression value;
	private final IParsedType type;
	private final boolean optional;

	public ParsedExpressionCast(CodePosition position, CompilableExpression value, IParsedType type, boolean optional) {
		super(position);

		this.value = value;
		this.type = type;
		this.optional = optional;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value.compile(compiler), type.compile(compiler.types()), optional);
	}

/*	@Override
	public Optional<CompilableLambdaHeader> toLambdaHeader() {
		if (optional)
			return Optional.empty();

		ParsedFunctionHeader header = value.toLambdaHeader();
		if (header.returnType != ParsedBasicType.UNDETERMINED)
			throw new ParseException(position, "Lambda parameter already has a return type");

		return new ParsedFunctionHeader(position, header.genericParameters, header.parameters, type, null);
	}

	@Override
	public ParsedFunctionParameter toLambdaParameter() throws ParseException {
		if (optional)
			throw new ParseException(position, "Not a valid lambda header");

		ParsedFunctionParameter parameter = value.toLambdaParameter();
		if (parameter.type != ParsedBasicType.UNDETERMINED)
			throw new ParseException(position, "Lambda parameter already has a type");

		return new ParsedFunctionParameter(ParsedAnnotation.NONE, parameter.name, type, null, false);
	}*/

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;
		private final TypeID type;
		private final boolean optional;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value, TypeID type, boolean optional) {
			super(compiler, position);
			this.value = value;
			this.type = type;
			this.optional = optional;
		}

		@Override
		public Expression eval() {
			return value.cast(new CastedEval(compiler, position, type, true, optional)).value;
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			return cast.of(eval());
		}
	}
}
