package org.openzen.zenscript.javabytecode;

import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;

public class JavaEnumInfo {
    public final JavaStatementVisitor clinitVisitor;

    public JavaEnumInfo(JavaStatementVisitor clinitVisitor) {
        this.clinitVisitor = clinitVisitor;
    }
}
