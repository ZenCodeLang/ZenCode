package org.openzen.zencode.java.module;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.converters.JavaNativeHeaderConverter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JavaNativeTypeTemplate {
	private final JavaRuntimeClass class_;
	private final TypeSymbol target;

	private List<MethodSymbol> constructors;
	private Map<String, List<MethodSymbol>> methods;

	public JavaNativeTypeTemplate(TypeSymbol target, JavaRuntimeClass class_) {
		this.target = target;
		this.class_ = class_;
	}

	public List<MethodSymbol> getConstructors() {
		if (constructors == null) {
			List<MethodSymbol> result = new ArrayList<>();
			for (Constructor<?> constructor : class_.cls.getConstructors()) {
				if (constructor.isAnnotationPresent(ZenCodeType.Constructor.class)) {
					result.add(loadJavaMethod(constructor));
				}
			}
			this.constructors = result;
		}
		return this.constructors;
	}

	public List<MethodSymbol> getMethod(String name) {
		if (methods == null) {
			loadMethods();
		}
		return methods.getOrDefault(name, Collections.emptyList());
	}

	private void loadMethods() {
		JavaNativeHeaderConverter headerConverter = class_.module.nativeConverter.headerConverter;
		for (Method method : class_.cls.getMethods()) {
			if (isNotAccessible(method) || isOverridden(class_.cls, method))
				continue;

			ZenCodeType.Method methodAnnotation = method.getAnnotation(ZenCodeType.Method.class);
			if (methodAnnotation != null) {
				headerConverter.
				MethodMember member = memberConverter.asMethod(typeConversionContext.context, definition, method, methodAnnotation.value());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
			if (getter != null) {
				GetterMember member = memberConverter.asGetter(typeConversionContext.context, definition, method, getter.value());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.getType()));
			}

			ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
			if (setter != null) {
				SetterMember member = memberConverter.asSetter(typeConversionContext.context, definition, method, setter.value());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, BasicTypeID.VOID));
			}

			ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
			if (operator != null) {
				OperatorMember member = memberConverter.asOperator(typeConversionContext.context, definition, method, OperatorType.valueOf(operator.value().toString()));
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
			if (caster != null) {
				CasterMember member = memberConverter.asCaster(typeConversionContext.context, definition, method, caster.implicit());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.toType));
			}
		}
	}

	private boolean isNotAccessible(Method method) {
		return !Modifier.isPublic(method.getModifiers());
	}

	private boolean isOverridden(Class<?> cls, Method method) {
		return !method.getDeclaringClass().equals(cls) || method.isBridge();
	}

	private MethodSymbol loadJavaMethod(Constructor<?> constructor) {
		JavaRuntimeMethod method = new JavaRuntimeMethod(class_, target, constructor);
		class_.module.getCompiled().setMethodInfo(method, method);
		return method;
	}
}
