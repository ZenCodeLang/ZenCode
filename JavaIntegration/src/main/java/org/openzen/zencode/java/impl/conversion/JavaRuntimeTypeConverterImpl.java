package org.openzen.zencode.java.impl.conversion;

import org.openzen.zencode.java.JavaRuntimeTypeConverter;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.impl.JavaNativeModuleSpace;
import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.java.TypeVariableContext;
import org.openzen.zencode.java.module.JavaAnnotatedType;
import org.openzen.zencode.java.module.JavaNativePackageInfo;
import org.openzen.zencode.java.module.JavaRuntimeClass;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.ModuleSpace;
import org.openzen.zenscript.codemodel.compilation.CompileContext;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.type.IParsedType;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;

public class JavaRuntimeTypeConverterImpl implements JavaRuntimeTypeConverter {
	private final JavaNativeModuleSpace nativeModuleSpace;
	private final JavaNativePackageInfo packageInfo;
	private final Map<Class<?>, TypeID> typeByClass = new HashMap<>();
	private final Map<Class<?>, TypeID> unsignedByClass = new HashMap<>();
	private final Map<Class<?>, Function<TypeID[], TypeID>> specialTypes = new HashMap<>();
	private JavaNativeHeaderConverter headerConverter;

	public JavaRuntimeTypeConverterImpl(JavaNativeModuleSpace nativeModuleSpace, JavaNativePackageInfo packageInfo) {
		this.nativeModuleSpace = nativeModuleSpace;
		this.packageInfo = packageInfo;
		fillClassMaps();
		fillSpecialTypes();
	}

	public void setHeaderConverter(JavaNativeHeaderConverter headerConverter) {
		this.headerConverter = headerConverter;
	}

	@Override
	public TypeID getType(TypeVariableContext context, AnnotatedType type) {
		TypeID result;
		if (type.isAnnotationPresent(ZenCodeType.Unsigned.class)) {
			result = unsignedByClass.get((Class<?>) type.getType());
		} else if (type.isAnnotationPresent(ZenCodeType.USize.class)) {
			result = BasicTypeID.USIZE;
		} else {
			boolean unsigned = type.isAnnotationPresent(ZenCodeType.Unsigned.class);
			result = loadType(context, JavaAnnotatedType.of(type), unsigned);
		}

		boolean isOptional = type.isAnnotationPresent(ZenCodeType.Nullable.class);
		if (isOptional && !result.isOptional())
			result = new OptionalTypeID(result);

		return result;
	}

	@Override
	public TypeID parseType(String type) {
		for (TypeID value : this.typeByClass.values()) {
			if (value.toString().equals(type))
				return value;
		}

		for (TypeID value : this.unsignedByClass.values()) {
			if (value.toString().equals(type))
				return value;
		}

		try {
			CompileContext context = new CompileContext(packageInfo.getRoot(), packageInfo.getPkg(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
			final ZSTokenParser tokens = ZSTokenParser.create(new LiteralSourceFile("type reading: " + type, type), null);
			return IParsedType.parse(tokens).compile(context);
		} catch (IOException ex) {
			throw new AssertionError("Not supposed to happen");
		} catch (ParseException ex) {
			// TODO: log this properly
			return null;
		}
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

	private TypeSymbol loadRawType(TypeVariableContext context, JavaAnnotatedType type, boolean unsigned) {
		final JavaAnnotatedType.ElementType elementType = type.getElementType();

		try {
			switch (elementType) {
				case CLASS:
					return findType((Class<?>) type.getType());
				default:
					throw new IllegalArgumentException("Didn't expect to get this kind of type: " + type);
			}
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to analyze type: " + type, e);
		}
	}

	private TypeID loadAnnotatedParameterizedType(TypeVariableContext context, AnnotatedParameterizedType type, boolean unsigned) {
		final ParameterizedType parameterizedType = this.getTypeIfValid(JavaAnnotatedType.of(type.getType()), JavaAnnotatedType.ElementType.PARAMETERIZED_TYPE);
		final JavaAnnotatedType rawType = JavaAnnotatedType.of(parameterizedType.getRawType());

		final JavaAnnotatedType[] actualTypeArguments = JavaAnnotatedType.arrayOf(type.getAnnotatedActualTypeArguments());
		final TypeID[] codeParameters = new TypeID[actualTypeArguments.length];

		for (int i = 0; i < actualTypeArguments.length; i++) {
			codeParameters[i] = this.loadType(context, actualTypeArguments[i], false);
		}

		if (rawType.getElementType() == JavaAnnotatedType.ElementType.CLASS) {
			if (specialTypes.containsKey(rawType.getType())) {
				return specialTypes.get(rawType.getType()).apply(codeParameters);
			}

			final TypeSymbol rawTypeSymbol = loadRawType(context, rawType, unsigned);
			return DefinitionTypeID.create(rawTypeSymbol, codeParameters);
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
			final TypeID baseType = this.loadType(context, JavaAnnotatedType.of(type.getComponentType()), false);
			return new ArrayTypeID(baseType);
		}
		if (type.isAnnotationPresent(FunctionalInterface.class)) {
			return loadFunctionalInterface(context, type);
		}
		if (typeByClass.containsKey(type)) {
			return typeByClass.get(type);
		}

		final TypeSymbol definition = findType(type);
		if (definition instanceof JavaRuntimeClass) {
			((JavaRuntimeClass) definition).translateTypeParameters(context);
		}

		final List<TypeID> typeParameters = new ArrayList<>();
		for (TypeVariable<? extends Class<?>> typeParameter : type.getTypeParameters()) {
			typeParameters.add(new GenericTypeID(context.get(typeParameter)));
		}

		return DefinitionTypeID.create(definition, typeParameters.toArray(TypeID.NONE));
	}

	private TypeID loadGenericArray(TypeVariableContext context, GenericArrayType type, boolean unsigned) {
		final JavaAnnotatedType componentType = JavaAnnotatedType.of(type.getGenericComponentType());
		final TypeID baseType = this.loadType(context, componentType, unsigned);
		return new ArrayTypeID(baseType);
	}

	private TypeID loadParameterizedType(TypeVariableContext context, ParameterizedType type) {
		final Class<?> rawType = this.getTypeIfValid(JavaAnnotatedType.of(type.getRawType()), JavaAnnotatedType.ElementType.CLASS);
		final JavaAnnotatedType[] typeArguments = JavaAnnotatedType.arrayOf(type.getActualTypeArguments());

		if (rawType.isAnnotationPresent(FunctionalInterface.class)) {
			return loadFunctionalInterface(context, rawType, typeArguments);
		}

		TypeID[] codeParameters = new TypeID[typeArguments.length];
		for (int i = 0; i < typeArguments.length; i++) {
			codeParameters[i] = this.loadType(context, typeArguments[i], false);
		}

		if (rawType == Map.class) {
			return new AssocTypeID(codeParameters[0], codeParameters[1]);
		}

		final TypeSymbol definition = findType(rawType);
		return DefinitionTypeID.create(definition, codeParameters);
	}

	private TypeID loadTypeVariable(TypeVariableContext context, TypeVariable<?> variable) {
		return new GenericTypeID(context.get(variable));
	}

	private TypeID loadWildcard() {
		return BasicTypeID.UNDETERMINED;
	}

	private TypeSymbol findType(Class<?> cls) {
		// ToDo: Have a custom type for Collection
		if (cls == List.class || cls == Collection.class) {
			return packageInfo.getRoot().getImport(Arrays.asList("stdlib", "List"), 0);
		}
		if (cls == Object.class) {
			TypeSymbol result = packageInfo.getRoot().getImport(Arrays.asList("stdlib", "Object"), 0);
			if (result == null) {

				ZSPackage stdlib = packageInfo.getRoot().getOptional("stdlib").orElseThrow(() -> new IllegalStateException("Must depend on stdlib if trying to register java.lang.Object"));
				ModuleSymbol module = nativeModuleSpace.moduleSpace.getModule("stdlib").module;
				// registers itself to the package automatically
				new ClassDefinition(CodePosition.BUILTIN, module, stdlib, "Object", Modifiers.PUBLIC, null);
				result = packageInfo.getRoot().getImport(Arrays.asList("stdlib", "Object"), 0);
			}
			return result;
		}


		JavaNativeModule module = nativeModuleSpace.getModule(cls)
				.orElseThrow(() -> new IllegalArgumentException("Could not find module for class " + cls.getName()));
		return module.findLocalClass(cls)
				.orElseThrow(() -> new IllegalArgumentException("Could not find class " + cls.getName() + " in module " + module.getModule().name));
	}

	@SuppressWarnings("unchecked")
	private <T> T getTypeIfValid(final JavaAnnotatedType type, final JavaAnnotatedType.ElementType expected) {
		if (type.getElementType() != expected) {
			throw new IllegalArgumentException(expected + " was expected as a type, but " + type + " was found");
		}
		return (T) type.getType();
	}

/*	public Class<?> getClassFromType(TypeID type) {
		if (type instanceof DefinitionTypeID) {
			DefinitionTypeID definitionType = ((DefinitionTypeID) type);

			for (Map.Entry<Class<?>, TypeSymbol> ent : typeConversionContext.definitionByClass.entrySet()) {
				if (ent.getValue() == definitionType.definition)
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

			for (TypeSymbol value : typeConversionContext.definitionByClass.values()) {
				if (actualName.equals(value.getName()) && value.getPackage().equals(zsPackage))
					return DefinitionTypeID.createThis(value);
			}
		} catch (IllegalArgumentException ignored) {
		}

		try {
			CompileContext context = new CompileContext(typeConversionContext.root, packageInfo.getPkg(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
			final ZSTokenParser tokens = ZSTokenParser.create(new LiteralSourceFile("type reading: " + className, className), bep);
			return IParsedType.parse(tokens).compile(context);
		} catch (IOException ex) {
			throw new AssertionError("Not supposed to happen");
		} catch (ParseException ex) {
			// TODO: log this properly
			return null;
		}
	}*/

	private TypeID loadFunctionalInterface(TypeVariableContext loadContext, Class<?> cls, JavaAnnotatedType... parameters) {
		Method functionalInterfaceMethod = getFunctionalInterfaceMethod(cls);
		TypeVariableContext context = convertTypeParameters(cls);

		//TODO: This breaks if the functional interface type appears in the method's signature
		FunctionHeader header = headerConverter.getHeader(context, functionalInterfaceMethod);

		Map<TypeParameter, TypeID> mapping = new HashMap<>();
		TypeVariable<?>[] javaParameters = cls.getTypeParameters();
		for (int i = 0; i < parameters.length; i++) {
			mapping.put(context.get(javaParameters[i]), loadType(loadContext, parameters[i], false));
		}

		header = header.withGenericArguments(new GenericMapper(mapping, TypeID.NONE));
		JavaNativeMethod method = new JavaNativeMethod(
				JavaClass.fromInternalName(getInternalName(cls), JavaClass.Kind.INTERFACE),
				JavaNativeMethod.Kind.INTERFACE,
				functionalInterfaceMethod.getName(),
				false,
				getMethodDescriptor(functionalInterfaceMethod),
				JavaModifiers.PUBLIC | JavaModifiers.ABSTRACT,
				header.getReturnType().isGeneric());
		return new JavaFunctionalInterfaceTypeID(header, functionalInterfaceMethod, method);
	}

	private <T> TypeVariableContext convertTypeParameters(Class<T> cls) {
		TypeVariableContext context = new TypeVariableContext();
		TypeVariable<Class<T>>[] javaTypeParameters = cls.getTypeParameters();
		TypeParameter[] typeParameters = new TypeParameter[cls.getTypeParameters().length];

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Class<T>> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				TypeID type = getType(context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
			typeParameters[i] = parameter;

			//Put up here so that Nested Type parameters may work..?
			context.put(typeVariable, parameter);
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			for (AnnotatedType bound : javaTypeParameters[i].getAnnotatedBounds()) {
				TypeID type = getType(context, bound);
				typeParameters[i].addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}
		return context;
	}

	private Method getFunctionalInterfaceMethod(Class<?> functionalInterface) {
		for (Method method : functionalInterface.getMethods()) {
			if (Modifier.isPublic(method.getModifiers()) && Modifier.isAbstract(method.getModifiers()) && !method.isDefault())
				return method;
		}

		throw new IllegalArgumentException("Could not find functionalInterface method for class " + functionalInterface.getCanonicalName());
	}

	private void fillClassMaps() {
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
		typeByClass.put(Boolean.class, new OptionalTypeID(BasicTypeID.BOOL));
		typeByClass.put(Byte.class, new OptionalTypeID(BasicTypeID.BYTE));
		typeByClass.put(Short.class, new OptionalTypeID(BasicTypeID.SHORT));
		typeByClass.put(Integer.class, new OptionalTypeID(BasicTypeID.INT));
		typeByClass.put(Long.class, new OptionalTypeID(BasicTypeID.LONG));
		typeByClass.put(Float.class, new OptionalTypeID(BasicTypeID.FLOAT));
		typeByClass.put(Double.class, new OptionalTypeID(BasicTypeID.DOUBLE));

		unsignedByClass.put(byte.class, BasicTypeID.BYTE);
		unsignedByClass.put(char.class, BasicTypeID.CHAR);
		unsignedByClass.put(short.class, BasicTypeID.USHORT);
		unsignedByClass.put(int.class, BasicTypeID.UINT);
		unsignedByClass.put(long.class, BasicTypeID.ULONG);
		unsignedByClass.put(Byte.class, new OptionalTypeID(BasicTypeID.BYTE));
		unsignedByClass.put(Short.class, new OptionalTypeID(BasicTypeID.SHORT));
		unsignedByClass.put(Integer.class, new OptionalTypeID(BasicTypeID.INT));
		unsignedByClass.put(Long.class, new OptionalTypeID(BasicTypeID.LONG));
	}

	private void fillSpecialTypes() {
		specialTypes.put(Map.class, args -> new AssocTypeID(args[0], args[1]));

		specialTypes.put(BiConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, args[0], args[1])));
		specialTypes.put(BiFunction.class, args -> new FunctionTypeID(new FunctionHeader(args[2], args[0], args[1])));
		specialTypes.put(BiPredicate.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.BOOL, args[0], args[1])));
		specialTypes.put(BooleanSupplier.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.BOOL)));
		specialTypes.put(Consumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, args[0])));
		specialTypes.put(DoubleBinaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE, BasicTypeID.DOUBLE, BasicTypeID.DOUBLE)));
		specialTypes.put(DoubleConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, BasicTypeID.DOUBLE)));
		specialTypes.put(DoubleFunction.class, args -> new FunctionTypeID(new FunctionHeader(args[0], BasicTypeID.DOUBLE)));
		specialTypes.put(DoublePredicate.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.BOOL, BasicTypeID.DOUBLE)));
		specialTypes.put(DoubleSupplier.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE)));
		specialTypes.put(DoubleToIntFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT, BasicTypeID.DOUBLE)));
		specialTypes.put(DoubleToLongFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG, BasicTypeID.DOUBLE)));
		specialTypes.put(DoubleUnaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE, BasicTypeID.DOUBLE)));
		specialTypes.put(Function.class, args -> new FunctionTypeID(new FunctionHeader(args[1], args[0])));
		specialTypes.put(IntBinaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT, BasicTypeID.INT, BasicTypeID.INT)));
		specialTypes.put(IntConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, BasicTypeID.INT)));
		specialTypes.put(IntFunction.class, args -> new FunctionTypeID(new FunctionHeader(args[0], BasicTypeID.INT)));
		specialTypes.put(IntPredicate.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.BOOL, BasicTypeID.INT)));
		specialTypes.put(IntSupplier.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT)));
		specialTypes.put(IntToDoubleFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE, BasicTypeID.INT)));
		specialTypes.put(IntToLongFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG, BasicTypeID.INT)));
		specialTypes.put(IntUnaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT, BasicTypeID.INT)));
		specialTypes.put(LongBinaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG, BasicTypeID.LONG, BasicTypeID.LONG)));
		specialTypes.put(LongConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, BasicTypeID.LONG)));
		specialTypes.put(LongFunction.class, args -> new FunctionTypeID(new FunctionHeader(args[0], BasicTypeID.LONG)));
		specialTypes.put(LongPredicate.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.BOOL, BasicTypeID.LONG)));
		specialTypes.put(LongSupplier.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG)));
		specialTypes.put(LongToDoubleFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE, BasicTypeID.LONG)));
		specialTypes.put(LongToIntFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT, BasicTypeID.LONG)));
		specialTypes.put(LongUnaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG, BasicTypeID.LONG)));
		specialTypes.put(ObjDoubleConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, args[0], BasicTypeID.DOUBLE)));
		specialTypes.put(ObjIntConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, args[0], BasicTypeID.INT)));
		specialTypes.put(ObjLongConsumer.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.VOID, args[0], BasicTypeID.LONG)));
		specialTypes.put(Predicate.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.BOOL, args[0])));
		specialTypes.put(Supplier.class, args -> new FunctionTypeID(new FunctionHeader(args[0])));
		specialTypes.put(ToDoubleBiFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE, args[0], args[1])));
		specialTypes.put(ToDoubleFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.DOUBLE, args[0])));
		specialTypes.put(ToIntBiFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT, args[0], args[1])));
		specialTypes.put(ToIntFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.INT, args[0])));
		specialTypes.put(ToLongBiFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG, args[0], args[1])));
		specialTypes.put(ToLongFunction.class, args -> new FunctionTypeID(new FunctionHeader(BasicTypeID.LONG, args[0])));
		specialTypes.put(UnaryOperator.class, args -> new FunctionTypeID(new FunctionHeader(args[0], args[0])));
	}
}
