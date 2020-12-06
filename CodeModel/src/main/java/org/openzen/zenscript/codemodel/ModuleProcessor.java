package org.openzen.zenscript.codemodel;

public interface ModuleProcessor {
	ScriptBlock process(ScriptBlock block);

	void process(HighLevelDefinition definition);
}
