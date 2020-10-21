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
public interface TypeVisitor<T> {
	T visitBasic(BasicTypeID basic);
	
	T visitArray(ArrayTypeID array);
	
	T visitAssoc(AssocTypeID assoc);
	
	T visitGenericMap(GenericMapTypeID map);
	
	T visitIterator(IteratorTypeID iterator);
	
	T visitFunction(FunctionTypeID function);
	
	T visitDefinition(DefinitionTypeID definition);
	
	T visitGeneric(GenericTypeID generic);
	
	T visitRange(RangeTypeID range);
	
	T visitOptional(OptionalTypeID type);
	
	default T visitInvalid(InvalidTypeID type) {
		throw new UnsupportedOperationException("Invalid type: " + type.message);
	}
}
