package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.openzen.zenscript.javashared.JavaTypeGenericVisitor;
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
import org.openzen.zenscript.javashared.JavaParameterInfo;
import org.openzen.zenscript.javabytecode.compiler.*;

import java.util.List;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;

public class JavaMemberVisitor implements MemberVisitor<Void> {
    private final ClassWriter writer;
	private final JavaBytecodeContext context;
    private final JavaClass toClass;
    private final HighLevelDefinition definition;
    private final JavaStatementVisitor clinitStatementVisitor;
    private EnumDefinition enumDefinition = null;

    public JavaMemberVisitor(JavaBytecodeContext context, ClassWriter writer, JavaClass toClass, HighLevelDefinition definition) {
        this.writer = writer;
        this.toClass = toClass;
        this.definition = definition;
		this.context = context;

        final JavaWriter javaWriter = new JavaWriter(writer, new JavaMethod(toClass, JavaMethod.Kind.STATICINIT, "<clinit>", true, "()V", 0, false), definition, null, null);
        this.clinitStatementVisitor = new JavaStatementVisitor(context, javaWriter);
        this.clinitStatementVisitor.start();
        CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, definition, true);
    }

	@Override
	public Void visitConst(ConstMember member) {
		JavaField field = member.getTag(JavaField.class);
        writer.visitField(CompilerUtils.calcAccess(member.modifiers), field.name, field.descriptor, field.signature, null).visitEnd();
        return null;
	}

	@Override
	public Void visitField(FieldMember member) {
		JavaField field = member.getTag(JavaField.class);
        writer.visitField(CompilerUtils.calcAccess(member.modifiers), field.name, field.descriptor, field.signature, null).visitEnd();
        return null;
    }

    @Override
    public Void visitConstructor(ConstructorMember member) {
        final boolean isEnum = definition instanceof EnumDefinition;
        final JavaMethod method = member.getTag(JavaMethod.class);

        final Label constructorStart = new Label();
        final Label constructorEnd = new Label();
        final JavaWriter constructorWriter = new JavaWriter(writer, method, definition, context.getMethodSignature(member.header), null);
        constructorWriter.label(constructorStart);
        CompilerUtils.tagConstructorParameters(context, member.header, isEnum);
        for (FunctionParameter parameter : member.header.parameters) {
            constructorWriter.nameVariable(
                    parameter.getTag(JavaParameterInfo.class).index,
                    parameter.name,
                    constructorStart,
                    constructorEnd,
                    context.getType(parameter.type));
        }

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, constructorWriter);
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
			} else if (definition.getSuperType() == null) {
				System.out.println("Writing regular constructor");
				constructorWriter.loadObject(0);
				constructorWriter.invokeSpecial(Type.getInternalName(Object.class), "<init>", "()V");
			}
        }

		member.body.accept(statementVisitor);
		constructorWriter.label(constructorEnd);
		statementVisitor.end();
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		final JavaMethod method = JavaMethod.getVirtual(toClass, "close", "()V", Opcodes.ACC_PUBLIC);

		final Label constructorStart = new Label();
		final Label constructorEnd = new Label();
		final JavaWriter destructorWriter = new JavaWriter(writer, method, definition, null, null);
		destructorWriter.label(constructorStart);

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, destructorWriter);
        statementVisitor.start();
		// TODO: destruction of members (to be done when memory tags are implemented)
		member.body.accept(statementVisitor);
		destructorWriter.label(constructorEnd);
		statementVisitor.end();
		return null;
	}

    @Override
    public Void visitMethod(MethodMember member) {
        CompilerUtils.tagMethodParameters(context, member.header, member.isStatic());

        final boolean isAbstract = member.body == null || Modifiers.isAbstract(member.modifiers);
        final JavaMethod method = member.getTag(JavaMethod.class);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();
	    final JavaWriter methodWriter = new JavaWriter(writer, method, definition, context.getMethodSignature(member.header), null);
		methodWriter.label(methodStart);
		for (final FunctionParameter parameter : member.header.parameters) {
			methodWriter.nameParameter(0, parameter.name);
			if (!isAbstract)
				methodWriter.nameVariable(parameter.getTag(JavaParameterInfo.class).index, parameter.name, methodStart, methodEnd, context.getType(parameter.type));
		}

        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, methodWriter);

		if (!isAbstract) {
			statementVisitor.start();
			member.body.accept(statementVisitor);
			methodWriter.label(methodEnd);
			statementVisitor.end();
		}
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
	public Void visitCustomIterator(IteratorMember member) {
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
				final String internalName = context.getInternalName(constant.constructor.type);
				final JavaWriter clinitWriter = clinitStatementVisitor.getJavaWriter();
				clinitWriter.newObject(internalName);
				clinitWriter.dup();
				clinitWriter.constant(constant.name);
				clinitWriter.constant(constant.ordinal);
				for (Expression argument : constant.constructor.arguments.arguments) {
					argument.accept(clinitStatementVisitor.expressionVisitor);
				}

				clinitWriter.invokeSpecial(internalName, "<init>", context.getEnumConstructorDescriptor(constant.constructor.constructor.getHeader()));
				clinitWriter.putStaticField(internalName, constant.name, "L" + internalName + ";");

				enumDefinition = (EnumDefinition) constant.definition;
			}

			final JavaWriter clinitWriter = clinitStatementVisitor.getJavaWriter();
			final List<EnumConstantMember> enumConstants = enumDefinition.enumConstants;
			clinitWriter.constant(enumConstants.size());
			clinitWriter.newArray(Type.getType("L" + definition.name + ";"));

			for (EnumConstantMember enumConstant : enumConstants) {
				clinitWriter.dup();
				clinitWriter.constant(enumConstant.ordinal);
				clinitWriter.getStaticField(definition.name, enumConstant.name, "L" + definition.name + ";");
				clinitWriter.arrayStore(Type.getType("L" + definition.name + ";"));
			}
			clinitWriter.putStaticField(definition.name, "$VALUES", "[L" + definition.name + ";");
		}


		clinitStatementVisitor.end();
	}
}
