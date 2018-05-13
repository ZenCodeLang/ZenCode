/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CallTranslator {
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
	
	Expression translate(Call call);
}
