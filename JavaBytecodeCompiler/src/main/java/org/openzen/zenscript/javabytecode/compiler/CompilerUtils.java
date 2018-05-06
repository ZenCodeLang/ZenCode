package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;

public class CompilerUtils {
    public static String calcDesc(FunctionHeader header, boolean isEnum) {
        StringBuilder descBuilder = new StringBuilder("(");
        if(isEnum)
            descBuilder.append("Ljava/lang/String;I");
        for (FunctionParameter parameter : header.parameters) {
            descBuilder.append(Type.getDescriptor(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }
        descBuilder.append(")");
        descBuilder.append(Type.getDescriptor(header.returnType.accept(JavaTypeClassVisitor.INSTANCE)));
        return descBuilder.toString();
    }

    public static String calcSign(FunctionHeader header, boolean isEnum) {
        StringBuilder signatureBuilder = new StringBuilder("(");
        for (FunctionParameter parameter : header.parameters) {
            signatureBuilder.append(parameter.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
        }
        signatureBuilder.append(")").append(header.returnType.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
        return signatureBuilder.toString();
    }

    public static boolean isPrimitive(ITypeID id) {
        if(id instanceof BasicTypeID) {
            switch ((BasicTypeID) id) {
                case BOOL:
                case BYTE:
                case SBYTE:
                case SHORT:
                case USHORT:
                case INT:
                case UINT:
                case LONG:
                case ULONG:
                case FLOAT:
                case DOUBLE:
                case CHAR:
                    return true;
            }
        }
        return false;
    }
}
