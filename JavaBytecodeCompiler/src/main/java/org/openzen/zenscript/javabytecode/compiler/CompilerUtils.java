package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.switchvalue.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaTypeParameterInfo;

public class CompilerUtils {

	private CompilerUtils() {}

	public static boolean isPrimitive(TypeID id) {
		return id instanceof BasicTypeID
				|| (id.isOptional() && id.withoutOptional() == BasicTypeID.USIZE);
	}

	public static boolean isLarge(StoredType type) {
		return type.type == BasicTypeID.DOUBLE || type.type == BasicTypeID.LONG;
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
		int index = header.getNumberOfTypeParameters();
		for (int i = 0; i < header.typeParameters.length; i++) {
			TypeParameter parameter = header.typeParameters[i];
			module.setTypeParameterInfo(parameter, new JavaTypeParameterInfo(index++));
		}
        for (int i = 0; i < header.parameters.length; i++) {
            FunctionParameter parameter = header.parameters[i];
            String parameterType = context.getDescriptor(parameter.type);
            module.setParameterInfo(parameter, new JavaParameterInfo(isStatic ? index : index + 1, parameterType));
			index++;
        }
    }

    public static void tagConstructorParameters(JavaBytecodeContext context, JavaCompiledModule module, HighLevelDefinition definition, FunctionHeader header, boolean isEnum) {
		int index = header.getNumberOfTypeParameters();
		for (int i = 0; i < definition.typeParameters.length; i++) {
			JavaTypeParameterInfo info = module.getTypeParameterInfo(definition.typeParameters[i]);
			if (info.field != null)
				index++;
		}
		for (int i = 0; i < header.typeParameters.length; i++) {
			TypeParameter parameter = header.typeParameters[i];
			module.setTypeParameterInfo(parameter, new JavaTypeParameterInfo(index++));
		}
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
                    constructorWriter.putStaticField(definition.name, field.name, context.getDescriptor(field.getType()));
                else
                    constructorWriter.putField(definition.name, field.name, context.getDescriptor(field.getType()));
            }
        }
    }

	public static int getKeyForSwitch(SwitchValue expression) {
		return expression.accept(JavaSwitchKeyVisitor.INSTANCE);
	}
}
