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
public class TypeParameterCollector implements ITypeVisitor<Void> {
	private final Map<TypeParameter, ITypeID> map;
	
	public TypeParameterCollector(Map<TypeParameter, ITypeID> map) {
		this.map = map;
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		array.elementType.accept(this);
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		assoc.keyType.accept(this);
		assoc.valueType.accept(this);
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		return null;
	}

	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		for (ITypeID type : iterator.iteratorTypes)
			type.accept(this);
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		function.header.returnType.accept(this);
		for (FunctionParameter parameter : function.header.parameters)
			parameter.type.accept(this);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		for (ITypeID argument : definition.typeParameters)
			argument.accept(this);
		if (definition.outer != null)
			visitDefinition(definition.outer);
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		map.put(generic.parameter, generic);
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		range.from.accept(this);
		range.to.accept(this);
		return null;
	}

	@Override
	public Void visitModified(ModifiedTypeID type) {
		type.baseType.accept(this);
		return null;
	}
}
