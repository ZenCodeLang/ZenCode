/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.CompareExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FunctionalKind;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionalMemberRef implements DefinitionMemberRef {
	private final FunctionalMember target;
	public final FunctionHeader header;
	
	public FunctionalMemberRef(FunctionalMember target, FunctionHeader header) {
		this.target = target;
		this.header = header;
	}
	
	@Override
	public CodePosition getPosition() {
		return target.position;
	}
	
	public String getCanonicalName() {
		return target.getCanonicalName();
	}
	
	@Override
	public String describe() {
		return target.describe();
	}
	
	public <T> T getTag(Class<T> cls) {
		return target.getTag(cls);
	}
	
	public BuiltinID getBuiltin() {
		return target.builtin;
	}
	
	public boolean isStatic() {
		return target.isStatic();
	}
	
	public boolean isConstructor() {
		return target.getKind() == FunctionalKind.CONSTRUCTOR;
	}
	
	public boolean isOperator() {
		return target.getKind() == FunctionalKind.OPERATOR;
	}
	
	// TODO: shouldn't this be a call operator?
	public boolean isCaller() {
		return target.getKind() == FunctionalKind.CALLER;
	}
	
	public OperatorType getOperator() {
		return ((OperatorMember) target).operator;
	}
	
	public String getMethodName() {
		return ((MethodMember) target).name;
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
		return new CallStaticExpression(position, target, this, instancedHeader, arguments, scope);
	}
}
