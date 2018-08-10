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
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;
import org.openzen.zenscript.validator.analysis.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionMemberValidator implements MemberVisitor<Void> {
	private final Validator validator;
	private final HighLevelDefinition definition;
	private final TypeScope scope;
	private final DefinitionMemberContext context;
	
	private final Set<String> fieldNames = new HashSet<>();
	private final Set<String> members = new HashSet<>();
	private final Set<FieldMember> initializedFields = new HashSet<>();
	private final List<FunctionHeader> constructors = new ArrayList<>();
	private final Set<EnumConstantMember> initializedEnumConstants = new HashSet<>();
	private final Set<ITypeID> implementedTypes = new HashSet<>();
	private boolean hasDestructor = false;
	
	public DefinitionMemberValidator(Validator validator, HighLevelDefinition definition, TypeScope scope, DefinitionMemberContext context) {
		this.validator = validator;
		this.definition = definition;
		this.scope = scope;
		this.context = context;
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		ValidationUtils.validateModifiers(
				validator,
				member.modifiers,
				Modifiers.PUBLIC | Modifiers.PROTECTED | Modifiers.PRIVATE,
				member.position,
				"Invalid modifier");
		if (member.type != member.value.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_TYPE,
					member.position,
					"Expression type doesn't match const type");
		}
		return null;
	}
	
	@Override
	public Void visitField(FieldMember member) {
		if (fieldNames.contains(member.name)) {
			validator.logError(
					ValidationLogEntry.Code.DUPLICATE_FIELD_NAME,
					member.position,
					"Duplicate field name: " + member.name);
		}
		fieldNames.add(member.name);
		member.type.accept(new TypeValidator(validator, member.position));
		
		if (member.initializer != null) {
			member.initializer.accept(new ExpressionValidator(validator, new FieldInitializerScope(member)));
		}
		
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		for (FunctionHeader existing : constructors) {
			if (existing.isSimilarTo(member.header)) {
				validator.logError(ValidationLogEntry.Code.DUPLICATE_CONSTRUCTOR, member.position, "Duplicate constructor, conflicts with this" + existing.toString());
			}
		}
		constructors.add(member.header);
		ValidationUtils.validateHeader(validator, member.position, member.header);
		
		if (member.body == null && !member.isExtern()) {
			validator.logError(ValidationLogEntry.Code.BODY_REQUIRED, member.position, "Constructors must have a body");
			return null;
		} else {
			StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
			member.body.accept(statementValidator);
			validateThrow(member);
			
			if (member.definition.getSuperType() != null && !statementValidator.constructorForwarded) {
				validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_MISSING, member.position, "Constructor not forwarded to base type");
			}
		}
		
		return null;
	}
	
	@Override
	public Void visitDestructor(DestructorMember member) {
		if (hasDestructor) {
			validator.logError(ValidationLogEntry.Code.MULTIPLE_DESTRUCTORS, member.position, "A type have only a single destructor");
		}
		hasDestructor = true;
		if (member.header.thrownType != null)
			validator.logError(ValidationLogEntry.Code.DESTRUCTOR_CANNOT_THROW, member.position, "Destructor cannot throw");
		
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		ValidationUtils.validateHeader(validator, member.position, member.header);
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		member.type.accept(new TypeValidator(validator, member.position));
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		member.type.accept(new TypeValidator(validator, member.position));
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}
	
	public void visitEnumConstant(EnumConstantMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		if (member.constructor != null) {
			member.constructor.accept(new ExpressionValidator(validator, new EnumConstantInitializerScope()));
		}
		if (members.contains(member.name)) {
			validator.logError(ValidationLogEntry.Code.INVALID_TYPE, member.position, "Duplicate enum value: " + member.name);
		}
		
		initializedEnumConstants.add(member);
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		ValidationUtils.validateHeader(validator, member.position, member.header);
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		member.toType.accept(new TypeValidator(validator, member.position));
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitCustomIterator(CustomIteratorMember member) {
		// TODO: validate iterators
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		ValidationUtils.validateHeader(validator, member.position, member.header);
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember implementation) {
		if (context == DefinitionMemberContext.IMPLEMENTATION) {
			validator.logError(ValidationLogEntry.Code.IMPLEMENTATION_NESTED, implementation.position, "Cannot nest implementations");
			return null;
		}
		if (implementedTypes.contains(implementation.type)) {
			validator.logError(
					ValidationLogEntry.Code.TYPE_ALREADY_IMPLEMENTED,
					implementation.position,
					"Type is already implemented: " + implementation.type);
		}
		implementedTypes.add(implementation.type);
		
		if (!(implementation.type instanceof DefinitionTypeID)) {
			validator.logError(ValidationLogEntry.Code.INVALID_IMPLEMENTATION_TYPE, implementation.position, "Implementation type must be an interface");
		} else {
			DefinitionTypeID type = (DefinitionTypeID)(implementation.type);
			if (!type.definition.isInterface())
				validator.logError(ValidationLogEntry.Code.INVALID_IMPLEMENTATION_TYPE, implementation.position, "Implementation type must be an interface");
		}
		
		DefinitionMemberValidator memberValidator = new DefinitionMemberValidator(validator, definition, scope, DefinitionMemberContext.IMPLEMENTATION);
		for (IDefinitionMember member : implementation.members) {
			member.accept(memberValidator);
		}
		
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember innerDefinition) {
		if (members.contains(innerDefinition.innerDefinition.name)) {
			validator.logError(
					ValidationLogEntry.Code.DUPLICATE_MEMBER_NAME,
					innerDefinition.position,
					"Duplicate member name: " + innerDefinition.innerDefinition.name);
		}
		
		innerDefinition.innerDefinition.accept(new DefinitionValidator(validator));
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		member.body.accept(new StatementValidator(validator, new StaticInitializerScope()));
		if (member.body.thrownType != null)
			validator.logError(ValidationLogEntry.Code.STATIC_INITIALIZER_CANNOT_THROW, member.position, "Static initializer cannot throw");
		return null;
	}
	
	private void validateThrow(FunctionalMember member) {
		if (member.body.thrownType != null && member.header.thrownType == null) // TODO: validate thrown type
			validator.logError(ValidationLogEntry.Code.THROW_WITHOUT_THROWS, member.position, "Method is throwing but doesn't declare throws type");
	}
	
	private void validateFunctional(FunctionalMember member, StatementScope scope) {
		if (Modifiers.isOverride(member.modifiers) || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			if (member.getOverrides() == null) {
				validator.logError(ValidationLogEntry.Code.OVERRIDE_MISSING_BASE, member.position, "Overridden method not identified");
			} else {
				ValidationUtils.validateValidOverride(validator, member.position, this.scope, member.header, member.getOverrides().getHeader());
			}
		}
		
		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);
			validateThrow(member);
		}
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
	
	private class MethodStatementScope implements StatementScope {
		private final FunctionHeader header;
		
		public MethodStatementScope(FunctionHeader header) {
			this.header = header;
		}

		@Override
		public boolean isConstructor() {
			return false;
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
	
	private class StaticInitializerScope implements StatementScope {
		private final FunctionHeader header = new FunctionHeader(BasicTypeID.VOID);
		
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
			return true;
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
