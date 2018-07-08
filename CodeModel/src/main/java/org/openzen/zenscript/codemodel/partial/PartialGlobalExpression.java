/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GlobalCallExpression;
import org.openzen.zenscript.codemodel.expression.GlobalExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialGlobalExpression implements IPartialExpression {
	private final CodePosition position;
	private final String name;
	private final IPartialExpression resolution;
	private final ITypeID[] typeParameters;
	
	public PartialGlobalExpression(CodePosition position, String name, IPartialExpression resolution, ITypeID[] typeParameters) {
		this.position = position;
		this.name = name;
		this.resolution = resolution;
		this.typeParameters = typeParameters;
	}
	
	@Override
	public Expression eval() {
		return new GlobalExpression(position, name, resolution.eval());
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		return resolution.predictCallTypes(scope, hints, arguments);
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		return resolution.getPossibleFunctionHeaders(scope, hints, arguments);
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		return eval().getMember(position, scope, hints, name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		return new GlobalCallExpression(position, name, arguments, resolution.call(position, scope, hints, arguments));
	}

	@Override
	public ITypeID[] getGenericCallTypes() {
		return typeParameters;
	}
}
