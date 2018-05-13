/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ICallableMember extends IDefinitionMember {
	public boolean isStatic();
	
	public String getInformalName();
	
	public FunctionHeader getHeader();
	
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope);
	
	public Expression callWithComparator(CodePosition position, CompareType operator, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope);
	
	public Expression callStatic(CodePosition position, ITypeID target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope);
	
	public Expression callStaticWithComparator(CodePosition position, ITypeID target, CompareType operator, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope);
}
