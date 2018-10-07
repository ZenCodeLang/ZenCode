
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.javashared.JavaContext;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.prepare.JavaPrepareDefinitionMemberVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFile {
	public final JavaSourceImporter importer;
	private final JavaSourceCompiler compiler;
	private final File file;
	private final JavaClass cls;
	private final StringBuilder contents = new StringBuilder();
	private final ZSPackage pkg;
	private final Module module;
	
	private HighLevelDefinition mainDefinition;
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	
	private final Map<HighLevelDefinition, SemanticModule> modules = new HashMap<>();
	private final Set<String> existing = new HashSet<>();
	
	public JavaSourceFile(JavaSourceCompiler compiler, File file, JavaClass cls, Module module, ZSPackage pkg) {
		this.compiler = compiler;
		this.pkg = pkg;
		this.cls = cls;
		this.module = module;
		this.file = file;
		
		importer = new JavaSourceImporter(compiler.context, cls);
	}
	
	public String getName() {
		return cls.getName();
	}
	
	public void add(HighLevelDefinition definition, SemanticModule module) {
		if (definition instanceof ExpansionDefinition) {
			expansions.add((ExpansionDefinition)definition);
		} else if (mainDefinition != null) {
			throw new IllegalStateException("Multiple main definitions in " + file + "!");
		} else {
			mainDefinition = definition;
		}
		
		modules.put(definition, module);
	}
	
	public void prepare(JavaContext context) {
		JavaPrepareDefinitionMemberVisitor visitor = new JavaPrepareDefinitionMemberVisitor(context, context.getJavaModule(module));
		
		if (mainDefinition != null)
			mainDefinition.accept(visitor);
		
		for (ExpansionDefinition expansion : expansions)
			expansion.accept(visitor);
	}
	
	private boolean isEmpty(HighLevelDefinition definition) {
		JavaClass cls = compiler.context.getJavaClass(definition);
		if (!cls.empty)
			return false;
		
		if (cls.isInterface() && definition.getTag(NativeTag.class) == null)
			return false;
		
		return true;
	}
	
	public void write() {
		System.out.println("Calling write on " + file.getName());
		
		if (mainDefinition == null || isEmpty(mainDefinition)) {
			if (expansions.isEmpty())
				return;
			
			mainDefinition = expansions.remove(0);
		}
		
		HighLevelDefinition definition = mainDefinition;
		JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(
				"",
				compiler,
				compiler.context.getJavaModule(module),
				cls,
				this,
				contents,
				expansions,
				modules);
		definition.accept(visitor);
		
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file, false)), StandardCharsets.UTF_8)) {
			writer.write("package ");
			writer.write(pkg.fullName);
			writer.write(";\n\n");
			
			boolean hasImports = false;
			for (JavaClass import_ : importer.getUsedImports()) {
				if (import_.pkg.equals("java.lang"))
					continue;

				writer.write("import ");
				writer.write(import_.fullName);
				writer.write(";\n");
				hasImports = true;
			}

			if (hasImports)
				writer.write("\n");
			
			writer.write(contents.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
