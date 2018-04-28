package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Type;

public class JavaLocalVariableInfo {
    public final Type type;
    public final int local;

    public JavaLocalVariableInfo(Type type, int local) {
        this.type = type;
        this.local = local;
    }
}
