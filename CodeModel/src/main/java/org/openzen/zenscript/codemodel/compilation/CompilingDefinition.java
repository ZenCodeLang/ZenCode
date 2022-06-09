package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CompilingDefinition {
	String getName();

	TypeSymbol getDefinition();

	boolean isInner();

	void linkTypes();

	Set<TypeSymbol> getDependencies();

	// prepareMembers will register all definition members to their
	// respective definitions, such as fields, constructors, methods...
	// It doesn't yet compile the method contents.
	void prepareMembers();

	void compileMembers(List<CompileException> errors);

	Optional<CompilingDefinition> getInner(String name);
}
