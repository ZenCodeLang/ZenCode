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
public interface DefinitionVisitorWithContext<C, R> {
	R visitClass(C context, ClassDefinition definition);
	
	R visitInterface(C context, InterfaceDefinition definition);
	
	R visitEnum(C context, EnumDefinition definition);
	
	R visitStruct(C context, StructDefinition definition);
	
	R visitFunction(C context, FunctionDefinition definition);
	
	R visitExpansion(C context, ExpansionDefinition definition);
	
	R visitAlias(C context, AliasDefinition definition);
	
	R visitVariant(C context, VariantDefinition variant);
}
