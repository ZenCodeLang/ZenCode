/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;

/**
 *
 * @author Hoofdgebruiker
 */
public class SuperclassValidator implements DefinitionVisitor<Boolean> {
	private final Validator validator;
	private final CodePosition position;
	
	public SuperclassValidator(Validator validator, CodePosition position) {
		this.validator = validator;
		this.position = position;
	}

	@Override
	public Boolean visitClass(ClassDefinition definition) {
		if (!Modifiers.isVirtual(definition.modifiers)) {
			validator.logError(
					ValidationLogEntry.Code.SUPERCLASS_NOT_VIRTUAL,
					definition.position,
					"Superclass is not virtual");
			return false;
		}
		
		return true;
	}

	@Override
	public Boolean visitInterface(InterfaceDefinition definition) {
		validator.logError(
				ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS,
				definition.position,
				"Superclass cannot be an interface");
		
		return false;
	}

	@Override
	public Boolean visitEnum(EnumDefinition definition) {
		validator.logError(
				ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS,
				definition.position,
				"Superclass cannot be an enum");
		
		return false;
	}

	@Override
	public Boolean visitStruct(StructDefinition definition) {
		validator.logError(
				ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS,
				definition.position,
				"Superclass cannot be a struct");
		
		return false;
	}

	@Override
	public Boolean visitFunction(FunctionDefinition definition) {
		validator.logError(
				ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS,
				definition.position,
				"Superclass cannot be a function");
		
		return false;
	}

	@Override
	public Boolean visitExpansion(ExpansionDefinition definition) {
		throw new AssertionError();
	}

	@Override
	public Boolean visitAlias(AliasDefinition definition) {
		return definition.type.accept(new SupertypeValidator(validator, position));
	}

	@Override
	public Boolean visitVariant(VariantDefinition variant) {
		validator.logError(
				ValidationLogEntry.Code.SUPERCLASS_NOT_A_CLASS,
				variant.position,
				"Superclass cannot be a variant");
		return false;
	}
}
