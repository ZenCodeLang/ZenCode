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
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.javasource.JavaOperator;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;
import org.openzen.zenscript.javasource.tags.JavaSourceMethod;
import org.openzen.zenscript.javasource.tags.JavaSourceVariantOption;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourcePrepareDefinitionVisitor implements DefinitionVisitor<JavaSourceClass> {
	private static final Map<String, JavaNativeClass> nativeClasses = new HashMap<>();
	
	static {
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaSourceClass("java.lang", "StringBuilder"));
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
			JavaNativeClass list = new JavaNativeClass(new JavaSourceClass("java.util", "List"));
			JavaSourceClass arrayList = new JavaSourceClass("java.util", "ArrayList");
			list.addMethod("constructor", new JavaSourceMethod(arrayList, JavaSourceMethod.Kind.CONSTRUCTOR, "", false));
			list.addInstanceMethod("add", "add");
			list.addInstanceMethod("insert", "add");
			list.addInstanceMethod("remove", "remove");
			list.addInstanceMethod("indexOf", "indexOf");
			list.addInstanceMethod("getAtIndex", "get");
			list.addInstanceMethod("setAtIndex", "set");
			list.addInstanceMethod("contains", "contains");
			list.addMethod("toArray", new JavaSourceMethod((formatter, call) -> formatter.listToArray((CastExpression)call)));
			list.addInstanceMethod("length", "length");
			list.addInstanceMethod("isEmpty", "isEmpty");
			nativeClasses.put("stdlib::List", list);
		}
		
		{
			JavaNativeClass comparable = new JavaNativeClass(new JavaSourceClass("java.lang", "Comparable"));
			comparable.addInstanceMethod("compareTo", "compareTo");
			nativeClasses.put("stdlib::Comparable", comparable);
		}
		
		{
			JavaSourceClass integer = new JavaSourceClass("java.lang", "Integer");
			JavaSourceClass math = new JavaSourceClass("java.lang", "Math");
			
			JavaNativeClass cls = new JavaNativeClass(integer);
			cls.addMethod("min", new JavaSourceMethod(math, JavaSourceMethod.Kind.STATIC, "min", false));
			cls.addMethod("max", new JavaSourceMethod(math, JavaSourceMethod.Kind.STATIC, "max", false));
			cls.addMethod("toHexString", new JavaSourceMethod(integer, JavaSourceMethod.Kind.EXPANSION, "toHexString", false));
			nativeClasses.put("stdlib::Integer", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaSourceClass("java.lang", "String"));
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
			JavaSourceClass arrays = new JavaSourceClass("java.lang", "Arrays");
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
			JavaNativeClass cls = new JavaNativeClass(new JavaSourceClass("java.lang", "IllegalArgumentException"));
			cls.addConstructor("constructor", "");
			nativeClasses.put("stdlib::IllegalArgumentException", cls);
		}
		
		{
			JavaNativeClass cls = new JavaNativeClass(new JavaSourceClass("java.lang", "Exception"));
			cls.addConstructor("constructor", "");
			cls.addConstructor("constructorWithCause", "");
			nativeClasses.put("stdlib::Exception", cls);
		}
	}
	
	private final String filename;
	
	public JavaSourcePrepareDefinitionVisitor(String filename) {
		this.filename = filename;
	}
	
	@Override
	public JavaSourceClass visitClass(ClassDefinition definition) {
		return visitClassCompiled(definition, true);
	}

	@Override
	public JavaSourceClass visitInterface(InterfaceDefinition definition) {
		return visitClassCompiled(definition, true);
	}

	@Override
	public JavaSourceClass visitEnum(EnumDefinition definition) {
		return visitClassCompiled(definition, false);
	}

	@Override
	public JavaSourceClass visitStruct(StructDefinition definition) {
		return visitClassCompiled(definition, true);
	}

	@Override
	public JavaSourceClass visitFunction(FunctionDefinition definition) {
		JavaSourceClass cls = new JavaSourceClass(definition.pkg.fullName, filename);
		definition.setTag(JavaSourceClass.class, cls);
		JavaSourceMethod method = new JavaSourceMethod(cls, JavaSourceMethod.Kind.STATIC, definition.name, true);
		definition.caller.setTag(JavaSourceMethod.class, method);
		return cls;
	}

	@Override
	public JavaSourceClass visitExpansion(ExpansionDefinition definition) {
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = null;
		if (nativeTag != null) {
			nativeClass = nativeClasses.get(nativeTag.value);
		}
		
		JavaSourceClass cls = new JavaSourceClass(definition.pkg.fullName, filename);
		definition.setTag(JavaSourceClass.class, cls);
		visitExpansionMembers(definition, cls, nativeClass);
		return cls;
	}

	@Override
	public JavaSourceClass visitAlias(AliasDefinition definition) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public JavaSourceClass visitVariant(VariantDefinition variant) {
		JavaSourceClass cls = new JavaSourceClass(variant.pkg.fullName, variant.name);
		variant.setTag(JavaSourceClass.class, cls);
		
		for (VariantDefinition.Option option : variant.options) {
			JavaSourceClass variantCls = new JavaSourceClass(cls.fullName, option.name);
			option.setTag(JavaSourceVariantOption.class, new JavaSourceVariantOption(cls, variantCls));
		}
		
		visitClassMembers(variant, cls, null, false);
		return cls;
	}
	
	private JavaSourceClass visitClassCompiled(HighLevelDefinition definition, boolean startsEmpty) {
		NativeTag nativeTag = definition.getTag(NativeTag.class);
		JavaNativeClass nativeClass = nativeTag == null ? null : nativeClasses.get(nativeTag.value);
		if (nativeClass == null) {
			JavaSourceClass cls = new JavaSourceClass(definition.pkg.fullName, definition.name);
			definition.setTag(JavaSourceClass.class, cls);
			visitClassMembers(definition, cls, null, startsEmpty);
			return cls;
		} else {
			JavaSourceClass cls = new JavaSourceClass(definition.pkg.fullName, filename);
			definition.setTag(JavaSourceClass.class, nativeClass.cls);
			definition.setTag(JavaNativeClass.class, nativeClass);
			visitExpansionMembers(definition, cls, nativeClass);
			return cls;
		}
	}
	
	private void visitClassMembers(HighLevelDefinition definition, JavaSourceClass cls, JavaNativeClass nativeClass, boolean startsEmpty) {
		JavaSourcePrepareClassMethodVisitor methodVisitor = new JavaSourcePrepareClassMethodVisitor(cls, nativeClass, startsEmpty);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
	}
	
	private void visitExpansionMembers(HighLevelDefinition definition, JavaSourceClass cls, JavaNativeClass nativeClass) {
		JavaSourcePrepareExpansionMethodVisitor methodVisitor = new JavaSourcePrepareExpansionMethodVisitor(cls, nativeClass);
		for (IDefinitionMember member : definition.members) {
			member.accept(methodVisitor);
		}
	}
}
