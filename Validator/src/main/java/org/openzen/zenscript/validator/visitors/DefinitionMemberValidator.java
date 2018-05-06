/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;
import org.openzen.zenscript.validator.analysis.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionMemberValidator implements MemberVisitor<Boolean> {
	private final Validator validator;
	private final Set<String> fieldNames = new HashSet<>();
	private final Set<String> members = new HashSet<>();
	private final Set<FieldMember> initializedFields = new HashSet<>();
	private final List<FunctionHeader> constructors = new ArrayList<>();
	private final HighLevelDefinition definition;
	private final Set<EnumConstantMember> initializedEnumConstants = new HashSet<>();
	private final Set<ITypeID> implementedTypes = new HashSet<>();
	
	public DefinitionMemberValidator(Validator validator, HighLevelDefinition definition) {
		this.validator = validator;
		this.definition = definition;
	}
	
	@Override
	public Boolean visitField(FieldMember member) {
		boolean isValid = true;
		if (fieldNames.contains(member.name)) {
			validator.logError(
					ValidationLogEntry.Code.DUPLICATE_FIELD_NAME,
					member.position,
					"Duplicate field name: " + member.name);
			isValid = false;
		}
		fieldNames.add(member.name);
		
		isValid &= member.type.accept(new TypeValidator(validator, member.position));
		
		if (member.initializer != null) {
			isValid &= member.initializer.accept(new ExpressionValidator(validator, new FieldInitializerScope(member)));
		}
		
		return isValid;
	}

	@Override
	public Boolean visitConstructor(ConstructorMember member) {
		boolean isValid = true;
		for (FunctionHeader existing : constructors) {
			if (existing.isSimilarTo(member.header)) {
				validator.logError(ValidationLogEntry.Code.DUPLICATE_CONSTRUCTOR, member.position, "Duplicate constructor, conflicts with this" + existing.toString());
				isValid = false;
			}
		}
		constructors.add(member.header);
		isValid &= ValidationUtils.validateHeader(validator, member.position, member.header);
		
		if (member.body == null && !member.isExtern()) {
			validator.logError(ValidationLogEntry.Code.BODY_REQUIRED, member.position, "Constructors must have a body");
		} else {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
			
			if (!statementValidator.constructorForwarded) {
				// TODO: does this type have a supertype with no-argument constructor?
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitMethod(MethodMember member) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateIdentifier(validator, member.position, member.name);
		isValid &= ValidationUtils.validateHeader(validator, member.position, member.header);
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitGetter(GetterMember member) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateIdentifier(validator, member.position, member.name);
		isValid &= member.type.accept(new TypeValidator(validator, member.position));
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitSetter(SetterMember member) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateIdentifier(validator, member.position, member.name);
		isValid &= member.type.accept(new TypeValidator(validator, member.position));
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitEnumConstant(EnumConstantMember member) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateIdentifier(validator, member.position, member.name);
		if (member.constructor != null) {
			isValid &= member.constructor.accept(new ExpressionValidator(validator, new EnumConstantInitializerScope()));
		}
		if (members.contains(member.name)) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, member.position, "Duplicate enum value: " + member.name);
			isValid = false;
		}
		
		initializedEnumConstants.add(member);
		return isValid;
	}

	@Override
	public Boolean visitOperator(OperatorMember member) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateHeader(validator, member.position, member.header);
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitCaster(CasterMember member) {
		boolean isValid = true;
		isValid &= member.toType.accept(new TypeValidator(validator, member.position));
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitCustomIterator(CustomIteratorMember member) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Boolean visitCaller(CallerMember member) {
		boolean isValid = true;
		isValid &= ValidationUtils.validateHeader(validator, member.position, member.header);
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			for (Statement statement : member.body) {
				isValid &= statement.accept(statementValidator);
			}
		}
		
		return isValid;
	}

	@Override
	public Boolean visitImplementation(ImplementationMember implementation) {
		boolean isValid = true;
		if (implementedTypes.contains(implementation.type)) {
			validator.logError(
					ValidationLogEntry.Code.TYPE_ALREADY_IMPLEMENTED,
					implementation.position,
					"Type is already implemented: " + implementation.type);
			isValid = false;
		}
		implementedTypes.add(implementation.type);
		
		DefinitionMemberValidator memberValidator = new DefinitionMemberValidator(validator, definition);
		for (IDefinitionMember member : implementation.members) {
			isValid &= member.accept(memberValidator);
		}
		
		return isValid;
	}

	@Override
	public Boolean visitInnerDefinition(InnerDefinitionMember innerDefinition) {
		boolean isValid = true;
		if (members.contains(innerDefinition.innerDefinition.name)) {
			validator.logError(
					ValidationLogEntry.Code.DUPLICATE_MEMBER_NAME,
					innerDefinition.position,
					"Duplicate member name: " + innerDefinition.innerDefinition.name);
			isValid = false;
		}
		
		isValid &= innerDefinition.innerDefinition.accept(new DefinitionValidator(validator));
		return isValid;
	}
	
	private class FieldInitializerScope implements ExpressionScope {
		private final FieldMember field;
		
		public FieldInitializerScope(FieldMember field) {
			this.field = field;
		}
		
		@Override
		public boolean isConstructor() {
			return false;
		}

		@Override
		public boolean isFirstStatement() {
			return true;
		}

		@Override
		public boolean hasThis() {
			return !field.isStatic();
		}

		@Override
		public boolean isFieldInitialized(FieldMember field) {
			return initializedFields.contains(field);
		}

		@Override
		public void markConstructorForwarded() {
			
		}

		@Override
		public boolean isEnumConstantInitialized(EnumConstantMember member) {
			return true;
		}

		@Override
		public boolean isLocalVariableInitialized(VarStatement variable) {
			return true; // TODO
		}

		@Override
		public boolean isStaticInitializer() {
			return false;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return definition;
		}
	}
	
	private class ConstructorStatementScope implements StatementScope {
		private final FunctionHeader header;
		
		public ConstructorStatementScope(FunctionHeader header) {
			this.header = header;
		}

		@Override
		public boolean isConstructor() {
			return true;
		}

		@Override
		public boolean isStatic() {
			return false;
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
			return definition;
		}
	}
	
	private class EnumConstantInitializerScope implements ExpressionScope {

		@Override
		public boolean isConstructor() {
			return false;
		}

		@Override
		public boolean isFirstStatement() {
			return false;
		}

		@Override
		public boolean hasThis() {
			return false;
		}

		@Override
		public boolean isFieldInitialized(FieldMember field) {
			return false;
		}

		@Override
		public void markConstructorForwarded() {
			
		}

		@Override
		public boolean isEnumConstantInitialized(EnumConstantMember member) {
			if (member.definition == definition) {
				return initializedEnumConstants.contains(member);
			} else {
				return true;
			}
		}

		@Override
		public boolean isLocalVariableInitialized(VarStatement variable) {
			return false;
		}

		@Override
		public boolean isStaticInitializer() {
			return false;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return definition;
		}
	}
}
