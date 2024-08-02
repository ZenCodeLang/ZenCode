package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.impl.BoundStaticCallable;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StaticCallable {
	private final List<StaticCallableMethod> overloads;

	public StaticCallable(List<StaticCallableMethod> overloads) {
		this.overloads = overloads;
	}

	public StaticCallable(StaticCallableMethod method) {
		this.overloads = Collections.singletonList(method);
	}

	public StaticCallable map(Function<StaticCallableMethod, StaticCallableMethod> projection) {
		return new StaticCallable(overloads.stream().map(projection).collect(Collectors.toList()));
	}

	public StaticCallable union(StaticCallable other) {
		List<StaticCallableMethod> concatenated = Stream.concat(overloads.stream(), other.overloads.stream()).collect(Collectors.toList());
		return new StaticCallable(concatenated);
	}

	/**
	 * Merges the given callable into this one; skipping any overloads that are already present.
	 *
	 * @param other
	 * @return
	 */
	public StaticCallable merge(StaticCallable other) {
		List<StaticCallableMethod> overloads = new ArrayList<>(this.overloads);
		outer: for (StaticCallableMethod overload : other.overloads) {
			for (StaticCallableMethod existing : this.overloads) {
				if (overload.getHeader().isEquivalentTo(existing.getHeader()))
					continue outer;
			}

			overloads.add(overload);
		}
		return new StaticCallable(overloads);
	}

	public boolean acceptsZeroArguments() {
		for (StaticCallableMethod method : overloads) {
			if (method.getHeader().accepts(0))
				return true;
		}
		return false;
	}

	public Expression call(ExpressionCompiler compiler, CodePosition position, TypeID[] typeArguments, CompilingExpression... arguments) {
		MatchedCallArguments<StaticCallableMethod> matched = MatchedCallArguments.match(compiler, position, overloads, null, typeArguments, arguments);
		return matched.eval(compiler.at(position), this::call);
	}

	public CastedExpression casted(ExpressionCompiler compiler, CodePosition position, CastedEval cast, TypeID[] typeArguments, CompilingExpression... arguments) {
		MatchedCallArguments<StaticCallableMethod> matched = MatchedCallArguments.match(compiler, position, overloads, cast.type, typeArguments, arguments);
		return matched.cast(compiler.at(position), cast, this::call);
	}

	public MatchedCallArguments<StaticCallableMethod> match(ExpressionCompiler compiler, CodePosition position, TypeID[] typeArguments, CompilingExpression... arguments) {
		return MatchedCallArguments.match(compiler, position, overloads, null, typeArguments, arguments);
	}

	public MatchedCallArguments<StaticCallableMethod> match(ExpressionCompiler compiler, CodePosition position, CastedEval cast, TypeID[] typeArguments, CompilingExpression... arguments) {
		return MatchedCallArguments.match(compiler, position, overloads, cast.type, typeArguments, arguments);
	}

	public Optional<FunctionHeader> getSingleHeader() {
		return overloads.size() == 1 ? Optional.of(overloads.get(0).getHeader()) : Optional.empty();
	}

	public Optional<StaticCallableMethod> getSingleOverload() {
		return overloads.size() == 1 ? Optional.of(overloads.get(0)) : Optional.empty();
	}

	private Expression call(ExpressionBuilder builder, StaticCallableMethod method, CallArguments arguments) {
		return method.call(builder, arguments);
	}

	public CompilingCallable bindTypeArguments(ExpressionCompiler compiler, TypeID[] typeArguments) {
		return new BoundStaticCallable(compiler, this, typeArguments);
	}
}
