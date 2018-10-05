/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeArgument;

/**
 *
 * @author Hoofdgebruiker
 */
public class PartialPackageExpression implements IPartialExpression {
	private final CodePosition position;
	private final ZSPackage pkg;

	public PartialPackageExpression(CodePosition position, ZSPackage pkg) {
		this.position = position;
		this.pkg = pkg;
	}
	
	@Override
	public Expression eval() throws CompileException {
		throw new CompileException(position, CompileExceptionCode.USING_PACKAGE_AS_EXPRESSION, "Cannot evaluate a package as expression");
	}

	@Override
	public List<StoredType>[] predictCallTypes(TypeScope scope, List<StoredType> hints, int arguments) {
		return new List[arguments];
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<StoredType> hints, int arguments) {
		return Collections.emptyList();
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<StoredType> hints, GenericName name) throws CompileException {
		return pkg.getMember(position, scope.getTypeRegistry(), name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.USING_PACKAGE_AS_CALL_TARGET, "Cannot call a package");
	}

	@Override
	public TypeArgument[] getTypeArguments() {
		return null;
	}
}
