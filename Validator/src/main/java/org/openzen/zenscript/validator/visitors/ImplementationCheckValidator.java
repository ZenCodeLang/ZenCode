package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.Validator;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class ImplementationCheckValidator implements MemberVisitor<Void> {

	private final Validator validator;
	private final Set<MethodSymbol> implementedMethods = new HashSet<>();
	private final InterfaceDefinition interfaceDefinition;

	public ImplementationCheckValidator(Validator validator, ImplementationMember implementationMember) {
		this.validator = validator;

		this.interfaceDefinition = implementationMember.asImplementation()
				.flatMap(TypeID::asDefinition)
				.map(d -> d.definition)
				.filter(InterfaceDefinition.class::isInstance)
				.map(InterfaceDefinition.class::cast)
				.orElseThrow(() -> new NoSuchElementException("Must be an implementation!"));
	}

	@Override
	public Void visitField(FieldMember member) {
		validator.logError(member.position, CompileErrors.definitionNotAllowedHere("Field members not allowed inside an interface implementation"));
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		validator.logError(member.position, CompileErrors.definitionNotAllowedHere("Constructor members not allowed inside an interface implementation"));
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visitFunctional(member);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		visitFunctional(member);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		visitFunctional(member);
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		visitFunctional(member);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		visitFunctional(member);
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		visitFunctional(member);
		return null;
	}

	private void visitFunctional(FunctionalMember member) {
		member.getOverrides().ifPresent(e -> {
			if (interfaceDefinition.equals(e.method.getDefiningType())) {
				if (implementedMethods.contains(e.method)) {
					validator.logError(member.position, CompileErrors.duplicateMember(member.toString()));
					return;
				}

				implementedMethods.add(e.method);
			} else {
				validator.logError(member.position, CompileErrors.invalidOverride("override not part of interface"));
			}
		});
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		validator.logError(member.position, CompileErrors.cannotNestImplementations());
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		validator.logError(member.position, CompileErrors.definitionNotAllowedHere("Inner definitions are not allowed inside an interface implementation"));
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		validator.logError(member.position, CompileErrors.definitionNotAllowedHere("Static initializer members not allowed inside an interface implementation"));
		return null;
	}

	public List<IDefinitionMember> getUnimplementedMembers() {
		return interfaceDefinition.members.stream()
				.filter(IDefinitionMember::isAbstract)
				.filter(o -> !implementedMethods.contains(o))
				.collect(Collectors.toList());
	}

}
