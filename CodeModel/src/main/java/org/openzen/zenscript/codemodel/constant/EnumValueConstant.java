package org.openzen.zenscript.codemodel.constant;

import org.openzen.zenscript.codemodel.member.EnumConstantMember;

import java.util.Optional;

public class EnumValueConstant implements CompileTimeConstant {
	public final EnumConstantMember member;

	public EnumValueConstant(EnumConstantMember member) {
		this.member = member;
	}

	@Override
	public Optional<EnumValueConstant> asEnumValue() {
		return Optional.of(this);
	}
}
