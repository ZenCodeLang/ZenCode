package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.java.module.TypeVariableContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.type.IParsedType;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;

public class JavaNativeTypeConverter {
	private final Map<Class<?>, TypeID> typeByClass = new HashMap<>();
	private final Map<Class<?>, TypeID> unsignedByClass = new HashMap<>();
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeModule javaNativeModule;
	private final JavaNativeTypeConversionContext typeConversionContext;

	private BracketExpressionParser bep;
	private JavaNativeHeaderConverter headerConverter;

	public JavaNativeTypeConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativePackageInfo packageInfo, JavaNativeModule javaNativeModule) {
		this.typeConversionContext = typeConversionContext;
		this.packageInfo = packageInfo;
		this.javaNativeModule = javaNativeModule;
		fillByClassMaps();
	}

	public TypeID loadStoredType(TypeVariableContext context, AnnotatedType annotatedType) {
		return loadType(context, annotatedType);
	}

	public TypeID loadStoredType(TypeVariableContext context, Parameter parameter) {
		final TypeID type = loadStoredType(context, parameter.getAnnotatedType());
		//Optional is a parameter annotation so passing the parameter's type does not pass the optional
		if (parameter.isAnnotationPresent(ZenCodeType.Optional.class) && !type.isOptional())
			return typeConversionContext.registry.getOptional(type);
		return type;
	}

	@Deprecated
	public TypeID loadType(TypeVariableContext context, AnnotatedElement element) {
		try {
			return loadType(context, JavaAnnotatedType.of(element));
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to analyze type: " + element, e);
		}
	}

	public TypeID loadType(TypeVariableContext context, JavaAnnotatedType annotatedType) {
		if (annotatedType.isAnnotationPresent(ZenCodeType.USize.class))
			return BasicTypeID.USIZE;
		else if (annotatedType.isAnnotationPresent(ZenCodeType.NullableUSize.class))
			return typeConversionContext.registry.getOptional(BasicTypeID.USIZE);

		boolean nullable = annotatedType.isAnnotationPresent(ZenCodeType.Nullable.class) || annotatedType.isAnnotationPresent(ZenCodeType.Optional.class);
		boolean unsigned = annotatedType.isAnnotationPresent(ZenCodeType.Unsigned.class);

		return loadType(context, annotatedType, nullable, unsigned);
	}

	@Deprecated
	public TypeID loadType(TypeVariableContext context, AnnotatedElement element, boolean nullable, boolean unsigned) {
		try {
			return loadType(context, JavaAnnotatedType.of(element), nullable, unsigned);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to analyze type: " + element, e);
		}
	}

	public TypeID loadType(TypeVariableContext context, JavaAnnotatedType type, boolean nullable, boolean unsigned) {
		TypeID result = loadType(context, type, unsigned);
		return nullable ? typeConversionContext.registry.getOptional(result) : result;
	}

	private TypeID loadType(TypeVariableContext context, JavaAnnotatedType type, boolean unsigned) {

		final JavaAnnotatedType.ElementType elementType = type.getElementType();

		try {
			switch (elementType) {
				case ANNOTATED_PARAMETERIZED_TYPE:
					return loadAnnotatedParameterizedType(context, (AnnotatedParameterizedType) type.getAnnotatedElement(), unsigned);
				case ANNOTATED_TYPE:
					return loadAnnotatedType(context, (AnnotatedType) type.getAnnotatedElement(), unsigned);
				case CLASS:
					return loadClass(context, (Class<?>) type.getType(), unsigned);
				case GENERIC_ARRAY:
					return loadGenericArray(context, (GenericArrayType) type.getType(), unsigned);
				case PARAMETERIZED_TYPE:
					return loadParameterizedType(context, (ParameterizedType) type.getType());
				case TYPE_VARIABLE:
					return loadTypeVariable(context, (TypeVariable<?>) type.getType());
				case WILDCARD:
					return loadWildcard();
			}
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to analyze type: " + type, e);
		}

		throw new IllegalArgumentException("Invalid type " + elementType + ": not yet implemented or foolery");
	}

	private TypeID loadAnnotatedParameterizedType(TypeVariableContext context, AnnotatedParameterizedType type, boolean unsigned) {
		final ParameterizedType parameterizedType = this.getTypeIfValid(JavaAnnotatedType.of(type.getType()), JavaAnnotatedType.ElementType.PARAMETERIZED_TYPE);
		final JavaAnnotatedType rawType = JavaAnnotatedType.of(parameterizedType.getRawType());

		final JavaAnnotatedType[] actualTypeArguments = JavaAnnotatedType.arrayOf(type.getAnnotatedActualTypeArguments());
		final TypeID[] codeParameters = new TypeID[actualTypeArguments.length];

		for (int i = 0; i < actualTypeArguments.length; i++) {
			codeParameters[i] = this.loadType(context, actualTypeArguments[i], false, false);
		}

		if (rawType.getElementType() == JavaAnnotatedType.ElementType.CLASS) {
			if (rawType.getType() == Map.class) {
				return typeConversionContext.registry.getAssociative(codeParameters[0], codeParameters[1]);
			}

			final Map<TypeParameter, TypeID> map = new HashMap<>();
			final JavaAnnotatedType[] typeParameters = JavaAnnotatedType.arrayOf(((Class<?>) rawType.getType()).getTypeParameters());
			final TypeID rawTypeId = this.loadType(context, rawType, unsigned);

			for (int i = 0; i < typeParameters.length; i++) {
				final TypeVariable<?> typeVariable = this.getTypeIfValid(typeParameters[i], JavaAnnotatedType.ElementType.TYPE_VARIABLE);
				final TypeParameter typeParameter = context.get(typeVariable);
				map.put(typeParameter, codeParameters[i]);
			}

			return rawTypeId.instance(new GenericMapper(CodePosition.NATIVE, typeConversionContext.registry, map));
		}
		return this.loadType(context, JavaAnnotatedType.of(type), unsigned);
	}

	private TypeID loadAnnotatedType(TypeVariableContext context, AnnotatedType type, boolean unsigned) {
		final JavaAnnotatedType annotatedType = JavaAnnotatedType.of(type.getType());
		return this.loadType(context, annotatedType, unsigned);
	}

	private TypeID loadClass(TypeVariableContext context, Class<?> type, boolean unsigned) {
		if (unsigned) {
			return unsignedByClass.computeIfAbsent(type, it -> {
				throw new IllegalArgumentException("This class cannot be used as unsigned: " + it);
			});
		}
		if (type.isArray()) {
			final TypeID baseType = this.loadType(context, JavaAnnotatedType.of(type.getComponentType()), false, false);
			return typeConversionContext.registry.getArray(baseType, 1);
		}
		if (type.isAnnotationPresent(FunctionalInterface.class)) {
			return loadFunctionalInterface(context, type);
		}
		if (typeByClass.containsKey(type)) {
			return typeByClass.get(type);
		}

		final HighLevelDefinition definition = javaNativeModule.addClass(type);

		final List<TypeID> typeParameters = new ArrayList<>();
		for (TypeVariable<? extends Class<?>> typeParameter : type.getTypeParameters()) {
			typeParameters.add(typeConversionContext.registry.getGeneric(context.get(typeParameter)));
		}

		return typeConversionContext.registry.getForDefinition(definition, typeParameters.toArray(TypeID.NONE));
	}

	private TypeID loadGenericArray(TypeVariableContext context, GenericArrayType type, boolean unsigned) {
		final JavaAnnotatedType componentType = JavaAnnotatedType.of(type.getGenericComponentType());
		final TypeID baseType = this.loadType(context, componentType, unsigned);
		return typeConversionContext.registry.getArray(baseType, 1);
	}

	private TypeID loadParameterizedType(TypeVariableContext context, ParameterizedType type) {
		final Class<?> rawType = this.getTypeIfValid(JavaAnnotatedType.of(type.getRawType()), JavaAnnotatedType.ElementType.CLASS);
		final JavaAnnotatedType[] typeArguments = JavaAnnotatedType.arrayOf(type.getActualTypeArguments());

		if (rawType.isAnnotationPresent(FunctionalInterface.class)) {

			return loadFunctionalInterface(context, rawType, typeArguments);
		}

		TypeID[] codeParameters = new TypeID[typeArguments.length];
		for (int i = 0; i < typeArguments.length; i++) {
			codeParameters[i] = this.loadType(context, typeArguments[i], false, false);
		}

		if (rawType == Map.class) {
			return typeConversionContext.registry.getAssociative(codeParameters[0], codeParameters[1]);
		}

		final HighLevelDefinition definition = javaNativeModule.addClass(rawType);
		return typeConversionContext.registry.getForDefinition(definition, codeParameters);
	}

	private TypeID loadTypeVariable(TypeVariableContext context, TypeVariable<?> variable) {
		return typeConversionContext.registry.getGeneric(context.get(variable));
	}

	private TypeID loadWildcard() {
		return BasicTypeID.UNDETERMINED;
	}

	@SuppressWarnings("unchecked")
	private <T> T getTypeIfValid(final JavaAnnotatedType type, final JavaAnnotatedType.ElementType expected) {
		if (type.getElementType() != expected) {
			throw new IllegalArgumentException(expected + " was expected as a type, but " + type + " was found");
		}
		return (T) type.getType();
	}

	public Class<?> getClassFromType(TypeID type) {
		if (type instanceof DefinitionTypeID) {
			DefinitionTypeID definitionType = ((DefinitionTypeID) type);

			for (Map.Entry<Class<?>, HighLevelDefinition> ent : typeConversionContext.definitionByClass.entrySet()) {
				if (ent.getValue().equals(definitionType.definition))
					return ent.getKey();
			}
		}

		for (Map.Entry<Class<?>, TypeID> ent : typeByClass.entrySet()) {
			if (ent.getValue().equals(type))
				return ent.getKey();
		}

		for (Map.Entry<Class<?>, TypeID> ent : unsignedByClass.entrySet()) {
			if (ent.getValue().equals(type))
				return ent.getKey();
		}

		return null;
	}

	public TypeID getTypeFromName(String className) {
		for (TypeID value : this.typeByClass.values()) {
			if (value.toString().equals(className))
				return value;
		}

		for (TypeID value : this.unsignedByClass.values()) {
			if (value.toString().equals(className))
				return value;
		}


		try {
			final ZSPackage zsPackage = packageInfo.getPackage(className);
			final String[] split = className.split("\\.");
			final String actualName = split[split.length - 1];

			for (HighLevelDefinition value : typeConversionContext.definitionByClass.values()) {
				if (actualName.equals(value.name) && value.pkg.equals(zsPackage))
					return typeConversionContext.registry.getForMyDefinition(value);
			}
		} catch (IllegalArgumentException ignored) {
		}


		//TODO: Can we get by with only this?
		final CompilingPackage rootCompiling = new CompilingPackage(packageInfo.getPkg().parent, packageInfo.getModule());
		final ModuleTypeResolutionContext context = new ModuleTypeResolutionContext(typeConversionContext.registry, new AnnotationDefinition[0], packageInfo.getPkg().parent, rootCompiling, typeConversionContext.globals);

		try {
			final ZSTokenParser tokens = ZSTokenParser.create(new LiteralSourceFile("type reading: " + className, className), bep);
			return IParsedType.parse(tokens).compile(context);
		} catch (Exception ignored) {
		}


		return null;
	}

	private TypeID loadFunctionalInterface(TypeVariableContext loadContext, Class<?> cls, JavaAnnotatedType... parameters) {
		Method functionalInterfaceMethod = getFunctionalInterfaceMethod(cls);
		TypeVariableContext context = convertTypeParameters(cls);

		//TODO: This breaks if the functional interface type appears in the method's signature
		FunctionHeader header = headerConverter.getHeader(context, functionalInterfaceMethod);

		Map<TypeParameter, TypeID> mapping = new HashMap<>();
		TypeVariable<?>[] javaParameters = cls.getTypeParameters();
		for (int i = 0; i < parameters.length; i++) {
			mapping.put(context.get(javaParameters[i]), loadType(loadContext, parameters[i], false, false));
		}

		header = header.withGenericArguments(new GenericMapper(CodePosition.NATIVE, typeConversionContext.registry, mapping));
		JavaMethod method = new JavaMethod(
				JavaClass.fromInternalName(getInternalName(cls), JavaClass.Kind.INTERFACE),
				JavaMethod.Kind.INTERFACE,
				functionalInterfaceMethod.getName(),
				false,
				getMethodDescriptor(functionalInterfaceMethod),
				JavaModifiers.PUBLIC | JavaModifiers.ABSTRACT,
				header.getReturnType().isGeneric());
		return new JavaFunctionalInterfaceTypeID(typeConversionContext.registry, header, functionalInterfaceMethod, method);
	}

	@SuppressWarnings("DuplicatedCode")
	private void fillByClassMaps() {
		typeByClass.put(void.class, BasicTypeID.VOID);
		typeByClass.put(boolean.class, BasicTypeID.BOOL);
		typeByClass.put(byte.class, BasicTypeID.SBYTE);
		typeByClass.put(char.class, BasicTypeID.CHAR);
		typeByClass.put(short.class, BasicTypeID.SHORT);
		typeByClass.put(int.class, BasicTypeID.INT);
		typeByClass.put(long.class, BasicTypeID.LONG);
		typeByClass.put(float.class, BasicTypeID.FLOAT);
		typeByClass.put(double.class, BasicTypeID.DOUBLE);
		typeByClass.put(String.class, BasicTypeID.STRING);
		typeByClass.put(Boolean.class, typeConversionContext.registry.getOptional(BasicTypeID.BOOL));
		typeByClass.put(Byte.class, typeConversionContext.registry.getOptional(BasicTypeID.BYTE));
		typeByClass.put(Short.class, typeConversionContext.registry.getOptional(BasicTypeID.SHORT));
		typeByClass.put(Integer.class, typeConversionContext.registry.getOptional(BasicTypeID.INT));
		typeByClass.put(Long.class, typeConversionContext.registry.getOptional(BasicTypeID.LONG));
		typeByClass.put(Float.class, typeConversionContext.registry.getOptional(BasicTypeID.FLOAT));
		typeByClass.put(Double.class, typeConversionContext.registry.getOptional(BasicTypeID.DOUBLE));

		unsignedByClass.put(byte.class, BasicTypeID.BYTE);
		unsignedByClass.put(char.class, BasicTypeID.CHAR);
		unsignedByClass.put(short.class, BasicTypeID.USHORT);
		unsignedByClass.put(int.class, BasicTypeID.UINT);
		unsignedByClass.put(long.class, BasicTypeID.ULONG);
		unsignedByClass.put(Byte.class, typeConversionContext.registry.getOptional(BasicTypeID.BYTE));
		unsignedByClass.put(Short.class, typeConversionContext.registry.getOptional(BasicTypeID.SHORT));
		unsignedByClass.put(Integer.class, typeConversionContext.registry.getOptional(BasicTypeID.INT));
		unsignedByClass.put(Long.class, typeConversionContext.registry.getOptional(BasicTypeID.LONG));
	}

	public void setBEP(BracketExpressionParser bep) {
		this.bep = bep;
	}

	private <T> TypeVariableContext convertTypeParameters(Class<T> cls) {
		//TypeVariableContext context = new TypeVariableContext();
		TypeVariable<Class<T>>[] javaTypeParameters = cls.getTypeParameters();
		TypeParameter[] typeParameters = new TypeParameter[cls.getTypeParameters().length];

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				TypeID type = loadType(typeConversionContext.context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
			typeParameters[i] = parameter;

			//Put up here so that Nested Type parameters may work..?
			typeConversionContext.context.put(typeVariable, parameter);
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			for (AnnotatedType bound : javaTypeParameters[i].getAnnotatedBounds()) {
				TypeID type = loadType(typeConversionContext.context, bound);
				typeParameters[i].addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}
		return typeConversionContext.context;
	}

	private Method getFunctionalInterfaceMethod(Class<?> functionalInterface) {
		for (Method method : functionalInterface.getMethods()) {
			if (Modifier.isPublic(method.getModifiers()) && Modifier.isAbstract(method.getModifiers()) && !method.isDefault())
				return method;
		}

		throw new IllegalArgumentException("Could not find functionalInterface method for class " + functionalInterface.getCanonicalName());
	}

	public void setHeaderConverter(JavaNativeHeaderConverter headerConverter) {
		this.headerConverter = headerConverter;
	}
}
