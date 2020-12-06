package org.openzen.zenscript.codemodel.scope;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public interface TypeScope extends TypeResolutionContext {
	LocalMemberCache getMemberCache();

	default TypeMembers getTypeMembers(TypeID type) {
		return getMemberCache().get(type);
	}

	@Override
	default GlobalTypeRegistry getTypeRegistry() {
		return getMemberCache().getRegistry();
	}

	TypeMemberPreparer getPreparer();

	GenericMapper getLocalTypeParameters();
}
