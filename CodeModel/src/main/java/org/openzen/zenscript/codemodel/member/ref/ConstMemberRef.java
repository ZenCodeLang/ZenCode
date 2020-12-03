package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ConstMemberRef extends PropertyRef {
	public final ConstMember member;
	
	public ConstMemberRef(TypeID owner, ConstMember member, GenericMapper mapper) {
		super(owner, member, mapper);
		this.member = member;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}
}
