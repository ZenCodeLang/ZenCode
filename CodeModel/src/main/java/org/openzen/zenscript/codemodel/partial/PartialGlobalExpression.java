/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GlobalCallExpression;
import org.openzen.zenscript.codemodel.expression.GlobalExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialGlobalExpression implements IPartialExpression {
	private final CodePosition position;
	private final String name;
	private final IPartialExpression resolution;
	private final StoredType[] typeArguments;
	
	public PartialGlobalExpression(CodePosition position, String name, IPartialExpression resolution, StoredType[] typeArguments) {
		this.position = position;
		this.name = name;
		this.resolution = resolution;
		this.typeArguments = typeArguments;
	}
	
	@Override
	public Expression eval() throws CompileException {
		return new GlobalExpression(position, name, resolution.eval());
	}

	@Override
	public List<StoredType>[] predictCallTypes(TypeScope scope, List<StoredType> hints, int arguments) throws CompileException {
		return resolution.predictCallTypes(scope, hints, arguments);
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<StoredType> hints, int arguments) throws CompileException {
		return resolution.getPossibleFunctionHeaders(scope, hints, arguments);
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<StoredType> hints, GenericName name) throws CompileException {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) throws CompileException {
		return new GlobalCallExpression(position, name, arguments, resolution.call(position, scope, hints, arguments));
	}

	@Override
	public StoredType[] getTypeArguments() {
		return typeArguments;
	}
}
