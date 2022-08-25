/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.validator.TypeContext;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 * @author Hoofdgebruiker
 */
public class TypeValidator implements TypeVisitorWithContext<TypeContext, Void, RuntimeException> {
	private final Validator validator;
	private final CodePosition position;

	public TypeValidator(Validator validator, CodePosition position) {
		this.validator = validator;
		this.position = position;
	}

	public void validate(TypeContext context, TypeID type) {
		type.accept(context, this);
	}

	@Override
	public Void visitBasic(TypeContext context, BasicTypeID basic) {
		if (basic == BasicTypeID.UNDETERMINED)
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, context.display + " could not be determined");

		return null;
	}

	@Override
	public Void visitArray(TypeContext context, ArrayTypeID array) {
		if (array.dimension < 1) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at least 1");
		} else if (array.dimension > 16) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at most 16");
		}

		validate(context, array.elementType);
		return null;
	}

	@Override
	public Void visitAssoc(TypeContext context, AssocTypeID assoc) {
		validate(context, assoc.keyType);
		validate(context, assoc.valueType);

		// TODO: does the keytype have == and hashcode operators?
		return null;
	}

	@Override
	public Void visitInvalid(TypeContext context, InvalidTypeID type) {
		validator.logError(ValidationLogEntry.Code.INVALID_TYPE, type.position, type.error.description);
		return null;
	}

	@Override
	public Void visitIterator(TypeContext context, IteratorTypeID iterator) {
		for (TypeID type : iterator.iteratorTypes)
			validate(context, type);

		return null;
	}

	@Override
	public Void visitFunction(TypeContext context, FunctionTypeID function) {
		ValidationUtils.validateHeader(validator, position, function.header);
		return null;
	}

	@Override
	public Void visitDefinition(TypeContext context, DefinitionTypeID definition) {
		ValidationUtils.validateTypeArguments(validator, position, definition.definition.getTypeParameters(), definition.typeArguments);
		return null;
	}

	@Override
	public Void visitGeneric(TypeContext context, GenericTypeID generic) {
		return null;
	}

	@Override
	public Void visitRange(TypeContext context, RangeTypeID range) {
		validate(context, range.baseType);
		return null;
	}

	@Override
	public Void visitOptional(TypeContext context, OptionalTypeID type) {
		// TODO: detect duplicate const
		validate(context, type.baseType);
		return null;
	}

	@Override
	public Void visitGenericMap(TypeContext context, GenericMapTypeID map) {
		validate(context, map.value);
		return null;
	}
}
