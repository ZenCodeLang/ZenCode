package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.*;

import java.util.Arrays;

public class JavaTypeCheckIfGenericVisitor implements TypeVisitor<Boolean> {
	@Override
	public Boolean visitBasic(BasicTypeID basic) {
		return false;
	}

	@Override
	public Boolean visitArray(ArrayTypeID array) {
		return array.elementType.accept(this);
	}

	@Override
	public Boolean visitAssoc(AssocTypeID assoc) {
		return assoc.keyType.accept(this) || assoc.valueType.accept(this);
	}

	@Override
	public Boolean visitGenericMap(GenericMapTypeID map) {
		return true;
	}

	@Override
	public Boolean visitIterator(IteratorTypeID iterator) {
		return true; //ToDo, is that right?
	}

	@Override
	public Boolean visitFunction(FunctionTypeID function) {
		return function.header.typeParameters.length > 0
				|| function.header.getReturnType().accept(this)
				|| Arrays.stream(function.header.parameters).anyMatch(functionParameter -> functionParameter.type.accept(this));
	}

	@Override
	public Boolean visitDefinition(DefinitionTypeID definition) {
		return definition.isGeneric();
	}

	@Override
	public Boolean visitGeneric(GenericTypeID generic) {
		return true;
	}

	@Override
	public Boolean visitRange(RangeTypeID range) {
		return range.baseType.accept(this);
	}

	@Override
	public Boolean visitOptional(OptionalTypeID type) {
		return type.baseType.accept(this);
	}
}
