package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ImplementationMemberRef implements DefinitionMemberRef {
	public final ImplementationMember member;
	private final TypeID type;
	public final TypeID implementsType;
	
	public ImplementationMemberRef(ImplementationMember member, TypeID owner, TypeID implementsType) {
		this.member = member;
		this.type = owner;
		this.implementsType = implementsType;
	}

	@Override
	public CodePosition getPosition() {
		return member.position;
	}
	
	@Override
	public TypeID getOwnerType() {
		return type;
	}

	@Override
	public String describe() {
		return member.describe();
	}

	@Override
	public <T extends Tag> T getTag(Class<T> type) {
		return member.getTag(type);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public FunctionHeader getHeader() {
		return null;
	}

	@Override
	public MemberAnnotation[] getAnnotations() {
		return member.annotations;
	}

	@Override
	public IDefinitionMember getTarget() {
		return member;
	}
}
