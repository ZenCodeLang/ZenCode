package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

/**
 * Uniquely identifies a method and allows a limited set of information to be retrieved from it.
 */
public interface MethodSymbol {
	DefinitionSymbol getDefiningType();

	TypeSymbol getTargetType();

	Modifiers getModifiers();

	String getName();

	Optional<OperatorType> getOperator();

	FunctionHeader getHeader();

	Optional<MethodInstance> getOverrides();

	/**
	 * Evaluates this method invocation as a compile-time constant, if possible.
	 * If this is a virtual method, the first argument will be `this`. If the current type has type arguments, they will
	 * be the first arguments in the typeArguments parameter, followed by any type arguments for the method.
	 *
	 * @param typeArguments class and method type arguments
	 * @param arguments method arguments
	 * @return compile-time result, if known
	 */
	default Optional<CompileTimeConstant> evaluate(TypeID[] typeArguments, CompileTimeConstant[] arguments) {
		return Optional.empty();
	}

	/**
	 * Returns all annotations on this method. May also be applied on overriding methods, depending on the
	 * annotation.
	 *
	 * @return method annotations
	 */
	default MemberAnnotation[] getAnnotations() {
		return MemberAnnotation.NONE;
	}
}
