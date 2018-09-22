/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.compiler.CompilationUnit;
import org.openzen.zenscript.compiler.SemanticModule;

/**
 *
 * @author Hoofdgebruiker
 */
public class DeserializingModule {
	public final String name;
	public final Module module;
	
	private final SemanticModule loaded;
	private final DeserializingModule[] dependencies;
	private final ZSPackage rootPackage;
	private final ZSPackage modulePackage;
	
	private final PackageDefinitions definitions = new PackageDefinitions();
	private final List<ScriptBlock> scripts = new ArrayList<>();
	private final AnnotationDefinition[] annotations;
	private final StorageType[] storageTypes;
	
	private final List<ExpansionDefinition> expansions;
	public final ModuleContext context;
	
	public DeserializingModule(
			String name,
			GlobalTypeRegistry registry,
			DeserializingModule[] dependencies,
			ZSPackage rootPackage,
			ZSPackage modulePackage,
			AnnotationDefinition[] annotations,
			StorageType[] storageTypes)
	{
		this.name = name;
		this.module = new Module(name);
		this.loaded = null;
		this.dependencies = dependencies;
		this.rootPackage = rootPackage;
		this.modulePackage = modulePackage;
		this.annotations = annotations;
		this.storageTypes = storageTypes;
		
		expansions = new ArrayList<>();
		context = new ModuleContext(registry, module, expansions, rootPackage);
	}
	
	public DeserializingModule(SemanticModule module) 
	{
		this.name = module.name;
		this.module = module.module;
		this.loaded = module;
		this.dependencies = null;
		this.rootPackage = module.rootPackage;
		this.modulePackage = module.modulePackage;
		this.annotations = module.annotations;
		this.storageTypes = module.storageTypes;
		
		expansions = module.expansions;
		context = module.getContext();
	}
	
	public boolean hasCode() {
		return module == null;
	}
	
	public SemanticModule load(CompilationUnit compilationUnit) {
		if (loaded != null)
			return loaded;
		
		SemanticModule[] dependencies = new SemanticModule[this.dependencies.length];
		List<ExpansionDefinition> expansions = new ArrayList<>();
		
		return new SemanticModule(
				name,
				module,
				dependencies,
				SemanticModule.State.ASSEMBLED,
				rootPackage,
				modulePackage,
				definitions,
				scripts,
				compilationUnit,
				expansions,
				annotations,
				storageTypes);
	}
	
	public void add(HighLevelDefinition definition) {
		definitions.add(definition);
	}
	
	public void add(ScriptBlock script) {
		scripts.add(script);
	}
}
