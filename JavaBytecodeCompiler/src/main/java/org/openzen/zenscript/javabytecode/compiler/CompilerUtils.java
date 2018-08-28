package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.switchvalue.*;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javabytecode.JavaParameterInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaSynthesizedClass;
import org.openzen.zenscript.javashared.JavaSynthesizedClassNamer;

public class CompilerUtils {
	private CompilerUtils() {}

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
		return position.getFilename().substring(0, position.getFilename().lastIndexOf('.')).replace("/", "_");
	}

    public static void tagMethodParameters(JavaBytecodeContext context, FunctionHeader header, boolean isStatic) {
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            Type parameterType = context.getType(parameter.type);
            parameter.setTag(JavaParameterInfo.class, new JavaParameterInfo(isStatic ? i : i + 1, parameterType));
        }
    }

    public static void tagConstructorParameters(JavaBytecodeContext context, FunctionHeader header, boolean isEnum) {
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            Type parameterType = context.getType(parameter.type);
            parameter.setTag(JavaParameterInfo.class, new JavaParameterInfo(isEnum ? i + 3 : i + 1, parameterType));
        }
    }

    public static void writeDefaultFieldInitializers(JavaBytecodeContext context, JavaWriter constructorWriter, HighLevelDefinition definition, boolean staticFields) {
        JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(context, constructorWriter);
        for (final IDefinitionMember definitionMember : definition.members) {
            if (!(definitionMember instanceof FieldMember))
                continue;

            FieldMember field = (FieldMember) definitionMember;
            if (field.isStatic() == staticFields && field.initializer != null) {
                if (!staticFields)
                    constructorWriter.loadObject(0);
                field.initializer.accept(expressionVisitor);
                if (staticFields)
                    constructorWriter.putStaticField(definition.name, field.name, context.getDescriptor(field.type));
                else
                    constructorWriter.putField(definition.name, field.name, context.getDescriptor(field.type));
            }
        }
    }

	private static final Map<String, JavaSynthesizedClass> functions = new HashMap<>();
	
    public static JavaSynthesizedClass getLambdaInterface(JavaBytecodeContext context, FunctionTypeID function) {
		String signature = JavaSynthesizedClassNamer.getFunctionSignature(function);
		if (functions.containsKey(signature))
			return functions.get(signature).withTypeParameters(JavaSynthesizedClassNamer.extractTypeParameters(function));
		
		System.out.println("Generating function " + signature);
		
		JavaSynthesizedClass result = JavaSynthesizedClassNamer.createFunctionName(function);
		functions.put(signature, result);
		
        createLambdaInterface(context, function.header, result.cls);
        return result;
    }

    private static void createLambdaInterface(JavaBytecodeContext context, FunctionHeader header, JavaClass cls) {
        ClassWriter ifaceWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
        ifaceWriter.visitAnnotation("java/lang/FunctionalInterface", true).visitEnd();
        ifaceWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, cls.internalName, null, "java/lang/Object", null);

        ifaceWriter
				.visitMethod(
					Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
					"accept",
					context.getMethodDescriptor(header),
					context.getMethodSignature(header),
					null)
				.visitEnd();

        context.register(cls.internalName.replace('/', '.'), ifaceWriter.toByteArray());

        try (FileOutputStream out = new FileOutputStream(cls.getClassName() + ".class")){
            out.write(ifaceWriter.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private static int lambdaCounter = 0;
    public static String getLambdaCounter() {
        return "lambda" + ++lambdaCounter;
    }

	public static int getKeyForSwitch(SwitchValue expression) {
		return expression.accept(new SwitchKeyVisitor());
	}

	private static class SwitchKeyVisitor implements SwitchValueVisitor<Integer> {

		@Override
		public Integer acceptInt(IntSwitchValue value) {
			return value.value;
		}

		@Override
		public Integer acceptChar(CharSwitchValue value) {
			return (int) value.value;
		}

		@Override
		public Integer acceptString(StringSwitchValue value) {
			return value.value.hashCode();
		}

		@Override
		public Integer acceptEnumConstant(EnumConstantSwitchValue value) {
			return value.constant.ordinal;
		}

		@Override
		public Integer acceptVariantOption(VariantOptionSwitchValue value) {
			return value.option.getOrdinal();
		}
	}
}
