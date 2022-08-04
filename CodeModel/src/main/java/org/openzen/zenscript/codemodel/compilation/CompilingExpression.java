package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;

import java.util.Optional;

public interface CompilingExpression {
	CompilingExpression[] NONE = new CompilingExpression[0];

	/**
	 * Compiles this expression. The return type is inferred from the expression.
	 *
	 * @return compiled expression
	 */
	Expression eval();

	/**
	 * Casts this expression to the given type, if possible.
	 *
	 * @param cast operation
	 * @return expression to be compiled
	 */
	CastedExpression cast(CastedEval cast);

	/**
	 * Attempts to make this expression callable.
	 *
	 * @return callable
	 */
	Optional<CompilingCallable> call();

	/**
	 * Finds a member from this expression.
	 * Returns an invalid expression if none could be found.
	 *
	 * @param position current position
	 * @param name member name
	 * @return member
	 */
	CompilingExpression getMember(CodePosition position, GenericName name);

	/**
	 * Compiles this expression as an lvalue with an assignment.
	 *
	 * @param value assigned value
	 * @return compiled expression
	 */
	CompilingExpression assign(CompilingExpression value);

	/**
	 * Compiles this expression as an identifier. (implemented on string literals and identifiers)
	 *
	 * @return this expression, as identifier
	 */
	default Optional<String> asStringKey() {
		return Optional.empty();
	}
}
