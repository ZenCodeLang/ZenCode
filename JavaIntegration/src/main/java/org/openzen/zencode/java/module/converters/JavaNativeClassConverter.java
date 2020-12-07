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

import static org.objectweb.asm.Type.getInternalName;

public class JavaNativeClassConverter {
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativeMemberConverter memberConverter;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeHeaderConverter headerConverter;
	private final GlobalTypeRegistry registry;

	public JavaNativeClassConverter(JavaNativeTypeConverter typeConverter, JavaNativeMemberConverter memberConverter, JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeHeaderConverter headerConverter, GlobalTypeRegistry registry) {
		this.typeConverter = typeConverter;
		this.memberConverter = memberConverter;
		this.packageInfo = packageInfo;
		this.typeConversionContext = typeConversionContext;
		this.headerConverter = headerConverter;
		this.registry = registry;
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
		String className = cls.getName();

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

	private HighLevelDefinition fillDefinition(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass, boolean foundRegistry) {
		typeConversionContext.compiled.setClassInfo(definition, javaClass);

		fillTypeParameters(cls, definition, foundRegistry);
		fillSupertype(cls, definition, foundRegistry);
		fillImplementedInterfaces(cls, definition, javaClass);

		fillFields(cls, definition, javaClass);
		fillConstructor(cls, definition, javaClass, foundRegistry);

		fillAnnotatedMethods(cls, definition, javaClass);

		return definition;
	}

	private void fillAnnotatedMethods(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass) {
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
		}
	}

	private void fillConstructor(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass, boolean foundRegistry) {
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
	}

	private void fillFields(Class<?> cls, HighLevelDefinition definition, JavaClass javaClass) {
		TypeID thisType = registry.getForMyDefinition(definition);
		for (Field field : cls.getDeclaredFields()) {
			ZenCodeType.Field annotation = field.getAnnotation(ZenCodeType.Field.class);
			if (annotation == null)
				continue;
			if (!Modifier.isPublic(field.getModifiers()))
				continue;

			final String fieldName = annotation.value().isEmpty() ? field.getName() : annotation.value();

			TypeID fieldType = typeConverter.loadStoredType(typeConversionContext.context, field.getAnnotatedType());
			FieldMember member = new FieldMember(CodePosition.NATIVE, definition, headerConverter.getMethodModifiers(field), fieldName, thisType, fieldType, registry, 0, 0, null);
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

		if (!foundRegistry && definition.getSuperType() == null && cls != Object.class && !(definition instanceof EnumDefinition)) {
			definition.setSuperType(typeConverter.loadType(typeConversionContext.context, Object.class, false, false));
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
			if (definition.typeParameters.length == cls.getTypeParameters().length) {
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
					continue; //Makes the stdlibs types work as they have "no" bounds for T
				}
				TypeID type = typeConverter.loadType(typeConversionContext.context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}
	}
}
