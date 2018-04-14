/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

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
	public Expression eval() {
		throw new CompileException(position, CompileExceptionCode.USING_PACKAGE_AS_EXPRESSION, "Cannot evaluate a package as expression");
	}

	@Override
	public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
		return new List[arguments];
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
		return Collections.emptyList();
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
		return pkg.getMember(position, scope.getTypeRegistry(), name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		throw new CompileException(position, CompileExceptionCode.USING_PACKAGE_AS_CALL_TARGET, "Cannot call a package");
	}
}
