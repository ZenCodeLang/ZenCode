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
import org.openzen.zenscript.codemodel.expression.CompareExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class FunctionalMember extends DefinitionMember {
	public final FunctionHeader header;
	public final String name;
	public final BuiltinID builtin;
	public Statement body = null;
	public FunctionalMember overrides = null;
	
	public FunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			FunctionHeader header,
			BuiltinID builtin) {
		super(position, definition, modifiers);
		
		this.name = name;
		this.header = header;
		this.builtin = builtin;
	}
	
	public void setBody(Statement body) {
		this.body = body;
	}
	
	public abstract String getInformalName();
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}
	
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallExpression(position, target, this, instancedHeader, arguments, scope);
	}
	
	public final Expression call(CodePosition position, Expression target, CallArguments arguments, TypeScope scope) {
		return call(position, target, header, arguments, scope);
	}
	
	public Expression callWithComparator(CodePosition position, CompareType comparison, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CompareExpression(position, target, arguments.arguments[0], this, comparison, scope);
	}
	
	public Expression callStatic(CodePosition position, ITypeID target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallStaticExpression(position, target, this, arguments, instancedHeader, scope);
	}
}
