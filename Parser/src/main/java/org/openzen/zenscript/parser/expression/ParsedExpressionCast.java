package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.compiler.types.ResolvedType;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

public class ParsedExpressionCast extends ParsedExpression {
	private final ParsedExpression value;
	private final IParsedType type;
	private final boolean optional;

	public ParsedExpressionCast(CodePosition position, ParsedExpression value, IParsedType type, boolean optional) {
		super(position);

		this.value = value;
		this.type = type;
		this.optional = optional;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(compiler, position, value.compile(compiler), type.compile(compiler.types()), optional);
	}

	@Override
	public ParsedFunctionHeader toLambdaHeader() throws ParseException {
		if (optional)
			throw new ParseException(position, "Not a valid lambda header");

		ParsedFunctionHeader header = value.toLambdaHeader();
		if (header.returnType != ParsedTypeBasic.UNDETERMINED)
			throw new ParseException(position, "Lambda parameter already has a return type");

		return new ParsedFunctionHeader(position, header.genericParameters, header.parameters, type, null);
	}

	@Override
	public ParsedFunctionParameter toLambdaParameter() throws ParseException {
		if (optional)
			throw new ParseException(position, "Not a valid lambda header");

		ParsedFunctionParameter parameter = value.toLambdaParameter();
		if (parameter.type != ParsedTypeBasic.UNDETERMINED)
			throw new ParseException(position, "Lambda parameter already has a type");

		return new ParsedFunctionParameter(ParsedAnnotation.NONE, parameter.name, type, null, false);
	}

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
		public Expression as(TypeID type) {
			Expression value = this.value.as(type);
			ResolvedType resolvedType = compiler.resolve(this.type);
			return resolvedType.findExplicitCast(type)
					.map(cast -> cast.apply(value, optional))
					.orElseGet(() -> compiler.at(position, type).invalid(CompileExceptionCode.INVALID_CAST, "Cannot cast " + value.type + " to " + type));
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			return compiler.matchType(type, returnType);
		}

		@Override
		public InferredType inferType() {
			return InferredType.success(type);
		}
	}
}
