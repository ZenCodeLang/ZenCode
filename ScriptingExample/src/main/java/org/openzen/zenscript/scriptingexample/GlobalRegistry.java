/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialMemberGroupExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.StaticExpressionStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalRegistry {
	public final ZSPackage globals = new ZSPackage(null, "");
	private final ZSPackage rootPackage = new ZSPackage(null, "");
	private final ZSPackage javaIo = rootPackage.getOrCreatePackage("java").getOrCreatePackage("io");
	private final ZSPackage javaLang = rootPackage.getOrCreatePackage("java").getOrCreatePackage("lang");
	
	public GlobalRegistry(GlobalTypeRegistry registry, ZSPackage globals) {
		PRINTSTREAM_PRINTLN = new MethodMember(
			CodePosition.NATIVE,
			PRINTSTREAM,
			Modifiers.EXPORT,
			"println",
			new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(StringTypeID.BORROW)),
			null);
		
		SYSTEM_OUT = new FieldMember(
			CodePosition.NATIVE,
			SYSTEM,
			Modifiers.EXPORT | Modifiers.FINAL,
			"out",
			null,
			DefinitionTypeID.forType(registry, SYSTEM).stored(SharedStorageTag.INSTANCE), null, 0, 0, null);
		
		JavaClass jPrintStream = new JavaClass("java.io", "PrintStream", JavaClass.Kind.CLASS);
		JavaMethod printstreamPrintln = JavaMethod.getNativeVirtual(jPrintStream, "println", "(Ljava/lang/String;)V");
		PRINTSTREAM_PRINTLN.setTag(JavaMethod.class, printstreamPrintln);
		
		JavaClass jSystem = new JavaClass("java.lang", "System", JavaClass.Kind.CLASS);
		SYSTEM_OUT.setTag(JavaField.class, new JavaField(jSystem, "out", "Ljava/io/PrintStream;"));
	}
	
	public ZSPackage collectPackages() {
		// register packages here
		
		{
			// eg. package my.package with a class MyClass with a single native method test() returning a string
			// the visitors can then during compilation check if a method is an instance of NativeMethodMember and treat it accordingly
			ZSPackage packageMyPackage = rootPackage.getOrCreatePackage("my").getOrCreatePackage("test");
			ClassDefinition myClassDefinition = new ClassDefinition(CodePosition.NATIVE, MODULE, packageMyPackage, "MyClass", Modifiers.PUBLIC, null);
			JavaClass myClassInfo = new JavaClass("my.test", "MyClass", JavaClass.Kind.CLASS);
			
			MethodMember member = new MethodMember(CodePosition.NATIVE, myClassDefinition, Modifiers.PUBLIC, "test", new FunctionHeader(StringTypeID.UNIQUE), null);
			member.setTag(JavaMethod.class, JavaMethod.getNativeVirtual(myClassInfo, "test", "()Ljava/lang/String;"));
			myClassDefinition.addMember(member);
			
			packageMyPackage.register(myClassDefinition);
		}
		
		return rootPackage;
	}
	
	public List<ExpansionDefinition> collectExpansions() {
		List<ExpansionDefinition> expansions = new ArrayList<>();
		
		return expansions;
	}
	
	public Map<String, ISymbol> collectGlobals() {
		Map<String, ISymbol> globals = new HashMap<>();
		
		// for example, let's add a println global so we can call println("Hello world!") from anywhere
		globals.put("println", new PrintlnSymbol());
		
		return globals;
	}
	
	private final Module MODULE = new Module("scriptingExample");
	private final ClassDefinition PRINTSTREAM = new ClassDefinition(CodePosition.NATIVE, MODULE, javaIo, "PrintStream", Modifiers.EXPORT);
	private final ClassDefinition SYSTEM = new ClassDefinition(CodePosition.NATIVE, MODULE, javaLang, "System", Modifiers.EXPORT);
	private final MethodMember PRINTSTREAM_PRINTLN;
	private final FieldMember SYSTEM_OUT;
	
	private class PrintlnSymbol implements ISymbol {

		@Override
		public IPartialExpression getExpression(CodePosition position, BaseScope scope, TypeID[] typeArguments) {
			return new PartialMemberGroupExpression(
					position,
					scope,
					new GetStaticFieldExpression(position, new FieldMemberRef(scope.getTypeRegistry().getForMyDefinition(SYSTEM).stored(StaticExpressionStorageTag.INSTANCE), SYSTEM_OUT, GenericMapper.EMPTY)),
					"println",
					PRINTSTREAM_PRINTLN.ref(scope.getTypeRegistry().getForDefinition(PRINTSTREAM).stored(BorrowStorageTag.INVOCATION), GenericMapper.EMPTY),
					null,
					false);
		}

		@Override
		public TypeID getType(CodePosition position, TypeResolutionContext context, TypeID[] typeArguments) {
			return null; // not a type
		}
	}
}
