/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeArgument;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.StaticExpressionStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialTypeExpression implements IPartialExpression {
	private final CodePosition position;
	private final StoredType type;
	private final TypeArgument[] typeArguments;
	
	public PartialTypeExpression(CodePosition position, TypeID type, TypeArgument[] typeArguments) {
		this.position = position;
		this.type = type.stored(StaticExpressionStorageTag.INSTANCE);
		this.typeArguments = typeArguments;
	}

	@Override
	public Expression eval() {
		return new InvalidExpression(position, type, CompileExceptionCode.USING_TYPE_AS_EXPRESSION, "Not a valid expression");
	}

	@Override
	public List<StoredType>[] predictCallTypes(TypeScope scope, List<StoredType> hints, int arguments) {
		return scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL).predictCallTypes(scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<StoredType> hints, int arguments) {
		return scope.getTypeMembers(type)
				.getOrCreateGroup(OperatorType.CALL)
				.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().parameters.length == arguments && method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<StoredType> hints, GenericName name) {
		return scope.getTypeMembers(type).getStaticMemberExpression(position, scope, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) throws CompileException {
		if (arguments.getNumberOfTypeArguments() == 0 && (typeArguments != null && typeArguments.length > 0))
			arguments = new CallArguments(typeArguments, arguments.arguments);
		
		return scope.getTypeMembers(type).getOrCreateGroup(OperatorType.CALL).callStatic(position, type.type, scope, arguments);
	}

	@Override
	public TypeArgument[] getTypeArguments() {
		return typeArguments;
	}
	
	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		return this;
	}
}
