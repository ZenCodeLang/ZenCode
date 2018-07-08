/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IPartialExpression {
	default List<ITypeID> getAssignHints() {
		return Collections.emptyList();
	}
	
	Expression eval();
	
	List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments);
	
	List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments);
	
	IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name);
	
	Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments);
	
	ITypeID[] getGenericCallTypes();
	
	default Expression assign(CodePosition position, TypeScope scope, Expression value) {
		throw new CompileException(position, CompileExceptionCode.CANNOT_ASSIGN, "This expression is not assignable");
	}
	
	default IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		throw new CompileException(position, CompileExceptionCode.UNAVAILABLE_IN_CLOSURE, "expression not allowed in closure");
	}
}
