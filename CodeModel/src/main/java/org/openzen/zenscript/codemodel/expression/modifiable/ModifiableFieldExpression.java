package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ModifiableFieldExpression implements ModifiableExpression {
	public final CodePosition position;
	public final Expression target;
	public final FieldInstance field;

	public ModifiableFieldExpression(CodePosition position, Expression target, FieldInstance field) {
		this.position = position;
		this.target = target;
		this.field = field;
	}

	@Override
	public TypeID getType() {
		return field.getType();
	}

	@Override
	public <T> T accept(ModifiableExpressionVisitor<T> visitor) {
		return visitor.visitField(this);
	}

	@Override
	public <C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitField(context, this);
	}

	@Override
	public ModifiableExpression transform(ExpressionTransformer transformer) {
		Expression target = transformer.transform(this.target);
		return target == this.target ? this : new ModifiableFieldExpression(position, target, field);
	}
}
