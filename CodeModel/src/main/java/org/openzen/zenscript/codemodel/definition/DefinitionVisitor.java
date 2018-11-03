/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DefinitionVisitor<T> {
	T visitClass(ClassDefinition definition);
	
	T visitInterface(InterfaceDefinition definition);
	
	T visitEnum(EnumDefinition definition);
	
	T visitStruct(StructDefinition definition);
	
	T visitFunction(FunctionDefinition definition);
	
	T visitExpansion(ExpansionDefinition definition);
	
	T visitAlias(AliasDefinition definition);
	
	T visitVariant(VariantDefinition variant);
}
