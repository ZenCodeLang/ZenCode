package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class IteratorMemberRef implements DefinitionMemberRef {
	public final IteratorMember target;
	public final TypeID[] types;
	private final TypeID owner;

	public IteratorMemberRef(IteratorMember target, TypeID owner, TypeID... types) {
		this.target = target;
		this.owner = owner;
		this.types = types;
	}

	@Override
	public CodePosition getPosition() {
		return target.getPosition();
	}

	@Override
	public TypeID getOwnerType() {
		return owner;
	}

	@Override
	public String describe() {
		return target.describe();
	}

	@Override
	public <T extends Tag> T getTag(Class<T> type) {
		return target.getTag(type);
	}

	public int getLoopVariableCount() {
		return types.length;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return target.getOverrides();
	}

	@Override
	public FunctionHeader getHeader() {
		return null; // TODO
	}

	@Override
	public MemberAnnotation[] getAnnotations() {
		return target.annotations;
	}

	@Override
	public IDefinitionMember getTarget() {
		return target;
	}
}
