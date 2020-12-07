package org.openzen.zencode.java.module;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.FileResolutionContext;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.expression.ParsedExpression;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class JavaNativeMemberConverter {

	private final JavaNativeTypeConverter typeConverter;
	private final ZSPackage pkg;
	private final Module module;
	private final Map<String, ISymbol> globals;
	private final GlobalTypeRegistry registry;
	private BracketExpressionParser bep;

	public JavaNativeMemberConverter(JavaNativeTypeConverter typeConverter, ZSPackage pkg, Module module, Map<String, ISymbol> globals, GlobalTypeRegistry registry) {
		this.typeConverter = typeConverter;
		this.pkg = pkg;
		this.module = module;
		this.globals = globals;
		this.registry = registry;
	}

	@SuppressWarnings("rawtypes")
	public ConstructorMember asConstructor(TypeVariableContext context, HighLevelDefinition definition, java.lang.reflect.Constructor method) {
		FunctionHeader header = getHeader(context, method);
		return new ConstructorMember(
				CodePosition.NATIVE,
				definition,
				Modifiers.PUBLIC,
				header,
				null);
	}

	public MethodMember asMethod(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Method annotation) {
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

	public OperatorMember asOperator(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Operator annotation) {
		FunctionHeader header = getHeader(context, method);
		if (Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException("operator method \"" + method.toString() + "\"cannot be static");

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

	public GetterMember asGetter(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Getter annotation) {
		TypeID type = typeConverter.loadStoredType(context, method.getAnnotatedReturnType());
		String name = null;
		if (annotation != null && !annotation.value().isEmpty())
			name = annotation.value();
		if (name == null)
			name = translateGetterName(method.getName());

		return new GetterMember(CodePosition.NATIVE, definition, getMethodModifiers(method), name, type, null);
	}

	public SetterMember asSetter(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Setter annotation) {
		if (method.getParameterCount() != 1)
			throw new IllegalArgumentException("Illegal setter: \"" + method.toString() + "\"must have exactly 1 parameter");

		TypeID type = typeConverter.loadStoredType(context, method.getAnnotatedParameterTypes()[0]);
		String name = null;
		if (annotation != null && !annotation.value().isEmpty())
			name = annotation.value();
		if (name == null)
			name = translateSetterName(method.getName());

		return new SetterMember(CodePosition.NATIVE, definition, getMethodModifiers(method), name, type, null);
	}

	public CasterMember asCaster(TypeVariableContext context, HighLevelDefinition definition, Method method, ZenCodeType.Caster annotation) {
		boolean implicit = annotation != null && annotation.implicit();
		int modifiers = Modifiers.PUBLIC;
		if (implicit)
			modifiers |= Modifiers.IMPLICIT;

		TypeID toType = typeConverter.loadStoredType(context, method.getAnnotatedReturnType());
		return new CasterMember(CodePosition.NATIVE, definition, modifiers, toType, null);
	}

	public boolean isGetterName(String name) {
		return name.startsWith("get") || name.startsWith("is") || name.startsWith("has");
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	public FunctionHeader getHeader(TypeVariableContext context, java.lang.reflect.Constructor constructor) {
		return getHeader(
				context,
				null,
				constructor.getParameters(),
				constructor.getTypeParameters(),
				constructor.getAnnotatedExceptionTypes());
	}

	public FunctionHeader getHeader(TypeVariableContext context, Method method) {
		return getHeader(
				context,
				method.getAnnotatedReturnType(),
				method.getParameters(),
				method.getTypeParameters(),
				method.getAnnotatedExceptionTypes());
	}

	public FunctionHeader getHeader(
			TypeVariableContext context,
			AnnotatedType javaReturnType,
			Parameter[] javaParameters,
			TypeVariable<Method>[] javaTypeParameters,
			AnnotatedType[] exceptionTypes) {


		TypeParameter[] typeParameters = new TypeParameter[javaTypeParameters.length];
		for (int i = 0; i < javaTypeParameters.length; i++) {
			//Put up here for nested parameters?
			TypeVariable<Method> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			typeParameters[i] = parameter;
			context.put(typeVariable, parameter);
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<Method> javaTypeParameter = javaTypeParameters[i];

			for (AnnotatedType bound : javaTypeParameter.getAnnotatedBounds())
				typeParameters[i].addBound(new ParameterTypeBound(CodePosition.NATIVE, typeConverter.loadType(context, bound)));
		}

		FunctionParameter[] parameters = new FunctionParameter[javaParameters.length];
		int classParameters = 0;
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = javaParameters[i];
			if (parameter.getType().getCanonicalName().contentEquals("java.lang.Class")) {
				classParameters++;
			}

			//AnnotatedType parameterType = parameter.getAnnotatedType();
			TypeID type = typeConverter.loadStoredType(context, parameter);
			Expression defaultValue = getDefaultValue(parameter, type);
			parameters[i] = new FunctionParameter(type, parameter.getName(), defaultValue, parameter.isVarArgs());
		}
		if (classParameters > 0 && classParameters == typeParameters.length) {
			parameters = Arrays.copyOfRange(parameters, classParameters, parameters.length);
		}

		if (exceptionTypes.length > 1)
			throw new IllegalArgumentException("A method can only throw a single exception type!");

		TypeID returnType = javaReturnType == null ? BasicTypeID.VOID : typeConverter.loadStoredType(context, javaReturnType);
		TypeID thrownType = exceptionTypes.length == 0 ? null : typeConverter.loadStoredType(context, exceptionTypes[0]);
		return new FunctionHeader(typeParameters, returnType, thrownType, parameters);
	}

	public int getMethodModifiers(Member method) {
		int result = Modifiers.PUBLIC;
		if (Modifier.isStatic(method.getModifiers()))
			result |= Modifiers.STATIC;
		if (Modifier.isFinal(method.getModifiers()))
			result |= Modifiers.FINAL;

		return result;
	}

	public Expression getDefaultValue(Parameter parameter, TypeID type) {
		if (parameter.isAnnotationPresent(ZenCodeType.Optional.class)) {
			final String s = parameter.getAnnotation(ZenCodeType.Optional.class).value();
			if (s.isEmpty()) {
				Expression defaultValue = type.getDefaultValue();
				if (defaultValue == null)
					throw new IllegalArgumentException(type.toString() + " doesn't have a default value");
				return defaultValue;
			}
			try {
				final String filename = "internal: " + parameter.getDeclaringExecutable().getName();

				final CompilingPackage rootCompiling = new CompilingPackage(pkg, module);
				final ModuleTypeResolutionContext context = new ModuleTypeResolutionContext(registry, new AnnotationDefinition[0], pkg, rootCompiling, globals);
				final FileResolutionContext fContext = new FileResolutionContext(context, pkg, rootCompiling);
				final FileScope fileScope = new FileScope(fContext, Collections.emptyList(), globals, member -> {
				});
				final ZSTokenParser tokens = ZSTokenParser.create(new LiteralSourceFile(filename, s), bep);

				return ParsedExpression.parse(tokens).compile(new ExpressionScope(fileScope)).eval().castExplicit(CodePosition.GENERATED, fileScope, type, type.isOptional());
			} catch (IOException | ParseException | CompileException ex) {
				//TODO REMOVE
				ex.printStackTrace();
				return null;
			}
			//}
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalInt.class)) {
			ZenCodeType.OptionalInt annotation = parameter.getAnnotation(ZenCodeType.OptionalInt.class);
			if (type == BasicTypeID.BYTE)
				return new ConstantByteExpression(CodePosition.NATIVE, annotation.value());
			else if (type == BasicTypeID.SBYTE)
				return new ConstantSByteExpression(CodePosition.NATIVE, (byte) annotation.value());
			else if (type == BasicTypeID.SHORT)
				return new ConstantShortExpression(CodePosition.NATIVE, (short) annotation.value());
			else if (type == BasicTypeID.USHORT)
				return new ConstantUShortExpression(CodePosition.NATIVE, annotation.value());
			else if (type == BasicTypeID.INT)
				return new ConstantIntExpression(CodePosition.NATIVE, annotation.value());
			else if (type == BasicTypeID.UINT)
				return new ConstantUIntExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use int default values for " + type.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalLong.class)) {
			ZenCodeType.OptionalLong annotation = parameter.getAnnotation(ZenCodeType.OptionalLong.class);
			if (type == BasicTypeID.LONG)
				return new ConstantLongExpression(CodePosition.NATIVE, annotation.value());
			else if (type == BasicTypeID.ULONG)
				return new ConstantULongExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use long default values for " + type.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalFloat.class)) {
			ZenCodeType.OptionalFloat annotation = parameter.getAnnotation(ZenCodeType.OptionalFloat.class);
			if (type == BasicTypeID.FLOAT)
				return new ConstantFloatExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use float default values for " + type.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalDouble.class)) {
			ZenCodeType.OptionalDouble annotation = parameter.getAnnotation(ZenCodeType.OptionalDouble.class);
			if (type == BasicTypeID.DOUBLE)
				return new ConstantDoubleExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use double default values for " + type.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalString.class)) {
			ZenCodeType.OptionalString annotation = parameter.getAnnotation(ZenCodeType.OptionalString.class);
			if (type == BasicTypeID.STRING) {
				return new ConstantStringExpression(CodePosition.NATIVE, annotation.value());
			} else {
				throw new IllegalArgumentException("Cannot use string default values for " + type.toString());
			}
		} else {
			return null;
		}
	}

	public void setBEP(BracketExpressionParser bep) {
		this.bep = bep;
	}
}
