package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ModifiableStaticFieldExpression implements ModifiableExpression {
	public final FieldInstance field;

	public ModifiableStaticFieldExpression(FieldInstance field) {
		this.field = field;
	}

	@Override
	public TypeID getType() {
		return field.getType();
	}

	@Override
	public <T> T accept(ModifiableExpressionVisitor<T> visitor) {
		return visitor.visitStaticField(this);
	}

	@Override
	public <C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitStaticField(context, this);
	}

	@Override
	public ModifiableExpression transform(ExpressionTransformer transformer) {
		return this;
	}
}
