/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeAnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.PreconditionAnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.storage.StorageType;

/**
 *
 * @author Hoofdgebruiker
 */
public final class ModuleSpace {
	public final ZSPackage rootPackage = new ZSPackage(null, "");
	public final GlobalTypeRegistry registry;
	public final ZSPackage globalsPackage = new ZSPackage(null, "");
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	private final Map<String, ISymbol> globals = new HashMap<>();
	private final AnnotationDefinition[] annotations;
	private final StorageType[] storageTypes;
	
	public ModuleSpace(GlobalTypeRegistry registry, List<AnnotationDefinition> annotations, StorageType[] storageTypes) {
		this.registry = registry;
		
		annotations.add(NativeAnnotationDefinition.INSTANCE);
		annotations.add(PreconditionAnnotationDefinition.INSTANCE);
		this.annotations = annotations.toArray(new AnnotationDefinition[annotations.size()]);
		this.storageTypes = storageTypes;
	}
	
	public void addModule(String name, SemanticModule dependency) throws CompileException {
		rootPackage.add(name, dependency.modulePackage);
		dependency.definitions.registerExpansionsTo(expansions);
		
		for (Map.Entry<String, ISymbol> globalEntry : dependency.globals.entrySet()) {
			if (globals.containsKey(globalEntry.getKey()))
				throw new CompileException(CodePosition.META, CompileExceptionCode.DUPLICATE_GLOBAL, "Duplicate global: " + globalEntry.getKey());
			
			globals.put(globalEntry.getKey(), globalEntry.getValue());
		}
	}
	
	public void addGlobal(String name, ISymbol global) {
		globals.put(name, global);
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
	
	public AnnotationDefinition[] getAnnotations() {
		return annotations;
	}
	
	public StorageType[] getStorageTypes() {
		return storageTypes;
	}
}
