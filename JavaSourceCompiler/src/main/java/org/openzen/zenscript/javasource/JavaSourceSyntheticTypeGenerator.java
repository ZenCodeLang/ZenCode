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
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javashared.JavaClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceSyntheticTypeGenerator {
	private final Map<String, JavaSynthesizedClass> functions = new HashMap<>();
	private final File directory;
	private final JavaSourceFormattingSettings settings;
	
	public JavaSourceSyntheticTypeGenerator(File directory, JavaSourceFormattingSettings settings) {
		this.directory = new File(directory, "zsynthetic");
		this.settings = settings;
	}
	
	public JavaSynthesizedClass createFunction(JavaSourceTypeVisitor typeFormatter, FunctionTypeID function) {
		String signature = getFunctionSignature(function);
		if (functions.containsKey(signature))
			return functions.get(signature).withTypeParameters(extractTypeParameters(function));
		
		String className = "Function" + signature;
		JavaClass cls = new JavaClass("zsynthetic", className, JavaClass.Kind.INTERFACE);
		JavaSynthesizedClass result = new JavaSynthesizedClass(cls, extractTypeParameters(function));
		functions.put(signature, result);
		
		JavaSourceImporter importer = new JavaSourceImporter(cls);
		JavaSourceTypeVisitor typeVisitor = new JavaSourceTypeVisitor(importer, this);
		
		StringBuilder contents = new StringBuilder();
		contents.append("@FunctionalInterface\n");
		contents.append("public interface ");
		contents.append(className);
		JavaSourceUtils.formatTypeParameters(typeFormatter, contents, result.typeParameters, false);
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
		return result;
	}
	
	private TypeParameter[] extractTypeParameters(ITypeID type) {
		List<TypeParameter> result = new ArrayList<>();
		type.extractTypeParameters(result);
		return result.toArray(new TypeParameter[result.size()]);
	}
	
	private static String getFunctionSignature(FunctionTypeID type) {
		return new JavaSyntheticTypeSignatureConverter().visitFunction(type);
	}
	
	private TypeParameter[] getFunctionTypeParameters(FunctionTypeID type) {
		JavaSyntheticTypeSignatureConverter converter = new JavaSyntheticTypeSignatureConverter();
		converter.visitFunction(type);
		return converter.typeParameterList.toArray(new TypeParameter[converter.typeParameterList.size()]);
	}
}
