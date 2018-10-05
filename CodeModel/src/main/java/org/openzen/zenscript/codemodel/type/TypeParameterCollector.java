/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeParameterCollector implements TypeVisitorWithContext<Map<TypeParameter, GenericTypeID>, Void, RuntimeException> {
	private final Map<TypeParameter, GenericTypeID> map;
	
	public TypeParameterCollector(Map<TypeParameter, GenericTypeID> map) {
		this.map = map;
	}

	@Override
	public Void visitBasic(Map<TypeParameter, GenericTypeID> context, BasicTypeID basic) {
		return null;
	}
	
	@Override
	public Void visitString(Map<TypeParameter, GenericTypeID> context, StringTypeID string) {
		return null;
	}

	@Override
	public Void visitArray(Map<TypeParameter, GenericTypeID> context, ArrayTypeID array) {
		array.elementType.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitAssoc(Map<TypeParameter, GenericTypeID> context, AssocTypeID assoc) {
		assoc.keyType.type.accept(context, this);
		assoc.valueType.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitGenericMap(Map<TypeParameter, GenericTypeID> context, GenericMapTypeID map) {
		return null;
	}

	@Override
	public Void visitIterator(Map<TypeParameter, GenericTypeID> context, IteratorTypeID iterator) {
		for (StoredType type : iterator.iteratorTypes)
			type.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitFunction(Map<TypeParameter, GenericTypeID> context, FunctionTypeID function) {
		function.header.getReturnType().type.accept(context, this);
		for (FunctionParameter parameter : function.header.parameters)
			parameter.type.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitDefinition(Map<TypeParameter, GenericTypeID> context, DefinitionTypeID definition) {
		for (TypeArgument argument : definition.typeArguments)
			argument.type.accept(context, this);
		if (definition.outer != null)
			visitDefinition(context, definition.outer);
		return null;
	}

	@Override
	public Void visitGeneric(Map<TypeParameter, GenericTypeID> context, GenericTypeID generic) {
		map.put(generic.parameter, generic);
		return null;
	}

	@Override
	public Void visitRange(Map<TypeParameter, GenericTypeID> context, RangeTypeID range) {
		range.baseType.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitModified(Map<TypeParameter, GenericTypeID> context, ModifiedTypeID type) {
		type.baseType.accept(context, this);
		return null;
	}
}
