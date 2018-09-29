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
public class TypeParameterCollector implements TypeVisitorWithContext<Map<TypeParameter, TypeID>, Void, RuntimeException> {
	private final Map<TypeParameter, TypeID> map;
	
	public TypeParameterCollector(Map<TypeParameter, TypeID> map) {
		this.map = map;
	}

	@Override
	public Void visitBasic(Map<TypeParameter, TypeID> context, BasicTypeID basic) {
		return null;
	}
	
	@Override
	public Void visitString(Map<TypeParameter, TypeID> context, StringTypeID string) {
		return null;
	}

	@Override
	public Void visitArray(Map<TypeParameter, TypeID> context, ArrayTypeID array) {
		array.elementType.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitAssoc(Map<TypeParameter, TypeID> context, AssocTypeID assoc) {
		assoc.keyType.type.accept(context, this);
		assoc.valueType.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitGenericMap(Map<TypeParameter, TypeID> context, GenericMapTypeID map) {
		return null;
	}

	@Override
	public Void visitIterator(Map<TypeParameter, TypeID> context, IteratorTypeID iterator) {
		for (StoredType type : iterator.iteratorTypes)
			type.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitFunction(Map<TypeParameter, TypeID> context, FunctionTypeID function) {
		function.header.getReturnType().type.accept(context, this);
		for (FunctionParameter parameter : function.header.parameters)
			parameter.type.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitDefinition(Map<TypeParameter, TypeID> context, DefinitionTypeID definition) {
		for (TypeID argument : definition.typeArguments)
			argument.accept(context, this);
		if (definition.outer != null)
			visitDefinition(context, definition.outer);
		return null;
	}

	@Override
	public Void visitGeneric(Map<TypeParameter, TypeID> context, GenericTypeID generic) {
		map.put(generic.parameter, generic);
		return null;
	}

	@Override
	public Void visitRange(Map<TypeParameter, TypeID> context, RangeTypeID range) {
		range.baseType.type.accept(context, this);
		return null;
	}

	@Override
	public Void visitModified(Map<TypeParameter, TypeID> context, ModifiedTypeID type) {
		type.baseType.accept(context, this);
		return null;
	}
}
