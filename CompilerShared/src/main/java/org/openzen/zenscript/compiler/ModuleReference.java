package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.SemanticModule;

public interface ModuleReference {
	String getModuleName();
	
	SemanticModule load(ModuleRegistry modules);
}
