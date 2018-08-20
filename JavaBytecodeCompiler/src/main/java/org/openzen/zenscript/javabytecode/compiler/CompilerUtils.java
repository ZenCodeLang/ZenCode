package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValueVisitor;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.javabytecode.JavaParameterInfo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CompilerUtils {
    public static String calcDesc(FunctionHeader header, boolean isEnum) {
        StringBuilder descBuilder = new StringBuilder("(");
        if (isEnum)
            descBuilder.append("Ljava/lang/String;I");
        for (FunctionParameter parameter : header.parameters) {
            //descBuilder.append(Type.getDescriptor(parameter.type.accept(JavaTypeClassVisitor.INSTANCE)));
            descBuilder.append(parameter.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
        }
        descBuilder.append(")");
        descBuilder.append(header.returnType.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
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
        return position.getFilename().substring(0, position.getFilename().lastIndexOf('.')).replace("/", "_");
    }

    public static void tagMethodParameters(FunctionHeader header, boolean isStatic) {
        JavaTypeVisitor typeVisitor = new JavaTypeVisitor();
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            Type parameterType = parameter.type.accept(typeVisitor);
            parameter.setTag(JavaParameterInfo.class, new JavaParameterInfo(isStatic ? i : i + 1, parameterType));
        }
    }

    public static void tagConstructorParameters(FunctionHeader header, boolean isEnum) {
        JavaTypeVisitor typeVisitor = new JavaTypeVisitor();
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            Type parameterType = parameter.type.accept(typeVisitor);
            parameter.setTag(JavaParameterInfo.class, new JavaParameterInfo(isEnum ? i + 3 : i + 1, parameterType));
        }
    }

    public static void writeDefaultFieldInitializers(JavaWriter constructorWriter, HighLevelDefinition definition, boolean staticFields) {
        JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(constructorWriter);
        for (final IDefinitionMember definitionMember : definition.members) {
            if (!(definitionMember instanceof FieldMember))
                continue;

            FieldMember field = (FieldMember) definitionMember;
            if (field.isStatic() == staticFields && field.initializer != null) {
                if (!staticFields)
                    constructorWriter.loadObject(0);
                field.initializer.accept(expressionVisitor);
                if (staticFields)
                    constructorWriter.putStaticField(definition.name, field.name, Type.getDescriptor(field.type.accept(JavaTypeClassVisitor.INSTANCE)));
                else
                    constructorWriter.putField(definition.name, field.name, Type.getDescriptor(field.type.accept(JavaTypeClassVisitor.INSTANCE)));
            }
        }
    }

    private static final Map<String, String> lambdas = new HashMap<>();
    private static int lambdaCounter = 0;
    private static int lambdaICounter = 0;
    public static String getLambdaInterface(final FunctionHeader header) {
        StringBuilder builder = new StringBuilder("(");
        for (FunctionParameter parameter : header.parameters) {
            builder.append(parameter.type.accept(JavaTypeVisitor.INSTANCE).getDescriptor());
        }

        //final String identifier = header.toString();
        final String identifier = builder.append(")").append(header.returnType.accept(JavaTypeVisitor.INSTANCE).getDescriptor()).toString();
        if(!lambdas.containsKey(identifier)) {
            final String name = "lambdaInterface" + ++lambdaICounter;
            lambdas.put(identifier, name);
            createLambdaInterface(header, name);
        }
        return lambdas.get(identifier);
    }

    private static void createLambdaInterface(FunctionHeader header, String name) {
        ClassWriter ifaceWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ifaceWriter.visitAnnotation("java/lang/FunctionalInterface", true).visitEnd();
        ifaceWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, name, null, "java/lang/Object", null);

        ifaceWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "accept", calcDesc(header, false), calcSign(header, false), null).visitEnd();

        ifaceWriter.visitEnd();

        JavaModule.classes.putIfAbsent(name, ifaceWriter.toByteArray());

        try (FileOutputStream out = new FileOutputStream(name + ".class")){
            out.write(ifaceWriter.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
			return (int)value.value;
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
			throw new UnsupportedOperationException("Not there yet");
		}
	}
}
