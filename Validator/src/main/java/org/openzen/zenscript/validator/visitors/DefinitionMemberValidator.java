/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.validator.TypeContext;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;
import org.openzen.zenscript.validator.analysis.StatementScope;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
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
	private final Set<TypeID> implementedTypes = new HashSet<>();
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
				member.getEffectiveModifiers(),
				Modifiers.PUBLIC | Modifiers.PROTECTED | Modifiers.PRIVATE,
				member.position,
				"Invalid modifier");
		if (member.getType() != member.value.type) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_TYPE,
					member.position,
					"Expression type doesn't match const type");
		}
		member.value.accept(new ExpressionValidator(validator, new FieldInitializerScope(member)));
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
		new TypeValidator(validator, member.position).validate(TypeContext.FIELD_TYPE, member.getType());

		if (member.initializer != null) {
			member.initializer.accept(new ExpressionValidator(validator, new FieldInitializerScope(member)));
		}

		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		if (member.getDefinition() instanceof EnumDefinition) {
			ValidationUtils.validateModifiers(
					validator,
					member.getEffectiveModifiers(),
					Modifiers.PRIVATE,
					member.position,
					"Invalid modifier");
		}

		for (FunctionHeader existing : constructors) {
			if (existing.isSimilarTo(member.header)) {
				validator.logError(ValidationLogEntry.Code.DUPLICATE_CONSTRUCTOR, member.position, "Duplicate constructor, conflicts with this" + existing.toString());
			}
		}
		constructors.add(member.header);
		ValidationUtils.validateHeader(validator, member.position, member.header, member.getAccessScope(), scope
				.getMemberCache());

		if (member.body == null) {
			if (member.getBuiltin() == BuiltinID.CLASS_DEFAULT_CONSTRUCTOR) {
				checkConstructorForwarded(member);
			}

			if (!member.isExtern()) {
				validator.logError(ValidationLogEntry.Code.BODY_REQUIRED, member.position, "Constructors must have a body");
				return null;
			}
		} else {
			checkConstructorForwarded(member);
		}

		return null;
	}

	/**
	 * Checks that the constructor forwards to a super/this constructor
	 * If it does not, asserts that the supertype has an empty constructor
	 */
	private void checkConstructorForwarded(ConstructorMember member) {
		final Statement body = member.body == null ? new EmptyStatement(member.position) : member.body;
		StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header, member.getAccessScope()));
		body.accept(statementValidator);
		validateThrow(member, member.header, body);

		if (member.definition.getSuperType() != null && !statementValidator.constructorForwarded) {
			if (member.definition.getSuperType() instanceof DefinitionTypeID && ((DefinitionTypeID) member.definition.getSuperType()).definition.hasEmptyConstructor()) {
				return;
			}

			validator.logError(ValidationLogEntry.Code.CONSTRUCTOR_FORWARD_MISSING, member.position, "Constructor not forwarded to base type");
		}
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		if (hasDestructor) {
			validator.logError(ValidationLogEntry.Code.MULTIPLE_DESTRUCTORS, member.position, "A type have only a single destructor");
		}
		hasDestructor = true;
		if (member.header.thrownType != null)
			validator.logError(ValidationLogEntry.Code.DESTRUCTOR_CANNOT_THROW, member.position, "Destructor cannot throw");

		validateFunctional(member, new MethodStatementScope(member.header, member.getAccessScope()));
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		ValidationUtils.validateHeader(validator, member.position, member.header, member.getAccessScope(), scope
				.getMemberCache());
		validateFunctional(member, new MethodStatementScope(member.header, member.getAccessScope()));
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		new TypeValidator(validator, member.position).validate(TypeContext.GETTER_TYPE, member.getType());
		validateGetter(member, new MethodStatementScope(new FunctionHeader(member.getType()), member.getAccessScope()));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		new TypeValidator(validator, member.position).validate(TypeContext.SETTER_TYPE, member.getType());
		validateSetter(member, new MethodStatementScope(new FunctionHeader(BasicTypeID.VOID, member.parameter), member.getAccessScope()));
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
		ValidationUtils.validateHeader(validator, member.position, member.header, member.getAccessScope(), scope
				.getMemberCache());
		validateFunctional(member, new MethodStatementScope(member.header, member.getAccessScope()));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		new TypeValidator(validator, member.position).validate(TypeContext.CASTER_TYPE, member.toType);
		validateFunctional(member, new MethodStatementScope(member.header, member.getAccessScope()));
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		TypeValidator typeValidator = new TypeValidator(validator, member.position);
		for (TypeID type : member.getLoopVariableTypes())
			typeValidator.validate(TypeContext.ITERATOR_TYPE, type);

		validateFunctional(member, new MethodStatementScope(
				new FunctionHeader(scope.getTypeRegistry().getIterator(member.getLoopVariableTypes())),
				member.getAccessScope()));
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		ValidationUtils.validateHeader(validator, member.position, member.header, member.getAccessScope(), scope
				.getMemberCache());
		validateFunctional(member, new MethodStatementScope(member.header, member.getAccessScope()));
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
			DefinitionTypeID type = (DefinitionTypeID) (implementation.type);
			if (!type.definition.isInterface())
				validator.logError(ValidationLogEntry.Code.INVALID_IMPLEMENTATION_TYPE, implementation.position, "Implementation type must be an interface");
		}

		DefinitionMemberValidator memberValidator = new DefinitionMemberValidator(validator, definition, scope, DefinitionMemberContext.IMPLEMENTATION);
		for (IDefinitionMember member : implementation.members) {
			member.accept(memberValidator);
		}

		checkImplementationComplete(implementation);
		return null;
	}

	private void checkImplementationComplete(ImplementationMember implementation) {
		Set<IDefinitionMember> implemented = new HashSet<>();
		for (IDefinitionMember member : implementation.members)
			if (member.getOverrides() != null)
				implemented.add(member.getOverrides().getTarget());
		for (DefinitionMemberRef member : implementation.definitionBorrowedMembers.keySet())
			implemented.add(member.getTarget());

		TypeMembers members = scope.getTypeMembers(implementation.type);
		List<IDefinitionMember> unimplemented = members.getUnimplementedMembers(implemented);
		if (unimplemented.size() == 1) {
			validator.logError(ValidationLogEntry.Code.INCOMPLETE_IMPLEMENTATION, implementation.position, unimplemented.get(0).describe() + " not implemented");
		} else if (unimplemented.size() > 1) {
			StringBuilder message = new StringBuilder();
			message.append("Implementation incomplete: ").append(unimplemented.size()).append(" members not yet implemented:");
			for (IDefinitionMember member : unimplemented) {
				message.append("\n").append("  - ").append(member.describe());
			}
			validator.logError(ValidationLogEntry.Code.INCOMPLETE_IMPLEMENTATION, implementation.position, message.toString());
		}
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
		member.body.accept(new StatementValidator(validator, new StaticInitializerScope(member.getAccessScope())));
		if (member.body.thrownType != null)
			validator.logError(ValidationLogEntry.Code.STATIC_INITIALIZER_CANNOT_THROW, member.position, "Static initializer cannot throw");
		return null;
	}

	private void validateThrow(DefinitionMember member, FunctionHeader header, Statement body) {
		if (body.thrownType != null && header.thrownType == null) // TODO: validate thrown type
			validator.logError(ValidationLogEntry.Code.THROW_WITHOUT_THROWS, member.position, "Method is throwing but doesn't declare throws type");
	}

	private void validateFunctional(FunctionalMember member, StatementScope scope) {
		if (Modifiers.isOverride(member.getEffectiveModifiers()) || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			if (member.getOverrides() == null) {
				validator.logError(ValidationLogEntry.Code.OVERRIDE_MISSING_BASE, member.position, "Overridden method not identified");
			} else {
				ValidationUtils.validateValidOverride(validator, member.position, this.scope, member.header, member.getOverrides().getHeader());
			}
		}

		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);
			validateThrow(member, member.header, member.body);
		}
	}

	private void validateGetter(GetterMember member, StatementScope scope) {
		if (Modifiers.isOverride(member.getEffectiveModifiers()) || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			if (member.getOverrides() == null) {
				validator.logError(ValidationLogEntry.Code.OVERRIDE_MISSING_BASE, member.position, "Overridden method not identified");
			}
		}

		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);

			validateThrow(member, new FunctionHeader(member.getType()), member.body);
		}
	}

	private void validateSetter(SetterMember member, StatementScope scope) {
		if (Modifiers.isOverride(member.getEffectiveModifiers()) || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			if (member.getOverrides() == null) {
				validator.logError(ValidationLogEntry.Code.OVERRIDE_MISSING_BASE, member.position, "Overridden method not identified");
			}
		}

		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);
			validateThrow(member, new FunctionHeader(BasicTypeID.VOID, member.getType()), member.body);
		}
	}

	private class FieldInitializerScope implements ExpressionScope {
		private final DefinitionMember field;

		public FieldInitializerScope(DefinitionMember field) {
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

		@Override
		public AccessScope getAccessScope() {
			return field.getAccessScope();
		}
	}

	private class ConstructorStatementScope implements StatementScope {
		private final FunctionHeader header;
		private final AccessScope access;

		public ConstructorStatementScope(FunctionHeader header, AccessScope access) {
			this.header = header;
			this.access = access;
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

		@Override
		public AccessScope getAccessScope() {
			return access;
		}
	}

	private class MethodStatementScope implements StatementScope {
		private final FunctionHeader header;
		private final AccessScope access;

		public MethodStatementScope(FunctionHeader header, AccessScope access) {
			this.header = header;
			this.access = access;
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

		@Override
		public AccessScope getAccessScope() {
			return access;
		}
	}

	private class StaticInitializerScope implements StatementScope {
		private final FunctionHeader header = new FunctionHeader(BasicTypeID.VOID);
		private final AccessScope access;

		public StaticInitializerScope(AccessScope access) {
			this.access = access;
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
			return true;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return definition;
		}

		@Override
		public AccessScope getAccessScope() {
			return access;
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

		@Override
		public AccessScope getAccessScope() {
			return definition.getAccessScope();
		}
	}
}
