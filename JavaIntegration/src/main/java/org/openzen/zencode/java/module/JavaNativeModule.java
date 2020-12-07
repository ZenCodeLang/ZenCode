/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java.module;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.converters.JavaNativeExpansionConverter;
import org.openzen.zencode.java.module.converters.JavaNativeMemberConverter;
import org.openzen.zencode.java.module.converters.JavaNativeTypeConverter;
import org.openzen.zencode.java.module.converters.JavaNativePackageInfo;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.annotations.NativeDefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.expression.ExpressionSymbol;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.PartialStaticMemberGroupExpression;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author Stan Hebben
 */
public class JavaNativeModule {
	public final Map<String, ISymbol> globals = new HashMap<>();

	//TODO Fix visibility
	public final Map<Class<?>, HighLevelDefinition> definitionByClass = new HashMap<>();
	//TODO Fix visibility
	public final JavaNativeMemberConverter memberConverter;
	private final GlobalTypeRegistry registry;
	private final PackageDefinitions definitions = new PackageDefinitions();
	private final JavaCompiledModule compiled;
	private final TypeVariableContext context = new TypeVariableContext();
	private final IZSLogger logger;
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeExpansionConverter expansionConverter;

	public JavaNativeModule(
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage,
			GlobalTypeRegistry registry,
			JavaNativeModule[] dependencies) {
		this.packageInfo = new JavaNativePackageInfo(pkg, basePackage, new Module(name));
		this.registry = registry;
		this.logger = logger;

		this.compiled = new JavaCompiledModule(packageInfo.getModule(), FunctionParameter.NONE);

		for (JavaNativeModule dependency : dependencies) {
			definitionByClass.putAll(dependency.definitionByClass);
			context.putAllFrom(dependency.context);
			compiled.addAllFrom(dependency.compiled);
		}

		this.typeConverter = new JavaNativeTypeConverter(context, registry, packageInfo, globals, this);
		this.memberConverter = new JavaNativeMemberConverter(typeConverter, packageInfo, globals, registry);
		this.expansionConverter = new JavaNativeExpansionConverter(typeConverter, logger, packageInfo, memberConverter, context, compiled, definitions, definitionByClass);
	}

	public SemanticModule toSemantic(ModuleSpace space) {
		return new SemanticModule(
				packageInfo.getModule(),
				SemanticModule.NONE,
				FunctionParameter.NONE,
				SemanticModule.State.NORMALIZED,
				space.rootPackage,
				packageInfo.getPkg(),
				definitions,
				Collections.emptyList(),
				space.registry,
				space.collectExpansions(),
				space.getAnnotations(),
				logger);
	}

	public JavaCompiledModule getCompiled() {
		return compiled;
	}

	public HighLevelDefinition addClass(Class<?> cls) {
		if (definitionByClass.containsKey(cls)) {
			return definitionByClass.get(cls);
		}
		return convertClass(cls);
	}

	public void addGlobals(Class<?> cls) {

		final HighLevelDefinition definition = addClass(cls);
		final JavaClass jcls;

		if (compiled.hasClassInfo(definition)) {
			jcls = compiled.getClassInfo(definition);
		} else {
			jcls = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(cls), JavaClass.Kind.CLASS);
			compiled.setClassInfo(definition, jcls);
		}

		TypeID thisType = registry.getForMyDefinition(definition);
		//TypeVariableContext context = new TypeVariableContext();

		for (Field field : cls.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(field.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = field.getAnnotation(ZenCodeGlobals.Global.class);
			TypeID type = typeConverter.loadStoredType(context, field.getAnnotatedType());
			String name = global.value().isEmpty() ? field.getName() : global.value();
			FieldMember fieldMember = new FieldMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, thisType, type, registry, Modifiers.PUBLIC, 0, null);
			definition.addMember(fieldMember);
			JavaField javaField = new JavaField(jcls, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType()));
			compiled.setFieldInfo(fieldMember, javaField);
			compiled.setFieldInfo(fieldMember.autoGetter, javaField);
			globals.put(name, new ExpressionSymbol((position, scope) -> new StaticGetterExpression(CodePosition.BUILTIN, fieldMember.autoGetter.ref(thisType, GenericMapper.EMPTY))));
		}

		for (Method method : cls.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(method.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = method.getAnnotation(ZenCodeGlobals.Global.class);
			String name = global.value().isEmpty() ? method.getName() : global.value();
			//MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, getHeader(context, method), null);
			//definition.addMember(methodMember);
			MethodMember methodMember = memberConverter.asMethod(context, definition, method, new ZenCodeType.Method() {
				@Override
				public String value() {
					return name;
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return ZenCodeType.Method.class;
				}
			});
			definition.addMember(methodMember);

			//boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
			//compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, method.getName(), false, memberConverter.getMethodDescriptor(method), method.getModifiers(), isGenericResult));
			compiled.setMethodInfo(methodMember, memberConverter.getMethod(jcls, method, typeConverter.loadType(context, method.getAnnotatedReturnType())));
			globals.put(name, new ExpressionSymbol((position, scope) -> {
				TypeMembers members = scope.getTypeMembers(thisType);
				return new PartialStaticMemberGroupExpression(position, scope, thisType, members.getGroup(name), TypeID.NONE);
			}));
		}
	}

	public FunctionalMemberRef loadStaticMethod(Method method) {
		if (!Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException("Method \"" + method.toString() + "\" is not static");

		HighLevelDefinition definition = addClass(method.getDeclaringClass());
		JavaClass jcls = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(method.getDeclaringClass()), JavaClass.Kind.CLASS);

		if (method.isAnnotationPresent(ZenCodeType.Method.class)) {
			//The method should already have been loaded let's use that one.
			final String methodDescriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
			final Optional<MethodMember> matchingMember = definition.members.stream()
					.filter(m -> m instanceof MethodMember)
					.map(m -> ((MethodMember) m))
					.filter(m -> {
						final JavaMethod methodInfo = compiled.optMethodInfo(m);
						return methodInfo != null && methodDescriptor.equals(methodInfo.descriptor);
					})
					.findAny();

			if (matchingMember.isPresent()) {
				return matchingMember.get().ref(registry.getForDefinition(definition));
			}
		}
		MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, method.getName(), memberConverter.getHeader(context, method), null);
		definition.addMember(methodMember);
		boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
		compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, method.getName(), false, org.objectweb.asm.Type.getMethodDescriptor(method), method.getModifiers(), isGenericResult));
		return methodMember.ref(registry.getForDefinition(definition));
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

	private <T> HighLevelDefinition convertClass(Class<T> cls) {
		if ((cls.getModifiers() & Modifier.PUBLIC) == 0)
			throw new IllegalArgumentException("Class \" " + cls.getName() + "\" must be public");

		if (cls.isAnnotationPresent(ZenCodeType.Expansion.class)) {
			return expansionConverter.convertExpansion(cls);
		}

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
		definitionByClass.put(cls, definition);
		if (!shouldLoadClass(cls)) {
			return definition;
		}

		//TypeVariableContext context = new TypeVariableContext();
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
			context.put(typeVariable, parameter);
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = definition.typeParameters[i];
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				if (bound.getType() == Object.class) {
					continue; //Makes the stdlibs types work as they have "no" bounds for T
				}
				TypeID type = typeConverter.loadType(context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}

		if (!foundRegistry && definition instanceof ClassDefinition && cls.getAnnotatedSuperclass() != null && shouldLoadType(cls.getAnnotatedSuperclass().getType())) {
			definition.setSuperType(typeConverter.loadType(context, cls.getAnnotatedSuperclass()));
		}

		if (!foundRegistry && definition.getSuperType() == null && cls != Object.class && !(definition instanceof EnumDefinition)) {
			definition.setSuperType(typeConverter.loadType(context, Object.class, false, false));
		}

		for (AnnotatedType iface : cls.getAnnotatedInterfaces()) {
			if (shouldLoadType(iface.getType())) {
				TypeID type = typeConverter.loadType(context, iface);
				ImplementationMember member = new ImplementationMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC, type);
				definition.members.add(member);
				compiled.setImplementationInfo(member, new JavaImplementation(true, javaClass));
			}
		}

		compiled.setClassInfo(definition, javaClass);

		TypeID thisType = registry.getForMyDefinition(definition);
		for (Field field : cls.getDeclaredFields()) {
			ZenCodeType.Field annotation = field.getAnnotation(ZenCodeType.Field.class);
			if (annotation == null)
				continue;
			if (!Modifier.isPublic(field.getModifiers()))
				continue;

			final String fieldName = annotation.value().isEmpty() ? field.getName() : annotation.value();

			TypeID fieldType = typeConverter.loadStoredType(context, field.getAnnotatedType());
			FieldMember member = new FieldMember(CodePosition.NATIVE, definition, memberConverter.getMethodModifiers(field), fieldName, thisType, fieldType, registry, 0, 0, null);
			definition.addMember(member);
			compiled.setFieldInfo(member, new JavaField(javaClass, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType())));
		}

		boolean hasConstructor = false;
		for (java.lang.reflect.Constructor<?> constructor : cls.getConstructors()) {
			ZenCodeType.Constructor constructorAnnotation = constructor.getAnnotation(ZenCodeType.Constructor.class);
			if (constructorAnnotation != null) {
				ConstructorMember member = memberConverter.asConstructor(context, definition, constructor);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, constructor));
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

				MethodMember member = memberConverter.asMethod(context, definition, method, methodAnnotation);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
			if (getter != null) {
				GetterMember member = memberConverter.asGetter(context, definition, method, getter);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.getType()));
			}

			ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
			if (setter != null) {
				SetterMember member = memberConverter.asSetter(context, definition, method, setter);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, BasicTypeID.VOID));
			}

			ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
			if (operator != null) {
				OperatorMember member = memberConverter.asOperator(context, definition, method, operator);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}

			ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
			if (caster != null) {
				CasterMember member = memberConverter.asCaster(context, definition, method, caster);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.toType));
			}

			/*if (!annotated) {
				MethodMember member = asMethod(definition, method, null);
				definition.addMember(member);
				compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.header.getReturnType()));
			}*/
		}

		return definition;
	}

	private boolean shouldLoadType(Type type) {
		if (type instanceof Class)
			return definitionByClass.containsKey(type) || shouldLoadClass((Class<?>) type);
		if (type instanceof ParameterizedType)
			return shouldLoadType(((ParameterizedType) type).getRawType());

		return false;
	}

	private String getClassName(Class<?> cls) {
		return cls.isAnnotationPresent(ZenCodeType.Name.class) ? cls.getAnnotation(ZenCodeType.Name.class).value() : cls.getName();
	}

	private boolean shouldLoadClass(Class<?> cls) {
		return packageInfo.isInBasePackage(getClassName(cls));
	}


	public void registerBEP(BracketExpressionParser bep) {
		memberConverter.setBEP(bep);
		typeConverter.setBEP(bep);
	}

	public Module getModule() {
		return packageInfo.getModule();
	}
}
