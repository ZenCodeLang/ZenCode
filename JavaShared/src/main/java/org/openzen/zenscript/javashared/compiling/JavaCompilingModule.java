package org.openzen.zenscript.javashared.compiling;

import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaContext;

import java.util.HashMap;
import java.util.Map;

public class JavaCompilingModule {
	public final JavaContext context;
	public final JavaCompiledModule module;

	private final Map<DefinitionSymbol, JavaCompilingClass> classes = new HashMap<>();

	public JavaCompilingModule(JavaContext context, JavaCompiledModule module) {
		this.context = context;
		this.module = module;
	}

	public void addClass(DefinitionSymbol definitionSymbol, JavaCompilingClass class_) {
		classes.put(definitionSymbol, class_);
		module.setClassInfo(definitionSymbol, class_.compiled);
	}

	public JavaCompilingClass getClass(DefinitionSymbol definition) {
		return classes.get(definition);
	}
}
