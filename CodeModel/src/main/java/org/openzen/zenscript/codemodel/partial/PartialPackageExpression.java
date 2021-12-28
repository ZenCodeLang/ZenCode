package org.openzen.zenscript.codemodel.partial;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;

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
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) {
		return new List[arguments];
	}

	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) {
		return Collections.emptyList();
	}

	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		return pkg.getMember(position, scope.getTypeRegistry(), name);
	}

	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.USING_PACKAGE_AS_CALL_TARGET, "Cannot call a package");
	}

	@Override
	public TypeID[] getTypeArguments() {
		return null;
	}

	@Override
	public IPartialExpression capture(CodePosition position, LambdaClosure closure) throws CompileException {

		return this;
	}
}
