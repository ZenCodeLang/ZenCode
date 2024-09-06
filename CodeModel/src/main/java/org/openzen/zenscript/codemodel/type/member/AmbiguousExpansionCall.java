package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ModificationExpression;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

import java.util.List;
import java.util.Optional;

public class AmbiguousExpansionCall implements InstanceCallableMethod, StaticCallableMethod {
	private final FunctionHeader header;
	private final List<MethodInstance> methods;

	public AmbiguousExpansionCall(FunctionHeader header, List<MethodInstance> methods) {
		this.header = header;
		this.methods = methods;
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
	public StaticCallableMethod withGenericArguments(GenericMapper mapper) {
		return this;
	}

	@Override
	public Modifiers getModifiers() {
		return Modifiers.PUBLIC;
	}

	@Override
	public Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments) {
		return builder.invalid(CompileErrors.ambiguousExpansionCall(methods));
	}

	@Override
	public Expression callModification(ExpressionBuilder builder, ModifiableExpression instance, ModificationExpression.Modification modification) {
		return builder.invalid(CompileErrors.ambiguousExpansionCall(methods));
	}

	@Override
	public Expression call(ExpressionBuilder builder, CallArguments arguments) {
		return builder.invalid(CompileErrors.ambiguousExpansionCall(methods));
	}

	@Override
	public boolean isImplicit() {
		return false;
	}
}
