package org.openzen.zenscript.codemodel.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.modifiable.ModifiableExpression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;

/**
 * Used for post-increment and post-decrement.
 */
public class ModificationExpression extends Expression {
	public final ModifiableExpression target;
	public final MethodInstance method;
	public final Modification modification;

	public ModificationExpression(CodePosition position, ModifiableExpression target, MethodInstance method, Modification modification) {
		super(position, target.getType(), null);

		this.target = target;
		this.method = method;
		this.modification = modification;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitModification(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitModification(context, this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		ModifiableExpression target = this.target.transform(transformer);
		return target == this.target ? this : new ModificationExpression(position, target, method, modification);
	}

	public enum Modification {
		PreIncrement(OperatorType.INCREMENT),
		PreDecrement(OperatorType.DECREMENT),
		PostIncrement(OperatorType.INCREMENT),
		PostDecrement(OperatorType.DECREMENT);

		public final OperatorType operator;

		Modification(OperatorType operator) {
			this.operator = operator;
		}
	}
}
