/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialMemberGroupExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeImplementation;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaFieldInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.linker.symbol.ISymbol;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalRegistry {
	public final ZSPackage globals = new ZSPackage("");
	private final ZSPackage rootPackage = new ZSPackage("");
	private final ZSPackage javaIo = rootPackage.getOrCreatePackage("java").getOrCreatePackage("io");
	private final ZSPackage javaLang = rootPackage.getOrCreatePackage("java").getOrCreatePackage("lang");
	
	public GlobalRegistry(ZSPackage globals) {
		JavaClassInfo jPrintStream = new JavaClassInfo("java/io/PrintStream");
		PRINTSTREAM_PRINTLN.setTag(JavaMethodInfo.class, new JavaMethodInfo(jPrintStream, "println", "(Ljava/lang/String;)V"));
		
		JavaClassInfo jSystem = new JavaClassInfo("java/lang/System");
		SYSTEM_OUT.setTag(JavaFieldInfo.class, new JavaFieldInfo(jSystem, "out", "Ljava/io/PrintStream;"));
		
		PRINTLN.caller.setTag(JavaBytecodeImplementation.class, writer -> {
			writer.getField(System.class, "out", PrintStream.class);
			writer.invokeVirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		});
	}
	
	public ZSPackage collectPackages() {
		// register packages here
		
		{
			// eg. package my.package with a class MyClass with a single native method test() returning a string
			// the visitors can then during compilation check if a method is an instance of NativeMethodMember and treat it accordingly
			ZSPackage packageMyPackage = rootPackage.getOrCreatePackage("my").getOrCreatePackage("package");
			ClassDefinition myClassDefinition = new ClassDefinition(CodePosition.NATIVE, packageMyPackage, "MyClass", Modifiers.PUBLIC, null);
			JavaClassInfo myClassInfo = new JavaClassInfo("my/test/MyClass");
			
			MethodMember member = new MethodMember(CodePosition.NATIVE, myClassDefinition, Modifiers.PUBLIC, "test", new FunctionHeader(BasicTypeID.STRING));
			member.setTag(JavaMethodInfo.class, new JavaMethodInfo(myClassInfo, "test", "()Ljava/lang/String;"));
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
	
	private final ClassDefinition PRINTSTREAM = new ClassDefinition(CodePosition.NATIVE, javaIo, "PrintStream", Modifiers.EXPORT);
	private final ClassDefinition SYSTEM = new ClassDefinition(CodePosition.NATIVE, javaLang, "System", Modifiers.EXPORT);
	private final MethodMember PRINTSTREAM_PRINTLN = new MethodMember(
			CodePosition.NATIVE,
			PRINTSTREAM,
			Modifiers.EXPORT,
			"println",
			new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(BasicTypeID.STRING)));
	
	private final FieldMember SYSTEM_OUT = new FieldMember(
			CodePosition.NATIVE,
			SYSTEM,
			Modifiers.EXPORT | Modifiers.FINAL,
			"out",
			DefinitionTypeID.forType(SYSTEM));
	
	
	private final FunctionDefinition PRINTLN = new FunctionDefinition(
			CodePosition.NATIVE,
			globals,
			"println",
			Modifiers.EXPORT,
			new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(BasicTypeID.STRING)));
	
	private class PrintlnSymbol implements ISymbol {

		@Override
		public IPartialExpression getExpression(CodePosition position, GlobalTypeRegistry types, List<ITypeID> typeArguments) {
			//return new PartialStaticMemberGroupExpression(position, PRINTLN.callerGroup);
			return new PartialMemberGroupExpression(
					position,
					new GetStaticFieldExpression(position, SYSTEM_OUT),
					PRINTSTREAM_PRINTLN,
					false);
		}

		@Override
		public ITypeID getType(CodePosition position, GlobalTypeRegistry types, List<ITypeID> typeArguments) {
			// don't be fooled! this symbol is the System.out.println bound method and thus its type is a function
			return new FunctionTypeID(PRINTSTREAM_PRINTLN.header);
		}
	}
}
