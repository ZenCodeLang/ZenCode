/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class Expression implements IPartialExpression {
	public final CodePosition position;
	public final ITypeID type;
	
	public Expression(CodePosition position, ITypeID type) {
		this.position = position;
		this.type = type;
	}
	
	public ITypeID getType() {
		return type;
	}
	
	public abstract <T> T accept(ExpressionVisitor<T> visitor);
	
	@Override
	public List<ITypeID> getAssignHints() {
		return Collections.singletonList(type);
	}
	
	@Override
	public Expression eval() {
		return this;
	}
	
	public Expression castExplicit(CodePosition position, TypeScope scope, ITypeID asType, boolean optional) {
		return scope.getTypeMembers(type).castExplicit(position, this, asType, optional);
	}
	
	public Expression castImplicit(CodePosition position, TypeScope scope, ITypeID asType) {
		return scope.getTypeMembers(type).castImplicit(position, this, asType, true);
	}
	
	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		return scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL).predictCallTypes(scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		return scope.getTypeMembers(type)
				.getOrCreateGroup(OperatorType.CALL)
				.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().parameters.length == arguments && !method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}
	
	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL).call(position, scope, this, arguments, false);
	}
	
	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		TypeMembers members = scope.getTypeMembers(type);
		return members.getMemberExpression(position, this, name, false);
	}
}
