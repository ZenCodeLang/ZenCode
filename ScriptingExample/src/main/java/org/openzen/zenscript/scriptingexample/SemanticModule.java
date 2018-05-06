/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import java.util.List;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;

/**
 *
 * @author Hoofdgebruiker
 */
public class SemanticModule {
	public final boolean isValid;
	public final PackageDefinitions definitions;
	public final List<ScriptBlock> scripts;

	public SemanticModule(boolean isValid, PackageDefinitions definitions, List<ScriptBlock> scripts) {
		this.isValid = isValid;
		this.definitions = definitions;
		this.scripts = scripts;
	}
}
