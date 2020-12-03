package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.SemanticModule;

public interface ZenCodeCompiler {
	void addModule(SemanticModule module);
	
	void finish();
	
	void run();
}
