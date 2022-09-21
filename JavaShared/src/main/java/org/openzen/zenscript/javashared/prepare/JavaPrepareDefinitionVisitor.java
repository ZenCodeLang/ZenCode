/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingModule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public class JavaPrepareDefinitionVisitor implements DefinitionVisitor<JavaClass> {
	private final JavaCompilingModule module;
	private final Map<String, JavaNativeClass> nativeClasses = new HashMap<>();
	private final String filename;
	private final String className;
	private final JavaClass outerClass;

	{
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "StringBuilder", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "()V", "()V");
			cls.addConstructor("constructorWithCapacity", "(I)V", "(I)V");
			cls.addConstructor("constructorWithValue", "(Ljava/lang/String;)V", "(Ljava/lang/String;)V");
			cls.addMethod("isEmpty", JavaSpecialMethod.STRINGBUILDER_ISEMPTY);
			cls.addInstanceMethod("length", "length", "()I", "()I");
			cls.addInstanceMethod("appendBool", "append", "(Z)Ljava/lang/StringBuilder;", "(Z)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendByte", "append", "(I)Ljava/lang/StringBuilder;", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendSByte", "append", "(B)Ljava/lang/StringBuilder;", "(B)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendShort", "append", "(S)Ljava/lang/StringBuilder;", "(S)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendUShort", "append", "(I)Ljava/lang/StringBuilder;", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendInt", "append", "(I)Ljava/lang/StringBuilder;", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendUInt", "append", "(I)Ljava/lang/StringBuilder;", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendLong", "append", "(J)Ljava/lang/StringBuilder;", "(J)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendULong", "append", "(J)Ljava/lang/StringBuilder;", "(J)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendUSize", "append", "(I)Ljava/lang/StringBuilder;", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendFloat", "append", "(F)Ljava/lang/StringBuilder;", "(F)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendDouble", "append", "(D)Ljava/lang/StringBuilder;", "(D)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendChar", "append", "(C)Ljava/lang/StringBuilder;", "(C)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendString", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("asString", "toString", "()Ljava/lang/String;", "()Ljava/lang/String;");
			nativeClasses.put("stdlib::StringBuilder", cls);
		}

		{
			JavaNativeClass list = new JavaNativeClass(new JavaClass("java.util", "List", JavaClass.Kind.INTERFACE));
			JavaClass arrayList = new JavaClass("java.util", "ArrayList", JavaClass.Kind.CLASS);
			list.addConstructor("constructor", "()V", "()V");
			list.addInstanceMethod("add", "add", "(Ljava/lang/Object;)Z", "(TT;)Z");
			list.addInstanceMethod("insert", "add", "(ILjava/lang/Object;)V", "(ITT;)V");
			list.addInstanceMethod("remove", "remove", "(I)Ljava/lang/Object;", "(I)LTT;", true);
			list.addInstanceMethod("removeValue", "remove", "(Ljava/lang/Object;)Z", "(LTT;)Z");
			list.addInstanceMethod("indexOf", "indexOf", "(Ljava/lang/Object;)I", "(LTT;)I");
			list.addInstanceMethod("lastIndexOf", "lastIndexOf", "(Ljava/lang/Object;)I", "(LTT;)I");
			list.addInstanceMethod("getAtIndex", "get", "(I)Ljava/lang/Object;", "(I)TT;", true);
			list.addInstanceMethod("setAtIndex", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", "(ILjava/lang/Object;)TT;", true);
			list.addInstanceMethod("contains", "contains", "(Ljava/lang/Object;)Z", "(TT;)Z");
			list.addMethod("toArray", JavaSpecialMethod.LIST_TO_ARRAY);
			list.addInstanceMethod("length", "size", "()I", "()I");
			list.addInstanceMethod("isEmpty", "isEmpty", "()Z", "()Z");
			list.addInstanceMethod("iterate", "iterator", "()Ljava/util/Iterator;", "()Ljava/util/Iterator<TT;>;");
			nativeClasses.put("stdlib::List", list);
		}

		{
			JavaNativeClass iterable = new JavaNativeClass(new JavaClass("java.lang", "Iterable", JavaClass.Kind.INTERFACE));
			iterable.addInstanceMethod("iterate", "iterator", "()Ljava/util/Iterator;", "()Ljava/util/Iterator;");
			nativeClasses.put("stdlib::Iterable", iterable);
		}

		{
			JavaNativeClass iterator = new JavaNativeClass(new JavaClass("java.util", "Iterator", JavaClass.Kind.INTERFACE));
			iterator.addMethod("empty", new JavaNativeMethod(JavaClass.COLLECTIONS, JavaNativeMethod.Kind.STATIC, "emptyIterator", false, "()Ljava/lang/Iterator;", "()Ljava/lang/Iterator<TT;>;", JavaModifiers.STATIC | JavaModifiers.PUBLIC, false));
			iterator.addInstanceMethod("hasNext", "hasNext", "()Z", "()Z");
			iterator.addInstanceMethod("next", "next", "()Ljava/lang/Object;", "()TT;", true);
			nativeClasses.put("stdlib::Iterator", iterator);
		}

		{
			JavaNativeClass comparable = new JavaNativeClass(new JavaClass("java.lang", "Comparable", JavaClass.Kind.INTERFACE));
			comparable.addInstanceMethod("compareTo", "compareTo", "(Ljava/lang/Object;)I", "(TT;)I");
			nativeClasses.put("stdlib::Comparable", comparable);
		}

		{
			JavaClass integer = new JavaClass("java.lang", "Integer", JavaClass.Kind.CLASS);
			JavaClass math = new JavaClass("java.lang", "Math", JavaClass.Kind.CLASS);

			JavaNativeClass cls = new JavaNativeClass(integer);
			cls.addMethod("min", JavaNativeMethod.getNativeStatic(math, "min", "(II)I", "(II)I"));
			cls.addMethod("max", JavaNativeMethod.getNativeStatic(math, "max", "(II)I", "(II)I"));
			cls.addMethod("toHexString", JavaNativeMethod.getNativeExpansion(integer, "toHexString", "(I)Ljava/lang/String;", "(I)Ljava/lang/String;"));
			nativeClasses.put("stdlib::Integer", cls);
			nativeClasses.put("stdlib::USize", cls);
		}

		{
			JavaClass string = new JavaClass("java.lang", "String", JavaClass.Kind.CLASS);
			JavaNativeClass cls = new JavaNativeClass(string);
			cls.addMethod("contains", JavaSpecialMethod.CONTAINS_AS_INDEXOF);
			cls.addInstanceMethod("compareToIgnoreCase","compareToIgnoreCase", "(Ljava/lang/String;)I", "(Ljava/lang/String;)I");
			cls.addInstanceMethod("endsWith","endsWith", "(Ljava/lang/String;)Z", "(Ljava/lang/String;)Z");
			cls.addInstanceMethod("equalsIgnoreCase","equalsIgnoreCase", "(Ljava/lang/String;)Z", "(Ljava/lang/String;)Z");

			cls.addInstanceMethod("indexOfFrom","indexOf", "(II)I", "(II)I");
			cls.addInstanceMethod("lastIndexOf", "lastIndexOf", "(I)I", "(I)I");
			cls.addInstanceMethod("lastIndexOfFrom", "lastIndexOf", "(II)I", "(II)I");
			cls.addInstanceMethod("indexOf", "indexOf", "(I)I", "(I)I");
			cls.addInstanceMethod("indexOfString", "indexOf", "(Ljava/lang/String;)I", "(Ljava/lang/String;)I");
			cls.addInstanceMethod("indexOfStringFrom", "indexOf", "(Ljava/lang/String;I)I", "(Ljava/lang/String;I)I");
			cls.addInstanceMethod("lastIndexOfString", "lastIndexOf", "(Ljava/lang/String;)I", "(Ljava/lang/String;)I");
			cls.addInstanceMethod("lastIndexOfStringFrom", "lastIndexOf", "(Ljava/lang/String;I)I", "(Ljava/lang/String;I)I");

			cls.addInstanceMethod("replace", "replace", "(CC)Ljava/lang/String;", "(CC)Ljava/lang/String;");
			cls.addInstanceMethod("trim", "trim", "()Ljava/lang/String;", "()Ljava/lang/String;");
			cls.addInstanceMethod("startsWith", "startsWith", "(Ljava/lang/String;)Z", "(Ljava/lang/String;)Z");
			cls.addInstanceMethod("endsWith", "endsWith", "(Ljava/lang/String;)Z", "(Ljava/lang/String;)Z");
			cls.addMethod("fromAsciiBytes", JavaSpecialMethod.BYTES_ASCII_TO_STRING);
			cls.addMethod("fromUTF8Bytes", JavaSpecialMethod.BYTES_UTF8_TO_STRING);
			cls.addMethod("toAsciiBytes", JavaSpecialMethod.STRING_TO_ASCII);
			cls.addMethod("toUTF8Bytes", JavaSpecialMethod.STRING_TO_UTF8);
			nativeClasses.put("stdlib::String", cls);
		}

		{
			JavaClass arrays = JavaClass.ARRAYS;
			JavaNativeClass cls = new JavaNativeClass(arrays);
			cls.addMethod("sort", JavaNativeMethod.getNativeExpansion(arrays, "sort", "([Ljava/lang/Object;)V", "([Ljava/lang/Object;)V"));
			cls.addMethod("sorted", JavaSpecialMethod.SORTED);
			cls.addMethod("sortWithComparator", JavaNativeMethod.getNativeExpansion(arrays, "sort", "([Ljava/lang/Object;Ljava/util/Comparator;)V", "([Ljava/lang/Object;Ljava/util/Comparator;)V"));
			cls.addMethod("sortedWithComparator", JavaSpecialMethod.SORTED_WITH_COMPARATOR);
			cls.addMethod("copy", JavaSpecialMethod.ARRAY_COPY);
			cls.addMethod("copyResize", JavaSpecialMethod.ARRAY_COPY_RESIZE);
			cls.addMethod("copyTo", JavaSpecialMethod.ARRAY_COPY_TO);
			nativeClasses.put("stdlib::Arrays", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "IllegalArgumentException", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "(Ljava/lang/String;)V", "(Ljava/lang/String;)V");
			nativeClasses.put("stdlib::IllegalArgumentException", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "Exception", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "(Ljava/lang/String;)V", "(Ljava/lang/String;)V");
			cls.addConstructor("constructorWithCause", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nativeClasses.put("stdlib::Exception", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "IOException", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "(Ljava/lang/String;)V", "(Ljava/lang/String;)V");
			nativeClasses.put("io::IOException", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "Reader", JavaClass.Kind.INTERFACE));
			cls.addInstanceMethod("destruct", "close", "()V", "()V");
			cls.addInstanceMethod("readCharacter", "read", "()C", "()C");
			cls.addInstanceMethod("readArray", "read", "([C)I", "([C)I");
			cls.addInstanceMethod("readArraySlice", "read", "([CII)I", "([CII)I");
			nativeClasses.put("io::Reader", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "StringReader", JavaClass.Kind.CLASS), true);
			cls.addConstructor("constructor", "(Ljava/lang/String;)V", "(Ljava/lang/String;)V");
			cls.addInstanceMethod("destructor", "close", "()V", "()V");
			cls.addInstanceMethod("readCharacter", "read", "()C", "()C");
			cls.addInstanceMethod("readArray", "read", "([C)I", "([C)I");
			cls.addInstanceMethod("readSlice", "read", "([CII)I", "([CII)I");
			nativeClasses.put("io::StringReader", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "InputStream", JavaClass.Kind.INTERFACE), true);
			cls.addInstanceMethod("destructor", "close", "()V", "()V");
			cls.addInstanceMethod("read", "read", "()I", "()I");
			cls.addInstanceMethod("readArray", "read", "([B)I", "([B)I");
			cls.addInstanceMethod("readSlice", "read", "([BII)I", "([BII)I");
			nativeClasses.put("io::InputStream", cls);
		}

		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "OutputStream", JavaClass.Kind.INTERFACE), true);
			cls.addInstanceMethod("destructor", "close", "()V", "()V");
			cls.addInstanceMethod("write", "write", "()I", "()I");
			cls.addInstanceMethod("writeArray", "write", "([B)V", "([B)V");
			cls.addInstanceMethod("writeSlice", "write", "([BII)V", "([BII)V");
			nativeClasses.put("io::OutputStream", cls);
		}

		 {
			 final JavaClass math = new JavaClass("java.lang", "Math", JavaClass.Kind.CLASS);
			 final JavaNativeClass mathFunctions = new JavaNativeClass(math, true);

			 for (String doubleToDoubleMethodName : new String[]{"sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "toRadians", "toDegrees", "exp", "log", "log10", "sqrt", "cbrt", "ceil", "floor", "abs", "signum"}) {
				 mathFunctions.addMethod(doubleToDoubleMethodName, JavaNativeMethod.getNativeStatic(math, doubleToDoubleMethodName, "(D)D", "(D)D"));
			 }

			 mathFunctions.addMethod("minInteger", JavaNativeMethod.getNativeStatic(math, "min", "(II)I", "(II)I"));
			 mathFunctions.addMethod("maxInteger", JavaNativeMethod.getNativeStatic(math, "max", "(II)I", "(II)I"));
			 mathFunctions.addMethod("minLong", JavaNativeMethod.getNativeStatic(math, "min", "(JJ)J", "(JJ)J"));
			 mathFunctions.addMethod("maxLong", JavaNativeMethod.getNativeStatic(math, "max", "(JJ)J", "(JJ)J"));
			 mathFunctions.addMethod("minFloat", JavaNativeMethod.getNativeStatic(math, "min", "(FF)F", "(FF)F"));
			 mathFunctions.addMethod("maxFloat", JavaNativeMethod.getNativeStatic(math, "max", "(FF)F", "(FF)F"));
			 mathFunctions.addMethod("minDouble", JavaNativeMethod.getNativeStatic(math, "min", "(DD)D", "(DD)D"));
			 mathFunctions.addMethod("maxDouble", JavaNativeMethod.getNativeStatic(math, "max", "(DD)D", "(DD)D"));

			 mathFunctions.addMethod("roundDouble", JavaNativeMethod.getNativeStatic(math, "round", "(D)J", "(D)J"));
			 mathFunctions.addMethod("roundFloat", JavaNativeMethod.getNativeStatic(math, "round", "(F)J", "(F)J"));
			 mathFunctions.addMethod("absInteger", JavaNativeMethod.getNativeStatic(math, "abs", "(I)I", "(I)I"));
			 mathFunctions.addMethod("pow", JavaNativeMethod.getNativeStatic(math, "pow", "(DD)D", "(DD)D"));

			 nativeClasses.put("math::Functions", mathFunctions);
		 }
	}

	public JavaPrepareDefinitionVisitor(JavaCompilingModule module, String filename, JavaClass outerClass) {
		this(module, filename, outerClass, JavaClass.getNameFromFile(filename));
	}

	public JavaPrepareDefinitionVisitor(JavaCompilingModule module, String filename, JavaClass outerClass, String className) {
		this.module = module;
		this.filename = filename;
		this.outerClass = outerClass;
		this.className = className;
	}

	private boolean isPrepared(HighLevelDefinition definition) {
		return module.getClass(definition) != null;
	}

	public void prepare(TypeID type) {
		if (!(type instanceof DefinitionTypeID))
			return;

		TypeSymbol definition = ((DefinitionTypeID) type).definition;
		if (!(definition instanceof HighLevelDefinition))
			return;

		((HighLevelDefinition)definition).accept(this);
	}

	@Override
	public JavaClass visitClass(ClassDefinition definition) {
		if (isPrepared(definition))
			return module.getClass(definition).compiled;

		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitInterface(InterfaceDefinition definition) {
		if (isPrepared(definition))
			return module.getClass(definition).compiled;

		for (TypeID baseType : definition.baseInterfaces)
			prepare(baseType);

		return visitClassCompiled(definition, true, JavaClass.Kind.INTERFACE);
	}

	@Override
	public JavaClass visitEnum(EnumDefinition definition) {
		if (isPrepared(definition))
			return module.getClass(definition).compiled;

		return visitClassCompiled(definition, false, JavaClass.Kind.ENUM);
	}

	@Override
	public JavaClass visitStruct(StructDefinition definition) {
		if (isPrepared(definition))
			return module.getClass(definition).compiled;

		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitFunction(FunctionDefinition definition) {
		if (isPrepared(definition))
			return module.getClass(definition).compiled;

		JavaClass cls = new JavaClass(module.context.getPackageName(definition.pkg), className, JavaClass.Kind.CLASS);
		module.addClass(definition, new JavaCompilingClass(module, definition, cls, null));
		return cls;
	}

	@Override
	public JavaClass visitExpansion(ExpansionDefinition definition) {
		if (isPrepared(definition))
			return module.getClass(definition).compiled;

		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = null;
		if (nativeTag != null) {
			nativeClass = nativeClasses.get(nativeTag.value);
			module.context.setJavaNativeClass(definition, nativeClass);
		}

		JavaClass cls = new JavaClass(module.context.getPackageName(definition.pkg), className, JavaClass.Kind.CLASS);
		module.addClass(definition, new JavaCompilingClass(module, definition, cls, nativeClass));
		return cls;
	}

	@Override
	public JavaClass visitAlias(AliasDefinition definition) {
		// nothing to do
		return null;
	}

	@Override
	public JavaClass visitVariant(VariantDefinition variant) {
		if (isPrepared(variant))
			return module.getClass(variant).compiled;

		JavaClass cls = new JavaClass(module.context.getPackageName(variant.pkg), variant.name, JavaClass.Kind.CLASS);
		module.addClass(variant, new JavaCompilingClass(module, variant, cls, null));

		for (VariantDefinition.Option option : variant.options) {
			JavaClass variantCls = new JavaClass(cls, option.name, JavaClass.Kind.CLASS);
			module.module.setVariantOption(option, new JavaVariantOption(cls, variantCls));
		}

		return cls;
	}

	private JavaClass visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty, JavaClass.Kind kind) {
		if (definition.getSuperType() != null)
			prepare(definition.getSuperType());

		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = nativeTag == null ? null : nativeClasses.get(nativeTag.value);
		JavaClass cls;
		JavaCompilingClass compiling;
		if (nativeClass == null) {
			cls = outerClass == null ? new JavaClass(module.context.getPackageName(definition.pkg), definition.name, kind) : new JavaClass(outerClass, definition.name, kind);
			compiling = new JavaCompilingClass(module, definition, cls, null);
			module.addClass(definition, compiling);
		} else {
			cls = outerClass == null ? new JavaClass(module.context.getPackageName(definition.pkg), definition.name + "Expansion", kind) : new JavaClass(outerClass, definition.name + "Expansion", kind);

			compiling = new JavaCompilingClass(module, definition, nativeClass.cls, nativeClass);
			module.addClass(definition, compiling);
			module.context.setJavaExpansionClass(definition, cls);
			module.context.setJavaNativeClass(definition, nativeClass);
		}

		if (definition.getSuperType() != null) {
			definition.getSuperType().asDefinition().ifPresent(definitionTypeID -> compiling.addDependency(definitionTypeID.definition));
		}
		if (definition instanceof InterfaceDefinition) {
			InterfaceDefinition interfaceDefinition = (InterfaceDefinition) definition;
			for (TypeID baseInterface : interfaceDefinition.baseInterfaces) {
				baseInterface.asDefinition().ifPresent(definitionTypeID -> compiling.addDependency(definitionTypeID.definition));
			}
		}

		for (IDefinitionMember member : definition.members) {
			if (member instanceof InnerDefinitionMember) {
				((InnerDefinitionMember) member).innerDefinition.accept(new JavaPrepareDefinitionVisitor(module, filename, cls));
			}
			if (member instanceof ImplementationMember) {
				((ImplementationMember) member).type.asDefinition().ifPresent(definitionTypeID -> compiling.addDependency(definitionTypeID.definition));
			}
		}

		return cls;
	}
}
