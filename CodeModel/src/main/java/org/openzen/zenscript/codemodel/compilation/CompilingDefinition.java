package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CompilingDefinition {
	CompilingPackage getPackage();

	String getName();

	HighLevelDefinition getDefinition();

	boolean isInner();

	void linkTypes();

	Set<TypeSymbol> getDependencies();

	// prepareMembers will register all definition members to their
	// respective definitions, such as fields, constructors, methods...
	// It doesn't yet compile the method contents.
	void prepareMembers(List<CompileException> errors);

	void compileMembers(List<CompileException> errors);

	Optional<CompilingDefinition> getInner(String name);
}
