/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.codemodel.type.TypeVisitor;
import org.openzen.zenscript.codemodel.type.storage.InvalidStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeValidator implements TypeVisitor<Void> {
	private final Validator validator;
	private final CodePosition position;
	
	public TypeValidator(Validator validator, CodePosition position) {
		this.validator = validator;
		this.position = position;
	}
	
	public void validate(StoredType type) {
		if (type.storage instanceof InvalidStorageTag) {
			InvalidStorageTag storage = (InvalidStorageTag)type.storage;
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, storage.position, storage.message);
		}
		
		validate(type.type);
	}
	
	public void validate(TypeID type) {
		type.accept(this);
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		if (basic == BasicTypeID.UNDETERMINED)
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "type could not be determined");
		
		return null;
	}
	
	@Override
	public Void visitString(StringTypeID string) {
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		if (array.dimension < 1) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at least 1");
		} else if (array.dimension > 16) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at most 16");
		}
		
		validate(array.elementType);
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		validate(assoc.keyType);
		validate(assoc.valueType);
		
		// TODO: does the keytype have == and hashcode operators?
		return null;
	}
	
	@Override
	public Void visitInvalid(InvalidTypeID type) {
		validator.logError(ValidationLogEntry.Code.INVALID_TYPE, type.position, type.message);
		return null;
	}

	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		for (StoredType type : iterator.iteratorTypes)
			validate(type);
		
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		ValidationUtils.validateHeader(validator, position, function.header);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		ValidationUtils.validateTypeArguments(validator, position, definition.definition.typeParameters, definition.typeArguments);
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		validate(range.baseType);
		return null;
	}

	@Override
	public Void visitModified(ModifiedTypeID type) {
		// TODO: detect duplicate const
		validate(type.baseType);
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		validate(map.value);
		return null;
	}
}
