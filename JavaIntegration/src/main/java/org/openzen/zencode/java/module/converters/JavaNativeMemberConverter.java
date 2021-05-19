package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.java.module.TypeVariableContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import static org.objectweb.asm.Type.getConstructorDescriptor;

public class JavaNativeMemberConverter {

	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeHeaderConverter headerConverter;

	public JavaNativeMemberConverter(JavaNativeTypeConverter typeConverter, JavaNativeTypeConversionContext typeConversionContext, JavaNativeHeaderConverter headerConverter) {
		this.typeConverter = typeConverter;
		this.typeConversionContext = typeConversionContext;
		this.headerConverter = headerConverter;
	}

	@SuppressWarnings("rawtypes")
	public ConstructorMember asConstructor(TypeVariableContext context, HighLevelDefinition definition, java.lang.reflect.Constructor method) {
		FunctionHeader header = headerConverter.getHeader(context, method);
		return new ConstructorMember(
				CodePosition.NATIVE,
				definition,
				Modifiers.PUBLIC,
				header,
				null);
	}

	public MethodMember asMethod(TypeVariableContext context, HighLevelDefinition definition, Method method, String methodName) {
		String name = methodName != null && !methodName.isEmpty() ? methodName : method.getName();
		FunctionHeader header = headerConverter.getHeader(context, method);
		return new MethodMember(
				CodePosition.NATIVE,
				definition,
				headerConverter.getMethodModifiers(method),
				name,
				header,
				null);
	}

	public OperatorMember asOperator(TypeVariableContext context, HighLevelDefinition definition, Method method, OperatorType operatorType) {
		FunctionHeader header = headerConverter.getHeader(context, method);
		if (Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException("operator method \"" + method.toString() + "\"cannot be static");

		// TODO: check number of parameters
		//if (header.parameters.length != annotation.value().parameters)

		return new OperatorMember(
				CodePosition.NATIVE,
				definition,
				headerConverter.getMethodModifiers(method),
				operatorType,
				header,
				null);
	}

	public GetterMember asGetter(TypeVariableContext context, HighLevelDefinition definition, Method method, String getterName) {
		TypeID type = typeConverter.loadStoredType(context, method.getAnnotatedReturnType());
		String name = null;
		if (getterName != null && !getterName.isEmpty())
			name = getterName;
		if (name == null)
			name = translateGetterName(method.getName());

		return new GetterMember(CodePosition.NATIVE, definition, headerConverter.getMethodModifiers(method), name, type, null);
	}

	public SetterMember asSetter(TypeVariableContext context, HighLevelDefinition definition, Method method, String setterName) {
		if (method.getParameterCount() != 1)
			throw new IllegalArgumentException("Illegal setter: \"" + method.toString() + "\"must have exactly 1 parameter");

		TypeID type = typeConverter.loadStoredType(context, method.getAnnotatedParameterTypes()[0]);
		String name = null;
		if (setterName != null && !setterName.isEmpty())
			name = setterName;
		if (name == null)
			name = translateSetterName(method.getName());

		return new SetterMember(CodePosition.NATIVE, definition, headerConverter.getMethodModifiers(method), name, type, null);
	}

	public CasterMember asCaster(TypeVariableContext context, HighLevelDefinition definition, Method method, boolean implicit) {
		int modifiers = Modifiers.PUBLIC;
		if (implicit)
			modifiers |= Modifiers.IMPLICIT;

		TypeID toType = typeConverter.loadStoredType(context, method.getAnnotatedReturnType());
		return new CasterMember(CodePosition.NATIVE, definition, modifiers, toType, null);
	}

	public String translateGetterName(String name) {
		if (name.startsWith("get"))
			return name.substring(3, 4).toLowerCase() + name.substring(4);

		return name;
	}

	public String translateSetterName(String name) {
		if (name.startsWith("set"))
			return name.substring(3, 4).toLowerCase() + name.substring(4);

		return name;
	}

	@SuppressWarnings("rawtypes")
	public JavaMethod getMethod(JavaClass cls, java.lang.reflect.Constructor constructor) {
		return new JavaMethod(
				cls,
				JavaMethod.Kind.CONSTRUCTOR,
				"<init>",
				false,
				getConstructorDescriptor(constructor),
				constructor.getModifiers(),
				false);
	}

	public JavaMethod getMethod(JavaClass cls, Method method, TypeID result) {
		JavaMethod.Kind kind;
		if (method.getName().equals("<init>"))
			kind = JavaMethod.Kind.CONSTRUCTOR;
		else if (method.getName().equals("<clinit>"))
			kind = JavaMethod.Kind.STATICINIT;
		else if (Modifier.isStatic(method.getModifiers()))
			kind = JavaMethod.Kind.STATIC;
		else
			kind = JavaMethod.Kind.INSTANCE;

		final int length = method.getTypeParameters().length;
		boolean compile = length > 0 && length == Arrays.stream(method.getParameterTypes())
				.filter(s -> s.getCanonicalName().contentEquals("java.lang.Class"))
				.count();

		return new JavaMethod(cls, kind, method.getName(), compile, org.objectweb.asm.Type.getMethodDescriptor(method), method
				.getModifiers(), result.isGeneric());
	}

	public FunctionalMemberRef loadStaticMethod(Method method, HighLevelDefinition definition) {
		if (!Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException("Method \"" + method.toString() + "\" is not static");

		JavaClass jcls = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(method.getDeclaringClass()), JavaClass.Kind.CLASS);

		if (method.isAnnotationPresent(ZenCodeType.Method.class)) {
			//The method should already have been loaded let's use that one.
			final String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
			final Optional<MethodMember> matchingMember = definition.members.stream()
					.filter(m -> m instanceof MethodMember)
					.map(m -> ((MethodMember) m))
					.filter(m -> {
						final JavaMethod methodInfo = typeConversionContext.compiled.optMethodInfo(m);
						return methodInfo != null && methodDescriptor.equals(methodInfo.descriptor);
					})
					.findAny();

			if (matchingMember.isPresent()) {
				return matchingMember.get().ref(typeConversionContext.registry.getForDefinition(definition));
			}
		}
		MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, method.getName(), headerConverter.getHeader(typeConversionContext.context, method), null);
		definition.addMember(methodMember);
		boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
		typeConversionContext.compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, method.getName(), false, org.objectweb.asm.Type.getMethodDescriptor(method), method.getModifiers(), isGenericResult));
		return methodMember.ref(typeConversionContext.registry.getForDefinition(definition));
	}
}
