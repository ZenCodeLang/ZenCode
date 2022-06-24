package org.openzen.zenscript.codemodel.identifiers.instances;

import org.openzen.zenscript.codemodel.compilation.ExpressionBuilder;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public final class FieldInstance implements ResolvedType.Field, ResolvedType.StaticField {
	public final FieldSymbol field;
	private final TypeID type;

	public FieldInstance(FieldSymbol field) {
		this.field = field;
		this.type = field.getType();
	}

	public FieldInstance(FieldSymbol field, TypeID type) {
		this.field = field;
		this.type = type;
	}

	public String getName() {
		return field.getName();
	}

	public TypeID getType() {
		return type;
	}

	@Override
	public Expression get(ExpressionBuilder builder, Expression target) {
		return builder.getInstanceField(target, this);
	}

	@Override
	public Expression set(ExpressionBuilder builder, Expression target, Expression value) {
		return builder.setInstanceField(target, this, value);
	}

	@Override
	public Expression get(ExpressionBuilder builder) {
		return builder.getStaticField(this);
	}

	@Override
	public Expression set(ExpressionBuilder builder, Expression value) {
		return builder.setStaticField(this, value);
	}
}
