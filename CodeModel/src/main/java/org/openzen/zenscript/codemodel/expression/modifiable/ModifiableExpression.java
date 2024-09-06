package org.openzen.zenscript.codemodel.expression.modifiable;

import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface ModifiableExpression {
	TypeID getType();

	<T> T accept(ModifiableExpressionVisitor<T> visitor);

	<C, R> R accept(C context, ModifiableExpressionVisitorWithContext<C, R> visitor);

	ModifiableExpression transform(ExpressionTransformer transformer);
}
