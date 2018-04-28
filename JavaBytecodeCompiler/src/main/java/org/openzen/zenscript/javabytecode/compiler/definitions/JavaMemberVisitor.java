package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaTypeClassVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaTypeVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;

public class JavaMemberVisitor implements MemberVisitor<Void> {

    private final ClassWriter writer;
    private final String className;

    public JavaMemberVisitor(ClassWriter writer, String className) {
        this.writer = writer;
        this.className = className;
    }

    @Override
    public Void visitField(FieldMember member) {

        //TODO calc signature
        String signature = null;
        writer.visitField(member.modifiers, member.name, Type.getDescriptor(member.type.accept(JavaTypeClassVisitor.INSTANCE)), signature, null).visitEnd();
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorMember member) {
        final Label constructorStart = new Label();
        final Label constructorEnd = new Label();
        final JavaWriter constructorWriter = new JavaWriter(writer, member.modifiers, "<init>", calcDesc(member.header), calcSign(member.header), null);
        constructorWriter.label(constructorStart);
        for (FunctionParameter parameter : member.header.parameters) {
            constructorWriter.nameVariable(parameter.index + 1, parameter.name, constructorStart, constructorEnd, Type.getType(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }
        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(constructorWriter);
        statementVisitor.start();

        //TODO super constructor
        constructorWriter.loadObject(0);
        constructorWriter.invokeSpecial("java/lang/Object", "<init>", "()V");

        if (member.hasTag(JavaInitializedVariables.class)) {
            final JavaInitializedVariables tag = member.getTag(JavaInitializedVariables.class);
            for (final FieldMember field : tag.fields) {
                constructorWriter.loadObject(0);
                field.initializer.accept(statementVisitor.expressionVisitor);
                statementVisitor.getJavaWriter().putField(tag.owner, field.name, Type.getDescriptor(field.type.accept(JavaTypeClassVisitor.INSTANCE)));
            }
        }

        for (Statement statement : member.body) {
            statement.accept(statementVisitor);
        }
        constructorWriter.label(constructorEnd);
        statementVisitor.end();
        return null;
    }

    @Override
    public Void visitMethod(MethodMember member) {
        final Label methodStart = new Label();
        final Label methodEnd = new Label();
        final JavaWriter methodWriter = new JavaWriter(writer, member.modifiers, member.name, calcDesc(member.header), calcSign(member.header), null);
        methodWriter.label(methodStart);
        for (final FunctionParameter parameter : member.header.parameters) {
            methodWriter.nameParameter(0, parameter.name);
            methodWriter.nameVariable(parameter.index + (member.isStatic() ? 0 : 1), parameter.name, methodStart, methodEnd, Type.getType(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(methodWriter);
        statementVisitor.start();

        for (Statement statement : member.body) {
            statement.accept(statementVisitor);
        }

        methodWriter.label(methodEnd);
        statementVisitor.end();
        member.setTag(JavaMethodInfo.class, new JavaMethodInfo(new JavaClassInfo(className), member.name, calcSign(member.header), member.isStatic()));

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
        return null;
    }

    @Override
    public Void visitCaster(CasterMember member) {
        return null;
    }

    @Override
    public Void visitCustomIterator(CustomIteratorMember member) {
        return null;
    }

    @Override
    public Void visitCaller(CallerMember member) {
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

    private String calcDesc(FunctionHeader header) {
        StringBuilder descBuilder = new StringBuilder("(");
        for (FunctionParameter parameter : header.parameters) {
            descBuilder.append(Type.getDescriptor(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }
        descBuilder.append(")");
        descBuilder.append(Type.getDescriptor(header.returnType.accept(JavaTypeClassVisitor.INSTANCE)));
        return descBuilder.toString();
    }

    private String calcSign(FunctionHeader header) {
        StringBuilder signatureBuilder = new StringBuilder("(");
        for (FunctionParameter parameter : header.parameters) {
            signatureBuilder.append(parameter.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
        }
        signatureBuilder.append(")").append(header.returnType.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
        return signatureBuilder.toString();
    }
}
