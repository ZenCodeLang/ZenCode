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
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceSyntheticTypeGenerator {
	private final Map<FunctionTypeID, JavaSourceClass> functions = new HashMap<>();
	private final File directory;
	private final JavaSourceFormattingSettings settings;
	
	public JavaSourceSyntheticTypeGenerator(File directory, JavaSourceFormattingSettings settings) {
		this.directory = new File(directory, "zsynthetic");
		this.settings = settings;
	}
	
	public JavaSourceClass createFunction(FunctionTypeID function) {
		if (functions.containsKey(function))
			return functions.get(function);
		
		String className = createFunctionClassName(function);
		JavaSourceClass result = new JavaSourceClass(className, "zsynthetic." + className);
		functions.put(function, result);
		
		JavaSourceImporter importer = new JavaSourceImporter(new ZSPackage(null, "zsynthetic"));
		JavaSourceTypeVisitor typeVisitor = new JavaSourceTypeVisitor(importer, this);
		
		StringBuilder contents = new StringBuilder();
		contents.append("@FunctionalInterface\n");
		contents.append("public interface ");
		contents.append(className);
		contents.append(" {\n");
		contents.append(settings.indent);
		if (function.header.getNumberOfTypeParameters() > 0) {
			contents.append('<');
			for (int i = 0; i < function.header.getNumberOfTypeParameters(); i++) {
				
			}
			contents.append("> ");
		}
		contents.append(function.header.returnType.accept(typeVisitor));
		contents.append(' ');
		contents.append("invoke(");
		boolean first = true;
		for (FunctionParameter parameter : function.header.parameters) {
			if (first) {
				first = false;
			} else {
				contents.append(", ");
			}
			contents.append(parameter.type.accept(typeVisitor));
			contents.append(' ');
			contents.append(parameter.name);
			if (parameter.variadic)
				contents.append("...");
		}
		contents.append(");\n");
		contents.append("}\n");
		
		if (!directory.exists())
			directory.mkdirs();
		
		File file = new File(directory, className + ".java");
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
			writer.write("package zsynthetic;\n");
			
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
			
			writer.write('\n');
			writer.write(contents.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	private String createFunctionClassName(FunctionTypeID function) {
		StringBuilder className = new StringBuilder();
		className.append("Function");
		className.append(functions.size() + 1); // TODO: create more meaningful names
		return className.toString();
	}
}
