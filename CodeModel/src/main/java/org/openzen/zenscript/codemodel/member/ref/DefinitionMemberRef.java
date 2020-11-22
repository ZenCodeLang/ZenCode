package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface DefinitionMemberRef {
	CodePosition getPosition();
	
	String describe();
	
	default <T extends Tag> T getTag(Class<T> type) {
		return getTarget().getTag(type);
	}
	
	default <T extends Tag> boolean hasTag(Class<T> type) {
		return getTarget().hasTag(type);
	}
	
	TypeID getOwnerType();
	
	DefinitionMemberRef getOverrides();
	
	FunctionHeader getHeader();
	
	MemberAnnotation[] getAnnotations();
	
	IDefinitionMember getTarget();
}
