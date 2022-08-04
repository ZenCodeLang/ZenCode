package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CasterMemberRef implements DefinitionMemberRef {
	public final CasterMember member;
	public final TypeID type;
	public final TypeID toType;

	public CasterMemberRef(CasterMember member, TypeID type, TypeID toType) {
		this.member = member;
		this.type = type;
		this.toType = toType;
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

	public Expression cast(CodePosition position, Expression value, boolean implicit) {
		return new CastExpression(position, value, this, implicit);
	}

	public boolean isImplicit() {
		return member.getSpecifiedModifiers().isImplicit();
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return member.getOverrides();
	}

	@Override
	public FunctionHeader getHeader() {
		return member.header;
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
