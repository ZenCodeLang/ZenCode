package org.openzen.zenscript.compiler;

import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.SemanticModule;

public interface Target {
	ZenCodeCompiler createCompiler(SemanticModule module, IZSLogger logger);

	String getModule();

	String getName();

	boolean canRun();

	boolean canBuild();
}
