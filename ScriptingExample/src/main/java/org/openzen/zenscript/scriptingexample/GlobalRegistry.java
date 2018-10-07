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
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialMemberGroupExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StaticExpressionStorageTag;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalRegistry {
	public final ZSPackage globals = new ZSPackage(null, "");
	private final ZSPackage rootPackage = new ZSPackage(null, "");
	private final ZSPackage javaIo = rootPackage.getOrCreatePackage("java").getOrCreatePackage("io");
	private final ZSPackage javaLang = rootPackage.getOrCreatePackage("java").getOrCreatePackage("lang");
	
	private final Module MODULE = new Module("scriptingExample");
	private final ClassDefinition PRINTSTREAM = new ClassDefinition(CodePosition.NATIVE, MODULE, javaIo, "PrintStream", Modifiers.PUBLIC);
	private final ClassDefinition SYSTEM = new ClassDefinition(CodePosition.NATIVE, MODULE, javaLang, "System", Modifiers.PUBLIC);
	private final MethodMember PRINTSTREAM_PRINTLN;
	private final GetterMember SYSTEM_OUT;
	
	public GlobalRegistry(GlobalTypeRegistry registry, ZSPackage globals) {
		PRINTSTREAM_PRINTLN = new MethodMember(
			CodePosition.NATIVE,
			PRINTSTREAM,
			Modifiers.PUBLIC,
			"println",
			new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(StringTypeID.BORROW)),
			null);
		
		SYSTEM_OUT = new GetterMember(
			CodePosition.NATIVE,
			SYSTEM,
			Modifiers.PUBLIC | Modifiers.FINAL | Modifiers.STATIC,
			"out",
			DefinitionTypeID.forType(registry, SYSTEM).stored(AutoStorageTag.INSTANCE),
			null);
	}
	
	public void register(JavaContext context) {
		context.addModule(MODULE);
		JavaCompiledModule javaModule = context.getJavaModule(MODULE);
		
		JavaClass jSystem = new JavaClass("java.lang", "System", JavaClass.Kind.CLASS);
		javaModule.setFieldInfo(SYSTEM_OUT, new JavaField(jSystem, "out", "Ljava/io/PrintStream;"));
		
		JavaClass jPrintStream = new JavaClass("java.io", "PrintStream", JavaClass.Kind.CLASS);
		JavaMethod printstreamPrintln = JavaMethod.getNativeVirtual(jPrintStream, "println", "(Ljava/lang/String;)V");
		javaModule.setMethodInfo(PRINTSTREAM_PRINTLN, printstreamPrintln);
	}
	
	public ZSPackage collectPackages() {
		// register packages here
		
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
	
	private class PrintlnSymbol implements ISymbol {

		@Override
		public IPartialExpression getExpression(CodePosition position, BaseScope scope, StoredType[] typeArguments) {
			return new PartialMemberGroupExpression(
					position,
					scope,
					new StaticGetterExpression(position, new GetterMemberRef(scope.getTypeRegistry().getForMyDefinition(SYSTEM).stored(StaticExpressionStorageTag.INSTANCE), SYSTEM_OUT, GenericMapper.EMPTY)),
					"println",
					PRINTSTREAM_PRINTLN.ref(scope.getTypeRegistry().getForDefinition(PRINTSTREAM).stored(BorrowStorageTag.INVOCATION), GenericMapper.EMPTY),
					null,
					false);
		}

		@Override
		public TypeID getType(CodePosition position, TypeResolutionContext context, StoredType[] typeArguments) {
			return null; // not a type
		}
	}
}
