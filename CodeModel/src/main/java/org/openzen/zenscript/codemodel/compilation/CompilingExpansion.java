package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public interface CompilingExpansion {
	ExpansionDefinition getCompiling();

	void linkTypes();

	TypeID getTarget();

	void prepareMembers(List<CompileException> errors);

	void compileMembers(List<CompileException> errors);
}
