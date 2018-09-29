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
public interface TypeVisitorWithContext<C, R, E extends Exception> {
	R visitBasic(C context, BasicTypeID basic) throws E;
	
	R visitString(C context, StringTypeID string) throws E;
	
	R visitArray(C context, ArrayTypeID array) throws E;
	
	R visitAssoc(C context, AssocTypeID assoc) throws E;
	
	R visitGenericMap(C context, GenericMapTypeID map) throws E;
	
	R visitIterator(C context, IteratorTypeID iterator) throws E;
	
	R visitFunction(C context, FunctionTypeID function) throws E;
	
	R visitDefinition(C context, DefinitionTypeID definition) throws E;
	
	R visitGeneric(C context, GenericTypeID generic) throws E;
	
	R visitRange(C context, RangeTypeID range) throws E;
	
	R visitModified(C context, ModifiedTypeID type) throws E;
	
	default R visitInvalid(C context, InvalidTypeID type) throws E {
		throw new RuntimeException("Invalid type: " + type.message);
	}
}
