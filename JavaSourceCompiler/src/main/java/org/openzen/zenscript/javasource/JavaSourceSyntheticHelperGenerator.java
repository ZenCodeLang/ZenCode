/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.BufferedWriter;
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
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaContext;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.codemodel.type.TypeVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceSyntheticHelperGenerator {
	private final JavaContext context;
	private final File directory;
	private final JavaSourceFormattingSettings settings;
	private final Map<String, List<String>> members = new HashMap<>();
	private final JavaClass arrayHelpers = new JavaClass("zsynthetic", "ArrayHelpers", JavaClass.Kind.CLASS);
	private final Map<ArrayKind, JavaMethod> existingContains = new HashMap<>();
	private final Map<ArrayKind, JavaMethod> existingIndexOf = new HashMap<>();
	
	public JavaSourceSyntheticHelperGenerator(JavaContext context, File directory, JavaSourceFormattingSettings settings) {
		this.context = context;
		this.directory = new File(directory, "zsynthetic");
		this.settings = settings;
	}
	
	public JavaMethod createArrayContains(ArrayTypeID type) {
		ArrayKind kind = type.accept(new ArrayKindVisitor());
		if (existingContains.containsKey(kind))
			return existingContains.get(kind);
		
		String method = generateContains(kind);
		addMember(arrayHelpers, method);
		
		String descriptor = "(" + context.getDescriptor(type) + context.getDescriptor(type.elementType) + ")Z";
		JavaMethod sourceMethod = JavaMethod.getNativeExpansion(arrayHelpers, kind.containsName, descriptor);
		existingContains.put(kind, sourceMethod);
		return sourceMethod;
	}
	
	public JavaMethod createArrayIndexOf(ArrayTypeID type) {
		ArrayKind kind = type.accept(new ArrayKindVisitor());
		if (existingContains.containsKey(kind))
			return existingContains.get(kind);
		
		String method = generateContains(kind);
		addMember(arrayHelpers, method);
		
		String descriptor = "(" + context.getDescriptor(type) + ")I";
		JavaMethod sourceMethod = JavaMethod.getNativeExpansion(arrayHelpers, kind.containsName, descriptor);
		existingContains.put(kind, sourceMethod);
		return sourceMethod;
	}
	
	private String generateContains(ArrayKind kind) {
		StringBuilder method = new StringBuilder();
		method.append(settings.indent).append("public static ");
		if (kind == ArrayKind.OBJECT)
			method.append("<T> ");
		method.append("boolean ").append(kind.containsName).append("(").append(kind.type).append("[] haystack, ").append(kind.type).append(" needle) {\n");
		method.append(settings.indent).append(settings.indent).append("for (int i = 0; i < haystack.length; i++)\n");
		method.append(settings.indent).append(settings.indent).append(settings.indent);
		if (kind == ArrayKind.OBJECT)
			method.append("if (haystack[i].equals(needle))\n");
		else
			method.append("if (haystack[i] == needle)\n");
		method.append(settings.indent).append(settings.indent).append(settings.indent).append(settings.indent).append("return true;\n");
		method.append(settings.indent).append(settings.indent).append("return false;\n");
		method.append(settings.indent).append("}\n");
		return method.toString();
	}
	
	public void write() {
		for (String filename : members.keySet()) {
			StringBuilder contents = new StringBuilder();
			contents.append("package zsynthetic;\n\n");
			contents.append("public class ").append(filename).append(" {\n");
			
			List<String> methods = members.get(filename);
			for (int i = 0; i < methods.size(); i++) {
				if (i > 0)
					contents.append(settings.indent).append('\n');
				contents.append(methods.get(i));
			}
			contents.append("}\n");
			
			File target = new File(directory, filename + ".java");
			
			try (Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8))) {
				output.write(contents.toString());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void addMember(JavaClass className, String content) {
		if (!members.containsKey(className.getName()))
			members.put(className.getName(), new ArrayList<>());
		
		members.get(className.getName()).add(content);
	}
	
	private static class ArrayKindVisitor implements TypeVisitor<ArrayKind> {
		
		@Override
		public ArrayKind visitBasic(BasicTypeID basic) {
			switch (basic) {
				case BOOL:
					return ArrayKind.BOOL;
				case BYTE:
				case SBYTE:
					return ArrayKind.BYTE;
				case SHORT:
				case USHORT:
					return ArrayKind.SHORT;
				case INT:
				case UINT:
					return ArrayKind.INT;
				case LONG:
				case ULONG:
					return ArrayKind.LONG;
				case FLOAT:
					return ArrayKind.FLOAT;
				case DOUBLE:
					return ArrayKind.DOUBLE;
				case CHAR:
					return ArrayKind.CHAR;
				default:
					throw new UnsupportedOperationException("Invalid array base type: " + basic);
			}
		}
		
		@Override
		public ArrayKind visitString(StringTypeID string) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitArray(ArrayTypeID array) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitAssoc(AssocTypeID assoc) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitGenericMap(GenericMapTypeID map) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitIterator(IteratorTypeID iterator) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitFunction(FunctionTypeID function) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitDefinition(DefinitionTypeID definition) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitGeneric(GenericTypeID generic) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitRange(RangeTypeID range) {
			return ArrayKind.OBJECT;
		}

		@Override
		public ArrayKind visitOptional(OptionalTypeID type) {
			return ArrayKind.OBJECT;
		}
	}
	
	private enum ArrayKind {
		BOOL("boolean", "containsBool", "indexOfBool"),
		BYTE("byte", "containsByte", "indexOfByte"),
		SHORT("short", "containsShort", "indexOfShort"),
		INT("int", "containsInt", "indexOfInt"),
		LONG("long", "containsLong", "indexOfLong"),
		FLOAT("float", "containsFloat", "indexOfFloat"),
		DOUBLE("double", "containsDouble", "indexOfDouble"),
		OBJECT("T", "contains", "indexOf"),
		CHAR("char", "containsChar", "indexOfChar");
		
		public final String type;
		public final String containsName;
		public final String indexOfName;
		
		ArrayKind(String type, String containsName, String indexOfName) {
			this.type = type;
			this.containsName = containsName;
			this.indexOfName = indexOfName;
		}
	}
}
