package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeImplementation;
import org.openzen.zenscript.javabytecode.JavaFieldInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;

public class JavaMemberVisitor implements MemberVisitor<Void> {

    private final JavaWriter javaWriter;
    private final JavaExpressionVisitor expressionVisitor;

    public JavaMemberVisitor(JavaWriter javaWriter, JavaExpressionVisitor expressionVisitor) {
        this.javaWriter = javaWriter;
        this.expressionVisitor = expressionVisitor;
    }


    @Override
    public Void visitField(FieldMember member) {
        JavaFieldInfo fieldInfo = member.getTag(JavaFieldInfo.class);
        if (fieldInfo == null)
            throw new IllegalStateException("Missing field info on a field member!");

        javaWriter.getStaticField(
                fieldInfo.javaClass.internalClassName,
                fieldInfo.name,
                fieldInfo.signature);
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorMember member) {
        return null;
    }

    @Override
    public Void visitMethod(MethodMember member) {
        if(!checkAndExecuteByteCodeImplementation(member) && !checkAndExecuteMethodInfo(member))
            throw new IllegalStateException("Call target has no method info!");

        return null;
    }

    @Override
    public Void visitGetter(GetterMember member) {
        return null;
    }

    @Override
    public Void visitSetter(SetterMember member) {
        return null;
    }

    @Override
    public Void visitEnumConstant(EnumConstantMember member) {
        return null;
    }

    @Override
    public Void visitOperator(OperatorMember member) {
        if(!checkAndExecuteByteCodeImplementation(member) && !checkAndExecuteMethodInfo(member))
            throw new IllegalStateException("Call target has no method info!");
        return null;
    }

    @Override
    public Void visitCaster(CasterMember member) {
        if(!checkAndExecuteByteCodeImplementation(member) && !checkAndExecuteMethodInfo(member))
            throw new IllegalStateException("Call target has no method info!");
        return null;
    }

    @Override
    public Void visitCustomIterator(CustomIteratorMember member) {
        return null;
    }

    @Override
    public Void visitCaller(CallerMember member) {
        if(!checkAndExecuteByteCodeImplementation(member) && !checkAndExecuteMethodInfo(member))
            throw new IllegalStateException("Call target has no method info!");
        return null;
    }

    @Override
    public Void visitImplementation(ImplementationMember member) {
        return null;
    }

    @Override
    public Void visitInnerDefinition(InnerDefinitionMember member) {
        return null;
    }


    private boolean checkAndExecuteByteCodeImplementation(DefinitionMember member) {
        JavaBytecodeImplementation implementation = member.getTag(JavaBytecodeImplementation.class);
        if (implementation != null) {
            implementation.compile(javaWriter);
            return true;
        }
        return false;
    }

    private boolean checkAndExecuteMethodInfo(DefinitionMember member) {
        JavaMethodInfo methodInfo = member.getTag(JavaMethodInfo.class);
        if (methodInfo == null)
            return false;
        if (methodInfo.isStatic) {
            javaWriter.invokeStatic(
                    methodInfo.javaClass.internalClassName,
                    methodInfo.name,
                    methodInfo.signature);
        } else {
            javaWriter.invokeVirtual(
                    methodInfo.javaClass.internalClassName,
                    methodInfo.name,
                    methodInfo.signature);
        }
        return true;
    }
}
