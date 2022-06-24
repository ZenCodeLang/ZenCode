package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.constant.EnumValueConstant;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class EnumConstantExpression extends Expression {
	public final EnumConstantMember value;

	public EnumConstantExpression(CodePosition position, TypeID type, EnumConstantMember value) {
		super(position, type, null);

		this.value = value;
	}

	public EnumConstantExpression(CodePosition position, GlobalTypeRegistry registry, EnumDefinition type, EnumConstantMember value) {
		super(position, registry.getForDefinition(type), null);

		this.value = value;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitEnumConstant(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitEnumConstant(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		return this;
	}

	@Override
	public Optional<CompileTimeConstant> evaluate() {
		return Optional.of(new EnumValueConstant(value));
	}
}
