package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.impl.BoundInstanceCallable;
import org.openzen.zenscript.codemodel.compilation.impl.CallUtilities;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InstanceCallable {
	private final List<InstanceCallableMethod> overloads;

	public InstanceCallable(List<InstanceCallableMethod> overloads) {
		this.overloads = overloads;
	}

	public Expression call(ExpressionBuilder builder, Expression instance, CompilingExpression... arguments) {
		Expression result = null;
		List<FunctionHeader> candidates = new ArrayList<>();
		boolean implicit = false;
		boolean ambiguous = false;

		for (InstanceCallableMethod method : overloads) {
			if (method.getHeader().accepts(arguments.length)) {
				CallArguments matched = CallUtilities.match(method, arguments);

				if (matched.level == CastedExpression.Level.EXACT) {
					if (result == null || implicit) {
						if (implicit) {
							candidates = new ArrayList<>();
							ambiguous = false;
						}

						result = method.call(builder, instance, matched.arguments);
						implicit = false;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				} else if (matched.level == CastedExpression.Level.IMPLICIT) {
					if (result == null) {
						result = method.call(builder, instance, matched.arguments);
						implicit = true;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				}
			}
		}

		if (ambiguous) {
			return builder.invalid(CompileErrors.ambiguousCall(candidates));
		} else if (result == null) {
			return builder.invalid(CompileErrors.noMethodMatched(overloads.stream()
					.map(AnyMethod::getHeader)
					.collect(Collectors.toList())));
		} else {
			return result;
		}
	}


	public CastedExpression cast(ExpressionBuilder builder, CastedEval cast, Expression instance, CompilingExpression... arguments) {
		Expression result = null;
		List<FunctionHeader> candidates = new ArrayList<>();
		boolean implicit = false;
		boolean ambiguous = false;

		for (InstanceCallableMethod method : overloads) {
			if (method.getHeader().accepts(arguments.length)) {
				CallArguments matched = CallUtilities.match(method, cast.type, arguments);

				if (matched.level == CastedExpression.Level.EXACT) {
					if (result == null || implicit) {
						if (implicit) {
							candidates = new ArrayList<>();
							ambiguous = false;
						}

						result = method.call(builder, instance, matched.arguments);
						implicit = false;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				} else if (matched.level == CastedExpression.Level.IMPLICIT) {
					if (result == null) {
						result = method.call(builder, instance, matched.arguments);
						implicit = true;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				}
			}
		}

		if (ambiguous) {
			return CastedExpression.invalid(builder.invalid(CompileErrors.ambiguousCall(candidates)));
		} else if (result == null) {
			return CastedExpression.invalid(builder.invalid(CompileErrors.noMethodMatched(overloads.stream()
					.map(AnyMethod::getHeader)
					.collect(Collectors.toList()))));
		} else {
			return cast.of(result);
		}
	}


	public Expression callPostfix(ExpressionBuilder builder, Expression instance) {
		return builder.invalid(CompileErrors.invalidPostfix());
	}

	public StaticCallable bind(Expression instance, TypeID[] typeArguments) {
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
