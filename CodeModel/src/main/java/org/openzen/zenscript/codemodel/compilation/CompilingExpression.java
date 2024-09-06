package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ModificationExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

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
	 * Checks if this expression can be compiled to be of the given type. Either because it's the correct kind
	 * of constant or because there was an implicit constructor.
	 *
	 * @param type type to check for
	 * @return
	 */
	boolean canConstructAs(TypeID type);

	/**
	 * Attempts to make this expression callable.
	 *
	 * @return callable
	 */
	Optional<CompilingCallable> call();

	default Optional<ModifiableExpression> asModifiable() {
		return Optional.empty();
	}

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
	 * Compiles this expression to the given type. Attempts an implicit cast.
	 *
	 * @param type target type
	 * @return compiled expression
	 */
	Expression as(TypeID type);

	/**
	 * Collects all usages and assignments of variables for SSA analysis.
	 *
	 * @param collector variable collector
	 */
	void collect(SSAVariableCollector collector);

	void linkVariables(CodeBlockStatement.VariableLinker linker);

	/**
	 * Compiles this expression as an identifier. (implemented on string literals and identifiers)
	 *
	 * @return this expression, as identifier
	 */
	default Optional<String> asStringKey() {
		return Optional.empty();
	}
}
