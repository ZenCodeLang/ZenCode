package org.openzen.zenscript.javashared.types;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;

import java.lang.reflect.Method;

public class JavaFunctionalInterfaceTypeID extends FunctionTypeID {
    public final Method functionalInterfaceMethod;
    public final JavaMethod method;

    public JavaFunctionalInterfaceTypeID(GlobalTypeRegistry registry, FunctionHeader header, Method functionalInterfaceMethod, JavaMethod method) {
        super(registry, header);

        this.functionalInterfaceMethod = functionalInterfaceMethod;
        this.method = method;
    }

    @Override
    public Expression castImplicit(CodePosition position, TypeID other, Expression value) {
        if (other instanceof JavaFunctionalInterfaceTypeID) {
            JavaFunctionalInterfaceTypeID otherType = (JavaFunctionalInterfaceTypeID)other;
            if (header.isEquivalentTo(otherType.header))
                return new JavaFunctionInterfaceCastExpression(position, otherType, value);

            return null;
        } else {
            return null;
        }
    }

    @Override
    public Expression castExplicit(CodePosition position, TypeID other, Expression value) {
        return this.castImplicit(position, other, value);
    }
}
