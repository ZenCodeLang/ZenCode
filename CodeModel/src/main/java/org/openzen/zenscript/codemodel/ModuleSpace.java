package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeAnnotationDefinition;
import org.openzen.zenscript.codemodel.annotations.PreconditionAnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.globals.IGlobal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModuleSpace {
	public final ZSPackage rootPackage = new ZSPackage(null, "");
	public final ZSPackage globalsPackage = new ZSPackage(null, "");
	public final ZSPackage stdlib = new ZSPackage(rootPackage, "stdlib");

	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	private final Map<String, IGlobal> globals = new HashMap<>();
	private final List<AnnotationDefinition> annotations;
	private final Map<String, SemanticModule> modules = new HashMap<>();

	public ModuleSpace(List<AnnotationDefinition> annotations) {
		annotations.add(NativeAnnotationDefinition.INSTANCE);
		annotations.add(PreconditionAnnotationDefinition.INSTANCE);
		this.annotations = annotations;
	}

	public void addModule(String name, SemanticModule dependency) throws CompileException {
		modules.put(name, dependency);
		rootPackage.add(name, dependency.modulePackage);
		dependency.definitions.registerExpansionsTo(expansions);

		for (Map.Entry<String, IGlobal> globalEntry : dependency.globals.entrySet()) {
			if (globals.containsKey(globalEntry.getKey()))
				throw new CompileException(CodePosition.META, CompileErrors.duplicateGlobal(globalEntry.getKey()));

			globals.put(globalEntry.getKey(), globalEntry.getValue());
		}
	}

	public void addGlobal(String name, IGlobal global) {
		globals.put(name, global);
	}

	public SemanticModule getModule(String name) {
		return modules.get(name);
	}

	public ZSPackage collectPackages() {
		return rootPackage;
	}

	public List<ExpansionDefinition> collectExpansions() {
		return expansions;
	}

	public Map<String, IGlobal> collectGlobals() {
		return globals;
	}

	public List<AnnotationDefinition> getAnnotations() {
		return annotations;
	}

	public void addAnnotation(AnnotationDefinition definition) {
		annotations.add(definition);
	}
}
