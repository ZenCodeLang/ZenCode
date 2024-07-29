package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;

public class MatchedCallArguments<T extends AnyMethod> {

	private static final CastedExpression.Level[] candidateLevelsInOrderOfPriority = {CastedExpression.Level.EXACT, CastedExpression.Level.WIDENING, CastedExpression.Level.IMPLICIT};

	public static <T extends AnyMethod> MatchedCallArguments<T> match(
			ExpressionCompiler compiler,
			CodePosition position,
			List<T> overloads,
			TypeID asType,
			TypeID[] typeArguments,
			CompilingExpression... arguments
	) {
		final Map<CastedExpression.Level, List<MatchedCallArguments<T>>> methodsGroupedByMatchLevel = overloads.stream()
				.map(method -> match(compiler, position, method, asType, typeArguments, arguments))
				.collect(Collectors.groupingBy(matched -> matched.arguments.level, Collectors.toList()));

		for (final CastedExpression.Level level : candidateLevelsInOrderOfPriority) {
			final List<MatchedCallArguments<T>> matchingMethods = methodsGroupedByMatchLevel.getOrDefault(level, Collections.emptyList());

			switch (matchingMethods.size()) {
				case 0:
					continue;
				case 1:
					return matchingMethods.get(0);
				default:
					return ambiguousCall(methodsGroupedByMatchLevel);
			}
		}

		return methodsGroupedByMatchLevel.getOrDefault(CastedExpression.Level.INVALID, Collections.emptyList()).stream().findFirst()
				.orElseGet(() -> {
					List<FunctionHeader> headers = overloads.stream().map(AnyMethod::getHeader).collect(Collectors.toList());
					List<TypeID> types = Arrays.stream(arguments).map(CompilingExpression::eval).map(it -> it.type).collect(Collectors.toList());
					return new MatchedCallArguments<>(CompileErrors.noMethodMatched(headers, types));
				});
	}

	private static <T extends AnyMethod> MatchedCallArguments<T> ambiguousCall(Map<CastedExpression.Level, List<MatchedCallArguments<T>>> methodsGroupedByMatchLevel) {
		List<FunctionHeader> candidates = Arrays.stream(candidateLevelsInOrderOfPriority)
				.flatMap(level -> methodsGroupedByMatchLevel.getOrDefault(level, Collections.emptyList()).stream())
				.map(pair -> pair.method.getHeader())
				.collect(Collectors.toList());

		return new MatchedCallArguments<>(CompileErrors.ambiguousCall(candidates));
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

	public boolean requiresWidenedInstance(TypeID instanceType) {
		return method != null && method.hasWideningConversions() && method.asMethod().map(method -> !method.getTarget().equals(instanceType)).orElse(false);
	}

	public TypeID getWidenedInstanceType() {
		return method.asMethod().map(MethodInstance::getTarget).orElse(null);
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
	public interface CallEvaluator<T> {
		Expression eval(ExpressionBuilder builder, T method, CallArguments arguments);
	}

	private static <T extends AnyMethod> MatchedCallArguments<T> match(
			ExpressionCompiler compiler,
			CodePosition position,
			T method,
			TypeID result,
			TypeID[] typeArguments,
			CompilingExpression... arguments
	) {
		TypeID[] expansionTypeArguments = method.asMethod().map(MethodInstance::getExpansionTypeArguments).orElse(TypeID.NONE);

		if (!method.getHeader().accepts(arguments.length))
			return new MatchedCallArguments<>(method, new CallArguments(
					CastedExpression.Level.INVALID,
					expansionTypeArguments,
					typeArguments,
					Expression.NONE));

		if ((typeArguments == null || typeArguments.length == 0) && method.getHeader().typeParameters.length > 0) {
			// attempt to infer type arguments from the return type
			final Map<TypeParameter, TypeID> typeArgumentMap = new HashMap<>();
			if (result != null) {
				typeArgumentMap.putAll(method.getHeader().getReturnType().inferTypeParameters(result));
			}

			// create a mapping with everything found so far
			// NOTE - this means that inference is sensitive to order of parameters
			GenericMapper mapper = new GenericMapper(typeArgumentMap, expansionTypeArguments);

			// now try to infer type arguments from the arguments
			for (int i = 0; i < arguments.length; i++) {
				CompilingExpression argument = arguments[i];
				Expression evaluated = argument.eval();
				if (evaluated.type != BasicTypeID.UNDETERMINED) {
					TypeID parameterType = mapper.map(method.getHeader().parameters[i].type);
					Map<TypeParameter, TypeID> mapping = parameterType.inferTypeParameters(evaluated.type);
					if (mapping != null)
						typeArgumentMap.putAll(mapping);
				}
			}

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
				return new MatchedCallArguments<>(
						method,
						new CallArguments(CastedExpression.Level.INVALID, TypeID.NONE, typeArguments, Expression.NONE));
			}

			typeArguments = typeArguments2;
		} else if (typeArguments == null && method.getHeader().typeParameters.length == 0) {
			typeArguments = TypeID.NONE;
		}

		GenericMapper mapper = GenericMapper.create(method.getHeader().typeParameters, typeArguments);
		T instancedMethod = (T) method.withGenericArguments(mapper);
		FunctionHeader header = instancedMethod.getHeader();

		// Use Parameters.length instead of maxParameters to get number of declared parameters
		// We could be variadic if we have 0 vararg parameters (=#parameters-1 arguments), 1 (=#parameters arguments) or more
		// We could be "normal" if we have as many args as parameters, or less (for optionals)
		boolean couldBeVariadic = header.isVariadic() && arguments.length >= header.parameters.length - 1;
		TypeID variadicType = header.getVariadicParameterType().orElse(null);

		Expression[] cArguments = new Expression[header.parameters.length];
		List<Expression> varargArguments = new ArrayList<>();
		CastedExpression.Level levelNormalCall = CastedExpression.Level.EXACT;
		CastedExpression.Level levelVarargCall = null;
		boolean hasInvalidArguments = false;

		for (int i = 0; i < cArguments.length; i++) {
			if (couldBeVariadic && i >= header.parameters.length - 1) {
				if (levelVarargCall == null)
					levelVarargCall = levelNormalCall;

				CastedExpression cArgument = arguments[i].cast(CastedEval.implicit(compiler, position, variadicType));
				varargArguments.add(cArgument.value);
				levelVarargCall = levelVarargCall.max(cArgument.level);
				hasInvalidArguments |= cArgument.level == CastedExpression.Level.INVALID;
			} else {
				TypeID parameterType = header.getParameterType(false, i);
				CastedExpression cArgument = arguments[i].cast(CastedEval.implicit(compiler, position, parameterType));
				cArguments[i] = cArgument.value;
				levelNormalCall = levelNormalCall.max(cArgument.level);
				hasInvalidArguments |= cArgument.level == CastedExpression.Level.INVALID;
			}
		}
		if (hasInvalidArguments) {
			return new MatchedCallArguments<>(instancedMethod, new CallArguments(CastedExpression.Level.INVALID, expansionTypeArguments, typeArguments, cArguments));
		}

		if (method.hasWideningConversions()) {
			if (levelNormalCall == CastedExpression.Level.EXACT)
				levelNormalCall = CastedExpression.Level.WIDENING;

			FunctionHeader originalHeader = method.asMethod().map(instance -> instance.method.getHeader()).orElse(header);

			for (int i = 0; i < originalHeader.parameters.length; i++) {
				if (!cArguments[i].type.equals(originalHeader.parameters[i].type)) {
					int finali = i;
					cArguments[i] = compiler.resolve(cArguments[i].type).findCaster(originalHeader.parameters[i].type)
							.map(caster -> caster.call(compiler.at(position), cArguments[finali], CallArguments.EMPTY))
							.orElse(cArguments[i]);
				}
			}
		}

		if (levelVarargCall != null && levelVarargCall.min(levelNormalCall) == levelVarargCall) {
			cArguments[cArguments.length - 1] = compiler.at(position).newArray(new ArrayTypeID(variadicType), varargArguments.toArray(Expression.NONE));
			levelNormalCall = levelVarargCall;
		}
		return new MatchedCallArguments<>(instancedMethod, new CallArguments(levelNormalCall, expansionTypeArguments, typeArguments, cArguments));
	}
}
