package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchedCallArguments<T extends AnyMethod> {
	public static <T extends AnyMethod> MatchedCallArguments<T> match(
			ExpressionCompiler compiler,
			CodePosition position,
			List<T> overloads,
			TypeID asType,
			TypeID[] typeArguments,
			CompilingExpression... arguments
	) {
		MatchedCallArguments<T> result = null;
		List<FunctionHeader> candidates = new ArrayList<>();
		boolean implicit = false;
		boolean ambiguous = false;

		for (T method : overloads) {
			if (method.getHeader().accepts(arguments.length)) {
				CallArguments matched = match(compiler, position, method, asType, typeArguments, arguments);

				if (matched.level == CastedExpression.Level.EXACT) {
					if (result == null || implicit) {
						if (implicit) {
							candidates = new ArrayList<>();
							ambiguous = false;
						}

						result = new MatchedCallArguments<>(method, matched);
						implicit = false;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				} else if (matched.level == CastedExpression.Level.IMPLICIT) {
					if (result == null) {
						result = new MatchedCallArguments<>(method, matched);
						implicit = true;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				}
			}
		}

		if (ambiguous) {
			return new MatchedCallArguments<>(CompileErrors.ambiguousCall(candidates));
		} else if (result == null) {
			return new MatchedCallArguments<>(CompileErrors.noMethodMatched(overloads.stream()
					.map(AnyMethod::getHeader)
					.collect(Collectors.toList())));
		} else {
			return result;
		}
	}

	private final T method;
	private final CallArguments arguments;
	private final CompileError error;

	private MatchedCallArguments(T method, CallArguments arguments) {
		this.method = method;
		this.arguments = arguments;
		this.error = null;
	}

	private MatchedCallArguments(CompileError error) {
		this.method = null;
		this.arguments = null;
		this.error = error;
	}

	public Expression eval(ExpressionBuilder builder, CallEvaluator<T> evaluator) {
		if (this.error != null) {
			return builder.invalid(error);
		} else {
			return evaluator.eval(builder, method, arguments);
		}
	}

	public CastedExpression cast(ExpressionBuilder builder, CastedEval eval, CallEvaluator<T> evaluator) {
		if (this.error != null) {
			return eval.invalid(error);
		} else {
			return eval.of(evaluator.eval(builder, method, arguments));
		}
	}

	public Optional<CallArguments> getArguments() {
		return Optional.ofNullable(arguments);
	}

	public Optional<CompileError> getError() {
		return Optional.ofNullable(error);
	}

	@FunctionalInterface
	interface CallEvaluator<T> {
		Expression eval(ExpressionBuilder builder, T method, CallArguments arguments);
	}

	public static CallArguments match(
			ExpressionCompiler compiler,
			CodePosition position,
			AnyMethod method,
			TypeID result,
			TypeID[] typeArguments,
			CompilingExpression... arguments
	) {
		if (!method.getHeader().accepts(arguments.length))
			return new CallArguments(
					CastedExpression.Level.INVALID,
					typeArguments,
					Expression.NONE);

		if (result != null && (typeArguments == null || typeArguments.length == 0)) {
			// attempt to infer type arguments from the map
			final Map<TypeParameter, TypeID> typeArgumentMap = method.getHeader().getReturnType().inferTypeParameters(result);

			TypeID[] typeArguments2 = new TypeID[method.getHeader().typeParameters.length];
			for (int i = 0; i < method.getHeader().typeParameters.length; i++) {
				typeArguments2[i] = typeArgumentMap.get(method.getHeader().typeParameters[i]);
			}

			boolean hasUnknowns = false;
			for (int i = 0; i < typeArguments2.length; i++) {
				if (typeArguments2[i] == null) {
					hasUnknowns = true;
					break;
				}
			}
			if (hasUnknowns) {
				// TODO: improve type inference
				return new CallArguments(CastedExpression.Level.INVALID, typeArguments, Expression.NONE);
			}

			typeArguments = typeArguments2;
		}

		FunctionHeader header = method
				.getHeader()
				.withGenericArguments(GenericMapper.create(method.getHeader().typeParameters, typeArguments));

		// Use Parameters.length instead of maxParameters to get number of declared parameters
		// We could be variadic if we have 0 vararg parameters (=#parameters-1 arguments), 1 (=#parameters arguments) or more
		// We could be "normal" if we have as many args as parameters, or less (for optionals)
		boolean couldBeVariadic = header.isVariadic() && arguments.length >= header.parameters.length - 1;
		TypeID variadicType = header.getVariadicParameterType().orElse(null);

		Expression[] cArguments = new Expression[header.parameters.length];
		List<Expression> varargArguments = new ArrayList<>();
		CastedExpression.Level levelNormalCall = CastedExpression.Level.EXACT;
		CastedExpression.Level levelVarargCall = null;

		for (int i = 0; i < cArguments.length; i++) {
			if (couldBeVariadic && i >= header.parameters.length - 1) {
				if (levelVarargCall == null)
					levelVarargCall = levelNormalCall;

				CastedExpression cArgument = arguments[i].cast(CastedEval.implicit(compiler, position, variadicType));
				varargArguments.add(cArgument.value);
				levelVarargCall = levelVarargCall.max(cArgument.level);
			} else {
				TypeID parameterType = header.getParameterType(false, i);
				CastedExpression cArgument = arguments[i].cast(CastedEval.implicit(compiler, position, parameterType));
				cArguments[i] = cArgument.value;
				levelNormalCall = levelNormalCall.max(cArgument.level);
			}
		}

		if (levelVarargCall != null && levelVarargCall.min(levelNormalCall) == levelVarargCall) {
			cArguments[cArguments.length - 1] = compiler.at(position).newArray(new ArrayTypeID(variadicType), varargArguments.toArray(Expression.NONE));
		}
		return new CallArguments(typeArguments, cArguments);
	}
}
