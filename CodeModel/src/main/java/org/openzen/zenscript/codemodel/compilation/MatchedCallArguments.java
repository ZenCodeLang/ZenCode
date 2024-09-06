package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.expression.WrappedCompilingExpression;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

		return methodsGroupedByMatchLevel.getOrDefault(CastedExpression.Level.INVALID, Collections.emptyList()).stream()
				.filter(i -> i.error != null)
				.findFirst()
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

		if (!method.getHeader().accepts(arguments.length)) {
			return new MatchedCallArguments<>(method, new CallArguments(
					CastedExpression.Level.INVALID,
					expansionTypeArguments,
					typeArguments,
					Expression.NONE));
		}

		// Type inference
		Optional<TypeID[]> inferred = inferTypeArguments(expansionTypeArguments, method, result, typeArguments, arguments);
		if (!inferred.isPresent()) {
			return new MatchedCallArguments<>(
					method,
					new CallArguments(CastedExpression.Level.INVALID, TypeID.NONE, typeArguments, Expression.NONE));
		}

		typeArguments = inferred.get();

		GenericMapper mapper = GenericMapper.create(method.getHeader().typeParameters, typeArguments);
		@SuppressWarnings("unchecked")
		T instancedMethod = (T) method.withGenericArguments(mapper);

		Optional<MatchedCallArguments<T>> matchedVarArg = matchVarArg(expansionTypeArguments, compiler, position, method, instancedMethod, typeArguments, arguments);

		MatchedCallArguments<T> matchedWidening;
		{
			MatchedCallArguments<T> matchedNormal = matchNormal(expansionTypeArguments, compiler, position, method, instancedMethod, typeArguments, arguments);
			matchedWidening = applyWidening(matchedNormal, expansionTypeArguments, compiler, position, method, instancedMethod, typeArguments);
		}

		if (!matchedVarArg.isPresent()) {
			return matchedWidening;
		} else {
			return Stream.of(matchedVarArg.get(), matchedWidening).min(Comparator.comparing(match -> match.arguments.level)).orElseThrow(() -> new IllegalStateException("Should never happen"));
		}
	}

	private static <T extends AnyMethod> MatchedCallArguments<T> applyWidening(
			MatchedCallArguments<T> matchedNormal,
			TypeID[] expansionTypeArguments,
			ExpressionCompiler compiler,
			CodePosition position,
			T method,
			T instancedMethod,
			TypeID[] typeArguments
	) {
		FunctionHeader header = instancedMethod.getHeader();

		if (matchedNormal.arguments.level != CastedExpression.Level.EXACT || !method.hasWideningConversions()) {
			return matchedNormal;
		}

		FunctionHeader originalHeader = method.asMethod().map(instance -> instance.method.getHeader()).orElse(header);

		Expression[] expressions = IntStream.range(0, matchedNormal.arguments.arguments.length)
				.mapToObj(i -> {
					final Expression value = matchedNormal.arguments.arguments[i];

					final TypeID originalParameterType = originalHeader.getParameterType(false, i);
					if (value.type.equals(originalParameterType)) {
						return value;
					}

					return compiler.resolve(value.type).findCaster(originalHeader.parameters[i].type)
							.map(caster -> caster.call(compiler.at(position), value, CallArguments.EMPTY))
							.orElse(value);
				}).toArray(Expression[]::new);

		if (Stream.of(expressions).anyMatch(e -> e.type == BasicTypeID.INVALID)) {
			return new MatchedCallArguments<>(
					method,
					new CallArguments(CastedExpression.Level.INVALID, expansionTypeArguments, typeArguments, expressions)
			);
		}

		return new MatchedCallArguments<>(
				method,
				new CallArguments(CastedExpression.Level.WIDENING, expansionTypeArguments, typeArguments, expressions)
		);
	}

	private static <T extends AnyMethod> MatchedCallArguments<T> matchNormal(
			TypeID[] expansionTypeArguments,
			ExpressionCompiler compiler,
			CodePosition position,
			T method,
			T instancedMethod,
			TypeID[] typeArguments,
			CompilingExpression[] arguments
	) {
		FunctionHeader header = instancedMethod.getHeader();

		// skip vararg calls here (handled in matchVarArg)
		if (arguments.length > header.parameters.length) {
			return new MatchedCallArguments<>(
					method,
					new CallArguments(CastedExpression.Level.INVALID, expansionTypeArguments, typeArguments, Expression.NONE));
		}

		CastedExpression[] providedArguments = IntStream.range(0, header.parameters.length)
				.mapToObj(i -> {
					CompilingExpression argument;
					if (i < arguments.length) {
						// parameter provided
						argument = arguments[i];
					} else if (header.getParameter(false, i).defaultValue != null) {
						// default value
						argument = new WrappedCompilingExpression(compiler, header.getParameter(false, i).defaultValue);
					} else {
						// invalid
						return CastedExpression.invalid(compiler.at(position).invalid(CompileErrors.missingParameter(header.getParameter(false, i).name)));
					}
					return argument.cast(CastedEval.implicit(compiler, position, header.getParameterType(false, i)));
				})
				.toArray(CastedExpression[]::new);

		CastedExpression.Level level = Stream.of(providedArguments)
				.map(e -> e.level)
				.max(Comparator.naturalOrder())
				.orElse(CastedExpression.Level.EXACT);

		return new MatchedCallArguments<>(
				instancedMethod,
				new CallArguments(level, expansionTypeArguments, typeArguments, Stream.of(providedArguments).map(casted -> casted.value).toArray(Expression[]::new))
		);
	}

	private static <T extends AnyMethod> Optional<MatchedCallArguments<T>> matchVarArg(
			TypeID[] expansionTypeArguments,
			ExpressionCompiler compiler,
			CodePosition position,
			T method,
			T instancedMethod,
			TypeID[] typeArguments,
			CompilingExpression[] arguments
	) {
		FunctionHeader header = instancedMethod.getHeader();

		// We only check vararg calls
		// - more arguments than parameters provided
		// - 0 vararg parameters provided (and variadic argument is NOT optional)
		if (!header.isVariadic()
				|| (arguments.length < header.parameters.length - 1 && header.getVariadicParameter().map(p -> p.defaultValue).isPresent())
		) {
			return Optional.empty();
		}

		CastedExpression[] castedExpressions = IntStream.range(0, arguments.length)
				.mapToObj(i -> arguments[i].cast(CastedEval.implicit(compiler, position, header.getParameterType(true, i))))
				.toArray(CastedExpression[]::new);

		Expression[] expressions = new Expression[header.parameters.length];
		Expression[] varargExpressions = new Expression[arguments.length - (header.parameters.length - 1)];
		IntStream.range(0, header.parameters.length - 1).forEach(i -> expressions[i] = castedExpressions[i].value);
		IntStream.range(header.parameters.length - 1, arguments.length).forEach(i -> varargExpressions[i - (header.parameters.length - 1)] = castedExpressions[i].value);

		CodePosition arrayPosition = Stream.of(varargExpressions).map(e -> e.position).reduce(CodePosition::merge).orElse(position);
		expressions[header.parameters.length - 1] = new ArrayExpression(arrayPosition, varargExpressions, header.getVariadicParameterType().orElseThrow(IllegalStateException::new));

		CastedExpression.Level level = Stream.of(castedExpressions)
				.map(e -> e.level)
				.max(Comparator.naturalOrder())
				.orElse(CastedExpression.Level.EXACT);

		return Optional.of(new MatchedCallArguments<>(
				instancedMethod,
				new CallArguments(level, expansionTypeArguments, typeArguments, expressions)
		));
	}


	private static <T extends AnyMethod> Optional<TypeID[]> inferTypeArguments(
			TypeID[] expansionTypeArguments,
			T method,
			TypeID result,
			TypeID[] typeArguments,
			CompilingExpression... arguments
	) {
		int providedTypeArguments = typeArguments == null ? 0 : typeArguments.length;

		if (providedTypeArguments == method.getHeader().typeParameters.length) {
			return Optional.of(typeArguments != null ? typeArguments : TypeID.NONE);
		}

		if(providedTypeArguments != 0 && providedTypeArguments != method.getHeader().typeParameters.length) {
			return Optional.empty();
		}

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

		TypeID[] typeArguments2 = Arrays.stream(method.getHeader().typeParameters)
				.map(typeArgumentMap::get)
				.toArray(TypeID[]::new);

		boolean allTypesResolved = Arrays.stream(typeArguments2).noneMatch(Objects::isNull);
		if (allTypesResolved) {
			return Optional.of(typeArguments2);
		}

		// TODO: improve type inference
		return Optional.empty();
	}
}
