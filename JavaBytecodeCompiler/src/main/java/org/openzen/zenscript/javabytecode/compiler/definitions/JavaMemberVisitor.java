package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaEnumInfo;
import org.openzen.zenscript.javabytecode.JavaFieldInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javabytecode.compiler.*;

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
        final String descriptor = Type.getDescriptor(member.type.accept(JavaTypeClassVisitor.INSTANCE));
        writer.visitField(member.modifiers, member.name, descriptor, signature, null).visitEnd();
        member.setTag(JavaFieldInfo.class, new JavaFieldInfo(new JavaClassInfo(className), member.name, descriptor));
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorMember member) {
        final Label constructorStart = new Label();
        final Label constructorEnd = new Label();
        final boolean isEnum = member.hasTag(JavaEnumInfo.class);
        final JavaWriter constructorWriter = new JavaWriter(writer, isEnum ? Opcodes.ACC_PRIVATE : member.modifiers, "<init>", CompilerUtils.calcDesc(member.header, isEnum), CompilerUtils.calcSign(member.header, isEnum), null);
        constructorWriter.label(constructorStart);
        for (FunctionParameter parameter : member.header.parameters) {
            if(isEnum)
                parameter.index += 2;
            constructorWriter.nameVariable(parameter.index + 1, parameter.name, constructorStart, constructorEnd, Type.getType(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }
        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(constructorWriter, true);
        statementVisitor.start();

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
        final boolean isAbstract = member.body == null || member.body.isEmpty() || Modifiers.isAbstract(member.modifiers);
        final JavaWriter methodWriter = new JavaWriter(writer, isAbstract ? member.modifiers | Opcodes.ACC_ABSTRACT : member.modifiers, member.name, CompilerUtils.calcDesc(member.header, false), CompilerUtils.calcSign(member.header, false), null);
        methodWriter.label(methodStart);
        for (final FunctionParameter parameter : member.header.parameters) {
            methodWriter.nameParameter(0, parameter.name);
            if (!isAbstract)
                methodWriter.nameVariable(parameter.index + (member.isStatic() ? 0 : 1), parameter.name, methodStart, methodEnd, Type.getType(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(methodWriter);

        if (!isAbstract) {
            statementVisitor.start();
            for (Statement statement : member.body) {
                statement.accept(statementVisitor);
            }

            methodWriter.label(methodEnd);
            statementVisitor.end();
        }

        member.setTag(JavaMethodInfo.class, new JavaMethodInfo(new JavaClassInfo(className), member.name, CompilerUtils.calcSign(member.header, false), member.isStatic()));

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
        final JavaStatementVisitor clinitVisitor = member.getTag(JavaEnumInfo.class).clinitVisitor;
        final JavaWriter clinitWriter = clinitVisitor.getJavaWriter();
        final String internalName = member.constructor.type.accept(JavaTypeVisitor.INSTANCE).getInternalName();

        clinitWriter.newObject(internalName);
        clinitWriter.dup();
        clinitWriter.constant(member.name);
        clinitWriter.constant(member.value);
        for (Expression argument : member.constructor.arguments.arguments) {
            argument.accept(clinitVisitor.expressionVisitor);
        }
        clinitWriter.invokeSpecial(internalName, "<init>", CompilerUtils.calcDesc(member.constructor.constructor.header, true));
        clinitWriter.putStaticField(internalName, member.name, "L" + internalName + ";");


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

}
