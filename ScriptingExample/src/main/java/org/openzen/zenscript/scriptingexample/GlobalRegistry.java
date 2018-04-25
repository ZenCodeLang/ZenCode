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
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialMemberGroupExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.symbol.ISymbol;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class GlobalRegistry {
	
	
	public ZSPackage collectPackages() {
		ZSPackage rootPackage = new ZSPackage();
		
		// register packages here
		
		{
			// eg. package my.package with a class MyClass with a single native method test() returning a string
			// the visitors can then during compilation check if a method is an instance of NativeMethodMember and treat it accordingly
			ClassDefinition myClassDefinition = new ClassDefinition("MyClass", Modifiers.MODIFIER_PUBLIC, null);
			myClassDefinition.addMember(new NativeMethodMember(Modifiers.MODIFIER_PUBLIC, "test", new FunctionHeader(BasicTypeID.STRING), "Lmy/test/MyClass;", "()Ljava/lang/String;"));

			ZSPackage packageMyPackage = rootPackage.getOrCreatePackage("my").getOrCreatePackage("package");
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
	
	private static final ClassDefinition printStream = new ClassDefinition("PrintStream", Modifiers.MODIFIER_EXPORT, null);
	private static final NativeMethodMember printStreamPrintln = new NativeMethodMember(
			Modifiers.MODIFIER_EXPORT,
			"println",
			new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(BasicTypeID.STRING)),
			"Ljava/io/PrintStream;",
			"(Ljava/lang/String;)V");
	
	private static final NativeFieldMember systemOut = new NativeFieldMember(Modifiers.MODIFIER_EXPORT, "out", DefinitionTypeID.forType(printStream), true, "java/lang/System");
	
	private class PrintlnSymbol implements ISymbol {

		@Override
		public IPartialExpression getExpression(CodePosition position, GlobalTypeRegistry types, List<ITypeID> typeArguments) {
			return new PartialMemberGroupExpression(
					position,
					new GetStaticFieldExpression(position, systemOut),
					printStreamPrintln,
					false);
		}

		@Override
		public ITypeID getType(CodePosition position, GlobalTypeRegistry types, List<ITypeID> typeArguments) {
			// don't be fooled! this symbol is the System.out.println bound method and thus its type is a function
			return new FunctionTypeID(printStreamPrintln.header);
		}
	}
}
