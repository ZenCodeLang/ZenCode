/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.ExpressionSymbol;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.PartialStaticMemberGroupExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.storage.AutoStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaFunctionalInterfaceStorageTag;
import org.openzen.zenscript.javashared.JavaImplementation;
import org.openzen.zenscript.javashared.JavaMethod;
import stdlib.Strings;

/**
 * @author Stan Hebben
 */
public class JavaNativeModule {
	public final Module module;
	private final ZSPackage pkg;
	private final String basePackage;
	private final GlobalTypeRegistry registry;
	private final PackageDefinitions definitions = new PackageDefinitions();
	private final JavaCompiledModule compiled;
	
	private final Map<Class<?>, HighLevelDefinition> definitionByClass = new HashMap<>();
	private final Map<Class<?>, TypeID> typeByClass = new HashMap<>();
	private final Map<Class<?>, TypeID> unsignedByClass = new HashMap<>();
	
	public final Map<String, ISymbol> globals = new HashMap<>();
	
	public JavaNativeModule(
			ZSPackage pkg,
			String name,
			String basePackage,
			GlobalTypeRegistry registry,
			JavaNativeModule[] dependencies) {
		this.pkg = pkg;
		this.basePackage = basePackage;
		module = new Module(name);
		this.registry = registry;
		
		for (JavaNativeModule dependency : dependencies) {
			definitionByClass.putAll(dependency.definitionByClass);
		}
		
		compiled = new JavaCompiledModule(module, FunctionParameter.NONE);
		
		typeByClass.put(void.class, BasicTypeID.VOID);
		typeByClass.put(boolean.class, BasicTypeID.BOOL);
		typeByClass.put(byte.class, BasicTypeID.SBYTE);
		typeByClass.put(short.class, BasicTypeID.SHORT);
		typeByClass.put(int.class, BasicTypeID.INT);
		typeByClass.put(long.class, BasicTypeID.LONG);
		typeByClass.put(float.class, BasicTypeID.FLOAT);
		typeByClass.put(double.class, BasicTypeID.DOUBLE);
		typeByClass.put(String.class, StringTypeID.INSTANCE);
		typeByClass.put(Boolean.class, registry.getOptional(BasicTypeID.BOOL));
		typeByClass.put(Byte.class, registry.getOptional(BasicTypeID.BYTE));
		typeByClass.put(Short.class, registry.getOptional(BasicTypeID.SHORT));
		typeByClass.put(Integer.class, registry.getOptional(BasicTypeID.INT));
		typeByClass.put(Long.class, registry.getOptional(BasicTypeID.LONG));
		typeByClass.put(Float.class, registry.getOptional(BasicTypeID.FLOAT));
		typeByClass.put(Double.class, registry.getOptional(BasicTypeID.DOUBLE));
		
		unsignedByClass.put(byte.class, BasicTypeID.BYTE);
		unsignedByClass.put(short.class, BasicTypeID.USHORT);
		unsignedByClass.put(int.class, BasicTypeID.UINT);
		unsignedByClass.put(long.class, BasicTypeID.ULONG);
		unsignedByClass.put(Byte.class, registry.getOptional(BasicTypeID.BYTE));
		unsignedByClass.put(Short.class, registry.getOptional(BasicTypeID.SHORT));
		unsignedByClass.put(Integer.class, registry.getOptional(BasicTypeID.INT));
		unsignedByClass.put(Long.class, registry.getOptional(BasicTypeID.LONG));
	}
	
	public SemanticModule toSemantic(ModuleSpace space) {
		return new SemanticModule(
				module,
				SemanticModule.NONE,
				FunctionParameter.NONE,
				SemanticModule.State.NORMALIZED,
				space.rootPackage,
				pkg,
				definitions,
				Collections.emptyList(),
				space.registry,
				space.collectExpansions(),
				space.getAnnotations(),
				space.getStorageTypes());
	}
	
	public JavaCompiledModule getCompiled() {
		return compiled;
	}
	
	public HighLevelDefinition addClass(Class<?> cls) {
		if (definitionByClass.containsKey(cls))
			return definitionByClass.get(cls);
		
		HighLevelDefinition result = convertClass(cls);
		return result;
	}
	
	public void addGlobals(Class<?> cls) {
		HighLevelDefinition definition = new ClassDefinition(CodePosition.NATIVE, module, pkg, "__globals__", Modifiers.PUBLIC);
		JavaClass jcls = JavaClass.fromInternalName(getInternalName(cls), JavaClass.Kind.CLASS);
		compiled.setClassInfo(definition, jcls);
		StoredType thisType = registry.getForMyDefinition(definition).stored();
		TypeVariableContext context = new TypeVariableContext();
		
		for (Field field : cls.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!isStatic(field.getModifiers()))
				continue;
			
			ZenCodeGlobals.Global global = field.getAnnotation(ZenCodeGlobals.Global.class);
			StoredType type = loadStoredType(context, field.getAnnotatedType());
			String name = global.value().isEmpty() ? field.getName() : global.value();
			FieldMember fieldMember = new FieldMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, thisType, type, registry, Modifiers.PUBLIC, 0, null);
			definition.addMember(fieldMember);
			JavaField javaField = new JavaField(jcls, field.getName(), getDescriptor(field.getType()));
			compiled.setFieldInfo(fieldMember, javaField);
			compiled.setFieldInfo(fieldMember.autoGetter, javaField);
			globals.put(name, new ExpressionSymbol((position, scope) -> new StaticGetterExpression(CodePosition.BUILTIN, fieldMember.autoGetter.ref(thisType, GenericMapper.EMPTY))));
		}
		
		for (Method method : cls.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!isStatic(method.getModifiers()))
				continue;
			
			ZenCodeGlobals.Global global = method.getAnnotation(ZenCodeGlobals.Global.class);
			String name = global.value().isEmpty() ? method.getName() : global.value();
			MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, getHeader(context, method), null);
			definition.addMember(methodMember);
			
			boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
			compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, method.getName(), false, getMethodDescriptor(method), method.getModifiers(), isGenericResult));
			globals.put(name, new ExpressionSymbol((position, scope) -> {
				TypeMembers members = scope.getTypeMembers(thisType);
				return new PartialStaticMemberGroupExpression(position, scope, thisType.type, members.getGroup(name), StoredType.NONE);
			}));
		}
	}
	
	public FunctionalMemberRef loadStaticMethod(Method method) {
		if (!isStatic(method.getModifiers()))
			throw new IllegalArgumentException("Method is not static");
		
		HighLevelDefinition definition = addClass(method.getDeclaringClass());
		JavaClass jcls = JavaClass.fromInternalName(getInternalName(method.getDeclaringClass()), JavaClass.Kind.CLASS);
		
		TypeVariableContext context = new TypeVariableContext();
		MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, method.getName(), getHeader(context, method), null);
		definition.addMember(methodMember);
		boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
		compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, method.getName(), false, getMethodDescriptor(method), method.getModifiers(), isGenericResult));
		return methodMember.ref(registry.getForDefinition(definition).stored());
	}
	
	private boolean isInBasePackage(String className) {
		return className.startsWith(basePackage + ".");
	}
	
	private ZSPackage getPackage(String className) {
		//TODO make a lang package?
		if (!className.contains(".") || className.startsWith("java.lang"))
			return pkg;
		
		if (className.startsWith("."))
			className = className.substring(1);
		else if (className.startsWith(basePackage + "."))
			className = className.substring(basePackage.length() + 1);
		else
			throw new IllegalArgumentException("Invalid class name: not in the given base package");
		
		String[] classNameParts = Strings.split(className, '.');
		ZSPackage classPkg = pkg;
		for (int i = 0; i < classNameParts.length - 1; i++)
			classPkg = classPkg.getOrCreatePackage(classNameParts[i]);
		
		return classPkg;
	}
	
	private <T> HighLevelDefinition convertClass(Class<T> cls) {
		if ((cls.getModifiers() & Modifier.PUBLIC) == 0)
			throw new IllegalArgumentException("Class must be public");
		
		ZenCodeType.Name name = cls.getDeclaredAnnotation(ZenCodeType.Name.class);
		String className = name == null ? cls.getName() : name.value();
		boolean isStruct = cls.getAnnotation(ZenCodeType.Struct.class) != null;
		
		ZSPackage classPkg = getPackage(className);
		className = className.contains(".") ? className.substring(className.lastIndexOf('.') + 1) : className;
		
		TypeVariableContext context = new TypeVariableContext();
		TypeVariable<Class<T>>[] javaTypeParameters = cls.getTypeParameters();
		TypeParameter[] typeParameters = new TypeParameter[cls.getTypeParameters().length];
		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				TypeID type = loadType(context, bound).type;
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
			typeParameters[i] = parameter;
			context.put(typeVariable, parameter);
		}
		
		HighLevelDefinition definition;
		String internalName = getInternalName(cls);
		JavaClass javaClass;
		if (cls.isInterface()) {
			definition = new InterfaceDefinition(CodePosition.NATIVE, module, classPkg, className, Modifiers.PUBLIC, null);
			javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.INTERFACE);
		} else if (cls.isEnum()) {
			definition = new EnumDefinition(CodePosition.NATIVE, module, pkg, className, Modifiers.PUBLIC, null);
			javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.ENUM);
		} else if (isStruct) {
			definition = new StructDefinition(CodePosition.NATIVE, module, pkg, className, Modifiers.PUBLIC, null);
			javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.CLASS);
		} else {
			definition = new ClassDefinition(CodePosition.NATIVE, module, classPkg, className, Modifiers.PUBLIC);
			javaClass = JavaClass.fromInternalName(internalName, JavaClass.Kind.CLASS);
			
			if (cls.getAnnotatedSuperclass() != null && shouldLoadType(cls.getAnnotatedSuperclass().getType())) {
				definition.setSuperType(loadType(context, cls.getAnnotatedSuperclass()).type);
			}
		}
		
		for (AnnotatedType iface : cls.getAnnotatedInterfaces()) {
			if (shouldLoadType(iface.getType())) {
				TypeID type = loadType(context, iface).type;
				ImplementationMember member = new ImplementationMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC, type);
				definition.members.add(member);
				compiled.setImplementationInfo(member, new JavaImplementation(true, javaClass));
			}
		}
		
		definition.typeParameters = typeParameters;
		compiled.setClassInfo(definition, javaClass);
		definitionByClass.put(cls, definition);
		
		StoredType thisType = new StoredType(registry.getForMyDefinition(definition), AutoStorageTag.INSTANCE);
		for (Field field : cls.getDeclaredFields()) {
			ZenCodeType.Field annotation = field.getAnnotation(ZenCodeType.Field.class);
			if (annotation == null)
				continue;
			if (!isPublic(field.getModifiers()))
				continue;
			
			final String fieldName = annotation.value().isEmpty() ? field.getName() : annotation.value();
			
			StoredType fieldType = loadStoredType(context, field.getAnnotatedType());
			FieldMember member = new FieldMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC, fieldName, thisType, fieldType, registry, 0, 0, null);
			definition.addMember(member);
			compiled.setFieldInfo(member, new JavaField(javaClass, field.getName(), getDescriptor(field.getType())));
		}
		
		boolean hasConstructor = false;
		for (java.lang.reflect.Constructor constructor : cls.getConstructors()) {
			ZenCodeType.Constructor constructorAnnotation = (ZenCodeType.Constructor) constructor.getAnnotation(ZenCodeType.Constructor.class);
			if (constructorAnnotation != null) {
				ConstructorMember member = asConstructor(context, definition, constructor);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, constructor));
				hasConstructor = true;
			}
		}
		
		if (!hasConstructor) {
			// no constructor! make a private constructor so the compiler doesn't add one
			ConstructorMember member = new ConstructorMember(CodePosition.BUILTIN, definition, Modifiers.PRIVATE, new FunctionHeader(BasicTypeID.VOID), BuiltinID.CLASS_DEFAULT_CONSTRUCTOR);
			definition.addMember(member);
		}
		
		for (Method method : cls.getDeclaredMethods()) {
			ZenCodeType.Method methodAnnotation = method.getAnnotation(ZenCodeType.Method.class);
			if (methodAnnotation != null) {
				MethodMember member = asMethod(context, definition, method, methodAnnotation);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
				continue;
			}
			
			ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
			if (getter != null) {
				GetterMember member = asGetter(context, definition, method, getter);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.getType()));
			}
			
			ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
			if (setter != null) {
				SetterMember member = asSetter(context, definition, method, setter);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, BasicTypeID.VOID.stored));
			}
			
			ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
			if (operator != null) {
				OperatorMember member = asOperator(context, definition, method, operator);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
			}
			
			ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
			if (caster != null) {
				CasterMember member = asCaster(context, definition, method, caster);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.toType));
			}
			
			/*if (!annotated) {
				MethodMember member = asMethod(definition, method, null);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
			}*/
		}
		
		return definition;
	}
	
	private boolean shouldLoadType(Type type) {
		if (type instanceof Class)
			return shouldLoadClass((Class<?>)type);
		if (type instanceof ParameterizedType)
			return shouldLoadType(((ParameterizedType)type).getRawType());
		
		return false;
	}
	
	private boolean shouldLoadClass(Class<?> cls) {
		return isInBasePackage(cls.getName());
	}
	
	private boolean isGetterName(String name) {
		return name.startsWith("get") || name.startsWith("is") || name.startsWith("has");
	}
	
	private String translateGetterName(String name) {
		if (name.startsWith("get"))
			return name.substring(3, 4).toLowerCase() + name.substring(4);
		
		return name;
	}
	
	private String translateSetterName(String name) {
		if (name.startsWith("set"))
			return name.substring(3, 4).toLowerCase() + name.substring(4);
		
		return name;
	}
	
	private ConstructorMember asConstructor(TypeVariableContext context, HighLevelDefinition definition, java.lang.reflect.Constructor method) {
		FunctionHeader header = getHeader(context, method);
		return new ConstructorMember(
				CodePosition.NATIVE,
				definition,
				Modifiers.PUBLIC,
				header,
				null);
	}
	
	private MethodMember asMethod(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Method annotation) {
		String name = annotation != null && !annotation.value().isEmpty() ? annotation.value() : method.getName();
		FunctionHeader header = getHeader(context, method);
		return new MethodMember(
				CodePosition.NATIVE,
				definition,
				getMethodModifiers(method),
				name,
				header,
				null);
	}
	
	private OperatorMember asOperator(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Operator annotation) {
		FunctionHeader header = getHeader(context, method);
		if (isStatic(method.getModifiers()))
			throw new IllegalArgumentException("operator method cannot be static");
		
		// TODO: check number of parameters
		//if (header.parameters.length != annotation.value().parameters)
		
		return new OperatorMember(
				CodePosition.NATIVE,
				definition,
				getMethodModifiers(method),
				OperatorType.valueOf(annotation.value().toString()),
				header,
				null);
	}
	
	private GetterMember asGetter(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Getter annotation) {
		StoredType type = loadStoredType(context, method.getAnnotatedReturnType());
		String name = null;
		if (annotation != null && !annotation.value().isEmpty())
			name = annotation.value();
		if (name == null)
			name = translateGetterName(method.getName());
		
		return new GetterMember(CodePosition.NATIVE, definition, getMethodModifiers(method), name, type, null);
	}
	
	private SetterMember asSetter(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Setter annotation) {
		if (method.getParameterCount() != 1)
			throw new IllegalArgumentException("Illegal setter: must have exactly 1 parameter");
		
		StoredType type = loadStoredType(context, method.getAnnotatedParameterTypes()[0]);
		String name = null;
		if (annotation != null && !annotation.value().isEmpty())
			name = annotation.value();
		if (name == null)
			name = translateSetterName(method.getName());
		
		return new SetterMember(CodePosition.NATIVE, definition, getMethodModifiers(method), name, type, null);
	}
	
	private CasterMember asCaster(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Caster annotation) {
		boolean implicit = annotation != null && annotation.implicit();
		int modifiers = Modifiers.PUBLIC;
		if (implicit)
			modifiers |= Modifiers.IMPLICIT;
		
		StoredType toType = loadStoredType(context, method.getAnnotatedReturnType());
		return new CasterMember(CodePosition.NATIVE, definition, modifiers, toType, null);
	}
	
	private FunctionHeader getHeader(TypeVariableContext context, java.lang.reflect.Constructor constructor) {
		return getHeader(
				context,
				null,
				constructor.getAnnotatedParameterTypes(),
				constructor.getTypeParameters(),
				constructor.getAnnotatedExceptionTypes());
	}
	
	private FunctionHeader getHeader(TypeVariableContext context, Method method) {
		return getHeader(
				context,
				method.getAnnotatedReturnType(),
				method.getAnnotatedParameterTypes(),
				method.getTypeParameters(),
				method.getAnnotatedExceptionTypes());
	}
	
	private FunctionHeader getHeader(
			TypeVariableContext context,
			AnnotatedType javaReturnType,
			AnnotatedType[] parameterTypes,
			TypeVariable<Method>[] javaTypeParameters,
			AnnotatedType[] exceptionTypes) {
		StoredType returnType = javaReturnType == null ? BasicTypeID.VOID.stored : loadStoredType(context, javaReturnType);
		
		FunctionParameter[] parameters = new FunctionParameter[parameterTypes.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new FunctionParameter(loadStoredType(context, parameterTypes[i]));
		}
		
		TypeParameter[] typeParameters = new TypeParameter[javaTypeParameters.length];
		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Method> javaTypeParameter = javaTypeParameters[i];
			typeParameters[i] = new TypeParameter(CodePosition.UNKNOWN, javaTypeParameter.getName());
			context.put(javaTypeParameter, typeParameters[i]);
			
			for (AnnotatedType bound : javaTypeParameter.getAnnotatedBounds())
				typeParameters[i].addBound(new ParameterTypeBound(CodePosition.NATIVE, loadType(context, bound).type));
		}
		
		if (exceptionTypes.length > 1)
			throw new IllegalArgumentException("A method can only throw a single exception type!");
		
		StoredType thrownType = exceptionTypes.length == 0 ? null : loadStoredType(context, exceptionTypes[0]);
		return new FunctionHeader(typeParameters, returnType, thrownType, AutoStorageTag.INSTANCE, parameters);
	}
	
	private StoredType loadStoredType(TypeVariableContext context, AnnotatedType annotatedType) {
		return loadType(context, annotatedType);
	}
	
	private StoredType loadType(TypeVariableContext context, AnnotatedType annotatedType) {
		if (annotatedType.isAnnotationPresent(ZenCodeType.USize.class))
			return BasicTypeID.USIZE.stored;
		else if (annotatedType.isAnnotationPresent(ZenCodeType.NullableUSize.class))
			return registry.getOptional(BasicTypeID.USIZE).stored();
		
		boolean nullable = annotatedType.isAnnotationPresent(ZenCodeType.Nullable.class);
		boolean unsigned = annotatedType.isAnnotationPresent(ZenCodeType.Unsigned.class);
		
		Type type = annotatedType.getType();
		return loadType(context, type, nullable, unsigned);
	}
	
	private StoredType loadType(TypeVariableContext context, Type type, boolean nullable, boolean unsigned) {
		if (type instanceof Class) {
			Class<?> classType = (Class<?>) type;
			if (unsigned) {
				if (unsignedByClass.containsKey(classType))
					return unsignedByClass.get(classType).stored();
				else
					throw new IllegalArgumentException("This class cannot be used as unsigned: " + classType);
			} else if (classType.isArray()) {
				return registry.getArray(loadType(context, classType.getComponentType(), false, false), 1).stored();
			} else if (classType.isAnnotationPresent(FunctionalInterface.class)) {
				return loadFunctionalInterface(context, classType, new Type[0]);
			}
			
			if (typeByClass.containsKey(classType))
				return typeByClass.get(classType).stored();
			
			HighLevelDefinition definition = addClass(classType);
			return registry.getForDefinition(definition).stored();
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Class<?> rawType = (Class) parameterizedType.getRawType();
			if (rawType.isAnnotationPresent(FunctionalInterface.class))
				return loadFunctionalInterface(context, rawType, parameterizedType.getActualTypeArguments());
			
			HighLevelDefinition definition = addClass(rawType);
			Type[] parameters = parameterizedType.getActualTypeArguments();
			StoredType[] codeParameters = new StoredType[parameters.length];
			for (int i = 0; i < parameters.length; i++)
				codeParameters[i] = loadType(context, parameters[i], false, false);
			
			return registry.getForDefinition(definition, codeParameters).stored();
		} else if (type instanceof TypeVariable) {
			TypeVariable variable = (TypeVariable)type;
			return registry.getGeneric(context.get(variable)).stored();
		} else {
			throw new IllegalArgumentException("Could not analyze type: " + type);
		}
	}
	
	private StoredType loadFunctionalInterface(TypeVariableContext loadContext, Class<?> cls, Type[] parameters) {
		Method functionalInterfaceMethod = getFunctionalInterfaceMethod(cls);
		TypeVariableContext context = convertTypeParameters(cls);
		FunctionHeader header = getHeader(context, functionalInterfaceMethod);
		
		Map<TypeParameter, StoredType> mapping = new HashMap<>();
		TypeVariable[] javaParameters = cls.getTypeParameters();
		for (int i = 0; i < javaParameters.length; i++)
			mapping.put(context.get(javaParameters[i]), loadType(loadContext, parameters[i], false, false));
		
		JavaMethod method = new JavaMethod(
				JavaClass.fromInternalName(cls.getName().replace('.', '/'), JavaClass.Kind.INTERFACE),
				JavaMethod.Kind.INTERFACE,
				functionalInterfaceMethod.getName(),
				false,
				getMethodDescriptor(functionalInterfaceMethod),
				Modifiers.PUBLIC | Modifiers.ABSTRACT,
				header.getReturnType().type.isGeneric());
		StorageTag tag = new JavaFunctionalInterfaceStorageTag(functionalInterfaceMethod, method);
		return registry.getFunction(header).stored(tag);
	}
	
	private <T> TypeVariableContext convertTypeParameters(Class<T> cls) {
		TypeVariableContext context = new TypeVariableContext();
		TypeVariable<Class<T>>[] javaTypeParameters = cls.getTypeParameters();
		TypeParameter[] typeParameters = new TypeParameter[cls.getTypeParameters().length];
		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				TypeID type = loadType(context, bound).type;
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
			typeParameters[i] = parameter;
			context.put(typeVariable, parameter);
		}
		return context;
	}
	
	private Method getFunctionalInterfaceMethod(Class<?> functionalInterface) {
		for (Method method : functionalInterface.getDeclaredMethods()) {
			if (!method.isDefault())
				return method;
		}
		
		return null;
	}
	
	private int getMethodModifiers(Method method) {
		int result = Modifiers.PUBLIC;
		if (isStatic(method.getModifiers()))
			result |= Modifiers.STATIC;
		if (isFinal(method.getModifiers()))
			result |= Modifiers.FINAL;
		
		return result;
	}
	
	private static boolean isPublic(int modifiers) {
		return (modifiers & Modifier.PUBLIC) > 0;
	}
	
	private static boolean isStatic(int modifiers) {
		return (modifiers & Modifier.STATIC) > 0;
	}
	
	private static boolean isFinal(int modifiers) {
		return (modifiers & Modifier.FINAL) > 0;
	}
	
	private static String getInternalName(Class<?> cls) {
		return org.objectweb.asm.Type.getInternalName(cls);
	}
	
	private static String getDescriptor(Class<?> cls) {
		return org.objectweb.asm.Type.getDescriptor(cls);
	}
	
	private static String getMethodDescriptor(java.lang.reflect.Constructor constructor) {
		return org.objectweb.asm.Type.getConstructorDescriptor(constructor);
	}
	
	private static String getMethodDescriptor(Method method) {
		return org.objectweb.asm.Type.getMethodDescriptor(method);
	}
	
	private static JavaMethod getMethod(JavaClass cls, java.lang.reflect.Constructor constructor) {
		return new JavaMethod(
				cls,
				JavaMethod.Kind.CONSTRUCTOR,
				"<init>",
				false,
				getMethodDescriptor(constructor),
				constructor.getModifiers(),
				false);
	}
	
	private static JavaMethod getMethod(JavaClass cls, Method method, StoredType result) {
		JavaMethod.Kind kind;
		if (method.getName().equals("<init>"))
			kind = JavaMethod.Kind.CONSTRUCTOR;
		else if (method.getName().equals("<clinit>"))
			kind = JavaMethod.Kind.STATICINIT;
		else if (isStatic(method.getModifiers()))
			kind = JavaMethod.Kind.STATIC;
		else
			kind = JavaMethod.Kind.INSTANCE;
		
		return new JavaMethod(cls, kind, method.getName(), false, getMethodDescriptor(method), method.getModifiers(), result.isGeneric());
	}
	
	private static class TypeVariableContext {
		private final Map<TypeVariable, TypeParameter> typeVariables = new HashMap<>();
		
		public void put(TypeVariable variable, TypeParameter parameter) {
			typeVariables.put(variable, parameter);
		}
		
		public TypeParameter get(TypeVariable variable) {
			if (!typeVariables.containsKey(variable))
				throw new IllegalStateException("Could not find type variable " + variable.getName());
			
			return typeVariables.get(variable);
		}
	}
}
