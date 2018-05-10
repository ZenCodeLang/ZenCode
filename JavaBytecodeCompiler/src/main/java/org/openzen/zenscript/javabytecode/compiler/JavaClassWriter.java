package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;

public class JavaClassWriter extends ClassWriter {

    public boolean hasRun;

    public JavaClassWriter(int flags) {
        super(flags);
        this.hasRun = false;
    }

    public JavaClassWriter(int flags, boolean hasRun) {
        super(flags);
        this.hasRun = hasRun;
    }
}
