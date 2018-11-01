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
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaSynthesizedFunction;
import org.openzen.zenscript.javashared.JavaSynthesizedRange;
import org.openzen.zenscript.javashared.JavaSyntheticClassGenerator;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceSyntheticTypeGenerator implements JavaSyntheticClassGenerator {
	private final File directory;
	private final JavaSourceFormattingSettings settings;
	private final JavaSourceContext context;
	
	public JavaSourceSyntheticTypeGenerator(File directory, JavaSourceFormattingSettings settings, JavaSourceContext context) {
		this.directory = new File(directory, "zsynthetic");
		this.settings = settings;
		this.context = context;
	}
	
	@Override
	public void synthesizeFunction(JavaSynthesizedFunction function) {
		JavaSourceImporter importer = new JavaSourceImporter(context, function.cls);
		JavaSourceTypeVisitor typeVisitor = new JavaSourceTypeVisitor(importer, context);
		
		StringBuilder contents = new StringBuilder();
		contents.append("@FunctionalInterface\n");
		contents.append("public interface ");
		contents.append(function.cls.getName());
		JavaSourceUtils.formatTypeParameters(typeVisitor, contents, function.typeParameters, false);
		contents.append(" {\n");
		contents.append(settings.indent);
		contents.append(typeVisitor.process(function.header.getReturnType()));
		contents.append(' ');
		contents.append("invoke(");
		boolean first = true;
		for (FunctionParameter parameter : function.header.parameters) {
			if (first) {
				first = false;
			} else {
				contents.append(", ");
			}
			contents.append(typeVisitor.process(parameter.type));
			contents.append(' ');
			contents.append(parameter.name);
			if (parameter.variadic)
				contents.append("...");
		}
		contents.append(");\n");
		contents.append("}\n");
		
		writeFile(function.cls, importer, contents);
	}
	
	@Override
	public void synthesizeRange(JavaSynthesizedRange range) {
		JavaSourceImporter importer = new JavaSourceImporter(context, range.cls);
		JavaSourceTypeVisitor typeVisitor = new JavaSourceTypeVisitor(importer, context);
		
		StringBuilder contents = new StringBuilder();
		contents.append("public final class ").append(range.cls.getName()).append(" {\n");
		contents.append(settings.indent).append("public final ").append(typeVisitor.process(range.baseType)).append(" from;\n");
		contents.append(settings.indent).append("public final ").append(typeVisitor.process(range.baseType)).append(" to;\n");
		contents.append(settings.indent).append("\n");
		contents.append(settings.indent)
				.append("public ")
				.append(range.cls.getName())
				.append("(")
				.append(typeVisitor.process(range.baseType))
				.append(" from, ")
				.append(typeVisitor.process(range.baseType))
				.append(" to) {\n");
		contents.append(settings.indent).append(settings.indent).append("this.from = from;\n");
		contents.append(settings.indent).append(settings.indent).append("this.to = to;\n");
		contents.append(settings.indent).append("}\n");
		contents.append("}\n");
		
		writeFile(range.cls, importer, contents);
	}
	
	@Override
	public void synthesizeShared() {
		JavaSourceImporter importer = new JavaSourceImporter(context, JavaClass.SHARED);
		
		StringBuilder contents = new StringBuilder();
		contents.append("public final class Shared<T extends AutoCloseable> {\n");
		contents.append(settings.indent).append("private final T value;\n");
		contents.append(settings.indent).append("private int refcount = 1;\n");
		contents.append(settings.indent).append("\n");
		contents.append(settings.indent).append("public Shared(T value) {\n");
		contents.append(settings.indent).append(settings.indent).append("this.value = value;\n");
		contents.append(settings.indent).append("}\n");
		contents.append(settings.indent).append("\n");
		contents.append(settings.indent).append("public T get() {\n");
		contents.append(settings.indent).append(settings.indent).append("return value;\n");
		contents.append(settings.indent).append("}\n");
		contents.append(settings.indent).append("\n");
		contents.append(settings.indent).append("public synchronized void addRef() {\n");
		contents.append(settings.indent).append(settings.indent).append("refcount++;\n");
		contents.append(settings.indent).append("}\n");
		contents.append(settings.indent).append("\n");
		contents.append(settings.indent).append("public synchronized void release() {\n");
		contents.append(settings.indent).append(settings.indent).append("refcount--;\n");
		contents.append(settings.indent).append(settings.indent).append("if (refcount == 0) {\n");
		contents.append(settings.indent).append(settings.indent).append(settings.indent).append("try {\n");
		contents.append(settings.indent).append(settings.indent).append(settings.indent).append(settings.indent).append("value.close();\n");
		contents.append(settings.indent).append(settings.indent).append(settings.indent).append("} catch (Exception ex) {}\n");
		contents.append(settings.indent).append(settings.indent).append("}\n");
		contents.append(settings.indent).append("}\n");
		contents.append("}\n");
		
		writeFile(JavaClass.SHARED, importer, contents);
	}
	
	private void line(StringBuilder output, int level) {
		for (int i = 0; i < level; i++)
			output.append(settings.indent);
	}
	
	private void writeFile(JavaClass cls, JavaSourceImporter importer, StringBuilder contents) {
		if (!directory.exists())
			directory.mkdirs();
		
		File file = new File(directory, cls.getName() + ".java");
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
