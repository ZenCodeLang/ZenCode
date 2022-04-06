package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public interface ICallableMember {
	Expression callVirtual(CodePosition position, TypeScope scope, Expression target, CallArguments arguments);

	Expression callStatic(CodePosition position, TypeScope scope, CallArguments arguments);
}
