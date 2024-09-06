package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ModifiablePropertyExpression implements ModifiableExpression {
	public final Expression instance;
	public final MethodInstance getter;
	public final MethodInstance setter;

	public ModifiablePropertyExpression(Expression instance, MethodInstance getter, MethodInstance setter) {
		this.instance = instance;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public TypeID getType() {
		return getter.getHeader().getReturnType();
	}

	@Override
	public <T> T accept(ModifiableExpressionVisitor<T> visitor) {
		return visitor.visitProperty(this);
	}

	@Override
	public <C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitProperty(context, this);
	}

	@Override
	public ModifiableExpression transform(ExpressionTransformer transformer) {
		Expression instance = transformer.transform(this.instance);
		return instance == this.instance ? this : new ModifiablePropertyExpression(instance, getter, setter);
	}
}
