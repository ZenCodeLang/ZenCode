/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.javashared.JavaNativeClass;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaVariantOption;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaPrepareDefinitionVisitor implements DefinitionVisitor<JavaClass> {
	private static final Map<String, JavaNativeClass> nativeClasses = new HashMap<>();
	
	static {
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "StringBuilder", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "()V");
			cls.addConstructor("constructorWithCapacity", "(I)V");
			cls.addConstructor("constructorWithValue", "(Ljava/lang/String;)V");
			cls.addMethod("isEmpty", new JavaMethod((expression, translator) -> translator.isEmptyAsLengthZero(((CallExpression)expression).target)));
			cls.addInstanceMethod("length", "length", "()I");
			cls.addInstanceMethod("appendBool", "append", "(Z)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendByte", "append", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendSByte", "append", "(B)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendShort", "append", "(S)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendUShort", "append", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendInt", "append", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendUInt", "append", "(I)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendLong", "append", "(J)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendULong", "append", "(J)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendFloat", "append", "(F)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendDouble", "append", "(D)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendChar", "append", "(C)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("appendString", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			cls.addInstanceMethod("asString", "toString", "()Ljava/lang/String;");
			nativeClasses.put("stdlib::StringBuilder", cls);
		}
		
		{
			JavaNativeClass list = new JavaNativeClass(new JavaClass("java.util", "List", JavaClass.Kind.INTERFACE));
			JavaClass arrayList = new JavaClass("java.util", "ArrayList", JavaClass.Kind.CLASS);
			list.addMethod("constructor", JavaMethod.getNativeConstructor(arrayList, "()V"));
			list.addInstanceMethod("add", "add", "(Ljava/lang/Object;)Z"); List<?> l;
			list.addInstanceMethod("insert", "add", "(Ljava/lang/Object;I)V");
			list.addInstanceMethod("remove", "remove", "(java/lang/Object;)Z");
			list.addInstanceMethod("indexOf", "indexOf", "(java/lang/Object;)I");
			list.addInstanceMethod("lastIndexOf", "lastIndexOf", "(Ljava/lang/Object;)I");
			list.addInstanceMethod("getAtIndex", "get", "(I)Ljava/lang/Object;");
			list.addInstanceMethod("setAtIndex", "set", "(ILjava/lang/Object;)Ljava/lang/Object;");
			list.addInstanceMethod("contains", "contains", "(Ljava/lang/Object;)Z");
			list.addMethod("toArray", new JavaMethod((expression, translator) -> translator.listToArray((CastExpression)expression)));
			list.addInstanceMethod("length", "size", "()I");
			list.addInstanceMethod("isEmpty", "isEmpty", "()Z");
			list.addInstanceMethod("iterate", "iterator", "()Ljava/util/Iterator;");
			nativeClasses.put("stdlib::List", list);
		}
		
		{
			JavaNativeClass iterable = new JavaNativeClass(new JavaClass("java.lang", "Iterable", JavaClass.Kind.INTERFACE));
			iterable.addInstanceMethod("iterate", "iterator", "()Ljava/util/Iterator;");
			nativeClasses.put("stdlib::Iterable", iterable);
		}
		
		{
			JavaNativeClass iterator = new JavaNativeClass(JavaClass.ITERATOR);
			iterator.addInstanceMethod("hasNext", "hasNext", "()Z");
			iterator.addInstanceMethod("next", "next", "()Ljava/lang/Object;");
			nativeClasses.put("stdlib::Iterator", iterator);
		}
		
		{
			JavaNativeClass comparable = new JavaNativeClass(new JavaClass("java.lang", "Comparable", JavaClass.Kind.INTERFACE));
			comparable.addInstanceMethod("compareTo", "compareTo", "(Ljava/lang/Object;)I");
			nativeClasses.put("stdlib::Comparable", comparable);
		}
		
		{
			JavaClass integer = new JavaClass("java.lang", "Integer", JavaClass.Kind.CLASS);
			JavaClass math = new JavaClass("java.lang", "Math", JavaClass.Kind.CLASS);
			
			JavaNativeClass cls = new JavaNativeClass(integer);
			cls.addMethod("min", JavaMethod.getNativeStatic(math, "min", "(II)I"));
			cls.addMethod("max", JavaMethod.getNativeStatic(math, "max", "(II)I"));
			cls.addMethod("toHexString", JavaMethod.getNativeExpansion(integer, "toHexString", "(I)Ljava/lang/String;"));
			nativeClasses.put("stdlib::Integer", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "String", JavaClass.Kind.CLASS));
			cls.addMethod("contains", new JavaMethod((expression, translator) -> {
				CallExpression call = (CallExpression)expression;
				Expression str = call.target;
				Expression character = call.arguments.arguments[0];
				return translator.containsAsIndexOf(str, character);
			}));
			cls.addInstanceMethod("indexOf", "indexOf", "(I)I");
			cls.addInstanceMethod("indexOfFrom", "indexOf", "(II)I");
			cls.addInstanceMethod("lastIndexOf", "lastIndexOf", "(I)I");
			cls.addInstanceMethod("lastIndexOfFrom", "lastIndexOf", "(II)I");
			cls.addInstanceMethod("trim", "trim", "()Ljava/lang/String;");
			nativeClasses.put("stdlib::String", cls);
		}
		
		{
			JavaClass arrays = new JavaClass("java.lang", "Arrays", JavaClass.Kind.CLASS);
			JavaNativeClass cls = new JavaNativeClass(arrays);
			cls.addMethod("sort", JavaMethod.getNativeExpansion(arrays, "sort", "([Ljava/lang/Object;)[Ljava/lang/Object;"));
			cls.addMethod("sorted", new JavaMethod((expression, translator) -> {
				return translator.sorted(((CallExpression)expression).target);
			}));
			cls.addMethod("sortWithComparator", JavaMethod.getNativeExpansion(arrays, "sort", "([Ljava/lang/Object;Ljava/lang/Comparator;)[Ljava/lang/Object;"));
			cls.addMethod("sortedWithComparator", new JavaMethod((expression, translator) -> {
				return translator.sortedWithComparator(
						((CallExpression)expression).target,
						((CallExpression)expression).arguments.arguments[0]);
			}));
			cls.addMethod("copy", new JavaMethod((expression, translator) -> {
				return translator.copy(((CallExpression)expression).target);
			}));
			cls.addMethod("copyResize", JavaMethod.getNativeExpansion(arrays, "copyOf", "([Ljava/lang/Object;I)[Ljava/lang/Object;"));
			cls.addMethod("copyTo", new JavaMethod((expression, translator) -> {
				return translator.copyTo((CallExpression)expression);
			}));
			nativeClasses.put("stdlib::Arrays", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "IllegalArgumentException", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "(Ljava/lang/String;)V");
			nativeClasses.put("stdlib::IllegalArgumentException", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "Exception", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "(Ljava/lang/String;)V");
			cls.addConstructor("constructorWithCause", "(Ljava/lang/String;Ljava/lang/Throwable;)V");
			nativeClasses.put("stdlib::Exception", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "IOException", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "(Ljava/lang/String;)V");
			nativeClasses.put("io::IOException", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "Reader", JavaClass.Kind.INTERFACE));
			cls.addInstanceMethod("destruct", "close", "()V");
			cls.addInstanceMethod("readCharacter", "read", "()C");
			cls.addInstanceMethod("readArray", "read", "([C)I");
			cls.addInstanceMethod("readArraySlice", "read", "([CII)I");
			nativeClasses.put("io::Reader", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "StringReader", JavaClass.Kind.CLASS), true);
			cls.addConstructor("constructor", "(Ljava/lang/String;)V");
			cls.addInstanceMethod("destructor", "close", "()V");
			cls.addInstanceMethod("readCharacter", "read", "()C");
			cls.addInstanceMethod("readArray", "read", "([C)I");
			cls.addInstanceMethod("readSlice", "read", "([CII)I");
			nativeClasses.put("io::StringReader", cls);
		}
	}
	
	private final String filename;
	private final JavaClass outerClass;
	
	public JavaPrepareDefinitionVisitor(String filename, JavaClass outerClass) {
		this.filename = filename;
		this.outerClass = outerClass;
	}
	
	private boolean isPrepared(HighLevelDefinition definition) {
		return definition.hasTag(JavaClass.class);
	}
	
	public void prepare(ITypeID type) {
		if (!(type instanceof DefinitionTypeID))
			return;
			
		HighLevelDefinition definition = ((DefinitionTypeID)type).definition;
		definition.accept(this);
	}
	
	@Override
	public JavaClass visitClass(ClassDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitInterface(InterfaceDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		for (ITypeID baseType : definition.baseInterfaces)
			prepare(baseType);
		
		return visitClassCompiled(definition, true, JavaClass.Kind.INTERFACE);
	}

	@Override
	public JavaClass visitEnum(EnumDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		return visitClassCompiled(definition, false, JavaClass.Kind.ENUM);
	}

	@Override
	public JavaClass visitStruct(StructDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		return visitClassCompiled(definition, true, JavaClass.Kind.CLASS);
	}

	@Override
	public JavaClass visitFunction(FunctionDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		JavaClass cls = new JavaClass(definition.pkg.fullName, JavaClass.getNameFromFile(filename), JavaClass.Kind.CLASS);
		definition.setTag(JavaClass.class, cls);
		return cls;
	}

	@Override
	public JavaClass visitExpansion(ExpansionDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		if (nativeTag != null) {
			definition.setTag(JavaNativeClass.class, nativeClasses.get(nativeTag.value));
		}
		
		JavaClass cls = new JavaClass(definition.pkg.fullName, JavaClass.getNameFromFile(filename), JavaClass.Kind.CLASS);
		definition.setTag(JavaClass.class, cls);
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
			return variant.getTag(JavaClass.class);
		
		JavaClass cls = new JavaClass(variant.pkg.fullName, variant.name, JavaClass.Kind.CLASS);
		variant.setTag(JavaClass.class, cls);
		
		for (VariantDefinition.Option option : variant.options) {
			JavaClass variantCls = new JavaClass(cls, option.name, JavaClass.Kind.CLASS);
			option.setTag(JavaVariantOption.class, new JavaVariantOption(cls, variantCls));
		}
		
		return cls;
	}
	
	private JavaClass visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty, JavaClass.Kind kind) {
		if (definition.getSuperType() != null)
			prepare(definition.getSuperType());
		
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = nativeTag == null ? null : nativeClasses.get(nativeTag.value);
		JavaClass cls;
		if (nativeClass == null) {
			cls = outerClass == null ? new JavaClass(definition.pkg.fullName, definition.name, kind) : new JavaClass(outerClass, definition.name, kind);
			cls.destructible = definition.isDestructible();
			definition.setTag(JavaClass.class, cls);
		} else {
			cls = outerClass == null ? new JavaClass(definition.pkg.fullName, filename, kind) : new JavaClass(outerClass, filename, kind);
			definition.setTag(JavaClass.class, nativeClass.cls);
			definition.setTag(JavaNativeClass.class, nativeClass);
			
			if (nativeClass.nonDestructible)
				cls.destructible = false;
		}
		
		for (IDefinitionMember member : definition.members) {
			if (member instanceof InnerDefinitionMember) {
				((InnerDefinitionMember) member).innerDefinition.accept(new JavaPrepareDefinitionVisitor(filename, cls));
			}
		}
		
		return cls;
	}
}
