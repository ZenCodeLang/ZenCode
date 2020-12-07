package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class JavaNativeExpansionConverter {
	private final JavaNativeTypeConverter typeConverter;
	private final IZSLogger logger;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeMemberConverter memberConverter;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final PackageDefinitions definitions;
	private final JavaNativeHeaderConverter headerConverter;


	public JavaNativeExpansionConverter(JavaNativeTypeConverter typeConverter, IZSLogger logger, JavaNativePackageInfo packageInfo, JavaNativeMemberConverter memberConverter, JavaNativeTypeConversionContext typeConversionContext, PackageDefinitions definitions, JavaNativeHeaderConverter headerConverter) {

		this.typeConverter = typeConverter;
		this.logger = logger;
		this.packageInfo = packageInfo;
		this.memberConverter = memberConverter;
		this.typeConversionContext = typeConversionContext;
		this.definitions = definitions;
		this.headerConverter = headerConverter;
	}

	public <T> ExpansionDefinition convertExpansion(Class<T> cls) {
		if (!cls.isAnnotationPresent(ZenCodeType.Expansion.class)) {
			throw new IllegalArgumentException("Cannot convert class " + cls + " as it does not have an Expansion annotation");
		}

		final String expandedName = cls.getAnnotation(ZenCodeType.Expansion.class).value();
		final TypeID expandedType = typeConverter.getTypeFromName(expandedName);
		if (expandedType == null)
			throw new IllegalArgumentException("Could not find definition for name " + expandedName);

		final ExpansionDefinition expansion = new ExpansionDefinition(CodePosition.NATIVE, packageInfo.getModule(), packageInfo.getPkg(), Modifiers.PUBLIC, null);
		final JavaClass javaClass = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(cls), JavaClass.Kind.CLASS);
		expansion.target = expandedType;
		typeConversionContext.definitionByClass.put(cls, expansion);

		boolean addExpansion = false;
		for (Method method : cls.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() < 1) {
				//Log?
				continue;
			}


			final Class<?> classFromType = typeConverter.getClassFromType(expandedType);
			if (classFromType == null) {
				//TODO REMOVE
				logger.debug("Could not get class for type " + expandedType + " attempting to do stuff anyways");
			}


			final ZenCodeType.Method methodAnnotation = method.getAnnotation(ZenCodeType.Method.class);
			if (methodAnnotation != null) {
				checkExpandedType(classFromType, method);
				String name = !methodAnnotation.value().isEmpty() ? methodAnnotation.value() : method.getName();
				//TypeVariableContext context = new TypeVariableContext();

				final Parameter[] parameters = getExpansionParameters(method);

				FunctionHeader header = headerConverter.getHeader(typeConversionContext.context, method.getAnnotatedReturnType(), parameters, method.getTypeParameters(), method.getAnnotatedExceptionTypes());
				final MethodMember member = new MethodMember(CodePosition.NATIVE, expansion, headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC, name, header, null);

				expansion.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, JavaMethod.getStatic(javaClass, name, org.objectweb.asm.Type.getMethodDescriptor(method), headerConverter.getMethodModifiers(method)));
				addExpansion = true;
			}

			final ZenCodeType.Getter getterAnnotation = method.getAnnotation(ZenCodeType.Getter.class);
			if (getterAnnotation != null) {
				checkExpandedType(classFromType, method);
				TypeID type = typeConverter.loadStoredType(typeConversionContext.context, method.getAnnotatedReturnType());
				int modifiers = headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC;
				final String name = getterAnnotation.value().isEmpty() ? memberConverter.translateGetterName(method.getName()) : getterAnnotation.value();
				final GetterMember member = new GetterMember(CodePosition.NATIVE, expansion, modifiers, name, type, null);

				expansion.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, type));
				addExpansion = true;
			}

			final ZenCodeType.Caster casterAnnotation = method.getAnnotation(ZenCodeType.Caster.class);
			if (casterAnnotation != null) {
				checkExpandedType(classFromType, method);
				boolean implicit = casterAnnotation.implicit();
				int modifiers = headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC;
				if (implicit) {
					modifiers |= Modifiers.IMPLICIT;
				}
				//TypeVariableContext typeConversionContext.context = new TypeVariableContext();
				TypeID toType = typeConverter.loadStoredType(typeConversionContext.context, method.getAnnotatedReturnType());
				final CasterMember member = new CasterMember(CodePosition.NATIVE, expansion, modifiers, toType, null);

				expansion.addMember(member);
				typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, member.toType));
				addExpansion = true;
			}

			//TODO not working, not sure if it *should* work
//            final ZenCodeType.Operator operatorAnnotation = method.getAnnotation(ZenCodeType.Operator.class);
//            if(operatorAnnotation != null) {
//
//                TypeVariableContext typeConversionContext.context = new TypeVariableContext();
//
//                final Parameter[] parameters = getExpansionParameters(method);
//
//                FunctionHeader header = getHeader(typeConversionContext.context, method.getAnnotatedReturnType(), parameters, method.getTypeParameters(), method.getAnnotatedExceptionTypes());
//                final OperatorMember member = new OperatorMember(CodePosition.NATIVE, expansion, headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC, OperatorType.valueOf(operatorAnnotation.value().toString()), header, null);
//
//                expansion.addMember(member);
//                typeConversionContext.compiled.setMethodInfo(member, getMethod(javaClass, method, member.header.getReturnType()));
//                addExpansion = true;
//            }
		}

		if (addExpansion) {
			typeConversionContext.compiled.setExpansionClassInfo(expansion, javaClass);
			definitions.add(expansion);
		}

		return expansion;
	}

	private void checkExpandedType(Class<?> clsType, Method method) {
		if (clsType == null) {
			return;
		}
		if (!method.getParameterTypes()[0].isAssignableFrom(clsType)) {
			throw new IllegalArgumentException("Cannot add extension method " + method + " as its first parameter does not match the extended type.");
		}
	}


	private Parameter[] getExpansionParameters(Method method) {
		final Parameter[] parameters = new Parameter[method.getParameterCount() - 1];
		System.arraycopy(method.getParameters(), 1, parameters, 0, method.getParameterCount() - 1);
		return parameters;
	}

}
