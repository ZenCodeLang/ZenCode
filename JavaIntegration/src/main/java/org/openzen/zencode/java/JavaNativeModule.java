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
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
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
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import stdlib.Strings;

/**
 *
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
			JavaNativeModule[] dependencies)
	{
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
		definitionByClass.put(cls, result);
		return result;
	}
	
	public void addGlobals(Class<?> cls) {
		HighLevelDefinition definition = new ClassDefinition(CodePosition.NATIVE, module, pkg, "__globals__", Modifiers.PUBLIC);
		JavaClass jcls = JavaClass.fromInternalName(getInternalName(cls), JavaClass.Kind.CLASS);
		compiled.setClassInfo(definition, jcls);
		StoredType thisType = registry.getForMyDefinition(definition).stored();
		
		for (Field field : cls.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!isStatic(field.getModifiers()))
				continue;
			
			ZenCodeGlobals.Global global = field.getAnnotation(ZenCodeGlobals.Global.class);
			StoredType type = loadStoredType(field.getAnnotatedType());
			String name = global.value().isEmpty() ? field.getName() : global.value();
			FieldMember fieldMember = new FieldMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, thisType, type, registry, Modifiers.PUBLIC, 0, null);
			definition.addMember(fieldMember);
			JavaField javaField = new JavaField(jcls, name, getDescriptor(field.getType()));
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
			MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, name, getHeader(method), null);
			definition.addMember(methodMember);
			
			boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
			compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, name, false, getMethodDescriptor(method), method.getModifiers(), isGenericResult));
			globals.put(name, new ExpressionSymbol((position, scope) -> {
				TypeMembers members = scope.getTypeMembers(thisType);
				return new PartialStaticMemberGroupExpression(position, scope, thisType.type, members.getGroup(name), StoredType.NONE);
			}));
		}
	}
	
	private ZSPackage getPackage(String className) {
		if (!className.contains("."))
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
		
		TypeVariable<Class<T>>[] javaTypeParameters = cls.getTypeParameters();
		TypeParameter[] typeParameters = new TypeParameter[cls.getTypeParameters().length];
		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				TypeID type = loadType(bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
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
		}
		
		definition.typeParameters = typeParameters;
		compiled.setClassInfo(definition, javaClass);
		
		StoredType thisType = new StoredType(registry.getForMyDefinition(definition), AutoStorageTag.INSTANCE);
		for (Field field : cls.getDeclaredFields()) {
			ZenCodeType.Field annotation = field.getAnnotation(ZenCodeType.Field.class);
			if (annotation == null)
				continue;
			if (!isPublic(field.getModifiers()))
				continue;

			String fieldName = field.getName();
			if (annotation != null && !annotation.value().isEmpty())
				fieldName = annotation.value();
			
			StoredType fieldType = loadStoredType(field.getAnnotatedType());
			FieldMember member = new FieldMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC, fieldName, thisType, fieldType, registry, 0, 0, null);
			definition.addMember(member);
			compiled.setFieldInfo(member, new JavaField(javaClass, field.getName(), getDescriptor(field.getType())));
		}
		
		boolean hasConstructor = false;
		for (java.lang.reflect.Constructor constructor : cls.getConstructors()) {
			ZenCodeType.Constructor constructorAnnotation = (ZenCodeType.Constructor)constructor.getAnnotation(ZenCodeType.Constructor.class);
			if (constructorAnnotation != null) {
				ConstructorMember member = asConstructor(definition, constructor);
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
				MethodMember member = asMethod(definition, method, methodAnnotation);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
				continue;
			}
			
			ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
			if (getter != null) {
				GetterMember member = asGetter(definition, method, getter);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.getType()));
				continue;
			}
			
			ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
			if (setter != null) {
				SetterMember member = asSetter(definition, method, setter);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, BasicTypeID.VOID.stored));
				continue;
			}
			
			ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
			if (operator != null) {
				OperatorMember member = asOperator(definition, method, operator);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
				continue;
			}
			
			ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
			if (caster != null) {
				CasterMember member = asCaster(definition, method, caster);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.toType));
				continue;
			}
			
			/*if (!annotated) {
				MethodMember member = asMethod(definition, method, null);
				definition.addMember(member);
				compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
			}*/
		}
		
		return definition;
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
	
	private ConstructorMember asConstructor(HighLevelDefinition definition, java.lang.reflect.Constructor method) {
		FunctionHeader header = getHeader(method);
		return new ConstructorMember(
				CodePosition.NATIVE,
				definition,
				Modifiers.PUBLIC,
				header,
				null);
	}
	
	private MethodMember asMethod(HighLevelDefinition definition, Method method, ZenCodeType.Method annotation) {
		String name = annotation != null && !annotation.value().isEmpty() ? annotation.value() : method.getName();
		FunctionHeader header = getHeader(method);
		return new MethodMember(
				CodePosition.NATIVE,
				definition,
				getMethodModifiers(method),
				name,
				header,
				null);
	}
	
	private OperatorMember asOperator(HighLevelDefinition definition, Method method, ZenCodeType.Operator annotation) {
		FunctionHeader header = getHeader(method);
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
	
	private GetterMember asGetter(HighLevelDefinition definition, Method method, ZenCodeType.Getter annotation) {
		StoredType type = loadStoredType(method.getAnnotatedReturnType());
		String name = null;
		if (annotation != null && !annotation.value().isEmpty())
			name = annotation.value();
		if (name == null)
			name = translateGetterName(method.getName());
		
		return new GetterMember(CodePosition.NATIVE, definition, getMethodModifiers(method), name, type, null);
	}
	
	private SetterMember asSetter(HighLevelDefinition definition, Method method, ZenCodeType.Setter annotation) {
		if (method.getParameterCount() != 1)
			throw new IllegalArgumentException("Illegal setter: must have exactly 1 parameter");
		
		StoredType type = loadStoredType(method.getAnnotatedParameterTypes()[0]);
		String name = null;
		if (annotation != null && !annotation.value().isEmpty())
			name = annotation.value();
		if (name == null)
			name = translateSetterName(method.getName());
		
		return new SetterMember(CodePosition.NATIVE, definition, getMethodModifiers(method), name, type, null);
	}
	
	private CasterMember asCaster(HighLevelDefinition definition, Method method, ZenCodeType.Caster annotation) {
		boolean implicit = annotation != null && annotation.implicit();
		int modifiers = Modifiers.PUBLIC;
		if (implicit)
			modifiers |= Modifiers.IMPLICIT;
		
		StoredType toType = loadStoredType(method.getAnnotatedReturnType());
		return new CasterMember(CodePosition.NATIVE, definition, modifiers, toType, null);
	}
	
	private FunctionHeader getHeader(java.lang.reflect.Constructor constructor) {
		return getHeader(
				null,
				constructor.getAnnotatedParameterTypes(),
				constructor.getTypeParameters(),
				constructor.getAnnotatedExceptionTypes());
	}
	
	private FunctionHeader getHeader(Method method) {
		return getHeader(
				method.getAnnotatedReturnType(),
				method.getAnnotatedParameterTypes(),
				method.getTypeParameters(),
				method.getAnnotatedExceptionTypes());
	}
	
	private FunctionHeader getHeader(
			AnnotatedType javaReturnType,
			AnnotatedType[] parameterTypes,
			TypeVariable<Method>[] javaTypeParameters,
			AnnotatedType[] exceptionTypes)
	{
		StoredType returnType = javaReturnType == null ? BasicTypeID.VOID.stored : loadStoredType(javaReturnType);
		
		FunctionParameter[] parameters = new FunctionParameter[parameterTypes.length];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new FunctionParameter(loadStoredType(parameterTypes[i]));
		}
		
		TypeParameter[] typeParameters = new TypeParameter[javaTypeParameters.length];
		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Method> javaTypeParameter = javaTypeParameters[i];
			typeParameters[i] = new TypeParameter(CodePosition.UNKNOWN, javaTypeParameter.getName());
			
			for (AnnotatedType bound : javaTypeParameter.getAnnotatedBounds())
				typeParameters[i].addBound(new ParameterTypeBound(CodePosition.NATIVE, loadType(bound)));
		}
		
		if (exceptionTypes.length > 1)
			throw new IllegalArgumentException("A method can only throw a single exception type!");
		
		StoredType thrownType = exceptionTypes.length == 0 ? null : loadStoredType(exceptionTypes[0]);
		return new FunctionHeader(typeParameters, returnType, thrownType, AutoStorageTag.INSTANCE, parameters);
	}
	
	private StoredType loadStoredType(AnnotatedType annotatedType) {
		TypeID baseType = loadType(annotatedType);
		return baseType.stored();
	}
	
	private TypeID loadType(AnnotatedType annotatedType) {
		if (annotatedType.isAnnotationPresent(ZenCodeType.USize.class))
			return BasicTypeID.USIZE;
		else if (annotatedType.isAnnotationPresent(ZenCodeType.NullableUSize.class))
			return registry.getOptional(BasicTypeID.USIZE);
		
		boolean nullable = annotatedType.isAnnotationPresent(ZenCodeType.Nullable.class);
		boolean unsigned = annotatedType.isAnnotationPresent(ZenCodeType.Unsigned.class);
		
		Type type = annotatedType.getType();
		return loadType(type, nullable, unsigned);
	}
	
	private TypeID loadType(Type type, boolean nullable, boolean unsigned) {
		if (type instanceof Class) {
			Class<?> classType = (Class<?>)type;
			if (unsigned) {
				if (unsignedByClass.containsKey(classType))
					return unsignedByClass.get(classType);
				else
					throw new IllegalArgumentException("This class cannot be used as unsigned: " + classType);
			}
			
			if (typeByClass.containsKey(classType))
				return typeByClass.get(classType);
			
			HighLevelDefinition definition = addClass(classType);
			return registry.getForDefinition(definition);
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Class<?> rawType = (Class)parameterizedType.getRawType();
			
			HighLevelDefinition definition = addClass(rawType);
			Type[] parameters = parameterizedType.getActualTypeArguments();
			StoredType[] codeParameters = new StoredType[parameters.length];
			for (int i = 0; i < parameters.length; i++)
				codeParameters[i] = loadType(parameters[i], false, false).stored();
			
			return registry.getForDefinition(definition, codeParameters);
		} else {
			throw new IllegalArgumentException("Could not analyze type: " + type);
		}
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
}
