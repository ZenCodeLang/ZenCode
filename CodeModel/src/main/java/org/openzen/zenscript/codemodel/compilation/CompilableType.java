package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.context.CompilingPackage;

import java.util.List;

public interface CompilableType {
	void registerCompiling(
			List<CompilingDefinition> definitions,
			List<CompilingExpansion> expansions,
			CompilingPackage pkg,
			DefinitionCompiler compiler,
			CompilingDefinition outer
	);
}
