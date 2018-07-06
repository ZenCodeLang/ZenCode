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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.compiler.CompileScope;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.javasource.prepare.JavaSourcePrepareDefinitionVisitor;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFile {
	private final JavaSourceImporter importer;
	private final JavaSourceCompiler compiler;
	private final File file;
	private final StringBuilder contents = new StringBuilder();
	private final ZSPackage pkg;
	
	private final Map<HighLevelDefinition, SemanticModule> definitions = new HashMap<>();
	private final Set<String> existing = new HashSet<>();
	
	public JavaSourceFile(JavaSourceCompiler compiler, File file, ZSPackage pkg) {
		this.compiler = compiler;
		this.pkg = pkg;
		this.file = file;
		
		importer = new JavaSourceImporter(pkg);
	}
	
	public String getName() {
		return file.getName().substring(0, file.getName().lastIndexOf('.'));
	}
	
	public void add(HighLevelDefinition definition, SemanticModule module) {
		if (existing.contains(definition.name))
			throw new IllegalStateException("Duplicate " + definition.name);
		
		System.out.println("adding " + definition.name + " to " + file.getName());
		
		JavaSourcePrepareDefinitionVisitor prepare = new JavaSourcePrepareDefinitionVisitor(this);
		definition.accept(prepare);
		definitions.put(definition, module);
	}
	
	public void write() {
		for (Map.Entry<HighLevelDefinition, SemanticModule> entry : definitions.entrySet()) {
			HighLevelDefinition definition = entry.getKey();
			SemanticModule module = entry.getValue();
			CompileScope scope = new CompileScope(definition.access, module.compilationUnit.globalTypeRegistry, module.expansions, module.annotations);
			JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(
					compiler.settings,
					new JavaSourceFileScope(importer, compiler.typeGenerator, compiler.helperGenerator, getName(), scope),
					contents);
			definition.accept(visitor);
		}
		
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		file.delete();
		
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
			writer.write("package ");
			writer.write(pkg.fullName);
			writer.write(";\n\n");
			
			JavaSourceImporter.Import[] imports = importer.getUsedImports();
			if (imports.length > 0) {
				for (JavaSourceImporter.Import import_ : imports) {
					if (import_.actualName.startsWith("java.lang."))
						continue;
					
					writer.write("import ");
					writer.write(import_.actualName);
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
