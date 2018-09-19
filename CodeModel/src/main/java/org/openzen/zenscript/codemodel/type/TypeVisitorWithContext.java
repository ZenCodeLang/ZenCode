/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeVisitorWithContext<C, R> {
	R visitBasic(C context, BasicTypeID basic);
	
	R visitString(C context, StringTypeID string);
	
	R visitArray(C context, ArrayTypeID array);
	
	R visitAssoc(C context, AssocTypeID assoc);
	
	R visitGenericMap(C context, GenericMapTypeID map);
	
	R visitIterator(C context, IteratorTypeID iterator);
	
	R visitFunction(C context, FunctionTypeID function);
	
	R visitDefinition(C context, DefinitionTypeID definition);
	
	R visitGeneric(C context, GenericTypeID generic);
	
	R visitRange(C context, RangeTypeID range);
	
	R visitModified(C context, ModifiedTypeID type);
}
