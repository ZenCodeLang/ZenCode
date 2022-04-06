package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetStaticFieldExpression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.type.TypeID;

public class FieldMemberRef extends PropertyRef {
	public final FieldMember member;

	public FieldMemberRef(TypeID owner, FieldMember member, GenericMapper mapper) {
		super(owner, member, mapper);
		this.member = member;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public Expression getStatic(CodePosition position) {
		return new GetStaticFieldExpression(position, this);
	}

	@Override
	public Expression getVirtual(CodePosition position, Expression target) {
		return new GetFieldExpression(position, target, this);
	}
}
