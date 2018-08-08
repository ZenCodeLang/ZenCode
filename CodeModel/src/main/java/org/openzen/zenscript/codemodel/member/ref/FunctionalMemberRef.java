/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
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

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionalMemberRef implements DefinitionMemberRef {
	private final FunctionalMember target;
	
	private FunctionHeader header;
	private GenericMapper mapper;
	
	public FunctionalMemberRef(FunctionalMember target, GenericMapper mapper) {
		this.target = target;
		
		if (target.header.hasUnknowns) {
			header = null;
			this.mapper = mapper;
		} else {
			header = mapper == null ? target.header : mapper.map(target.header);
			this.mapper = null;
		}
	}
	
	public boolean accepts(int arguments) {
		return arguments >= target.header.minParameters && arguments <= target.header.maxParameters;
	}
	
	@Override
	public FunctionHeader getHeader() {
		if (header == null) {
			if (target.header.hasUnknowns)
				throw new IllegalStateException("member is not yet resolved!");
			
			header = mapper == null ? target.header : mapper.map(target.header);
			this.mapper = null;
		}
		
		return header;
	}
	
	@Override
	public CodePosition getPosition() {
		return target.position;
	}
	
	public FunctionalMember getTarget() {
		return target;
	}
	
	public String getCanonicalName() {
		return target.getCanonicalName();
	}
	
	@Override
	public String describe() {
		return target.describe();
	}
	
	@Override
	public <T> T getTag(Class<T> cls) {
		return target.getTag(cls);
	}
	
	@Override
	public MemberAnnotation[] getAnnotations() {
		return target.annotations;
	}
	
	@Override
	public DefinitionMemberRef getOverrides() {
		return target.getOverrides();
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
		return new CallExpression(position, target, this, instancedHeader, arguments);
	}
	
	public final Expression call(CodePosition position, Expression target, CallArguments arguments, TypeScope scope) {
		return call(position, target, header, arguments, scope);
	}
	
	public Expression callWithComparator(CodePosition position, CompareType comparison, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CompareExpression(position, target, arguments.arguments[0], this, comparison, scope);
	}
	
	public Expression callStatic(CodePosition position, ITypeID target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallStaticExpression(position, target, this, instancedHeader, arguments);
	}
}
