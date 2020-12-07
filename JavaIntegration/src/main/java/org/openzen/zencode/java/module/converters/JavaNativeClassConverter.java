package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.annotations.NativeDefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaImplementation;

import java.lang.reflect.*;

public class JavaNativeClassConverter {
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativeMemberConverter memberConverter;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final GlobalTypeRegistry registry;

	public JavaNativeClassConverter(JavaNativeTypeConverter typeConverter, JavaNativeMemberConverter memberConverter, JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, GlobalTypeRegistry registry) {
		this.typeConverter = typeConverter;
		this.memberConverter = memberConverter;
		this.packageInfo = packageInfo;
		this.typeConversionContext = typeConversionContext;
		this.registry = registry;
	}


	public <T> HighLevelDefinition convertClass(Class<T> cls) {


		String className = cls.getName();
		boolean isStruct = cls.isAnnotationPresent(ZenCodeType.Struct.class);

		HighLevelDefinition definition = checkRegistry(cls);
		final boolean foundRegistry = definition != null;
		String internalName = org.objectweb.asm.Type.getInternalName(cls);
		JavaClass javaClass;

		if (foundRegistry) {
			javaClass = JavaClass.fromInternalName(internalName, definition.isInterface() ? JavaClass.Kind.INTERFACE : JavaClass.Kind.CLASS);
		} else {
			ZSPackage classPkg;
			ZenCodeType.Name nameAnnotation = cls.getDeclaredAnnotation(ZenCodeType.Name.class);
			className = className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className;
			if (nameAnnotation == null) {
				classPkg = packageInfo.getPackage(className);
			} else {
				String specifiedName = nameAnnotation.value();
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
					className = nameAnnotation.value();
				}
			}


			if (cls.isInterface()) {
				definition = new InterfaceDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC, null);
				javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.INTERFACE);
			} else if (cls.isEnum()) {
				definition = new EnumDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC, null);
				javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.ENUM);
			} else if (isStruct) {
				definition = new StructDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC, null);
				javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.CLASS);
			} else {
				definition = new ClassDefinition(CodePosition.NATIVE, packageInfo.getModule(), classPkg, className, Modifiers.PUBLIC);
				javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.CLASS);

			}
		}

		//Moved up here so that circular dependencies are caught (hopefully)
		typeConversionContext.definitionByClass.put(cls, definition);
		if (!shouldLoadClass(cls)) {
			return definition;
		}

		//TypeVariableContext typeConversionContext.context = new TypeVariableContext();
		TypeVariable<Class<T>>[] javaTypeParameters = cls.getTypeParameters();
		if (!foundRegistry || definition.typeParameters == null || definition.typeParameters.length != cls.getTypeParameters().length) {
			definition.typeParameters = new TypeParameter[cls.getTypeParameters().length];
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			//Put up here for nested parameters?
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
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
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = definition.typeParameters[i];
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				if (bound.getType() == Object.class) {
					continue; //Makes the stdlibs types work as they have "no" bounds for T
				}
				TypeID type = typeConverter.loadType(typeConversionContext.context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}

		if (!foundRegistry && definition instanceof ClassDefinition && cls.getAnnotatedSuperclass() != null && shouldLoadType(cls.getAnnotatedSuperclass().getType())) {
			definition.setSuperType(typeConverter.loadType(typeConversionContext.context, cls.getAnnotatedSuperclass()));
		}

		if (!foundRegistry && definition.getSuperType() == null && cls != Object.class && !(definition instanceof EnumDefinition)) {
			definition.setSuperType(typeConverter.loadType(typeConversionContext.context, Object.class, false, false));
		}

		for (AnnotatedType iface : cls.getAnnotatedInterfaces()) {
			if (shouldLoadType(iface.getType())) {
				TypeID type = typeConverter.loadType(typeConversionContext.context, iface);
				ImplementationMember member = new ImplementationMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC, type);
				definition.members.add(member);
				typeConversionContext.compiled.setImplementationInfo(member, new JavaImplementation(true, javaClass));
			}
		}

		typeConversionContext.compiled.setClassInfo(definition, javaClass);

		TypeID thisType = registry.getForMyDefinition(definition);
		for (Field field : cls.getDeclaredFields()) {
			ZenCodeType.Field annotation = field.getAnnotation(ZenCodeType.Field.class);
			if (annotation == null)
				continue;
			if (!Modifier.isPublic(field.getModifiers()))
				continue;

			final String fieldName = annotation.value().isEmpty() ? field.getName() : annotation.value();

			TypeID fieldType = typeConverter.loadStoredType(typeConversionContext.context, field.getAnnotatedType());
			FieldMember member = new FieldMember(CodePosition.NATIVE, definition, memberConverter.getMethodModifiers(field), fieldName, thisType, fieldType, registry, 0, 0, null);
			definition.addMember(member);
			typeConversionContext.compiled.setFieldInfo(member, new JavaField(javaClass, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType())));
		}

		boolean hasConstructor = false;
		for (java.lang.reflect.Constructor<?> constructor : cls.getConstructors()) {
			ZenCodeType.Constructor constructorAnnotation = constructor.getAnnotation(ZenCodeType.Constructor.class);
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

		for (Method method : cls.getDeclaredMethods()) {
			ZenCodeType.Method methodAnnotation = method.getAnnotation(ZenCodeType.Method.class);
			if (methodAnnotation != null) {

				//Simple check if the method was overwritten
				try {
					if (!cls.getDeclaredMethod(method.getName(), method.getParameterTypes()).equals(method)) {
						continue;
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					continue;
				}

				MethodMember member = memberConverter.asMethod(typeConversionContext.context, definition, method, methodAnnotation);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
			if (getter != null) {
				GetterMember member = memberConverter.asGetter(typeConversionContext.context, definition, method, getter);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.getType()));
			}

			ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
			if (setter != null) {
				SetterMember member = memberConverter.asSetter(typeConversionContext.context, definition, method, setter);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, BasicTypeID.VOID));
			}

			ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
			if (operator != null) {
				OperatorMember member = memberConverter.asOperator(typeConversionContext.context, definition, method, operator);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
			if (caster != null) {
				CasterMember member = memberConverter.asCaster(typeConversionContext.context, definition, method, caster);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.toType));
			}

			/*if (!annotated) {
				MethodMember member = asMethod(definition, method, null);
				definition.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}*/
		}

		return definition;
	}

	private <T> HighLevelDefinition checkRegistry(Class<T> cls) {
		String name = cls.getCanonicalName();
		if (!name.startsWith("java.lang.") && !name.startsWith("java.util.")) {
			return null;
		}

		name = name.substring("java.lang.".length());
		for (DefinitionTypeID definition : registry.getDefinitions()) {
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



	private boolean shouldLoadType(Type type) {
		if (type instanceof Class)
			return typeConversionContext.definitionByClass.containsKey(type) || shouldLoadClass((Class<?>) type);
		if (type instanceof ParameterizedType)
			return shouldLoadType(((ParameterizedType) type).getRawType());

		return false;
	}

	private boolean shouldLoadClass(Class<?> cls) {
		return packageInfo.isInBasePackage(getClassName(cls));
	}

	private String getClassName(Class<?> cls) {
		return cls.isAnnotationPresent(ZenCodeType.Name.class) ? cls.getAnnotation(ZenCodeType.Name.class).value() : cls.getName();
	}
}
