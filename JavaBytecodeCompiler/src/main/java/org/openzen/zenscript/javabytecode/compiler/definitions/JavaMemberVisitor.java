package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaFieldInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javabytecode.JavaParameterInfo;
import org.openzen.zenscript.javabytecode.compiler.*;

import java.util.List;

public class JavaMemberVisitor implements MemberVisitor<Void> {

    private final ClassWriter writer;
    private final JavaClassInfo toClass;
    private final HighLevelDefinition definition;
    private final JavaStatementVisitor clinitStatementVisitor;
    private EnumDefinition enumDefinition = null;

    public JavaMemberVisitor(ClassWriter writer, JavaClassInfo toClass, HighLevelDefinition definition) {
        this.writer = writer;
        this.toClass = toClass;
        this.definition = definition;

        final JavaWriter javaWriter = new JavaWriter(writer, new JavaMethodInfo(toClass, "<clinit>", "()V", 0), definition, null, null);
        this.clinitStatementVisitor = new JavaStatementVisitor(javaWriter);
        this.clinitStatementVisitor.start();
        CompilerUtils.writeDefaultFieldInitializers(javaWriter, definition, true);
    }

    @Override
    public Void visitField(FieldMember member) {

        //TODO calc signature
        String signature = null;
        final String descriptor = Type.getDescriptor(member.type.accept(JavaTypeClassVisitor.INSTANCE));
        writer.visitField(CompilerUtils.calcAccess(member.modifiers), member.name, descriptor, signature, null).visitEnd();
        member.setTag(JavaFieldInfo.class, new JavaFieldInfo(toClass, member.name, descriptor));
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorMember member) {
        final boolean isEnum = definition instanceof EnumDefinition;
        String descriptor = CompilerUtils.calcDesc(member.header, isEnum);
        final JavaMethodInfo method = new JavaMethodInfo(toClass, "<init>", descriptor, isEnum ? Opcodes.ACC_PRIVATE : CompilerUtils.calcAccess(member.modifiers));

        final Label constructorStart = new Label();
        final Label constructorEnd = new Label();
        final JavaWriter constructorWriter = new JavaWriter(writer, method, definition, CompilerUtils.calcSign(member.header, isEnum), null);
        constructorWriter.label(constructorStart);
        CompilerUtils.tagConstructorParameters(member.header, isEnum);
        for (FunctionParameter parameter : member.header.parameters) {
            constructorWriter.nameVariable(
                    parameter.getTag(JavaParameterInfo.class).index,
                    parameter.name,
                    constructorStart,
                    constructorEnd,
                    Type.getType(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(constructorWriter);
        statementVisitor.start();

        if (!member.isConstructorForwarded()) {
            if (isEnum) {
                System.out.println("Writing enum constructor");
                constructorWriter.getVisitor().newLocal(Type.getType(String.class));
                constructorWriter.getVisitor().newLocal(Type.getType(int.class));
                constructorWriter.loadObject(0);
                constructorWriter.loadObject(1);
                constructorWriter.loadInt(2);
                constructorWriter.invokeSpecial(Type.getInternalName(Enum.class), "<init>", "(Ljava/lang/String;I)V");
            } else if (definition.superType == null) {
                System.out.println("Writing regular constructor");
                constructorWriter.load(Type.getType(Object.class), 0);
                constructorWriter.invokeSpecial(Type.getInternalName(Object.class), "<init>", "()V");
            }

            CompilerUtils.writeDefaultFieldInitializers(constructorWriter, definition, false);
        }

        member.body.accept(statementVisitor);
        constructorWriter.label(constructorEnd);
        statementVisitor.end();
        return null;
    }
	
	@Override
	public Void visitDestructor(DestructorMember member) {
		final JavaMethodInfo method = new JavaMethodInfo(toClass, "close", "()V", Opcodes.ACC_PUBLIC);

        final Label constructorStart = new Label();
        final Label constructorEnd = new Label();
        final JavaWriter destructorWriter = new JavaWriter(writer, method, definition, null, null);
        destructorWriter.label(constructorStart);

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(destructorWriter);
        statementVisitor.start();
		// TODO: destruction of members (to be done when memory tags are implemented)
        member.body.accept(statementVisitor);
        destructorWriter.label(constructorEnd);
        statementVisitor.end();
        return null;
	}

    @Override
    public Void visitMethod(MethodMember member) {
        CompilerUtils.tagMethodParameters(member.header, member.isStatic());

        final boolean isAbstract = member.body == null || Modifiers.isAbstract(member.modifiers);
        int modifiers = (isAbstract ? Opcodes.ACC_ABSTRACT : 0)
                | (member.isStatic() ? Opcodes.ACC_STATIC : 0)
                | CompilerUtils.calcAccess(member.modifiers);
        final JavaMethodInfo method = new JavaMethodInfo(
                toClass,
                member.name,
                CompilerUtils.calcSign(member.header, false),
                modifiers);

        final Label methodStart = new Label();
        final Label methodEnd = new Label();
        final JavaWriter methodWriter = new JavaWriter(writer, method, definition, CompilerUtils.calcSign(member.header, false), null);
        methodWriter.label(methodStart);
        for (final FunctionParameter parameter : member.header.parameters) {
            methodWriter.nameParameter(0, parameter.name);
            if (!isAbstract)
                methodWriter.nameVariable(parameter.getTag(JavaParameterInfo.class).index, parameter.name, methodStart, methodEnd, Type.getType(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
        }

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(methodWriter);

        if (!isAbstract) {
            statementVisitor.start();
            member.body.accept(statementVisitor);
            methodWriter.label(methodEnd);
            statementVisitor.end();
        }

        member.setTag(JavaMethodInfo.class, method);
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

    @Override
    public Void visitStaticInitializer(StaticInitializerMember member) {
        member.body.accept(clinitStatementVisitor);
        return null;
    }

    public void end() {

        if (enumDefinition != null) {
			for (EnumConstantMember constant : enumDefinition.enumConstants) {
				writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM, constant.name, "L" + definition.name + ";", null, null).visitEnd();
				final String internalName = constant.constructor.type.accept(JavaTypeVisitor.INSTANCE).getInternalName();
				final JavaWriter clinitWriter = clinitStatementVisitor.getJavaWriter();
				clinitWriter.newObject(internalName);
				clinitWriter.dup();
				clinitWriter.constant(constant.name);
				clinitWriter.constant(constant.value);
				for (Expression argument : constant.constructor.arguments.arguments) {
					argument.accept(clinitStatementVisitor.expressionVisitor);
				}

				clinitWriter.invokeSpecial(internalName, "<init>", CompilerUtils.calcDesc(constant.constructor.constructor.header, true));
				clinitWriter.putStaticField(internalName, constant.name, "L" + internalName + ";");
				
				enumDefinition = (EnumDefinition) constant.definition;
			}
			
            final JavaWriter clinitWriter = clinitStatementVisitor.getJavaWriter();
            final List<EnumConstantMember> enumConstants = enumDefinition.enumConstants;
            clinitWriter.constant(enumConstants.size());
            clinitWriter.newArray(Type.getType("L" + definition.name + ";"));

            for (EnumConstantMember enumConstant : enumConstants) {
                clinitWriter.dup();
                clinitWriter.constant(enumConstant.value);
                clinitWriter.getStaticField(definition.name, enumConstant.name, "L" + definition.name + ";");
                clinitWriter.arrayStore(Type.getType("L" + definition.name + ";"));
            }
            clinitWriter.putStaticField(definition.name, "$VALUES", "[L" + definition.name + ";");
        }


        clinitStatementVisitor.end();
    }
}
