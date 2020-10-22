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
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialGlobalExpression implements IPartialExpression {
	private final CodePosition position;
	private final String name;
	private final IPartialExpression resolution;
	private final TypeID[] typeArguments;
	
	public PartialGlobalExpression(CodePosition position, String name, IPartialExpression resolution, TypeID[] typeArguments) {
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
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
		return resolution.predictCallTypes(position, scope, hints, arguments);
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
		return resolution.getPossibleFunctionHeaders(scope, hints, arguments);
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		return new GlobalCallExpression(position, name, arguments, resolution.call(position, scope, hints, arguments));
	}

	@Override
	public TypeID[] getTypeArguments() {
		return typeArguments;
	}

	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) {
		return this;
	}
}
