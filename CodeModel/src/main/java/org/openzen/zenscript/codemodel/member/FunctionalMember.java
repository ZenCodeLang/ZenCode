/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.List;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GenericCompareExpression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class FunctionalMember extends DefinitionMember implements ICallableMember {
	public final FunctionHeader header;
	public final String name;
	public List<Statement> body;
	
	public FunctionalMember(CodePosition position, int modifiers, String name, FunctionHeader header) {
		super(position, modifiers);
		
		this.name = name;
		this.header = header;
	}
	
	public void setBody(List<Statement> body) {
		this.body = body;
	}
	
	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments) {
		return new CallExpression(position, target, this, instancedHeader, arguments);
	}
	
	public final Expression call(CodePosition position, Expression target, CallArguments arguments) {
		return call(position, target, header, arguments);
	}
	
	@Override
	public Expression callWithComparator(CodePosition position, CompareType operator, Expression target, FunctionHeader instancedHeader, CallArguments arguments) {
		return new GenericCompareExpression(position, call(position, target, instancedHeader, arguments), operator);
	}
	
	@Override
	public Expression callStatic(CodePosition position, FunctionHeader instancedHeader, CallArguments arguments) {
		return new CallStaticExpression(position, this, arguments);
	}
	
	@Override
	public Expression callStaticWithComparator(CodePosition position, CompareType operator, FunctionHeader instancedHeader, CallArguments arguments) {
		return new GenericCompareExpression(position, callStatic(position, instancedHeader, arguments), operator);
	}
}
