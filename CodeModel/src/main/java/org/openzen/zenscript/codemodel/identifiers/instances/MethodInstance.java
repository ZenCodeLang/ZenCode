package org.openzen.zenscript.codemodel.identifiers.instances;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ExpressionBuilder;
import org.openzen.zenscript.codemodel.compilation.InstanceCallableMethod;
import org.openzen.zenscript.codemodel.compilation.StaticCallableMethod;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class MethodInstance implements InstanceCallableMethod, StaticCallableMethod {
	public final MethodSymbol method;
	private final FunctionHeader header;
	private final TypeID target;
	private final TypeID[] expansionTypeArguments;

	public MethodInstance(MethodSymbol method) {
		this.method = method;
		this.header = method.getHeader();
		this.target = method.getTargetType();
		this.expansionTypeArguments = TypeID.NONE;
	}

	public MethodInstance(MethodSymbol method, FunctionHeader header, TypeID target) {
		this.method = method;
		this.header = header;
		this.target = target;
		this.expansionTypeArguments = TypeID.NONE;
	}

	public MethodInstance(MethodSymbol method, FunctionHeader header, TypeID target, TypeID[] expansionTypeArguments) {
		this.method = method;
		this.header = header;
		this.target = target;
		this.expansionTypeArguments = expansionTypeArguments;
	}

	public TypeID getTarget() {
		return target;
	}

	public MethodID getID() {
		return method.getID();
	}

	public Modifiers getModifiers() {
		return method.getModifiers();
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodInstance> asMethod() {
		return Optional.of(this);
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
		return method.getModifiers().isImplicit();
	}

	public TypeID[] getExpansionTypeArguments() {
		return expansionTypeArguments;
	}
}
