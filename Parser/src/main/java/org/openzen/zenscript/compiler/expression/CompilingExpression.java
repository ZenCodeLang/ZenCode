package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.ResolvedCallable;

import java.util.Optional;

public interface CompilingExpression {
	/**
	 * Compiles this expression.
	 *
	 * The passed type is the type the expression is expected to resolve to. If the return type of
	 * the compiled expression can be implicitly casted, the compiler will make this conversion. Otherwise,
	 * a compilation error will be generated.
	 *
	 * @param type result type
	 * @return compiled expression
	 */
	Expression as(TypeID type);

	/**
	 * Compiles this expression. Similar to as(TypeID), but with the type inferred without any hints.
	 *
	 * @return compiled expression
	 */
	Expression eval();

	/**
	 * Attempts to make this expression callable.
	 *
	 * @return callable
	 */
	Optional<ResolvedCallable> call();

	/**
	 * Finds a member from this expression.
	 *
	 * @param position current position
	 * @param name member name
	 * @return member, if any could be found
	 */
	Optional<CompilingExpression> getMember(CodePosition position, GenericName name);

	/**
	 * Compiles this expression as an lvalue with an assignment.
	 *
	 * @param value assigned value
	 * @return compiled expression
	 */
	Expression assign(Expression value);

	/**
	 * Checks if this expression matches the given type.
	 * The expression may match exactly, with implicit conversions, or not at all.
	 *
	 * @param returnType expression return type to test against
	 * @return level of matching
	 */
	TypeMatch matches(TypeID returnType);

	/**
	 * Infers the type of this expression.
	 *
	 * @return inferred type, or empty if this expression is invalid or does not have an unambiguous type
	 */
	InferredType inferType();

	/**
	 * Infers the type of variables assigned to this expression.
	 * May be empty if no type is available (eg untyped local variable) or if the expression isn't assignable.
	 *
	 * @return assignable type
	 */
	default Optional<TypeID> inferAssignType() {
		return Optional.empty();
	}
}
