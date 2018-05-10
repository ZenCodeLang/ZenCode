package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

public class CompilerUtils {
    public static String calcDesc(FunctionHeader header, boolean isEnum) {
        StringBuilder descBuilder = new StringBuilder("(");
        if (isEnum)
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
        if (id instanceof BasicTypeID) {
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

    public static int calcAccess(int modifiers) {
        int out = 0;
        if (Modifiers.isStatic(modifiers))
            out |= Opcodes.ACC_STATIC;
        if (Modifiers.isFinal(modifiers))
            out |= Opcodes.ACC_FINAL;
        if (Modifiers.isPublic(modifiers))
            out |= Opcodes.ACC_PUBLIC;
        if (Modifiers.isPrivate(modifiers))
            out |= Opcodes.ACC_PRIVATE;
        if (Modifiers.isProtected(modifiers))
            out |= Opcodes.ACC_PROTECTED;
        if (Modifiers.isAbstract(modifiers))
            out |= Opcodes.ACC_ABSTRACT;
        return out;
    }

    public static String calcClasName(CodePosition position) {
        return position.filename.substring(0, position.filename.lastIndexOf('.')).replace("/", "_");
    }
}
