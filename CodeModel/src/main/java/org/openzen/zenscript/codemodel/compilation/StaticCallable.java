package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.impl.BoundStaticCallable;
import org.openzen.zenscript.codemodel.compilation.impl.CallUtilities;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StaticCallable {
	private final List<StaticCallableMethod> overloads;

	public StaticCallable(List<StaticCallableMethod> overloads) {
		this.overloads = overloads;
	}

	public Expression call(ExpressionBuilder builder, CompilingExpression... arguments) {
		Expression result = null;
		List<FunctionHeader> candidates = new ArrayList<>();
		boolean implicit = false;
		boolean ambiguous = false;

		for (StaticCallableMethod method : overloads) {
			if (method.getHeader().accepts(arguments.length)) {
				CallArguments matched = CallUtilities.match(method, arguments);

				if (matched.level == CastedExpression.Level.EXACT) {
					if (result == null || implicit) {
						if (implicit) {
							candidates = new ArrayList<>();
							ambiguous = false;
						}

						result = method.call(builder, matched.arguments);
						implicit = false;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				} else if (matched.level == CastedExpression.Level.IMPLICIT) {
					if (result == null) {
						result = method.call(builder, matched.arguments);
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

	public CastedExpression casted(ExpressionBuilder builder, CastedEval cast, CompilingExpression... arguments) {
		Expression result = null;
		List<FunctionHeader> candidates = new ArrayList<>();
		boolean implicit = false;
		boolean ambiguous = false;

		for (StaticCallableMethod method : overloads) {
			if (method.getHeader().accepts(arguments.length)) {
				CallArguments matched = CallUtilities.match(method, cast.type, arguments);

				if (matched.level == CastedExpression.Level.EXACT) {
					if (result == null || implicit) {
						if (implicit) {
							candidates = new ArrayList<>();
							ambiguous = false;
						}

						result = method.call(builder, matched.arguments);
						implicit = false;
					} else {
						ambiguous = true;
					}
					candidates.add(method.getHeader());
				} else if (matched.level == CastedExpression.Level.IMPLICIT) {
					if (result == null) {
						result = method.call(builder, matched.arguments);
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

	public Optional<FunctionHeader> getSingleHeader() {
		return overloads.size() == 1 ? Optional.of(overloads.get(0).getHeader()) : Optional.empty();
	}

	public StaticCallable bindTypeArguments(TypeID[] typeArguments) {
		if (typeArguments.length == 0)
			return this;

		return new BoundStaticCallable(this, typeArguments);
	}
}
