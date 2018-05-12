/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.javabytecode.JavaCompiler;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.linker.symbol.ISymbol;

/**
 *
 * @author Hoofdgebruiker
 */
public class SemanticModule {
	public final boolean isValid;
	public final ZSPackage pkg;
	public final PackageDefinitions definitions;
	public final List<ScriptBlock> scripts;
	public final Map<String, ISymbol> globals = new HashMap<>();
	
	public SemanticModule(boolean isValid, ZSPackage pkg, PackageDefinitions definitions, List<ScriptBlock> scripts) {
		this.isValid = isValid;
		this.pkg = pkg;
		this.definitions = definitions;
		this.scripts = scripts;
	}
	
	public JavaModule compileToJava() {
		JavaCompiler compiler = new JavaCompiler(false);
		for (HighLevelDefinition definition : definitions.getAll()) {
			compiler.addDefinition(definition);
		}
		for (ScriptBlock script : scripts) {
			compiler.addScriptBlock(script);
		}
		return compiler.finish();
	}
}
