/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openzen.zencode.shared.logging.*;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationProcessor;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;

/**
 *
 * @author Hoofdgebruiker
 */
public class SemanticModule {
	public static final SemanticModule[] NONE = new SemanticModule[0];
	
	public final String name;
	public final SemanticModule[] dependencies;
	public final FunctionParameter[] parameters;
	
	public final State state;
	public final Module module;
	public final ZSPackage rootPackage;
	public final ZSPackage modulePackage;
	public final PackageDefinitions definitions;
	public final List<ScriptBlock> scripts;
	public final Map<String, ISymbol> globals = new HashMap<>();
	
	public final GlobalTypeRegistry registry;
	public final List<ExpansionDefinition> expansions;
	public final AnnotationDefinition[] annotations;
	public final IZSLogger logger;
	
	public SemanticModule(
	        Module module,
            SemanticModule[] dependencies,
            FunctionParameter[] parameters,
            State state,
            ZSPackage rootPackage,
            ZSPackage modulePackage,
            PackageDefinitions definitions,
            List<ScriptBlock> scripts,
            GlobalTypeRegistry registry,
            List<ExpansionDefinition> expansions,
            AnnotationDefinition[] annotations,
            IZSLogger logger)
	{
		this.name = module.name;
		this.module = module;
		this.dependencies = dependencies;
		this.parameters = parameters;
		
		this.state = state;
		this.rootPackage = rootPackage;
		this.modulePackage = modulePackage;
		this.definitions = definitions;
		this.scripts = scripts;
		
		this.registry = registry;
		this.expansions = expansions;
		this.annotations = annotations;
        this.logger = logger;
    }
	
	public boolean isValid() {
		return state != State.INVALID;
	}
	
	public SemanticModule normalize() {
		if (state != State.ASSEMBLED)
			throw new IllegalStateException("Module is invalid");
		
		ModuleTypeResolutionContext context = new ModuleTypeResolutionContext(registry, annotations, rootPackage, null, globals);
		AnnotationProcessor annotationProcessor = new AnnotationProcessor(context, expansions);
		List<ScriptBlock> processedScripts = new ArrayList<>();
		FileScope fileScope = new FileScope(context, expansions, globals, member -> {});
			
		for (ScriptBlock block : scripts)
			processedScripts.add(annotationProcessor.process(block).normalize(fileScope));
		
		for (HighLevelDefinition definition : definitions.getAll()) {
			annotationProcessor.process(definition);
			definition.normalize(fileScope);
		}
		
		return new SemanticModule(
				module,
				dependencies,
				parameters,
				State.NORMALIZED,
				rootPackage,
				modulePackage,
				definitions,
				processedScripts,
				registry,
				expansions,
				annotations,
                logger);
	}
	
	public ModuleContext getContext() {
		return new ModuleContext(registry, module, expansions, rootPackage);
	}
	
	public enum State {
		INVALID,
		ASSEMBLED,
		NORMALIZED,
		VALIDATED
	}
}
