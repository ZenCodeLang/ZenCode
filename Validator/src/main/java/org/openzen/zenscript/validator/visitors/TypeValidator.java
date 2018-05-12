/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitor;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeValidator implements ITypeVisitor<Boolean> {
	private final Validator validator;
	private final CodePosition position;
	
	public TypeValidator(Validator validator, CodePosition position) {
		this.validator = validator;
		this.position = position;
	}

	@Override
	public Boolean visitBasic(BasicTypeID basic) {
		if (basic == BasicTypeID.UNDETERMINED) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "type could not be determined");
			return false;
		}
		
		return true;
	}

	@Override
	public Boolean visitArray(ArrayTypeID array) {
		boolean isValid = true;
		if (array.dimension < 1) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at least 1");
			isValid = false;
		} else if (array.dimension > 16) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at most 16");
			isValid = false;
		}
		
		return isValid & array.elementType.accept(this);
	}

	@Override
	public Boolean visitAssoc(AssocTypeID assoc) {
		boolean isValid = true;
		isValid &= assoc.keyType.accept(this);
		isValid &= assoc.valueType.accept(this);
		
		// TODO: does the keytype have == and hashcode operators?
		return isValid;
	}

	@Override
	public Boolean visitIterator(IteratorTypeID iterator) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Boolean visitFunction(FunctionTypeID function) {
		return ValidationUtils.validateHeader(validator, position, function.header);
	}

	@Override
	public Boolean visitDefinition(DefinitionTypeID definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateTypeArguments(validator, position, definition.definition.genericParameters, definition.typeParameters);
		return isValid;
	}

	@Override
	public Boolean visitGeneric(GenericTypeID generic) {
		return true;
	}

	@Override
	public Boolean visitRange(RangeTypeID range) {
		return range.from.accept(this) & range.to.accept(this);
	}

	@Override
	public Boolean visitConst(ConstTypeID type) {
		// TODO: detect duplicate const
		return type.baseType.accept(this);
	}

	@Override
	public Boolean visitOptional(OptionalTypeID optional) {
		// TODO: detect duplicate optional
		return optional.baseType.accept(this);
	}

	@Override
	public Boolean visitGenericMap(GenericMapTypeID map) {
		boolean isValid = true;
		isValid &= map.value.accept(this);
		
		return isValid;
	}
}
