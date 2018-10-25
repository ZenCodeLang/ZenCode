package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.switchvalue.*;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaCompiledModule;

public class CompilerUtils {
	private CompilerUtils() {}

	public static boolean isPrimitive(TypeID id) {
		return id instanceof BasicTypeID
				|| (id.isOptional() && id.withoutOptional() == BasicTypeID.USIZE);
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

    public static void tagMethodParameters(JavaBytecodeContext context, JavaCompiledModule module, FunctionHeader header, boolean isStatic) {
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            String parameterType = context.getDescriptor(parameter.type);
            module.setParameterInfo(parameter, new JavaParameterInfo(isStatic ? i : i + 1, parameterType));
        }
    }

    public static void tagConstructorParameters(JavaBytecodeContext context, JavaCompiledModule module, FunctionHeader header, boolean isEnum) {
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            String parameterType = context.getDescriptor(parameter.type);
			module.setParameterInfo(parameter, new JavaParameterInfo(isEnum ? i + 3 : i + 1, parameterType));
        }
    }

    public static void writeDefaultFieldInitializers(JavaBytecodeContext context, JavaWriter constructorWriter, HighLevelDefinition definition, boolean staticFields) {
        JavaExpressionVisitor expressionVisitor = new JavaExpressionVisitor(context, context.getJavaModule(definition.module), constructorWriter);
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

	public static int getKeyForSwitch(SwitchValue expression) {
		return expression.accept(JavaSwitchKeyVisitor.INSTANCE);
	}
}
