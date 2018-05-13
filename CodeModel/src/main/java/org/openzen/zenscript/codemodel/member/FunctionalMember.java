/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GenericCompareExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class FunctionalMember extends DefinitionMember implements ICallableMember {
	public final FunctionHeader header;
	public final String name;
	public Statement body;
	
	public FunctionalMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, FunctionHeader header) {
		super(position, definition, modifiers);
		
		this.name = name;
		this.header = header;
	}
	
	public void setBody(Statement body) {
		this.body = body;
	}
	
	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallExpression(position, target, this, instancedHeader, arguments, scope);
	}
	
	public final Expression call(CodePosition position, Expression target, CallArguments arguments, TypeScope scope) {
		return call(position, target, header, arguments, scope);
	}
	
	@Override
	public Expression callWithComparator(CodePosition position, CompareType operator, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new GenericCompareExpression(position, call(position, target, instancedHeader, arguments, scope), operator);
	}
	
	@Override
	public Expression callStatic(CodePosition position, ITypeID target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallStaticExpression(position, target, this, arguments, instancedHeader, scope);
	}
	
	@Override
	public Expression callStaticWithComparator(CodePosition position, ITypeID target, CompareType operator, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new GenericCompareExpression(position, callStatic(position, target, instancedHeader, arguments, scope), operator);
	}
}
