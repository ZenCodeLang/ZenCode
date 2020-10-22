package org.openzen.zenscript.javashared.types;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.javashared.JavaMethod;

import java.lang.reflect.Method;

public class JavaFunctionalInterfaceTypeID extends FunctionTypeID {
    public final Method functionalInterfaceMethod;
    public final JavaMethod method;

    public JavaFunctionalInterfaceTypeID(GlobalTypeRegistry registry, FunctionHeader header, Method functionalInterfaceMethod, JavaMethod method) {
        super(registry, header);

        this.functionalInterfaceMethod = functionalInterfaceMethod;
        this.method = method;
    }
}
