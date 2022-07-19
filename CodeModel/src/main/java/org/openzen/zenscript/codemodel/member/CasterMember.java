package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class CasterMember extends FunctionalMember {
	public TypeID toType;
	public MethodInstance overrides;

	public CasterMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			TypeID toType,
			BuiltinID builtin) {
		super(position, definition, modifiers, new FunctionHeader(toType), builtin);

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
		if (modifiers.isImplicit()) {
			members.implicitCast(mapper.map(targetType, this));
		} else {
			members.explicitCast(mapper.map(targetType, this));
		}
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
	public String getName() {
		return "as " + toType;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}
}
