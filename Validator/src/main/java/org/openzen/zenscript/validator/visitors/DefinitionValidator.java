/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import static org.openzen.zenscript.codemodel.Modifiers.*;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionValidator implements DefinitionVisitor<Boolean> {
	private final Validator validator;
	
	public DefinitionValidator(Validator validator) {
		this.validator = validator;
	}

	@Override
	public Boolean visitClass(ClassDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PRIVATE | ABSTRACT | STATIC | PROTECTED | VIRTUAL,
				definition.position,
				"Invalid class modifier");
		isValid &= ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		if (definition.superType != null)
			isValid &= definition.superType.accept(new SupertypeValidator(validator, definition.position));
		
		isValid &= validateMembers(definition);
		return isValid;
	}

	@Override
	public Boolean visitInterface(InterfaceDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid interface modifier");
		isValid &= ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		isValid &= validateMembers(definition);
		return isValid;
	}

	@Override
	public Boolean visitEnum(EnumDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid enum modifier");
		isValid &= ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		isValid &= validateMembers(definition);
		return isValid;
	}

	@Override
	public Boolean visitStruct(StructDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid struct modifier");
		isValid &= ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		isValid &= validateMembers(definition);
		return isValid;
	}

	@Override
	public Boolean visitFunction(FunctionDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid function modifier");
		isValid &= ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
				
		StatementValidator statementValidator = new StatementValidator(validator, new FunctionStatementScope(definition.header));
		isValid &= definition.statement.accept(statementValidator);
		return isValid;
	}

	@Override
	public Boolean visitExpansion(ExpansionDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid expansion modifier");
		
		isValid &= definition.target.accept(new TypeValidator(validator, definition.position));
		isValid &= validateMembers(definition);
		return isValid;
	}

	@Override
	public Boolean visitAlias(AliasDefinition definition) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid alias modifier");
		isValid &= ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		return isValid;
	}
	
	private boolean validateMembers(HighLevelDefinition definition) {
		DefinitionMemberValidator memberValidator = new DefinitionMemberValidator(validator, definition);
		boolean isValid = true;
		for (IDefinitionMember member : definition.members) {
			isValid &= member.accept(memberValidator);
		}
		return isValid;
	}
	
	private class FunctionStatementScope implements StatementScope {
		private final FunctionHeader header;
		
		public FunctionStatementScope(FunctionHeader header) {
			this.header = header;
		}

		@Override
		public boolean isConstructor() {
			return false;
		}

		@Override
		public boolean isStatic() {
			return true;
		}

		@Override
		public FunctionHeader getFunctionHeader() {
			return header;
		}

		@Override
		public boolean isStaticInitializer() {
			return false;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return null;
		}
	}
}
