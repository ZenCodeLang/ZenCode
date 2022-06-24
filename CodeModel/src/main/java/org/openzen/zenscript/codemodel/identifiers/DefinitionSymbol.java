package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.Module;

public interface DefinitionSymbol {
	Module getModule();

	String describe();

	boolean isInterface();

	boolean isExpansion();
}
