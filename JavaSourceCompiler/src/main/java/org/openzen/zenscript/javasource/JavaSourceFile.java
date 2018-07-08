/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

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
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFile {
	public final JavaSourceImporter importer;
	private final JavaSourceCompiler compiler;
	private final File file;
	private final JavaSourceClass cls;
	private final StringBuilder contents = new StringBuilder();
	private final ZSPackage pkg;
	
	private HighLevelDefinition mainDefinition;
	private final List<ExpansionDefinition> expansions = new ArrayList<>();
	
	private final Map<HighLevelDefinition, SemanticModule> modules = new HashMap<>();
	private final Set<String> existing = new HashSet<>();
	
	public JavaSourceFile(JavaSourceCompiler compiler, File file, JavaSourceClass cls, ZSPackage pkg) {
		this.compiler = compiler;
		this.pkg = pkg;
		this.cls = cls;
		this.file = file;
		
		importer = new JavaSourceImporter(cls);
	}
	
	public String getName() {
		return cls.name;
	}
	
	public void add(HighLevelDefinition definition, SemanticModule module) {
		if (definition instanceof ExpansionDefinition) {
			expansions.add((ExpansionDefinition)definition);
		} else if (mainDefinition != null) {
			throw new IllegalStateException("Multiple main definitions!");
		} else {
			mainDefinition = definition;
		}
		
		modules.put(definition, module);
	}
	
	public void write() {
		System.out.println("Calling write on " + file.getName());
		
		if (mainDefinition == null)
			mainDefinition = expansions.remove(0);
		
		HighLevelDefinition definition = mainDefinition;
		JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(
				compiler,
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
			
			JavaSourceClass[] imports = importer.getUsedImports();
			if (imports.length > 0) {
				for (JavaSourceClass import_ : imports) {
					if (import_.pkg.equals("java.lang"))
						continue;
					
					writer.write("import ");
					writer.write(import_.fullName);
					writer.write(";\n");
				}

				writer.write("\n");
			}
			
			writer.write(contents.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
