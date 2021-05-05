package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.annotations.NativeDefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaImplementation;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.function.BiConsumer;

import static org.objectweb.asm.Type.getInternalName;

public class JavaNativeClassConverter {
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativeMemberConverter memberConverter;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeHeaderConverter headerConverter;

	public JavaNativeClassConverter(JavaNativeTypeConverter typeConverter, JavaNativeMemberConverter memberConverter, JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeHeaderConverter headerConverter) {
		this.typeConverter = typeConverter;
		this.memberConverter = memberConverter;
		this.packageInfo = packageInfo;
		this.typeConversionContext = typeConversionContext;
		this.headerConverter = headerConverter;
	}


	public HighLevelDefinition convertClass(Class<?> cls) {
		HighLevelDefinition definition = checkRegistry(cls);
		final boolean foundRegistry = definition != null;

		if (!foundRegistry) {
			definition = getDefinitionForClass(cls);
		}
		final JavaClass javaClass = getJavaClassFor(cls, definition);

		//Moved up here so that circular dependencies are caught (hopefully)
		typeConversionContext.definitionByClass.put(cls, definition);
		typeConversionContext.compiled.setClassInfo(definition, javaClass);

		if (!shouldLoadClass(cls)) {
			return definition;
		}
		return fillDefinition(cls, definition, javaClass, foundRegistry);
	}

	private JavaClass getJavaClassFor(Class<?> cls, HighLevelDefinition definition) {
		final String internalName = getInternalName(cls);
		final JavaClass.Kind kind;

		if (definition instanceof EnumDefinition) {
			kind = JavaClass.Kind.ENUM;
		} else if (definition.isInterface()) {
			kind = JavaClass.Kind.INTERFACE;
		} else {
			kind = JavaClass.Kind.CLASS;
		}

		return JavaClass.fromInternalName(internalName, kind);
	}

	private HighLevelDefinition getDefinitionForClass(Class<?> cls) {
		boolean isStruct = cls.isAnnotationPresent(ZenCodeType.Struct.class);
		final String specifiedName = getNameForScripts(cls);

		ZSPackage classPkg;
		boolean hasAnnotation = cls.isAnnotationPresent(ZenCodeType.Name.class);
		String className = specifiedName.contains(".") ? specifiedName.substring(specifiedName.lastIndexOf('.') + 1) : specifiedName;
		if (!hasAnnotation) {
			if (!specifiedName.startsWith(packageInfo.getPkg().fullName)) {
				classPkg = packageInfo.getPackage(className);
			} else {
				classPkg = packageInfo.getPackage(packageInfo.getBasePackage() + specifiedName.substring(packageInfo.getPkg().fullName.length()));
				className = specifiedName.substring(specifiedName.lastIndexOf('.') + 1);
			}
		} else {
			if (specifiedName.startsWith(".")) {
				classPkg = packageInfo.getPackage(specifiedName);
				className = specifiedName.substring(specifiedName.lastIndexOf('.') + 1);
			} else if (specifiedName.indexOf('.') >= 0) {
				if (!specifiedName.startsWith(packageInfo.getPkg().fullName))
					throw new IllegalArgumentException("Specified @Name as \"" + specifiedName + "\" for class: \"" + cls
							.toString() + "\" but it's not in the module root package: \"" + packageInfo.getPkg().fullName + "\"");

				classPkg = packageInfo.getPackage(packageInfo.getBasePackage() + specifiedName.substring(packageInfo.getPkg().fullName.length()));
				className = specifiedName.substring(specifiedName.lastIndexOf('.') + 1);
			} else {
				classPkg = packageInfo.getPackage(specifiedName);
				className = specifiedName;
			}
		}


		if (cls.isInterface()) {
			return new InterfaceDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC, null);
		} else if (cls.isEnum()) {
			return new EnumDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC, null);
		} else if (isStruct) {
			return new StructDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC, null);
		} else {
			return new ClassDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC);
		}
	}

	private HighLevelDefinition checkRegistry(Class<?> cls) {
		String name = cls.getCanonicalName();
		if (!name.startsWith("java.lang.") && !name.startsWith("java.util.")) {
			return null;
		}

		name = name.substring("java.lang.".length());
		for (DefinitionTypeID definition : typeConversionContext.registry.getDefinitions()) {
			final HighLevelDefinition highLevelDefinition = definition.definition;
			for (DefinitionAnnotation annotation : highLevelDefinition.annotations) {
				if (annotation instanceof NativeDefinitionAnnotation) {
					final String identifier = ((NativeDefinitionAnnotation) annotation).getIdentifier();
					if (identifier.equals(name) || identifier.equals("stdlib::" + name)) {
						return highLevelDefinition;
					}
				}
			}
		}

		return null;
	}


	public boolean shouldLoadType(Type type) {
		if (type instanceof Class)
			return typeConversionContext.definitionByClass.containsKey(type) || shouldLoadClass((Class<?>) type);
		if (type instanceof ParameterizedType)
			return shouldLoadType(((ParameterizedType) type).getRawType());

		return false;
	}

	public boolean shouldLoadClass(Class<?> cls) {
		return packageInfo.isInBasePackage(getNameForScripts(cls));
	}

	public String getNameForScripts(Class<?> cls) {
		if (cls.isAnnotationPresent(ZenCodeType.Name.class)) {
			return cls.getAnnotation(ZenCodeType.Name.class).value();
		}
		return cls.getName();
	}

	private HighLevelDefinition fillDefinition(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass, boolean foundRegistry) {
		typeConversionContext.compiled.setClassInfo(definition, javaClass);

		fillTypeParameters(cls, definition, foundRegistry);
		fillSupertype(cls, definition, foundRegistry);
		fillImplementedInterfaces(cls, definition, javaClass);

		fillFields(cls, definition, javaClass);
		fillConstructor(cls, definition, javaClass, foundRegistry);

		fillDefaultMethods(cls, definition, javaClass);
		fillAnnotatedMethods(cls, definition, javaClass);

		return definition;
	}

	private void fillDefaultMethods(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass) {
		try {
			BiConsumer<Method, String> createGetter = (method, name) -> {
				GetterMember member = memberConverter.asGetter(typeConversionContext.context, definition, method, name);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.getType()));
			};
			BiConsumer<Method, String> createMethod = (method, name) -> {
				MethodMember member = memberConverter.asMethod(typeConversionContext.context, definition, method, name);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			};
			BiConsumer<Method, OperatorType> createOperator = (method, operator) -> {
				OperatorMember member = memberConverter.asOperator(typeConversionContext.context, definition, method, operator);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			};
			BiConsumer<Method, Boolean> createCaster = (method, implicit) -> {
				CasterMember member = memberConverter.asCaster(typeConversionContext.context, definition, method, implicit);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.toType));
			};


			if (Enum.class.equals(cls)) {
				createMethod.accept(cls.getMethod("compareTo", Enum.class), "compareTo");
			}
			if (cls.isEnum()) {
				createMethod.accept(cls.getMethod("name"), "name");
				createGetter.accept(cls.getMethod("name"), "name");

				createMethod.accept(cls.getMethod("ordinal"), "ordinal");
				createGetter.accept(cls.getMethod("ordinal"), "ordinal");

				createMethod.accept(cls.getMethod("values"), "values");
				createGetter.accept(cls.getMethod("values"), "values");

				createMethod.accept(cls.getMethod("valueOf", String.class), "valueOf");
			}
			if (Object.class.equals(cls)) {
				createMethod.accept(cls.getMethod("toString"), "toString");
				createCaster.accept(cls.getMethod("toString"), false);

				createMethod.accept(cls.getMethod("hashCode"), "hashCode");
				createGetter.accept(cls.getMethod("hashCode"), "hashCode");

				createMethod.accept(cls.getMethod("equals", Object.class), "equals");
				createOperator.accept(cls.getMethod("equals", Object.class), OperatorType.EQUALS);
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private void fillAnnotatedMethods(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass) {
		for (Method method : getMethodsIn(cls)) {
			if (isNotAccessible(method) || isOverridden(cls, method))
				continue;

			ZenCodeType.Method methodAnnotation = getAnnotation(method, ZenCodeType.Method.class);
			if (methodAnnotation != null) {
				MethodMember member = memberConverter.asMethod(typeConversionContext.context, definition, method, methodAnnotation.value());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Getter getter = getAnnotation(method, ZenCodeType.Getter.class);
			if (getter != null) {
				GetterMember member = memberConverter.asGetter(typeConversionContext.context, definition, method, getter.value());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.getType()));
			}

			ZenCodeType.Setter setter = getAnnotation(method, ZenCodeType.Setter.class);
			if (setter != null) {
				SetterMember member = memberConverter.asSetter(typeConversionContext.context, definition, method, setter.value());
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, BasicTypeID.VOID));
			}

			ZenCodeType.Operator operator = getAnnotation(method, ZenCodeType.Operator.class);
			if (operator != null) {
				OperatorMember member = memberConverter.asOperator(typeConversionContext.context, definition, method, OperatorType.valueOf(operator.value().toString()));
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Caster caster = getAnnotation(method, ZenCodeType.Caster.class);
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

	protected Method[] getMethodsIn(Class<?> cls) {
		return cls.getMethods();
	}

	/**
	 * Protected so that other implementations can inject "virtual" Annotations here
	 */
	protected <T extends Annotation> T getAnnotation(Method method, Class<T> cls) {
		return method.getAnnotation(cls);
	}

	/**
	 * Protected so that other implementations can inject "virtual" Annotations here
	 */
	protected ZenCodeType.Constructor getConstructorAnnotation(Constructor<?> constructor) {
		return constructor.getAnnotation(ZenCodeType.Constructor.class);
	}

	/**
	 * Protected so that other implementations can inject "virtual" Annotations here
	 */
	protected ZenCodeType.Field getFieldAnnotation(Field field) {
		return field.getAnnotation(ZenCodeType.Field.class);
	}

	private boolean isOverridden(Class<?> cls, Method method) {
		return !method.getDeclaringClass().equals(cls);
	}

	private void fillConstructor(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass, boolean foundRegistry) {
		boolean hasConstructor = false;
		for (java.lang.reflect.Constructor<?> constructor : cls.getConstructors()) {
			ZenCodeType.Constructor constructorAnnotation = getConstructorAnnotation(constructor);
			if (constructorAnnotation != null) {
				ConstructorMember member = memberConverter.asConstructor(typeConversionContext.context, definition, constructor);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, constructor));
				hasConstructor = true;
			}
		}


		if (!hasConstructor && !foundRegistry) {
			// no constructor! make a private constructor so the compiler doesn't add one
			ConstructorMember member = new ConstructorMember(CodePosition.BUILTIN, definition, Modifiers.PRIVATE, new FunctionHeader(BasicTypeID.VOID), BuiltinID.CLASS_DEFAULT_CONSTRUCTOR);
			definition.addMember(member);
		}
	}

	private void fillFields(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass) {
		TypeID thisType = typeConversionContext.registry.getForMyDefinition(definition);
		for (Field field : cls.getDeclaredFields()) {
			String fieldName = field.getName();
			ZenCodeType.Field annotation = getFieldAnnotation(field);
			if (annotation != null) {
				if (!annotation.value().isEmpty()) {
					fieldName = annotation.value();
				}
			} else if (!field.isEnumConstant()) {
				continue;
			}
			if (!Modifier.isPublic(field.getModifiers()))
				continue;


			TypeID fieldType = typeConverter.loadStoredType(typeConversionContext.context, field.getAnnotatedType());
			FieldMember member = new FieldMember(CodePosition.NATIVE, definition, headerConverter.getMethodModifiers(field), fieldName, thisType, fieldType, typeConversionContext.registry, 0, 0, null);
			definition.addMember(member);
			typeConversionContext.compiled.setFieldInfo(member, new JavaField(javaClass, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType())));
		}
	}

	private void fillImplementedInterfaces(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass) {
		for (AnnotatedType iface : cls.getAnnotatedInterfaces()) {
			if (shouldLoadType(iface.getType())) {
				TypeID type = typeConverter.loadType(typeConversionContext.context, iface);
				ImplementationMember member = new ImplementationMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC, type);
				definition.members.add(member);
				typeConversionContext.compiled.setImplementationInfo(member, new JavaImplementation(true, javaClass));
			}
		}
	}

	private void fillSupertype(Class<?> cls, HighLevelDefinition definition, boolean foundRegistry) {
		if (!foundRegistry && definition instanceof ClassDefinition && cls.getAnnotatedSuperclass() != null && shouldLoadType(cls.getAnnotatedSuperclass().getType())) {
			definition.setSuperType(typeConverter.loadType(typeConversionContext.context, cls.getAnnotatedSuperclass()));
		}

		if (!foundRegistry && definition.getSuperType() == null && cls != Object.class) {
			if (!(definition instanceof EnumDefinition)) {

				definition.setSuperType(typeConverter.loadType(typeConversionContext.context, Object.class, false, false));
			} else if (cls != Enum.class) {
				TypeID typeID = typeConverter.loadType(typeConversionContext.context, cls.getSuperclass(), false, false);
				if(!(typeID instanceof DefinitionTypeID)){
					return;
				}
				DefinitionTypeID superDefTypeId = (DefinitionTypeID) typeID;
				DefinitionTypeID definitionTypeID = typeConversionContext.registry.getForMyDefinition(definition);
				definition.setSuperType(typeConversionContext.registry.getForDefinition(superDefTypeId.definition, definitionTypeID));
			}
		}
	}

	private void fillTypeParameters(Class<?> cls, HighLevelDefinition definition, boolean foundRegistry) {
		//TypeVariableContext typeConversionContext.context = new TypeVariableContext();
		TypeVariable<?>[] javaTypeParameters = cls.getTypeParameters();
		if (!foundRegistry || definition.typeParameters == null || definition.typeParameters.length != cls.getTypeParameters().length) {
			definition.typeParameters = new TypeParameter[cls.getTypeParameters().length];
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			//Put up here for nested parameters?
			TypeVariable<?> typeVariable = javaTypeParameters[i];
			TypeParameter parameter;
			if (foundRegistry && definition.typeParameters.length == cls.getTypeParameters().length) {
				parameter = definition.typeParameters[i];
			} else {
				parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			}
			definition.typeParameters[i] = parameter;
			typeConversionContext.context.put(typeVariable, parameter);
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<?> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = definition.typeParameters[i];
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				if (bound.getType() == Object.class) {
					continue; //Makes the stdlib types work as they have "no" bounds for T
				}
				TypeID type = typeConverter.loadType(typeConversionContext.context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}
	}
}
