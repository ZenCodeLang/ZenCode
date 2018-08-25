/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.javashared.JavaSynthesizedClass;
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
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaSynthesizedClassNamer;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceSyntheticTypeGenerator implements JavaSyntheticClassGenerator {
	private final Map<String, JavaSynthesizedClass> functions = new HashMap<>();
	private final Map<String, JavaSynthesizedClass> ranges = new HashMap<>();
	private final File directory;
	private final JavaSourceFormattingSettings settings;
	
	public JavaSourceSyntheticTypeGenerator(File directory, JavaSourceFormattingSettings settings) {
		this.directory = new File(directory, "zsynthetic");
		this.settings = settings;
	}
	
	@Override
	public JavaSynthesizedClass synthesizeFunction(FunctionTypeID function) {
		String signature = JavaSynthesizedClassNamer.getFunctionSignature(function);
		if (functions.containsKey(signature))
			return functions.get(signature).withTypeParameters(JavaSynthesizedClassNamer.extractTypeParameters(function));
		
		JavaSynthesizedClass result = JavaSynthesizedClassNamer.createFunctionName(function);
		functions.put(signature, result);
		
		JavaSourceImporter importer = new JavaSourceImporter(result.cls);
		JavaSourceTypeVisitor typeVisitor = new JavaSourceTypeVisitor(importer, this);
		
		StringBuilder contents = new StringBuilder();
		contents.append("@FunctionalInterface\n");
		contents.append("public interface ");
		contents.append(result.cls.getName());
		JavaSourceUtils.formatTypeParameters(typeVisitor, contents, result.typeParameters, false);
		contents.append(" {\n");
		contents.append(settings.indent);
		if (function.header.getNumberOfTypeParameters() > 0) {
			contents.append('<');
			for (int i = 0; i < function.header.getNumberOfTypeParameters(); i++) {
				
			}
			contents.append("> ");
		}
		contents.append(function.header.getReturnType().accept(typeVisitor));
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
		
		writeFile(result, importer, contents);
		return result;
	}
	
	@Override
	public JavaSynthesizedClass synthesizeRange(RangeTypeID type) {
		String signature = JavaSynthesizedClassNamer.getRangeSignature(type);
		if (ranges.containsKey(signature))
			return ranges.get(signature).withTypeParameters(JavaSynthesizedClassNamer.extractTypeParameters(type));
		
		JavaSynthesizedClass result = JavaSynthesizedClassNamer.createRangeName(type);
		functions.put(signature, result);
		
		JavaSourceImporter importer = new JavaSourceImporter(result.cls);
		JavaSourceTypeVisitor typeVisitor = new JavaSourceTypeVisitor(importer, this);
		
		StringBuilder contents = new StringBuilder();
		contents.append("public final class ").append(result.cls.getName()).append(" {\n");
		contents.append(settings.indent).append("public final ").append(type.baseType.accept(typeVisitor)).append(" from;\n");
		contents.append(settings.indent).append("public final ").append(type.baseType.accept(typeVisitor)).append(" to;\n");
		contents.append(settings.indent).append("\n");
		contents.append(settings.indent)
				.append("public ")
				.append(result.cls.getName())
				.append("(")
				.append(type.baseType.accept(typeVisitor))
				.append(" from, ")
				.append(type.baseType.accept(typeVisitor))
				.append(" to) {\n");
		contents.append(settings.indent).append(settings.indent).append("this.from = from;\n");
		contents.append(settings.indent).append(settings.indent).append("this.to = to;\n");
		contents.append(settings.indent).append("}\n");
		contents.append("}\n");
		
		writeFile(result, importer, contents);
		return result;
	}
	
	private void writeFile(JavaSynthesizedClass result, JavaSourceImporter importer, StringBuilder contents) {
		if (!directory.exists())
			directory.mkdirs();
		
		File file = new File(directory, result.cls.getName() + ".java");
		try (Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
			writer.write("package zsynthetic;\n");
			
			JavaClass[] imports = importer.getUsedImports();
			if (imports.length > 0) {
				for (JavaClass import_ : imports) {
					if (import_.pkg.equals("java.lang"))
						continue;
					
					writer.write("import ");
					writer.write(import_.fullName);
					writer.write(";\n");
				}

				writer.write("\n");
			}
			
			writer.write('\n');
			writer.write(contents.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
