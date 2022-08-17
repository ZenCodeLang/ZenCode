package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class MethodMember extends FunctionalMember {
	public final String name;
	private MethodInstance overrides;

	public MethodMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers, String name, FunctionHeader header) {
		super(position, definition, modifiers, header);

		this.name = name;
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":" + name + header.getCanonical();
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.METHOD;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		if (isStatic()) {
			members.staticMethod(name, mapper.map(targetType, this));
		} else {
			members.method(name, mapper.map(targetType, this));
		}
	}

	@Override
	public String describe() {
		return name + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitMethod(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitMethod(context, this);
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = super.getEffectiveModifiers();
		if (overrides != null) {
			if (overrides.getModifiers().isPublic())
				result = result.withPublic();
			if (overrides.getModifiers().isProtected())
				result = result.withProtected();
		}
		return result;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	public void setOverrides(MethodInstance overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(overrides.getHeader());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.empty();
	}
}
