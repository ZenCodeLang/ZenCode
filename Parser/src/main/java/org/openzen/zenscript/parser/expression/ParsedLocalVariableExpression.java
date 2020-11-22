package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class ParsedLocalVariableExpression extends ParsedExpression {
	private final String name;
	
	public ParsedLocalVariableExpression(CodePosition position, String name) {
		super(position);
		
		this.name = name;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		TypeMembers members = scope.getTypeMembers(scope.getThisType());
		TypeMemberGroup group = members.getGroup(name);
		if (group == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "No such field: " + name);
		if (group.getField() == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "No such field: " + name);
		
		return new GetFieldExpression(position, new ThisExpression(position, scope.getThisType()), group.getField());
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
