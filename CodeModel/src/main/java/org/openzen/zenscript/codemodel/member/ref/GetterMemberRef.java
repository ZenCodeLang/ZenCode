package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetterExpression;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class GetterMemberRef extends PropertyRef {
	public final GetterMember member;

	public GetterMemberRef(TypeID owner, GetterMember member, GenericMapper mapper) {
		super(owner, member, mapper);

		this.member = member;
	}

	public Expression get(CodePosition position, Expression target) {
		return new GetterExpression(position, target, this);
	}

	@Override
	public Expression getStatic(CodePosition position) {
		return new StaticGetterExpression(position, this);
	}

	@Override
	public Expression getVirtual(CodePosition position, Expression target) throws CompileException {
		return new GetterExpression(position, target, this);
	}

	@Override
	public GetterMemberRef getOverrides() {
		return member.getOverrides();
	}
}
