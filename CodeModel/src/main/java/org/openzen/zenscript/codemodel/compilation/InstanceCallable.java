package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.impl.BoundInstanceCallable;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InstanceCallable {
	public final List<InstanceCallableMethod> overloads;

	public InstanceCallable(List<InstanceCallableMethod> overloads) {
		this.overloads = overloads;
	}

	public InstanceCallable(InstanceCallableMethod method) {
		this.overloads = Collections.singletonList(method);
	}

	public InstanceCallable union(InstanceCallable other) {
		List<InstanceCallableMethod> concatenated = Stream.concat(overloads.stream(), other.overloads.stream()).collect(Collectors.toList());
		return new InstanceCallable(concatenated);
	}

	public Expression call(ExpressionCompiler compiler, CodePosition position, Expression instance, TypeID[] typeArguments, CompilingExpression... arguments) {
		MatchedCallArguments<InstanceCallableMethod> matched = MatchedCallArguments.match(compiler, position, overloads, null, typeArguments, arguments);
		if (matched.requiresWidenedInstance(instance.type)) {
			Expression originalInstance = instance;
			instance = compiler.resolve(instance.type).findCaster(matched.getWidenedInstanceType())
					.map(caster -> caster.call(compiler.at(position), originalInstance, CallArguments.EMPTY))
					.orElseThrow(() -> new IllegalStateException("No widening conversion found for " + originalInstance.type + " to " + matched.getWidenedInstanceType()));
		}
		Expression finalInstance = instance;
		return matched.eval(compiler.at(position), (buildr, method, args) -> method.call(buildr, finalInstance, args));
	}

	public CastedExpression cast(ExpressionCompiler compiler, CodePosition position, CastedEval cast, Expression instance, TypeID[] typeArguments, CompilingExpression... arguments) {
		MatchedCallArguments<InstanceCallableMethod> matched = MatchedCallArguments.match(compiler, position, overloads, cast.type, typeArguments, arguments);
		return matched.cast(compiler.at(position), cast, (buildr, method, args) -> method.call(buildr, instance, args));
	}

	public Expression callPostfix(ExpressionBuilder builder, Expression instance) {
		return builder.invalid(CompileErrors.invalidPostfix());
	}

	public CompilingCallable bind(ExpressionCompiler compiler, Expression instance, TypeID[] typeArguments) {
		return new BoundInstanceCallable(compiler, this, instance, typeArguments);
	}

	/**
	 * Finds the method that is being overridden. If this callable cannot be overridden (eg. it's not a method) or
	 * doesn't match the given header, returns empty.
	 *
	 * The provided header may or may not have types defined on its parameters.
	 *
	 * @param header header as defined in the overriding method
	 * @return
	 */
    public Optional<MethodInstance> findOverriddenMethod(TypeResolver resolver, FunctionHeader header) {
		for (InstanceCallableMethod overload : overloads) {
			if (!header.canOverride(resolver, overload.getHeader()))
				continue;

			Optional<MethodInstance> method = overload.asMethod();
			if (!method.isPresent())
				continue;

			return method;
		}

		return Optional.empty();
	}
}
