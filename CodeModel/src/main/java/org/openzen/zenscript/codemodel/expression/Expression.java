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
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class Expression implements IPartialExpression {
	public final CodePosition position;
	public final ITypeID type;
	public final ITypeID thrownType;
	
	public Expression(CodePosition position, ITypeID type, ITypeID thrownType) {
		this.position = position;
		this.type = type;
		this.thrownType = thrownType;
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
	
	@Override
	public ITypeID[] getGenericCallTypes() {
		return null;
	}
	
	public static ITypeID binaryThrow(CodePosition position, ITypeID left, ITypeID right) {
		if (left == right)
			return left;
		else if (left == null)
			return right;
		else if (right == null)
			return left;
		else
			throw new CompileException(position, CompileExceptionCode.DIFFERENT_EXCEPTIONS, "two different exceptions in same operation: " + left.toString() + " and " + right.toString());
	}
	
	public static ITypeID multiThrow(CodePosition position, Expression[] expressions) {
		ITypeID result = null;
		for (Expression expression : expressions)
			result = binaryThrow(position, result, expression.thrownType);
		return result;
	}
}
