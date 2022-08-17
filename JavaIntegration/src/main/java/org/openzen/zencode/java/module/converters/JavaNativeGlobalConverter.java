package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.globals.ExpressionGlobal;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class JavaNativeGlobalConverter {
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativeMemberConverter memberConverter;

	public JavaNativeGlobalConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeMemberConverter memberConverter) {
		this.typeConversionContext = typeConversionContext;
		this.typeConverter = typeConverter;
		this.memberConverter = memberConverter;
	}


	public void addGlobal(Class<?> cls, TypeSymbol definition) {
		final JavaClass jcls;

		if (typeConversionContext.compiled.hasClassInfo(definition)) {
			jcls = typeConversionContext.compiled.getClassInfo(definition);
		} else {
			jcls = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(cls), JavaClass.Kind.CLASS);
			typeConversionContext.compiled.setClassInfo(definition, jcls);
		}

		TypeID thisType = DefinitionTypeID.createThis(definition);
		//TypeVariableContext typeConversionContext.context = new TypeVariableContext();

		for (Field field : cls.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(field.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = field.getAnnotation(ZenCodeGlobals.Global.class);
			TypeID type = typeConverter.loadStoredType(typeConversionContext.context, field.getAnnotatedType());
			String name = global.value().isEmpty() ? field.getName() : global.value();
			FieldMember fieldMember = new FieldMember(CodePosition.NATIVE, definition, new Modifiers(Modifiers.FLAG_PUBLIC | Modifiers.FLAG_STATIC), name, thisType, type, typeConversionContext.registry, new Modifiers(Modifiers.FLAG_PUBLIC), new Modifiers(0), null);
			definition.addMember(fieldMember);
			JavaField javaField = new JavaField(jcls, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType()));
			typeConversionContext.compiled.setFieldInfo(fieldMember, javaField);
			typeConversionContext.compiled.setFieldInfo(fieldMember.autoGetter, javaField);

			typeConversionContext.globals.put(name, new ExpressionGlobal((compiler, position, typeArguments) ->
				compiler.at(position).getStaticField(new FieldInstance(fieldMember)).wrap(compiler)));
		}

		for (Method method : cls.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(method.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = method.getAnnotation(ZenCodeGlobals.Global.class);
			String name = global.value().isEmpty() ? method.getName() : global.value();
			MethodMember methodMember = memberConverter.asMethod(typeConversionContext.context, definition, method, name);
			definition.addMember(methodMember);

			typeConversionContext.compiled.setMethodInfo(methodMember, memberConverter.getMethod(jcls, method, typeConverter.loadType(typeConversionContext.context, method.getAnnotatedReturnType())));
			typeConversionContext.globals.put(name, new ExpressionGlobal((compiler, position, typeArguments) ->
				compiler.at(position).callStatic(new MethodInstance(methodMember), CallArguments.EMPTY).wrap(compiler)));
		}
	}
}
