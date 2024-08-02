package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CheckNullExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.OptionalTypeID;

import java.util.Optional;

public class OptionalToStringMethod implements InstanceCallableMethod {
	private final OptionalTypeID type;
	private final ResolvedType resolvedBaseType;

	public OptionalToStringMethod(OptionalTypeID type, ResolvedType resolvedBaseType) {
		this.type = type;
		this.resolvedBaseType = resolvedBaseType;
	}

	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(BasicTypeID.STRING);
	}

	@Override
	public Optional<MethodInstance> asMethod() {
		return Optional.empty();
	}

	@Override
	public AnyMethod withGenericArguments(GenericMapper mapper) {
		return this;
	}

	@Override
	public Modifiers getModifiers() {
		return Modifiers.PUBLIC.withImplicit();
	}

	@Override
	public Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments) {
		MethodInstance isNullInstance = new MethodInstance(BuiltinMethodSymbol.OPTIONAL_IS_NULL, new FunctionHeader(BasicTypeID.BOOL), type);
		Expression isNull = builder.callVirtual(isNullInstance, instance, CallArguments.EMPTY);
		Expression unwrapped = new CheckNullExpression(instance.position, instance);
		Expression cast = resolvedBaseType.findCaster(BasicTypeID.STRING)
				.map(caster -> caster.call(builder, unwrapped, CallArguments.EMPTY))
				.orElseThrow(() -> new IllegalStateException("No caster found for " + resolvedBaseType));
		return builder.ternary(isNull, builder.constant("null"), cast);
	}
}
