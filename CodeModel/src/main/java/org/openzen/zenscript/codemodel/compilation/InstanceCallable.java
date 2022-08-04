package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.impl.BoundInstanceCallable;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class InstanceCallable {
	private final List<InstanceCallableMethod> overloads;

	public InstanceCallable(List<InstanceCallableMethod> overloads) {
		this.overloads = overloads;
	}

	public InstanceCallable(InstanceCallableMethod method) {
		this.overloads = Collections.singletonList(method);
	}

	public Expression call(ExpressionCompiler compiler, CodePosition position, Expression instance, TypeID[] typeArguments, CompilingExpression... arguments) {
		MatchedCallArguments<InstanceCallableMethod> matched = MatchedCallArguments.match(compiler, position, overloads, null, typeArguments, arguments);
		return matched.eval(compiler.at(position), (buildr, method, args) -> method.call(buildr, instance, args));
	}

	public CastedExpression cast(ExpressionCompiler compiler, CodePosition position, CastedEval cast, Expression instance, TypeID[] typeArguments, CompilingExpression... arguments) {
		MatchedCallArguments<InstanceCallableMethod> matched = MatchedCallArguments.match(compiler, position, overloads, cast.type, typeArguments, arguments);
		return matched.cast(compiler.at(position), cast, (buildr, method, args) -> method.call(buildr, instance, args));
	}

	public Expression callPostfix(ExpressionBuilder builder, Expression instance) {
		return builder.invalid(CompileErrors.invalidPostfix());
	}

	public CompilingCallable bind(Expression instance, TypeID[] typeArguments) {
		return new BoundInstanceCallable(this, instance, typeArguments);
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
    public Optional<MethodSymbol> findOverriddenMethod(FunctionHeader header) {
		for (InstanceCallableMethod overload : overloads) {
			if (!header.canOverride(overload.getHeader()))
				continue;

			Optional<MethodSymbol> method = overload.asMethod();
			if (!method.isPresent())
				continue;

			return method;
		}

		return Optional.empty();
	}
}
