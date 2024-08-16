package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.AnyMethod;
import org.openzen.zenscript.codemodel.compilation.ExpressionBuilder;
import org.openzen.zenscript.codemodel.compilation.InstanceCallableMethod;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberInstance;

import java.util.Optional;

public class InterfaceCaster implements InstanceCallableMethod {
	private final FunctionHeader header;
	private final ImplementationMemberInstance implementationInstance;

	public InterfaceCaster(FunctionHeader header, ImplementationMemberInstance implementationInstance) {
		this.header = header;
		this.implementationInstance = implementationInstance;
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodInstance> asMethod() {
		return Optional.empty();
	}

	@Override
	public AnyMethod withGenericArguments(GenericMapper mapper) {
		return new InterfaceCaster(header.withGenericArguments(mapper), implementationInstance);
	}

	@Override
	public Modifiers getModifiers() {
		return Modifiers.IMPLICIT;
	}

	@Override
	public Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments) {
		return builder.interfaceCast(implementationInstance, instance);
	}
}
