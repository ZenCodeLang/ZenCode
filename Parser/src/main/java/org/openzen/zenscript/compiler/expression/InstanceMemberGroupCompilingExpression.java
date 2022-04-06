package org.openzen.zenscript.compiler.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.types.MemberGroup;
import org.openzen.zenscript.compiler.types.Setter;

import java.util.Optional;

public class InstanceMemberGroupCompilingExpression extends AbstractCompilingExpression implements ResolvedCallable {
	private final Expression instance;
	private final MemberGroup group;

	public InstanceMemberGroupCompilingExpression(ExpressionCompiler compiler, CodePosition position, Expression instance, MemberGroup group) {
		super(compiler, position);

		this.instance = instance;
		this.group = group;
	}

	@Override
	public Expression as(TypeID type) {
		return compiler.at(position, type).instanceMemberGet(instance, group);
	}

	@Override
	public Optional<ResolvedCallable> call() {
		if (group.isCallable()) {
			return Optional.of(this);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Expression assign(Expression value) {
		return compiler.at(position, value.type).instanceMemberSet(this.instance, group, value);
	}

	@Override
	public TypeMatch matches(TypeID returnType) {
		return group.getGetter()
				.map(getter -> compiler.matchType(getter.getType(), returnType))
				.orElse(TypeMatch.NONE);
	}

	@Override
	public InferredType inferType() {
		return group.getGetter()
				.map(getter -> InferredType.success(getter.getType()))
				.orElseGet(() -> InferredType.failure(CompileExceptionCode.MEMBER_NO_GETTER, "not a getter"));
	}

	@Override
	public Optional<TypeID> inferAssignType() {
		return group.getSetter().map(Setter::getType);
	}

	@Override
	public Expression call(TypeID returnType, CompilingExpression... arguments) {
		return compiler.at(position, returnType).instanceMemberCall(instance, group, arguments);
	}
}
