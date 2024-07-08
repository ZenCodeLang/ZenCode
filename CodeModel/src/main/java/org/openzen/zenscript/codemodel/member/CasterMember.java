package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class CasterMember extends FunctionalMember {
	public TypeID toType;
	public MethodInstance overrides;

	public CasterMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			TypeID toType) {
		super(position, definition, modifiers, MethodID.caster(toType), new FunctionHeader(toType));

		this.toType = toType;
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":caster:" + toType.toString();
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CASTER;
	}

	@Override
	public String describe() {
		return "caster to " + toType.toString();
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.method(MethodID.caster(mapper.map(toType)), mapper.map(targetType, this));
	}

	public boolean isImplicit() {
		return modifiers.isImplicit();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCaster(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitCaster(context, this);
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = super.getEffectiveModifiers();
		if (overrides != null && overrides.method.getDefiningType().isInterface())
			result = result.withPublic();

		return result;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	public void setOverrides(MethodInstance overrides) {
		this.overrides = overrides;
	}
}
