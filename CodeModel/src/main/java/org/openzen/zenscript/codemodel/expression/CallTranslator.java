package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.scope.TypeScope;

public interface CallTranslator {
	Expression translate(Call call);

	class Call {
		public final CodePosition position;
		public final Expression target;
		public final FunctionHeader instancedHeader;
		public final CallArguments arguments;
		public final TypeScope scope;

		public Call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
			this.position = position;
			this.target = target;
			this.instancedHeader = instancedHeader;
			this.arguments = arguments;
			this.scope = scope;
		}
	}
}
