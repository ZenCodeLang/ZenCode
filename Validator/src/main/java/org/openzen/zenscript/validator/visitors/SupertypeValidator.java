/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.validator.Validator;

/**
 * @author Hoofdgebruiker
 */
public class SupertypeValidator implements TypeVisitor<Void> {
	private final Validator validator;
	private final CodePosition position;
	private final HighLevelDefinition subtype;

	public SupertypeValidator(
			Validator validator,
			CodePosition position,
			HighLevelDefinition subtype) {
		this.validator = validator;
		this.position = position;
		this.subtype = subtype;
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		validator.logError(position, CompileErrors.invalidSuperclass(basic));
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		validator.logError(position, CompileErrors.invalidSuperclass(array));
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		validator.logError(position, CompileErrors.invalidSuperclass(assoc));
		return null;
	}

	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		validator.logError(position, CompileErrors.invalidSuperclass(iterator));
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		validator.logError(position, CompileErrors.invalidSuperclass(function));
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		if (!definition.definition.getModifiers().isVirtual())
			validator.logError(position, CompileErrors.superclassNotVirtual(definition));
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		validator.logError(position, CompileErrors.invalidSuperclass(generic));
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		validator.logError(position, CompileErrors.invalidSuperclass(range));
		return null;
	}

	@Override
	public Void visitOptional(OptionalTypeID type) {
		validator.logError(position, CompileErrors.invalidSuperclass(type));
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		validator.logError(position, CompileErrors.invalidSuperclass(map));
		return null;
	}

	@Override
	public Void visitInvalid(InvalidTypeID type) {
		validator.logError(position, type.error);
		return null;
	}
}
