package org.openzen.zenscript.codemodel.member.builtin;

import java.util.Map;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.BasicCompareExpression;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Hoofdgebruiker
 */
public class ComparatorMember implements ICallableMember {
	private final FunctionHeader header;
	
	public ComparatorMember(ITypeID type) {
		header = new FunctionHeader(BasicTypeID.BOOL, new FunctionParameter(type));
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
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments) {
		throw new UnsupportedOperationException("Can't call a comparator");
	}

	@Override
	public Expression callWithComparator(CodePosition position, CompareType operator, Expression target, FunctionHeader instancedHeader, CallArguments arguments) {
		return new BasicCompareExpression(position, target, arguments.arguments[0], operator);
	}

	@Override
	public Expression callStatic(CodePosition position, FunctionHeader instancedHeader, CallArguments arguments) {
		throw new UnsupportedOperationException("Can't call a comparator");
	}

	@Override
	public Expression callStaticWithComparator(CodePosition position, CompareType operator, FunctionHeader instancedHeader, CallArguments arguments) {
		throw new UnsupportedOperationException("Can't call a comparator statically");
	}

	@Override
	public CodePosition getPosition() {
		return CodePosition.BUILTIN;
	}

	@Override
	public String describe() {
		return "comparator";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addOperator(OperatorType.COMPARE, this, priority);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return this; // only used for basic types, no instancing needed
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}
}
