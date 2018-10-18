/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ScriptBlock;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ZenCodeCompilingModule {
	void addDefinition(HighLevelDefinition definition);
	
	void addScriptBlock(ScriptBlock script);
	
	void finish();
}
