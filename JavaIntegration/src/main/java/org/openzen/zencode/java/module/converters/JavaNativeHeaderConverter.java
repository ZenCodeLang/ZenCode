package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.java.module.TypeVariableContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.FileResolutionContext;
import org.openzen.zenscript.codemodel.context.ModuleTypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaTypeInfo;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.parser.BracketExpressionParser;
import org.openzen.zenscript.parser.expression.ParsedExpression;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;

public class JavaNativeHeaderConverter {
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private BracketExpressionParser bep;

	public JavaNativeHeaderConverter(JavaNativeTypeConverter typeConverter, JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext) {
		this.typeConverter = typeConverter;
		this.packageInfo = packageInfo;
		this.typeConversionContext = typeConversionContext;
		typeConverter.setHeaderConverter(this);
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
			parameters[i] = new FunctionParameter(type, parameter.getName(), parameter.isVarArgs());
			parameters[i].defaultValue = getDefaultValue(parameter, type, parameters[i]);
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

	public Expression getDefaultValue(Parameter parameter, TypeID type, FunctionParameter functionParameter) {

		final TypeID baseType = type.isOptional() ? type.withoutOptional() : type;
		if (parameter.isAnnotationPresent(ZenCodeType.Optional.class)) {
			if (JavaTypeInfo.get(type).primitive) {
				throw new IllegalArgumentException("Cannot use generic Optional annotation for type (" + type.withoutOptional().toString() + ") as it is primitive! Use the corresponding primitive @Optional annotation instead (E.G. @OptionalInt, @OptionalBoolean).");
			}
			final String s = parameter.getAnnotation(ZenCodeType.Optional.class).value();
			if (s.isEmpty()) {
				Expression defaultValue = type.getDefaultValue();
				if (defaultValue == null)
					throw new IllegalArgumentException(type + " doesn't have a default value");
				return defaultValue;
			}
			try {
				final String filename = "internal: " + parameter.getDeclaringExecutable().getName();

				final CompilingPackage rootCompiling = new CompilingPackage(packageInfo.getPkg(), packageInfo.getModule());
				final ModuleTypeResolutionContext context = new ModuleTypeResolutionContext(typeConversionContext.registry, new AnnotationDefinition[0], packageInfo.getPkg(), rootCompiling, typeConversionContext.globals);
				final FileResolutionContext fContext = new FileResolutionContext(context, packageInfo.getPkg(), rootCompiling);
				final FileScope fileScope = new FileScope(fContext, typeConversionContext.compiled.getExpansions(), typeConversionContext.globals, member -> {
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
			if (baseType == BasicTypeID.BYTE)
				return new ConstantByteExpression(CodePosition.NATIVE, annotation.value());
			else if (baseType == BasicTypeID.SBYTE)
				return new ConstantSByteExpression(CodePosition.NATIVE, (byte) annotation.value());
			else if (baseType == BasicTypeID.SHORT)
				return new ConstantShortExpression(CodePosition.NATIVE, (short) annotation.value());
			else if (baseType == BasicTypeID.USHORT)
				return new ConstantUShortExpression(CodePosition.NATIVE, annotation.value());
			else if (baseType == BasicTypeID.INT)
				return new ConstantIntExpression(CodePosition.NATIVE, annotation.value());
			else if (baseType == BasicTypeID.UINT)
				return new ConstantUIntExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use int default values for " + baseType.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalLong.class)) {
			ZenCodeType.OptionalLong annotation = parameter.getAnnotation(ZenCodeType.OptionalLong.class);
			if (baseType == BasicTypeID.LONG)
				return new ConstantLongExpression(CodePosition.NATIVE, annotation.value());
			else if (baseType == BasicTypeID.ULONG)
				return new ConstantULongExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use long default values for " + baseType.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalFloat.class)) {
			ZenCodeType.OptionalFloat annotation = parameter.getAnnotation(ZenCodeType.OptionalFloat.class);
			if (baseType == BasicTypeID.FLOAT)
				return new ConstantFloatExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use float default values for " + baseType.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalDouble.class)) {
			ZenCodeType.OptionalDouble annotation = parameter.getAnnotation(ZenCodeType.OptionalDouble.class);
			if (baseType == BasicTypeID.DOUBLE)
				return new ConstantDoubleExpression(CodePosition.NATIVE, annotation.value());
			else
				throw new IllegalArgumentException("Cannot use double default values for " + baseType.toString());
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalString.class)) {
			ZenCodeType.OptionalString annotation = parameter.getAnnotation(ZenCodeType.OptionalString.class);
			if (baseType == BasicTypeID.STRING) {
				return new ConstantStringExpression(CodePosition.NATIVE, annotation.value());
			} else {
				throw new IllegalArgumentException("Cannot use string default values for " + baseType.toString());
			}
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalBoolean.class)) {
			ZenCodeType.OptionalBoolean annotation = parameter.getAnnotation(ZenCodeType.OptionalBoolean.class);
			if (baseType == BasicTypeID.BOOL) {
				return new ConstantBoolExpression(CodePosition.NATIVE, annotation.value());
			} else {
				throw new IllegalArgumentException("Cannot use boolean default values for " + baseType.toString());
			}
		} else if (parameter.isAnnotationPresent(ZenCodeType.OptionalChar.class)) {
			ZenCodeType.OptionalChar annotation = parameter.getAnnotation(ZenCodeType.OptionalChar.class);
			if (baseType == BasicTypeID.CHAR) {
				return new ConstantCharExpression(CodePosition.NATIVE, annotation.value());
			} else {
				throw new IllegalArgumentException("Cannot use char default values for " + baseType.toString());
			}
		} else {
			return null;
		}
	}

	public void setBEP(BracketExpressionParser bep) {
		this.bep = bep;
	}
}
