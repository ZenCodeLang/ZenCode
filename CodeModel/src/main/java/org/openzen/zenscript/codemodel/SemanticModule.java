package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationProcessor;
import org.openzen.zenscript.codemodel.compilation.CompileContext;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;

import java.util.*;
import java.util.stream.Stream;

public class SemanticModule {

	public static final SemanticModule[] NONE = new SemanticModule[0];

	public final String name;
	public final SemanticModule[] dependencies;
	public final FunctionParameter[] parameters;

	public final State state;
	public final ModuleSymbol module;
	public final ZSPackage rootPackage;
	public final ZSPackage modulePackage;
	public final PackageDefinitions definitions;
	public final List<ScriptBlock> scripts;
	public final Map<String, IGlobal> globals = new HashMap<>();
	public final List<ExpansionSymbol> expansions;
	public final AnnotationDefinition[] annotations;
	public final IZSLogger logger;

	public SemanticModule(
			ModuleSymbol module,
			SemanticModule[] dependencies,
			FunctionParameter[] parameters,
			State state,
			ZSPackage rootPackage,
			ZSPackage modulePackage,
			PackageDefinitions definitions,
			List<ScriptBlock> scripts,
			List<ExpansionSymbol> expansions,
			AnnotationDefinition[] annotations,
			IZSLogger logger) {
		this.name = module.name;
		this.module = module;
		this.dependencies = dependencies;
		this.parameters = parameters;

		this.state = state;
		this.rootPackage = rootPackage;
		this.modulePackage = modulePackage;
		this.definitions = definitions;
		this.scripts = scripts;

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

		AnnotationProcessor annotationProcessor = new AnnotationProcessor(Collections.unmodifiableList(expansions));
		List<ScriptBlock> processedScripts = new ArrayList<>();

		for (ScriptBlock block : scripts)
			processedScripts.add(annotationProcessor.process(block));

		Stream.concat(
				definitions.getAll().stream(),
				expansions.stream().filter(e -> e instanceof ExpansionDefinition).map(e -> (ExpansionDefinition) e)
		).forEach(annotationProcessor::process);

		return new SemanticModule(
				module,
				dependencies,
				parameters,
				State.NORMALIZED,
				rootPackage,
				modulePackage,
				definitions,
				processedScripts,
				expansions,
				annotations,
				logger);
	}

	public ModuleContext getContext() {
		return new ModuleContext(module, expansions, rootPackage);
	}

	public CompileContext createCompileContext() {
		return new CompileContext(rootPackage, modulePackage, Collections.unmodifiableList(expansions), globals, Arrays.asList(annotations));
	}

	public enum State {
		INVALID,
		ASSEMBLED,
		NORMALIZED,
		VALIDATED
	}
}
