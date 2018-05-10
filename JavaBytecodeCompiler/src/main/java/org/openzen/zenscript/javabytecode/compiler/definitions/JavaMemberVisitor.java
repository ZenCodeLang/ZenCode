package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaFieldInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javabytecode.JavaParameterInfo;
import org.openzen.zenscript.javabytecode.compiler.*;

public class JavaMemberVisitor implements MemberVisitor<Void> {

    private final ClassWriter writer;
	private final JavaClassInfo toClass;
	private final HighLevelDefinition definition;

    public JavaMemberVisitor(ClassWriter writer, JavaClassInfo toClass, HighLevelDefinition definition) {
        this.writer = writer;
		this.toClass = toClass;
		this.definition = definition;
    }

    @Override
    public Void visitField(FieldMember member) {

        //TODO calc signature
        String signature = null;
        final String descriptor = Type.getDescriptor(member.type.accept(JavaTypeClassVisitor.INSTANCE));
        writer.visitField(member.modifiers, member.name, descriptor, signature, null).visitEnd();
        member.setTag(JavaFieldInfo.class, new JavaFieldInfo(toClass, member.name, descriptor));
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorMember member) {
		System.out.println("Compiling constructor " + member.header.toString());
		
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
			
			CompilerUtils.writeDefaultFieldInitializers(constructorWriter, definition);
		}
		
        member.body.accept(statementVisitor);
        constructorWriter.label(constructorEnd);
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
}
