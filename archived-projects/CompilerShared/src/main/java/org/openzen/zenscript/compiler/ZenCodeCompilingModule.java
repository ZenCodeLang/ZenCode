package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;

public interface ZenCodeCompilingModule {
	void addDefinition(HighLevelDefinition definition);

	void addScriptBlock(ScriptBlock script);

	void finish();
}
