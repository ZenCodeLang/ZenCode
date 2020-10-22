/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class BaseScope implements TypeScope {
	public abstract IPartialExpression get(CodePosition position, GenericName name) throws CompileException;
	
	public abstract LoopStatement getLoop(String name);
	
	public abstract FunctionHeader getFunctionHeader();
	
	@Override
	public TypeMembers getTypeMembers(TypeID type) {
		return getMemberCache().get(type);
	}
	
	@Override
	public GlobalTypeRegistry getTypeRegistry() {
		return getMemberCache().getRegistry();
	}
	
	public abstract DollarEvaluator getDollar();
	
	public abstract IPartialExpression getOuterInstance(CodePosition position) throws CompileException;
	
	public interface DollarEvaluator {
		Expression apply(CodePosition position) throws CompileException;
	}
}
