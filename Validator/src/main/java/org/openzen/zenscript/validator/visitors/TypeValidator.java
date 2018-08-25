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
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.ITypeVisitor;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeValidator implements ITypeVisitor<Void> {
	private final Validator validator;
	private final CodePosition position;
	
	public TypeValidator(Validator validator, CodePosition position) {
		this.validator = validator;
		this.position = position;
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		if (basic == BasicTypeID.UNDETERMINED) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "type could not be determined");
		}
		
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		if (array.dimension < 1) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at least 1");
		} else if (array.dimension > 16) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, position, "array dimension must be at most 16");
		}
		
		array.elementType.accept(this);
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		assoc.keyType.accept(this);
		assoc.valueType.accept(this);
		
		// TODO: does the keytype have == and hashcode operators?
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
		ValidationUtils.validateHeader(validator, position, function.header);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		ValidationUtils.validateTypeArguments(validator, position, definition.definition.genericParameters, definition.typeParameters);
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		range.baseType.accept(this);
		return null;
	}

	@Override
	public Void visitModified(ModifiedTypeID type) {
		// TODO: detect duplicate const
		type.baseType.accept(this);
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		map.value.accept(this);
		return null;
	}
}
