package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.*;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.compiler.types.ResolvedType;

import java.util.*;
import java.util.function.BiFunction;

public class ParsedExpressionArray extends ParsedExpression {

	public static final List<BiFunction<ParsedExpressionArray, ExpressionScope, IPartialExpression>> compileOverrides = new ArrayList<>(0);
	public final List<ParsedExpression> contents;

	public ParsedExpressionArray(CodePosition position, List<ParsedExpression> contents) {
		super(position);

		this.contents = contents;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		CompilingExpression[] elements = contents.stream()
				.map(element -> element.compile(compiler))
				.toArray(CompilingExpression[]::new);

		return new Compiling(compiler, position, elements);
	}

	@Override
	public Expression compileKey(ExpressionCompiler compiler, TypeID asType) {
		if (contents.size() == 1) {
			return contents.get(0).compile(compiler).as(asType);
		} else {
			return compile(compiler).as(asType);
		}
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression[] elements;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression[] elements) {
			super(compiler, position);
			this.elements = elements;
		}

		@Override
		public Expression as(TypeID type) {
			Optional<ArrayTypeID> array = type.asArray();
			if (!array.isPresent()) {
				// use array constructor instead
				ResolvedType resolvedType = compiler.resolve(type);
				Optional<ResolvedCallable> constructor = resolvedType
						.findConstructor(c -> c.isImplicit() && c.isCompatible(type, t -> t.asArray().isPresent()));
				if (constructor.isPresent()) {
					return compiler.at(position, type).call(constructor.get(), this);
				} else {
					return compiler.at(position, type).invalid(CompileExceptionCode.INVALID_ARRAY_TYPE, "Invalid array type: " + type);
				}
			}

			ArrayTypeID arrayType = array.get();
			TypeID asBaseType = arrayType.elementType;
			Expression[] compiled = new Expression[elements.length];
			for (int i = 0; i < elements.length; i++) {
				compiled[i] = elements[i].as(asBaseType);
			}
			return new ArrayExpression(position, compiled, arrayType);
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			Optional<ArrayTypeID> actualHint = returnType
					.withoutOptional()
					.asArray()
					.filter(array -> array.dimension == 1);
			if (!actualHint.isPresent()) {
				return TypeMatch.NONE;
			}

			TypeID baseType = actualHint.get().elementType;

			TypeMatch match = TypeMatch.EXACT;
			for (CompilingExpression element : elements) {
				match = TypeMatch.min(match, element.matches(baseType));
			}
			return match;
		}

		@Override
		public InferredType inferType() {
			if (elements.length == 0) {
				return InferredType.failure(CompileExceptionCode.UNTYPED_EMPTY_ARRAY, "Cannot infer type of empty array");
			} else {
				InferredType inferredElementType = elements[0].inferType();
				if (inferredElementType.isFailed())
					return inferredElementType;

				TypeID elementType = inferredElementType.get();
				for (int i = 1; i < elements.length; i++) {
					InferredType inferred = elements[i].inferType();
					if (inferred.isFailed())
						return inferred;

					Optional<TypeID> joinedType = compiler.union(elementType, inferred.get());
					if (!joinedType.isPresent()) {
						return InferredType.failure(CompileExceptionCode.TYPE_CANNOT_UNITE, elementType + " and " + inferred.get() + " are incompatible");
					}

					elementType = joinedType.get();
				}
				return InferredType.success(compiler.types().arrayOf(elementType));
			}
		}
	}
}
