package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.ExpressionSymbol;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.partial.PartialStaticMemberGroupExpression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;

import java.lang.annotation.Annotation;
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


	public void addGlobal(Class<?> cls, HighLevelDefinition definition) {
		final JavaClass jcls;

		if (typeConversionContext.compiled.hasClassInfo(definition)) {
			jcls = typeConversionContext.compiled.getClassInfo(definition);
		} else {
			jcls = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(cls), JavaClass.Kind.CLASS);
			typeConversionContext.compiled.setClassInfo(definition, jcls);
		}

		TypeID thisType = typeConversionContext.registry.getForMyDefinition(definition);
		//TypeVariableContext typeConversionContext.context = new TypeVariableContext();

		for (Field field : cls.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(field.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = field.getAnnotation(ZenCodeGlobals.Global.class);
			TypeID type = typeConverter.loadStoredType(typeConversionContext.context, field.getAnnotatedType());
			String name = global.value().isEmpty() ? field.getName() : global.value();
			FieldMember fieldMember = new FieldMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, thisType, type, typeConversionContext.registry, Modifiers.PUBLIC, 0, null);
			definition.addMember(fieldMember);
			JavaField javaField = new JavaField(jcls, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType()));
			typeConversionContext.compiled.setFieldInfo(fieldMember, javaField);
			typeConversionContext.compiled.setFieldInfo(fieldMember.autoGetter, javaField);

			typeConversionContext.globals.put(name, new ExpressionSymbol((position, scope) -> {
				//autoGetter cannot be null, since we pass an autoGetter value to the constructor
				//noinspection ConstantConditions
				return new StaticGetterExpression(CodePosition.BUILTIN, fieldMember.autoGetter.ref(thisType, GenericMapper.EMPTY));
			}));
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
			typeConversionContext.globals.put(name, new ExpressionSymbol((position, scope) -> {
				TypeMembers members = scope.getTypeMembers(thisType);
				return new PartialStaticMemberGroupExpression(position, scope, thisType, members.getGroup(name), TypeID.NONE);
			}));
		}
	}
}
