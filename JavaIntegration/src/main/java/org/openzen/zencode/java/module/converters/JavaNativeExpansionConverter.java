package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class JavaNativeExpansionConverter {
	private final JavaNativeTypeConverter typeConverter;
	private final IZSLogger logger;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeMemberConverter memberConverter;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeHeaderConverter headerConverter;


	public JavaNativeExpansionConverter(JavaNativeTypeConverter typeConverter, IZSLogger logger, JavaNativePackageInfo packageInfo, JavaNativeMemberConverter memberConverter, JavaNativeTypeConversionContext typeConversionContext, JavaNativeHeaderConverter headerConverter) {
		this.typeConverter = typeConverter;
		this.logger = logger;
		this.packageInfo = packageInfo;
		this.memberConverter = memberConverter;
		this.typeConversionContext = typeConversionContext;
		this.headerConverter = headerConverter;
	}

	public ExpansionDefinition convertExpansion(Class<?> cls) {

		if (doesClassNotHaveAnnotation(cls)) {
			throw new IllegalArgumentException("Cannot convert class " + cls + " as it does not have an Expansion annotation");
		}

		final String expandedName = getExpandedName(cls);
		final TypeID expandedType = typeConverter.getTypeFromName(expandedName);
		if (expandedType == null)
			throw new IllegalArgumentException("Could not find definition for name " + expandedName);

		final ExpansionDefinition expansion = new ExpansionDefinition(CodePosition.NATIVE, packageInfo.getModule(), packageInfo.getPkg(), Modifiers.PUBLIC, null);
		final JavaClass javaClass = JavaClass.fromInternalName(org.objectweb.asm.Type.getInternalName(cls), JavaClass.Kind.CLASS);
		expansion.target = expandedType;
		typeConversionContext.definitionByClass.put(cls, expansion);

		fillAnnotatedMethods(cls, expandedType, expansion, javaClass);

		if (!expansion.members.isEmpty()) {
			typeConversionContext.compiled.setExpansionClassInfo(expansion, javaClass);
			typeConversionContext.packageDefinitions.add(expansion);
		}

		return expansion;
	}

	private void fillAnnotatedMethods(Class<?> cls, TypeID expandedType, ExpansionDefinition expansion, JavaClass javaClass) {
		for (Method method : cls.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				//Log?
				continue;
			}


			final Class<?> classFromType = typeConverter.getClassFromType(expandedType);
			if (classFromType == null) {
				//TODO REMOVE
				logger.debug("Could not get class for type " + expandedType + " attempting to do stuff anyways");
			}


			final ZenCodeType.Method methodAnnotation = getMethodAnnotation(method, ZenCodeType.Method.class);
			if (methodAnnotation != null) {
				fillMethod(expansion, javaClass, method, classFromType, methodAnnotation);
			}

			final ZenCodeType.StaticExpansionMethod staticExpansionMethod = getMethodAnnotation(method, ZenCodeType.StaticExpansionMethod.class);
			if (staticExpansionMethod != null) {
				fillStaticMethod(expansion, javaClass, method, staticExpansionMethod);
			}

			final ZenCodeType.Getter getterAnnotation = getMethodAnnotation(method, ZenCodeType.Getter.class);
			if (getterAnnotation != null) {
				fillGetter(expansion, javaClass, method, classFromType, getterAnnotation);
			}

			final ZenCodeType.Setter setterAnnotation = getMethodAnnotation(method, ZenCodeType.Setter.class);
			if (setterAnnotation != null) {
				fillSetter(expansion, javaClass, method, classFromType, setterAnnotation);
			}

			final ZenCodeType.Caster casterAnnotation = getMethodAnnotation(method, ZenCodeType.Caster.class);
			if (casterAnnotation != null) {
				fillCaster(expansion, javaClass, method, classFromType, casterAnnotation);
			}

			final ZenCodeType.Operator operatorAnnotation = method.getAnnotation(ZenCodeType.Operator.class);
			if (operatorAnnotation != null) {
				fillOperator(expansion, javaClass, method, classFromType, operatorAnnotation);
			}
		}
	}

	private void fillCaster(ExpansionDefinition expansion, JavaClass javaClass, Method method, Class<?> classFromType, ZenCodeType.Caster casterAnnotation) {
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
	}

	private void fillGetter(ExpansionDefinition expansion, JavaClass javaClass, Method method, Class<?> classFromType, ZenCodeType.Getter getterAnnotation) {
		checkExpandedType(classFromType, method);
		TypeID type = typeConverter.loadStoredType(typeConversionContext.context, method.getAnnotatedReturnType());
		int modifiers = headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC;
		final String name = getterAnnotation.value().isEmpty() ? memberConverter.translateGetterName(method.getName()) : getterAnnotation.value();
		final GetterMember member = new GetterMember(CodePosition.NATIVE, expansion, modifiers, name, type, null);

		expansion.addMember(member);
		typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, type));
	}

	private void fillSetter(ExpansionDefinition expansion, JavaClass javaClass, Method method, Class<?> classFromType, ZenCodeType.Setter setterAnnotation) {
		checkExpandedType(classFromType, method);
		Parameter[] expansionParameters = getExpansionParameters(method);
		if (expansionParameters.length != 1) {
			throw new IllegalArgumentException("Cannot add extension setter " + method + " as it does not have a single parameter for the set value!");
		}
		TypeID type = typeConverter.loadStoredType(typeConversionContext.context, expansionParameters[0]);
		int modifiers = headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC;
		final String name = setterAnnotation.value().isEmpty() ? memberConverter.translateSetterName(method.getName()) : setterAnnotation.value();
		final SetterMember member = new SetterMember(CodePosition.NATIVE, expansion, modifiers, name, type, null);

		expansion.addMember(member);
		typeConversionContext.compiled.setMethodInfo(member, memberConverter.getMethod(javaClass, method, type));
	}

	private void fillMethod(ExpansionDefinition expansion, JavaClass javaClass, Method method, Class<?> classFromType, ZenCodeType.Method methodAnnotation) {
		checkExpandedType(classFromType, method);
		String name = !methodAnnotation.value().isEmpty() ? methodAnnotation.value() : method.getName();
		//TypeVariableContext context = new TypeVariableContext();

		final Parameter[] parameters = getExpansionParameters(method);

		FunctionHeader header = headerConverter.getHeader(typeConversionContext.context, method.getAnnotatedReturnType(), parameters, method.getTypeParameters(), method.getAnnotatedExceptionTypes());
		final MethodMember member = new MethodMember(CodePosition.NATIVE, expansion, headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC, name, header, null);

		expansion.addMember(member);
		typeConversionContext.compiled.setMethodInfo(member, JavaMethod.getStatic(javaClass, method.getName(), org.objectweb.asm.Type.getMethodDescriptor(method), headerConverter.getMethodModifiers(method)));
	}

	private void fillStaticMethod(ExpansionDefinition expansion, JavaClass javaClass, Method method, ZenCodeType.StaticExpansionMethod annotation) {
		String name = !annotation.value().isEmpty() ? annotation.value() : method.getName();

		final Parameter[] parameters = method.getParameters();
		FunctionHeader header = headerConverter.getHeader(typeConversionContext.context, method.getAnnotatedReturnType(), parameters, method.getTypeParameters(), method.getAnnotatedExceptionTypes());
		final MethodMember member = new MethodMember(CodePosition.NATIVE, expansion, headerConverter.getMethodModifiers(method), name, header, null);

		expansion.addMember(member);
		typeConversionContext.compiled.setMethodInfo(member, JavaMethod.getStatic(javaClass, method.getName(), org.objectweb.asm.Type.getMethodDescriptor(method), headerConverter.getMethodModifiers(method)));
	}

	private void fillOperator(ExpansionDefinition expansion, JavaClass javaClass, Method method, Class<?> classFromType, ZenCodeType.Operator operator) {
		checkExpandedType(classFromType, method);

		final Parameter[] parameters = getExpansionParameters(method);

		final OperatorType operatorType = getOperatorTypeFrom(operator);
		FunctionHeader header = headerConverter.getHeader(typeConversionContext.context, method.getAnnotatedReturnType(), parameters, method.getTypeParameters(), method.getAnnotatedExceptionTypes());
		final OperatorMember member = new OperatorMember(CodePosition.NATIVE, expansion, headerConverter.getMethodModifiers(method) ^ Modifiers.STATIC, operatorType, header, null);

		expansion.addMember(member);
		typeConversionContext.compiled.setMethodInfo(member, JavaMethod.getStatic(javaClass, method.getName(), org.objectweb.asm.Type.getMethodDescriptor(method), headerConverter.getMethodModifiers(method)));
	}

	private OperatorType getOperatorTypeFrom(ZenCodeType.Operator operator) {
		switch (operator.value()) {
			case ADD:
				return OperatorType.ADD;
			case SUB:
				return OperatorType.SUB;
			case MUL:
				return OperatorType.MUL;
			case DIV:
				return OperatorType.DIV;
			case MOD:
				return OperatorType.MOD;
			case CAT:
				return OperatorType.CAT;
			case OR:
				return OperatorType.OR;
			case AND:
				return OperatorType.AND;
			case XOR:
				return OperatorType.XOR;
			case NEG:
				return OperatorType.NEG;
			case INVERT:
				return OperatorType.INVERT;
			case NOT:
				return OperatorType.NOT;
			case INDEXSET:
				return OperatorType.INDEXSET;
			case INDEXGET:
				return OperatorType.INDEXGET;
			case CONTAINS:
				return OperatorType.CONTAINS;
			case COMPARE:
				return OperatorType.COMPARE;
			case MEMBERGETTER:
				return OperatorType.MEMBERGETTER;
			case MEMBERSETTER:
				return OperatorType.MEMBERSETTER;
			case EQUALS:
				return OperatorType.EQUALS;
			case NOTEQUALS:
				return OperatorType.NOTEQUALS;
			case SHL:
				return OperatorType.SHL;
			case SHR:
				return OperatorType.SHR;
			case ADDASSIGN:
				return OperatorType.ADDASSIGN;
			case SUBASSIGN:
				return OperatorType.SUBASSIGN;
			case MULASSIGN:
				return OperatorType.MULASSIGN;
			case DIVASSIGN:
				return OperatorType.DIVASSIGN;
			case MODASSIGN:
				return OperatorType.MODASSIGN;
			case CATASSIGN:
				return OperatorType.CATASSIGN;
			case ORASSIGN:
				return OperatorType.ORASSIGN;
			case ANDASSIGN:
				return OperatorType.ANDASSIGN;
			case XORASSIGN:
				return OperatorType.XORASSIGN;
			case SHLASSIGN:
				return OperatorType.SHLASSIGN;
			case SHRASSIGN:
				return OperatorType.SHRASSIGN;
		}
		throw new IllegalArgumentException("Unknown OperatorType: " + operator.value());
	}

	protected String getExpandedName(Class<?> cls) {
		return cls.getAnnotation(ZenCodeType.Expansion.class).value();
	}

	protected boolean doesClassNotHaveAnnotation(Class<?> cls) {
		return !cls.isAnnotationPresent(ZenCodeType.Expansion.class);
	}

	private void checkExpandedType(Class<?> clsType, Method method) {
		if (method.getParameterTypes().length < 1) {
			throw new IllegalArgumentException("Cannot add extension method " + method + " as it does not have any parameters and is a normal expansion.");
		}

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

	/**
	 * Protected so that other implementations can inject "virtual" Annotations here
	 */
	protected <T extends Annotation> T getMethodAnnotation(Method method, Class<T> annotationClass) {
		return method.getAnnotation(annotationClass);
	}

}
