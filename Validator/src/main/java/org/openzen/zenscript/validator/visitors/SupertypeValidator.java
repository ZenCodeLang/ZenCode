/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.codemodel.type.TypeVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class SupertypeValidator implements TypeVisitor<Void> {
	private final Validator validator;
	private final CodePosition position;
	private final HighLevelDefinition subtype;
	
	public SupertypeValidator(
			Validator validator,
			CodePosition position,
			HighLevelDefinition subtype)
	{
		this.validator = validator;
		this.position = position;
		this.subtype = subtype;
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a basic type");
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an array");
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an associative array");
		return null;
	}

	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an iterator");
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a function");
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		if (!Modifiers.isVirtual(definition.definition.modifiers))
			validator.logError(ValidationLogEntry.Code.INVALID_SUPERTYPE, position, "Supertype must be virtual");
		if (subtype.isDestructible() && !definition.definition.isDestructible())
			validator.logError(ValidationLogEntry.Code.SUPERTYPE_NOT_DESTRUCTIBLE, position, "Superclass of a destructible type must be destructible");
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a type parameter");
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a range");
		return null;
	}

	@Override
	public Void visitOptional(OptionalTypeID type) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be an optional type");
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		validator.logError(ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS, position, "Superclass cannot be a generic map");
		return null;
	}
}
