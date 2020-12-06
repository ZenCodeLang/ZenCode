package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class SetterMemberRef extends PropertyRef {
	public final SetterMember member;

	public SetterMemberRef(TypeID owner, SetterMember member, GenericMapper mapper) {
		super(owner, member, mapper);

		this.member = member;
	}

	@Override
	public SetterMemberRef getOverrides() {
		return member.getOverrides();
	}
}
