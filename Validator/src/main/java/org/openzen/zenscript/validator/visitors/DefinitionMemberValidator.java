/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.TypeContext;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;
import org.openzen.zenscript.validator.analysis.StatementScope;

import java.util.*;

/**
 * @author Hoofdgebruiker
 */
public class DefinitionMemberValidator implements MemberVisitor<Void> {
	private final Validator validator;
	private final HighLevelDefinition definition;
	private final DefinitionMemberContext context;

	private final Set<String> fieldNames = new HashSet<>();
	private final Set<String> members = new HashSet<>();
	private final Set<FieldSymbol> initializedFields = new HashSet<>();
	private final List<FunctionHeader> constructors = new ArrayList<>();
	private final Set<EnumConstantMember> initializedEnumConstants = new HashSet<>();
	private final Set<TypeID> implementedTypes = new HashSet<>();
	private boolean hasDestructor = false;

	public DefinitionMemberValidator(Validator validator, HighLevelDefinition definition, DefinitionMemberContext context) {
		this.validator = validator;
		this.definition = definition;
		this.context = context;
	}

	@Override
	public Void visitField(FieldMember member) {
		if (fieldNames.contains(member.name)) {
			validator.logError(member.position, CompileErrors.duplicateFieldName(member.name));
		}
		if (member.getModifiers().isConst()) {
			ValidationUtils.validateModifiers(
					validator,
					member.getEffectiveModifiers(),
					Modifiers.FLAG_PUBLIC | Modifiers.FLAG_PROTECTED | Modifiers.FLAG_PRIVATE,
					member.position,
					"Invalid modifier");
			if (member.getType() != member.initializer.type) {
				validator.logError(member.position, CompileErrors.constValueInvalidType(member.name, member.getType(), member.initializer.type));
			}
			member.initializer.accept(new ExpressionValidator(validator, new FieldInitializerScope(member)));
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
					member.getSpecifiedModifiers(),
					Modifiers.FLAG_PRIVATE,
					member.position,
					"Invalid modifier");
		}

		for (FunctionHeader existing : constructors) {
			if (existing.isSimilarTo(member.header)) {
				validator.logError(member.position, CompileErrors.duplicateConstructor(existing));
			}
		}
		constructors.add(member.header);
		ValidationUtils.validateHeader(validator, member.position, member.header);

		if (member.body == null) {
			if (!member.isExtern()) {
				validator.logError(member.position, CompileErrors.constructorWithoutBody());
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
		StatementValidator statementValidator = new StatementValidator(validator, new ConstructorStatementScope(member.header));
		body.accept(statementValidator);
		validateThrow(member, member.header, body);

		if (member.definition.getSuperType() != null && !statementValidator.constructorForwarded) {
			if (member.definition.getSuperType().resolve().getConstructor().acceptsZeroArguments()) {
				return;
			}

			validator.logError(member.position, CompileErrors.constructorNotForwarded());
		}
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
		new TypeValidator(validator, member.position).validate(TypeContext.GETTER_TYPE, member.type);
		validateGetter(member, new MethodStatementScope(new FunctionHeader(member.type)));
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		new TypeValidator(validator, member.position).validate(TypeContext.SETTER_TYPE, member.type);
		validateSetter(member, new MethodStatementScope(new FunctionHeader(BasicTypeID.VOID, member.parameter)));
		return null;
	}

	public void visitEnumConstant(EnumConstantMember member) {
		ValidationUtils.validateIdentifier(validator, member.position, member.name);
		if (member.constructor != null) {
			member.constructor.accept(new ExpressionValidator(validator, new EnumConstantInitializerScope()));
		}
		if (members.contains(member.name)) {
			validator.logError(member.position, CompileErrors.duplicateEnumValue(member.name));
		}

		initializedEnumConstants.add(member);
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		if (member.operator == OperatorType.DESTRUCTOR) {
			if (hasDestructor) {
				validator.logError(member.position, CompileErrors.duplicateDestructor());
			}
			hasDestructor = true;
			if (member.header.thrownType != null)
				validator.logError(member.position, CompileErrors.destructorCannotThrow());
		} else {
			ValidationUtils.validateHeader(validator, member.position, member.header);
		}
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		new TypeValidator(validator, member.position).validate(TypeContext.CASTER_TYPE, member.toType);
		validateFunctional(member, new MethodStatementScope(member.header));
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		TypeValidator typeValidator = new TypeValidator(validator, member.position);
		for (TypeID type : member.getLoopVariableTypes())
			typeValidator.validate(TypeContext.ITERATOR_TYPE, type);

		validateFunctional(member, new MethodStatementScope(new FunctionHeader(new IteratorTypeID(member.getLoopVariableTypes()))));
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember implementation) {
		if (context == DefinitionMemberContext.IMPLEMENTATION) {
			validator.logError(implementation.position, CompileErrors.cannotNestImplementations());
			return null;
		}
		if (implementedTypes.contains(implementation.type)) {
			validator.logError(implementation.position, CompileErrors.duplicateImplementation(implementation.type));
		}
		implementedTypes.add(implementation.type);

		if (!(implementation.type instanceof DefinitionTypeID)) {
			validator.logError(implementation.position, CompileErrors.invalidImplementedType(implementation.type));
		} else {
			DefinitionTypeID type = (DefinitionTypeID) (implementation.type);
			if (!type.definition.isInterface())
				validator.logError(implementation.position, CompileErrors.invalidImplementedType(type));
		}

		DefinitionMemberValidator memberValidator = new DefinitionMemberValidator(validator, definition, DefinitionMemberContext.IMPLEMENTATION);
		for (IDefinitionMember member : implementation.members) {
			member.accept(memberValidator);
		}

		checkImplementationComplete(implementation);
		return null;
	}

	private void checkImplementationComplete(ImplementationMember implementation) {
		// TODO
		/*Set<IDefinitionMember> implemented = new HashSet<>();
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
		}*/
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember innerDefinition) {
		if (members.contains(innerDefinition.innerDefinition.name)) {
			validator.logError(innerDefinition.position, CompileErrors.duplicateMember(innerDefinition.innerDefinition.name));
		}

		innerDefinition.innerDefinition.accept(new DefinitionValidator(validator));
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		member.body.accept(new StatementValidator(validator, new StaticInitializerScope()));
		if (member.body.thrownType != null)
			validator.logError(member.position, CompileErrors.staticInitializerCannotThrow());
		return null;
	}

	private void validateThrow(DefinitionMember member, FunctionHeader header, Statement body) {
		if (body.thrownType != null) {
			if (header.thrownType == null)
				validator.logError(member.position, CompileErrors.cannotThrowWithoutThrows());
			else if (!body.thrownType.equals(header.thrownType))
				validator.logError(member.position, CompileErrors.invalidThrownType(header.thrownType, body.thrownType));
		}
	}

	private void validateFunctional(FunctionalMember member, StatementScope scope) {
		if (member.getEffectiveModifiers().isOverride() || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			Optional<MethodInstance> maybeOverrides = member.getOverrides();
			if (maybeOverrides.isPresent()) {
				ValidationUtils.validateValidOverride(validator, member.position, member.header, maybeOverrides.get().getHeader());
			} else {
				validator.logError(member.position, CompileErrors.overriddenMethodNotFound(member.getID(), member.header));
			}
		}

		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);
			validateThrow(member, member.header, member.body);
		}
	}

	private void validateGetter(GetterMember member, StatementScope scope) {
		if (member.getEffectiveModifiers().isOverride() || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			if (!member.getOverrides().isPresent()) {
				validator.logError(member.position, CompileErrors.overriddenMethodNotFound(member.getID(), member.header));
			}
		}

		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);

			validateThrow(member, new FunctionHeader(member.type), member.body);
		}
	}

	private void validateSetter(SetterMember member, StatementScope scope) {
		if (member.getEffectiveModifiers().isOverride() || (context == DefinitionMemberContext.IMPLEMENTATION && !member.isPrivate())) {
			if (!member.getOverrides().isPresent()) {
				validator.logError(member.position, CompileErrors.overriddenMethodNotFound(member.getID(), member.header));
			}
		}

		if (member.body != null) {
			StatementValidator statementValidator = new StatementValidator(validator, scope);
			member.body.accept(statementValidator);
			validateThrow(member, new FunctionHeader(BasicTypeID.VOID, member.type), member.body);
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
		public boolean isFieldInitialized(FieldSymbol field) {
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
		public boolean isFieldInitialized(FieldSymbol field) {
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
