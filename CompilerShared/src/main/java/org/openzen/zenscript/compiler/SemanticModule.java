/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.linker.symbol.ISymbol;

/**
 *
 * @author Hoofdgebruiker
 */
public class SemanticModule {
	public final String name;
	public final String[] dependencies;
	
	public final boolean isValid;
	public final ZSPackage pkg;
	public final PackageDefinitions definitions;
	public final List<ScriptBlock> scripts;
	public final Map<String, ISymbol> globals = new HashMap<>();
	
	public final CompilationUnit compilationUnit;
	public final List<ExpansionDefinition> expansions;
	
	public SemanticModule(
			String name,
			String[] dependencies,
			boolean isValid,
			ZSPackage pkg,
			PackageDefinitions definitions,
			List<ScriptBlock> scripts,
			CompilationUnit compilationUnit,
			List<ExpansionDefinition> expansions)
	{
		this.name = name;
		this.dependencies = dependencies;
		
		this.isValid = isValid;
		this.pkg = pkg;
		this.definitions = definitions;
		this.scripts = scripts;
		
		this.compilationUnit = compilationUnit;
		this.expansions = expansions;
	}
	
	public void compile(ZenCodeCompiler compiler) {
		for (HighLevelDefinition definition : definitions.getAll()) {
			compiler.addDefinition(definition, this);
		}
		for (ScriptBlock script : scripts) {
			compiler.addScriptBlock(script);
		}
		compiler.finish();
	}
}
