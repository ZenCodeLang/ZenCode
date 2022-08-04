package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.impl.BoundStaticCallable;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class StaticCallable {
	private final List<StaticCallableMethod> overloads;

	public StaticCallable(List<StaticCallableMethod> overloads) {
		this.overloads = overloads;
	}

	public StaticCallable(StaticCallableMethod method) {
		this.overloads = Collections.singletonList(method);
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

	private Expression call(ExpressionBuilder builder, StaticCallableMethod method, CallArguments arguments) {
		return method.call(builder, arguments);
	}

	public CompilingCallable bindTypeArguments(TypeID[] typeArguments) {
		return new BoundStaticCallable(this, typeArguments);
	}
}
