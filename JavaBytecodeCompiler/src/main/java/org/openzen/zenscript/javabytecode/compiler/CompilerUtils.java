package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaParameterInfo;
import org.openzen.zenscript.javashared.JavaTypeParameterInfo;

import java.util.List;

public class CompilerUtils {

	private CompilerUtils() {
	}

	public static boolean isPrimitive(TypeID id) {
		if (id instanceof BasicTypeID) {
			return id != BasicTypeID.STRING;
		}

		return id.isOptional() && id.withoutOptional() == BasicTypeID.USIZE;
	}

	public static boolean isLarge(TypeID type) {
		return type == BasicTypeID.DOUBLE || type == BasicTypeID.LONG || type == BasicTypeID.ULONG;
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

	public static void tagMethodParameters(JavaBytecodeContext context, JavaCompiledModule module, FunctionHeader header, boolean isStatic, List<TypeParameter> baseTypeTypeParameters) {
		int index = isStatic ? 0 : 1;

		for (TypeParameter baseTypeTypeParameter : baseTypeTypeParameters) {
			module.setTypeParameterInfo(baseTypeTypeParameter, new JavaTypeParameterInfo(index));
			index++;
		}

		for (int i = 0; i < header.typeParameters.length; i++) {
			TypeParameter parameter = header.typeParameters[i];
			module.setTypeParameterInfo(parameter, new JavaTypeParameterInfo(index));
			index++;
		}
		for (int i = 0; i < header.parameters.length; i++) {
			FunctionParameter parameter = header.parameters[i];
			String parameterType = context.getDescriptor(parameter.type);
			module.setParameterInfo(parameter, new JavaParameterInfo(index, parameterType));
			index += isLarge(parameter.type) ? 2 : 1;
		}
	}

	public static void tagConstructorParameters(JavaBytecodeContext context, JavaCompiledModule module, HighLevelDefinition definition, FunctionHeader header, boolean isEnum) {
		int index = isEnum ? 3 : 1;
		for (TypeParameter typeParameter : definition.typeParameters) {
			final JavaField field = new JavaField(context.getJavaClass(definition),
					"typeOf" + typeParameter.name,
					"Ljava/lang/Class;",
					//"Ljava/lang/Class;"
					"Ljava/lang/Class<T" + typeParameter.name + ";>;"
			);
			final JavaTypeParameterInfo info = new JavaTypeParameterInfo(index, field);
			module.setTypeParameterInfo(typeParameter, info);
			index++;
		}

		for (int i = 0; i < header.typeParameters.length; i++) {
			TypeParameter typeParameter = header.typeParameters[i];
			final JavaField field = new JavaField(context.getJavaClass(definition),
					"typeOf" + typeParameter.name,
					"Ljava/lang/Class;",
					//"Ljava/lang/Class;"
					"Ljava/lang/Class<T" + typeParameter.name + ";>;"
			);
			final JavaTypeParameterInfo info = new JavaTypeParameterInfo(index, field);
			module.setTypeParameterInfo(typeParameter, info);
			index++;
		}
		for (int i = 0; i < header.parameters.length; i++) {
			FunctionParameter parameter = header.parameters[i];
			String parameterType = context.getDescriptor(parameter.type);
			module.setParameterInfo(parameter, new JavaParameterInfo(index, parameterType));
			index += isLarge(parameter.type) ? 2 : 1;
		}
		/*
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
		 */
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
					constructorWriter.putStaticField(context.getJavaClass(definition).internalName , field.name, context.getDescriptor(field.getType()));
				else
					constructorWriter.putField(definition.name, field.name, context.getDescriptor(field.getType()));
			}
		}
	}

	public static int getKeyForSwitch(SwitchValue expression) {
		return expression.accept(JavaSwitchKeyVisitor.INSTANCE);
	}
}
