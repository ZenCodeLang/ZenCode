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
	public T visitClass(ClassDefinition definition);
	
	public T visitInterface(InterfaceDefinition definition);
	
	public T visitEnum(EnumDefinition definition);
	
	public T visitStruct(StructDefinition definition);
	
	public T visitFunction(FunctionDefinition definition);
	
	public T visitExpansion(ExpansionDefinition definition);
	
	public T visitAlias(AliasDefinition definition);
	
	public T visitVariant(VariantDefinition variant);
}
