package org.openzen.zenscript.formattershared;

import org.openzen.zenscript.codemodel.HighLevelDefinition;

public interface Importer {
	String importDefinition(HighLevelDefinition definition);
}
