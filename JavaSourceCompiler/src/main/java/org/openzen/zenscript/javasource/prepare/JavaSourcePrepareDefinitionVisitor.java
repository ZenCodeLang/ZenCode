/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.prepare;

import java.util.HashMap;
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
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.javasource.JavaOperator;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javasource.tags.JavaSourceMethod;
import org.openzen.zenscript.javasource.tags.JavaSourceVariantOption;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourcePrepareDefinitionVisitor implements DefinitionVisitor<JavaClass> {
	private static final Map<String, JavaNativeClass> nativeClasses = new HashMap<>();
	
	static {
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "StringBuilder", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "");
			cls.addConstructor("constructorWithCapacity", "");
			cls.addConstructor("constructorWithValue", "");
			cls.addMethod("isEmpty", new JavaSourceMethod((formatter, call) -> ((CallExpression)call).accept(formatter).unaryPostfix(JavaOperator.EQUALS, "length() == 0")));
			cls.addInstanceMethod("length", "length");
			cls.addInstanceMethod("appendBool", "append");
			cls.addInstanceMethod("appendByte", "append");
			cls.addInstanceMethod("appendSByte", "append");
			cls.addInstanceMethod("appendShort", "append");
			cls.addInstanceMethod("appendUShort", "append");
			cls.addInstanceMethod("appendInt", "append");
			cls.addInstanceMethod("appendUInt", "append");
			cls.addInstanceMethod("appendLong", "append");
			cls.addInstanceMethod("appendULong", "append");
			cls.addInstanceMethod("appendFloat", "append");
			cls.addInstanceMethod("appendDouble", "append");
			cls.addInstanceMethod("appendChar", "append");
			cls.addInstanceMethod("appendString", "append");
			cls.addInstanceMethod("asString", "toString");
			nativeClasses.put("stdlib::StringBuilder", cls);
		}
		
		{
			JavaNativeClass list = new JavaNativeClass(new JavaClass("java.util", "List", JavaClass.Kind.INTERFACE));
			JavaClass arrayList = new JavaClass("java.util", "ArrayList", JavaClass.Kind.CLASS);
			list.addMethod("constructor", new JavaSourceMethod(arrayList, JavaSourceMethod.Kind.CONSTRUCTOR, "", false));
			list.addInstanceMethod("add", "add");
			list.addInstanceMethod("insert", "add");
			list.addInstanceMethod("remove", "remove");
			list.addInstanceMethod("indexOf", "indexOf");
			list.addInstanceMethod("lastIndexOf", "lastIndexOf");
			list.addInstanceMethod("getAtIndex", "get");
			list.addInstanceMethod("setAtIndex", "set");
			list.addInstanceMethod("contains", "contains");
			list.addMethod("toArray", new JavaSourceMethod((formatter, call) -> formatter.listToArray((CastExpression)call)));
			list.addInstanceMethod("length", "size");
			list.addInstanceMethod("isEmpty", "isEmpty");
			list.addInstanceMethod("iterate", "iterator");
			nativeClasses.put("stdlib::List", list);
		}
		
		{
			JavaNativeClass iterable = new JavaNativeClass(new JavaClass("java.lang", "Iterable", JavaClass.Kind.INTERFACE));
			iterable.addInstanceMethod("iterate", "iterator");
			nativeClasses.put("stdlib::Iterable", iterable);
		}
		
		{
			JavaNativeClass iterator = new JavaNativeClass(JavaClass.ITERATOR);
			iterator.addInstanceMethod("hasNext", "hasNext");
			iterator.addInstanceMethod("next", "next");
			nativeClasses.put("stdlib::Iterator", iterator);
		}
		
		{
			JavaNativeClass comparable = new JavaNativeClass(new JavaClass("java.lang", "Comparable", JavaClass.Kind.INTERFACE));
			comparable.addInstanceMethod("compareTo", "compareTo");
			nativeClasses.put("stdlib::Comparable", comparable);
		}
		
		{
			JavaClass integer = new JavaClass("java.lang", "Integer", JavaClass.Kind.CLASS);
			JavaClass math = new JavaClass("java.lang", "Math", JavaClass.Kind.CLASS);
			
			JavaNativeClass cls = new JavaNativeClass(integer);
			cls.addMethod("min", new JavaSourceMethod(math, JavaSourceMethod.Kind.STATIC, "min", false));
			cls.addMethod("max", new JavaSourceMethod(math, JavaSourceMethod.Kind.STATIC, "max", false));
			cls.addMethod("toHexString", new JavaSourceMethod(integer, JavaSourceMethod.Kind.EXPANSION, "toHexString", false));
			nativeClasses.put("stdlib::Integer", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "String", JavaClass.Kind.CLASS));
			cls.addMethod("contains", new JavaSourceMethod((formatter, calle) -> {
				CallExpression call = (CallExpression)calle;
				ExpressionString str = call.arguments.arguments[0].accept(formatter);
				ExpressionString character = call.arguments.arguments[1].accept(formatter);
				return str.unaryPostfix(JavaOperator.GREATER_EQUALS, ".indexOf(" + character.value + ")");
			}));
			cls.addInstanceMethod("indexOf", "indexOf");
			cls.addInstanceMethod("indexOfFrom", "indexOf");
			cls.addInstanceMethod("lastIndexOf", "lastIndexOf");
			cls.addInstanceMethod("lastIndexOfFrom", "lastIndexOf");
			cls.addInstanceMethod("trim", "trim");
			nativeClasses.put("stdlib::String", cls);
		}
		
		{
			JavaClass arrays = new JavaClass("java.lang", "Arrays", JavaClass.Kind.CLASS);
			JavaNativeClass cls = new JavaNativeClass(arrays);
			cls.addMethod("sort", new JavaSourceMethod(arrays, JavaSourceMethod.Kind.EXPANSION, "sort", false));
			cls.addMethod("sorted", new JavaSourceMethod((formatter, calle) -> {
				Expression target = formatter.duplicable(((CallExpression)calle).target);
				ExpressionString targetString = target.accept(formatter);
				ExpressionString copy = new ExpressionString("Arrays.copyOf(" + targetString.value + ", " + targetString.value + ".length).sort()", JavaOperator.CALL);
				ExpressionString source = formatter.hoist(copy, formatter.scope.type(target.type));
				formatter.target.writeLine("Arrays.sort(" + source.value + ");");
				return source;
			}));
			cls.addMethod("sortWithComparator", new JavaSourceMethod(arrays, JavaSourceMethod.Kind.EXPANSION, "sort", false));
			cls.addMethod("sortedWithComparator", new JavaSourceMethod((formatter, calle) -> {
				Expression target = formatter.duplicable(((CallExpression)calle).target);
				ExpressionString comparator = ((CallExpression)calle).arguments.arguments[0].accept(formatter);
				ExpressionString targetString = target.accept(formatter);
				ExpressionString copy = new ExpressionString("Arrays.copyOf(" + targetString.value + ", " + targetString.value + ".length).sort()", JavaOperator.CALL);
				ExpressionString source = formatter.hoist(copy, formatter.scope.type(target.type));
				formatter.target.writeLine("Arrays.sort(" + source.value + ", " + comparator.value + ");");
				return source;
			}));
			cls.addMethod("copy", new JavaSourceMethod((formatter, calle) -> {
				Expression target = formatter.duplicable(((CallExpression)calle).target);
				ExpressionString source = target.accept(formatter);
				return new ExpressionString("Arrays.copyOf(" + source.value + ", " + source.value + ".length)", JavaOperator.CALL);
			}));
			cls.addMethod("copyResize", new JavaSourceMethod(arrays, JavaSourceMethod.Kind.EXPANSION, "copyOf", false));
			cls.addMethod("copyTo", new JavaSourceMethod((formatter, calle) -> {
				CallExpression call = (CallExpression)calle;
				Expression source = call.target;
				Expression target = call.arguments.arguments[0];
				Expression sourceOffset = call.arguments.arguments[1];
				Expression targetOffset = call.arguments.arguments[2];
				Expression length = call.arguments.arguments[3];
				return new ExpressionString("System.arraycopy("
					+ source.accept(formatter) + ", "
					+ sourceOffset.accept(formatter) + ", "
					+ target.accept(formatter) + ", "
					+ targetOffset.accept(formatter) + ", "
					+ length.accept(formatter) + ")", JavaOperator.CALL);
			}));
			nativeClasses.put("stdlib::Arrays", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "IllegalArgumentException", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "");
			nativeClasses.put("stdlib::IllegalArgumentException", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.lang", "Exception", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "");
			cls.addConstructor("constructorWithCause", "");
			nativeClasses.put("stdlib::Exception", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "IOException", JavaClass.Kind.CLASS));
			cls.addConstructor("constructor", "");
			nativeClasses.put("io::IOException", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "Reader", JavaClass.Kind.INTERFACE));
			cls.addInstanceMethod("destruct", "close");
			cls.addInstanceMethod("readCharacter", "read");
			cls.addInstanceMethod("readArray", "read");
			cls.addInstanceMethod("readArraySlice", "read");
			nativeClasses.put("io::Reader", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaClass("java.io", "StringReader", JavaClass.Kind.CLASS), true);
			cls.addConstructor("constructor", "");
			cls.addInstanceMethod("destructor", "close");
			cls.addInstanceMethod("readCharacter", "read");
			cls.addInstanceMethod("readSlice", "read");
			nativeClasses.put("io::StringReader", cls);
		}
	}
	
	private final String filename;
	private final JavaClass outerClass;
	
	public JavaSourcePrepareDefinitionVisitor(String filename, JavaClass outerClass) {
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
		
		JavaClass cls = new JavaClass(definition.pkg.fullName, filename, JavaClass.Kind.CLASS);
		definition.setTag(JavaClass.class, cls);
		JavaSourceMethod method = new JavaSourceMethod(cls, JavaSourceMethod.Kind.STATIC, definition.name, true);
		definition.caller.setTag(JavaSourceMethod.class, method);
		return cls;
	}

	@Override
	public JavaClass visitExpansion(ExpansionDefinition definition) {
		if (isPrepared(definition))
			return definition.getTag(JavaClass.class);
		
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = null;
		if (nativeTag != null) {
			nativeClass = nativeClasses.get(nativeTag.value);
		}
		
		JavaClass cls = new JavaClass(definition.pkg.fullName, filename, JavaClass.Kind.CLASS);
		definition.setTag(JavaClass.class, cls);
		visitExpansionMembers(definition, cls, nativeClass);
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
			JavaClass variantCls = new JavaClass(cls.fullName, option.name, JavaClass.Kind.CLASS);
			option.setTag(JavaSourceVariantOption.class, new JavaSourceVariantOption(cls, variantCls));
		}
		
		visitClassMembers(variant, cls, null, false);
		return cls;
	}
	
	private JavaClass visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty, JavaClass.Kind kind) {
		if (definition.getSuperType() != null)
			prepare(definition.getSuperType());
		
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = nativeTag == null ? null : nativeClasses.get(nativeTag.value);
		if (nativeClass == null) {
			JavaClass cls = outerClass == null ? new JavaClass(definition.pkg.fullName, definition.name, kind) : new JavaClass(outerClass, definition.name, kind);
			cls.destructible = definition.isDestructible();
			definition.setTag(JavaClass.class, cls);
			visitClassMembers(definition, cls, null, startsEmpty);
			return cls;
		} else {
			JavaClass cls = outerClass == null ? new JavaClass(definition.pkg.fullName, filename, kind) : new JavaClass(outerClass, filename, kind);
			definition.setTag(JavaClass.class, nativeClass.cls);
			definition.setTag(JavaNativeClass.class, nativeClass);
			visitExpansionMembers(definition, cls, nativeClass);
			
			if (nativeClass.nonDestructible)
				cls.destructible = false;
			
			return cls;
		}
	}
	
	private void visitClassMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass, boolean startsEmpty) {
		JavaSourcePrepareClassMethodVisitor methodVisitor = new JavaSourcePrepareClassMethodVisitor(this, filename, cls, nativeClass, startsEmpty);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
	}
	
	private void visitExpansionMembers(HighLevelDefinition definition, JavaClass cls, JavaNativeClass nativeClass) {
		JavaSourcePrepareExpansionMethodVisitor methodVisitor = new JavaSourcePrepareExpansionMethodVisitor(cls, nativeClass);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
	}
}
