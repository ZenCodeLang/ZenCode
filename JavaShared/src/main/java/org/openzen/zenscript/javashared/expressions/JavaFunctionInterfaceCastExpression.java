package org.openzen.zenscript.javashared.expressions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.ExpressionVisitorWithContext;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

public class JavaFunctionInterfaceCastExpression extends Expression {
    public final Expression value;
    public final FunctionTypeID functionType;

    public JavaFunctionInterfaceCastExpression(CodePosition position, FunctionTypeID type, Expression value) {
        super(position, type, value.thrownType);

        this.value = value;
        functionType = type;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitPlatformSpecific(this);
    }

    @Override
    public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
        return visitor.visitPlatformSpecific(context, this);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new JavaFunctionInterfaceCastExpression(position, functionType, transformer.transform(value));
    }

    @Override
    public Expression normalize(TypeScope scope) {
        return new JavaFunctionInterfaceCastExpression(position, functionType, value.normalize(scope));
    }
}
