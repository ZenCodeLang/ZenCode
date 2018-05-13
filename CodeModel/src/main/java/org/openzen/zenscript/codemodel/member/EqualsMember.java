/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.BasicCompareExpression;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public class EqualsMember extends Taggable implements ICallableMember {
	private final ITypeID type;
	private final FunctionHeader header;
	
	public EqualsMember(ITypeID type) {
		this.type = type;
		this.header = new FunctionHeader(BasicTypeID.BOOL, new FunctionParameter(type));
	}
	
	@Override
	public String getInformalName() {
		return "equals operator";
	}
	
	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new BasicCompareExpression(position, target, arguments.arguments[0], CompareType.EQ);
	}

	@Override
	public Expression callWithComparator(CodePosition position, CompareType operator, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		throw new UnsupportedOperationException("Comparator not supported here");
	}

	@Override
	public Expression callStatic(CodePosition position, ITypeID target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		throw new UnsupportedOperationException("Cannot be called statically");
	}

	@Override
	public Expression callStaticWithComparator(CodePosition position, ITypeID target, CompareType operator, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		throw new UnsupportedOperationException("Cannot be called statically");
	}

	@Override
	public CodePosition getPosition() {
		return CodePosition.BUILTIN;
	}

	@Override
	public String describe() {
		return "equals operator";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addOperator(OperatorType.EQUALS, this, priority);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		ITypeID instanced = type.withGenericArguments(registry, mapping);
		return instanced == type ? this : new EqualsMember(instanced);
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not yet supported");
	}
}
