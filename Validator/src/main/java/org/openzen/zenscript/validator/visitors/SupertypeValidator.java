/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ConstTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class SupertypeValidator implements ITypeVisitor<Boolean> {
	private final Validator validator;
	private final CodePosition position;
	
	public SupertypeValidator(
			Validator validator,
			CodePosition position)
	{
		this.validator = validator;
		this.position = position;
	}

	@Override
	public Boolean visitBasic(BasicTypeID basic) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a basic type");
		return false;
	}

	@Override
	public Boolean visitArray(ArrayTypeID array) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an array");
		return false;
	}

	@Override
	public Boolean visitAssoc(AssocTypeID assoc) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an associative array");
		return false;
	}

	@Override
	public Boolean visitIterator(IteratorTypeID iterator) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an iterator");
		return false;
	}

	@Override
	public Boolean visitFunction(FunctionTypeID function) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a function");
		return false;
	}

	@Override
	public Boolean visitDefinition(DefinitionTypeID definition) {
		boolean isValid = true;
		if (!Modifiers.isVirtual(definition.definition.modifiers)) {
			validator.logError(ValidationLogEntry.Code.INVALID_SUPERTYPE, position, "Supertype must be virtual");
			isValid = false;
		}
		return isValid;
	}

	@Override
	public Boolean visitGeneric(GenericTypeID generic) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a type parameter");
		return false;
	}

	@Override
	public Boolean visitRange(RangeTypeID range) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a range");
		return false;
	}

	@Override
	public Boolean visitConst(ConstTypeID type) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a const type");
		return false;
	}

	@Override
	public Boolean visitOptional(OptionalTypeID optional) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an optional type");
		return false;
	}

	@Override
	public Boolean visitGenericMap(GenericMapTypeID map) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a generic map");
		return false;
	}
}
