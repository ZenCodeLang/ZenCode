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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFile {
	private final JavaSourceCompiler compiler;
	private final File file;
	private final String packageName;
	private final Map<String, String> imports = new HashMap<>();
	private final StringBuilder contents = new StringBuilder();
	
	public JavaSourceFile(JavaSourceCompiler compiler, File file, String packageName) {
		this.compiler = compiler;
		this.packageName = packageName;
		this.file = file;
	}
	
	public String importType(HighLevelDefinition definition) {
		return importType(compiler.getFullName(definition));
	}
	
	public String importType(String fullName) {
		String name = fullName.substring(fullName.lastIndexOf('.') + 1);
		if (imports.containsKey(name))
			return fullName;
		
		imports.put(name, fullName);
		return name;
	}
	
	public void add(HighLevelDefinition definition) {
		JavaDefinitionVisitor visitor = new JavaDefinitionVisitor(this, contents);
		definition.accept(visitor);
	}
	
	public void write() {
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
			writer.write("package ");
			writer.write(packageName);
			writer.write(";\n\n");
			
			if (this.imports.size() > 0) {
				String[] imports = this.imports.values().toArray(new String[this.imports.size()]);
				Arrays.sort(imports);

				for (String importName : imports) {
					writer.write("import ");
					writer.write(importName);
					writer.write(";");
				}

				writer.write("\n\n");
			}
			
			writer.write(contents.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
