package org.openzen.zenscript.codemodel.identifiers.instances;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.ExpressionBuilder;
import org.openzen.zenscript.codemodel.compilation.InstanceCallableMethod;
import org.openzen.zenscript.codemodel.compilation.StaticCallableMethod;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class MethodInstance implements InstanceCallableMethod, StaticCallableMethod {
	public final MethodSymbol method;
	private final FunctionHeader header;
	private final TypeID target;

	public MethodInstance(MethodSymbol method) {
		this.method = method;
		this.header = method.getHeader();
		this.target = DefinitionTypeID.create(method.getTargetType(), TypeID.NONE);
	}

	public MethodInstance(MethodSymbol method, FunctionHeader header, TypeID target) {
		this.method = method;
		this.header = header;
		this.target = target;
	}

	public TypeID getTarget() {
		return target;
	}

	public String getName() {
		return method.getName();
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodSymbol> asMethod() {
		return Optional.of(method);
	}

	@Override
	public Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments) {
		return builder.callVirtual(this, instance, arguments);
	}

	@Override
	public Expression call(ExpressionBuilder builder, CallArguments arguments) {
		return builder.callStatic(this, arguments);
	}

	@Override
	public boolean isImplicit() {
		return false;
	}
}
