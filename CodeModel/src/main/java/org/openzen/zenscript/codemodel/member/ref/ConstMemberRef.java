package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.ConstExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
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

	@Override
	public Expression getStatic(CodePosition position) {
		return new ConstExpression(position, this);
	}

	@Override
	public Expression getVirtual(CodePosition position, Expression target) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.MEMBER_IS_STATIC, "Const member is static");
	}
}
