package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CompilingMember {
	void linkTypes();

	/**
	 * Retrieves the compiled definition member. Only available after invocation of linkTypes.
	 *
	 * @return compiled member
	 */
	IDefinitionMember getCompiled();

	void prepare(List<CompileException> errors);

	void compile(List<CompileException> errors);

	/**
	 * Finds all dependencies that affect compilation order. Circular dependencies are not allowed!
	 *
	 * Used by interface implementations.
	 */
	default void listCompilationOrderDependencies(Set<TypeSymbol> result) {}

	/**
	 * If this member is an inner definition, returns the compiling definition; otherwise, returns empty.
	 *
	 * @return inner definition of this member, if any
	 */
	default Optional<CompilingDefinition> asInner() {
		return Optional.empty();
	}
}
