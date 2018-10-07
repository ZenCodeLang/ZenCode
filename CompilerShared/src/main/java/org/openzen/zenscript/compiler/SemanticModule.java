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
import java.util.function.Consumer;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.annotations.AnnotationProcessor;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.storage.StorageType;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class SemanticModule {
	public final String name;
	public final SemanticModule[] dependencies;
	
	private State state;
	public final Module module;
	public final ZSPackage rootPackage;
	public final ZSPackage modulePackage;
	public final PackageDefinitions definitions;
	public final List<ScriptBlock> scripts;
	public final Map<String, ISymbol> globals = new HashMap<>();
	
	public final CompilationUnit compilationUnit;
	public final List<ExpansionDefinition> expansions;
	public final AnnotationDefinition[] annotations;
	public final StorageType[] storageTypes;
	
	public SemanticModule(
			String name,
			Module module,
			SemanticModule[] dependencies,
			State state,
			ZSPackage rootPackage,
			ZSPackage modulePackage,
			PackageDefinitions definitions,
			List<ScriptBlock> scripts,
			CompilationUnit compilationUnit,
			List<ExpansionDefinition> expansions,
			AnnotationDefinition[] annotations,
			StorageType[] storageTypes)
	{
		this.name = name;
		this.module = module;
		this.dependencies = dependencies;
		
		this.state = state;
		this.rootPackage = rootPackage;
		this.modulePackage = modulePackage;
		this.definitions = definitions;
		this.scripts = scripts;
		
		this.compilationUnit = compilationUnit;
		this.expansions = expansions;
		this.annotations = annotations;
		this.storageTypes = storageTypes;
	}
	
	public boolean isValid() {
		return state != State.INVALID;
	}
	
	public SemanticModule normalize() {
		if (state != State.ASSEMBLED)
			throw new IllegalStateException("Module is invalid");
		
		ModuleTypeResolutionContext context = new ModuleTypeResolutionContext(compilationUnit.globalTypeRegistry, annotations, storageTypes, rootPackage, null, globals);
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
				name,
				module,
				dependencies,
				State.NORMALIZED,
				rootPackage,
				modulePackage,
				definitions,
				processedScripts,
				compilationUnit,
				expansions,
				annotations,
				storageTypes);
	}
	
	public boolean validate(Consumer<ValidationLogEntry> logger) {
		if (state != State.NORMALIZED)
			throw new IllegalStateException("Module is not yet normalized");
		
		Validator validator = new Validator(compilationUnit.globalTypeRegistry, expansions, annotations);
		for (ScriptBlock script : scripts) {
			validator.validate(script);
		}
		for (HighLevelDefinition definition : definitions.getAll()) {
			validator.validate(definition);
		}
		
		for (ValidationLogEntry entry : validator.getLog()) {
			logger.accept(entry);
		}
		state = validator.hasErrors() ? State.INVALID : State.VALIDATED;
		return !validator.hasErrors();
	}
	
	public void compile(ZenCodeCompiler compiler) {
		if (state != State.VALIDATED)
			throw new IllegalStateException("Module is not yet validated");
		
		compiler.addModule(this);
		for (HighLevelDefinition definition : definitions.getAll()) {
			compiler.addDefinition(definition, this);
		}
		for (ScriptBlock script : scripts) {
			compiler.addScriptBlock(script);
		}
	}
	
	public ModuleContext getContext() {
		return new ModuleContext(compilationUnit.globalTypeRegistry, module, expansions, rootPackage);
	}
	
	public enum State {
		INVALID,
		ASSEMBLED,
		NORMALIZED,
		VALIDATED
	}
}
