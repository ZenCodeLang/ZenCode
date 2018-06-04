/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor.module;

import org.openzen.zenscript.compiler.SemanticModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.constructor.ConstructorException;
import org.openzen.zenscript.linker.symbol.ISymbol;

/**
 *
 * @author Hoofdgebruiker
 */
public class ModuleSpace {
	private final ZSPackage rootPackage = new ZSPackage(null, "");
	private final ZSPackage stdlib = new ZSPackage(null, "stdlib");
	public final GlobalTypeRegistry typeRegistry = new GlobalTypeRegistry(stdlib);
	public final ZSPackage globalsPackage = new ZSPackage(null, "");
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	private final Map<String, ISymbol> globals = new HashMap<>();
	
	public ModuleSpace() {
		
	}
	
	public void addModule(String name, SemanticModule dependency) {
		rootPackage.add(name, dependency.pkg);
		dependency.definitions.registerExpansionsTo(expansions);
		
		for (Map.Entry<String, ISymbol> globalEntry : dependency.globals.entrySet()) {
			if (globals.containsKey(globalEntry.getKey()))
				throw new ConstructorException("Duplicate global: " + globalEntry.getKey());
			
			globals.put(globalEntry.getKey(), globalEntry.getValue());
		}
	}
	
	public ZSPackage collectPackages() {
		return rootPackage;
	}
	
	public List<ExpansionDefinition> collectExpansions() {
		return expansions;
	}
	
	public Map<String, ISymbol> collectGlobals() {
		return globals;
	}
}
