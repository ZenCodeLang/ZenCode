package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;

import java.util.List;

public interface CompilableType {
	void registerCompiling(
			List<CompilingDefinition> definitions,
			List<CompilingExpansion> expansions,
			DefinitionCompiler compiler
	);

	CompilingDefinition compileAsDefinition(DefinitionCompiler compiler, HighLevelDefinition outer);
}
