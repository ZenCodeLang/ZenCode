package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
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

	@Override
	public Expression getStatic(CodePosition position) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Attempting to get from a setter");
	}

	@Override
	public Expression getVirtual(CodePosition position, Expression target) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.INTERNAL_ERROR, "Attempting to get from a setter");
	}
}
