package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class CallerMember extends FunctionalMember {
	public MethodInstance overrides;

	public CallerMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			FunctionHeader header,
			BuiltinID builtin) {
		super(position, definition, modifiers, header, builtin);
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":caller:" + header.getCanonical();
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CALLER;
	}

	@Override
	public void registerTo(TypeID target, MemberSet.Builder members, GenericMapper mapper) {
		members.operator(OperatorType.CALL, mapper.map(target, this));
	}

	@Override
	public String describe() {
		return "caller " + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCaller(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitCaller(context, this);
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = super.getEffectiveModifiers();
		if (overrides != null && overrides.method.getDefiningType().isInterface())
			result = result.withPublic();

		return result;
	}

	public void setOverrides(MethodInstance overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(overrides.getHeader());
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	@Override
	public String getName() {
		return "()";
	}
}
