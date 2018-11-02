
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.javashared.JavaContext;
import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionMemberVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFile {
	public final JavaSourceImporter importer;
	private final JavaSourceCompiler compiler;
	private final JavaSourceContext context;
	private final JavaClass cls;
	private final StringBuilder contents = new StringBuilder();
	private final ZSPackage pkg;
	private final SemanticModule module;
	
	private HighLevelDefinition mainDefinition;
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	
	public JavaSourceFile(JavaSourceCompiler compiler, JavaSourceContext context, JavaClass cls, SemanticModule module, ZSPackage pkg) {
		this.compiler = compiler;
		this.context = context;
		this.pkg = pkg;
		this.cls = cls;
		this.module = module;
		
		importer = new JavaSourceImporter(context, cls);
	}
	
	public String getName() {
		return cls.getName();
	}
	
	public void add(HighLevelDefinition definition) {
		if (definition instanceof ExpansionDefinition) {
			expansions.add((ExpansionDefinition)definition);
		} else if (mainDefinition != null) {
			throw new IllegalStateException("Multiple main definitions in " + cls.internalName + "!");
		} else {
			mainDefinition = definition;
		}
	}
	
	public void prepare(JavaContext context) {
		JavaPrepareDefinitionMemberVisitor visitor = new JavaPrepareDefinitionMemberVisitor(context, context.getJavaModule(module.module));
		
		if (mainDefinition != null)
			mainDefinition.accept(visitor);
		
		for (ExpansionDefinition expansion : expansions)
			expansion.accept(visitor);
	}
	
	private boolean isEmpty(HighLevelDefinition definition) {
		JavaClass cls = context.getJavaClass(definition);
		if (!cls.empty)
			return false;
		
		if (cls.isInterface() && definition.getTag(NativeTag.class) == null)
			return false;
		
		return true;
	}
	
	public String generate() {
		if (mainDefinition == null || isEmpty(mainDefinition)) {
			if (expansions.isEmpty())
				return null;
			
			mainDefinition = expansions.remove(0);
		}
		
		HighLevelDefinition definition = mainDefinition;
		JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(
				"",
				compiler,
				context,
				context.getJavaModule(module.module),
				cls,
				this,
				contents,
				expansions,
				module);
		definition.accept(visitor);
		
		StringBuilder result = new StringBuilder();
		result.append("package ").append(pkg.fullName).append(";\n\n");
			
		boolean hasImports = false;
		for (JavaClass import_ : importer.getUsedImports()) {
			if (import_.pkg.equals("java.lang"))
				continue;
			
			result.append("import ").append(import_.fullName).append(";\n");
			hasImports = true;
		}
		
		if (hasImports)
			result.append("\n");
		
		result.append(contents.toString());
		return result.toString();
	}
}
