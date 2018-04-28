package org.openzen.zenscript.javabytecode;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class JavaLocalVariableInfo {
    public final Type type;
    public final int local;
    public final Label start;
    public final String name;
    public Label end;

    public JavaLocalVariableInfo(Type type, int local, Label start, String name) {
        this.type = type;
        this.local = local;
        this.start = start;
        this.end = start;
        this.name = name;
    }
}
