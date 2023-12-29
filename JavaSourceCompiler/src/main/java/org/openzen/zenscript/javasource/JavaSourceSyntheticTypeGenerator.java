/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.javashared.*;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceSyntheticTypeGenerator implements JavaSyntheticClassGenerator {
	private final JavaSourceModule helpers;
	private final JavaSourceFormattingSettings settings;
	private final JavaSourceContext context;

	public JavaSourceSyntheticTypeGenerator(JavaSourceModule helpers, JavaSourceFormattingSettings settings, JavaSourceContext context) {
		this.helpers = helpers;
		this.settings = settings;
		this.context = context;
	}

	@Override
	public JavaMethod synthesizeFunction(JavaSynthesizedFunction function) {
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

		return new JavaNativeMethod(
				function.cls,
				JavaNativeMethod.Kind.INSTANCE,
				function.method,
				false,
				function.header.getDescriptor(),
				JavaModifiers.PUBLIC,
				function.header.getReturnType().isGeneric());
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

	private void writeFile(JavaClass cls, JavaSourceImporter importer, StringBuilder contents) {
		StringBuilder output = new StringBuilder();
		output.append("package zsynthetic;\n");

		JavaClass[] imports = importer.getUsedImports();
		if (imports.length > 0) {
			for (JavaClass import_ : imports) {
				if (import_.pkg.equals("java.lang"))
					continue;

				output.append("import ");
				output.append(import_.fullName);
				output.append(";\n");
			}

			output.append("\n");
		}

		output.append('\n');
		output.append(contents.toString());

		String target = "zsynthetic/" + cls.getName() + ".java";
		helpers.addFile(target, output.toString());
	}
}
