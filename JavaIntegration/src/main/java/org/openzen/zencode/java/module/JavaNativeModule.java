/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java.module;

import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.converters.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.ExpressionSymbol;
import org.openzen.zenscript.codemodel.expression.StaticGetterExpression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.PartialStaticMemberGroupExpression;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


/**
 * @author Stan Hebben
 */
public class JavaNativeModule {


	//TODO Fix visibility
	public final JavaNativeMemberConverter memberConverter;
	private final GlobalTypeRegistry registry;
	private final PackageDefinitions definitions = new PackageDefinitions();

	private final IZSLogger logger;
	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeExpansionConverter expansionConverter;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeClassConverter classConverter;
	private final JavaNativeGlobalConverter globalConverter;

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
		this.typeConversionContext = new JavaNativeTypeConversionContext(packageInfo, dependencies);

		this.typeConverter = new JavaNativeTypeConverter(typeConversionContext, registry, packageInfo, this);
		this.memberConverter = new JavaNativeMemberConverter(typeConverter, packageInfo, typeConversionContext, registry);
		this.expansionConverter = new JavaNativeExpansionConverter(typeConverter, logger, packageInfo, memberConverter, typeConversionContext, definitions);
		this.classConverter = new JavaNativeClassConverter(typeConverter, memberConverter, packageInfo, typeConversionContext, registry);
		this.globalConverter = new JavaNativeGlobalConverter(typeConversionContext, registry, typeConverter, memberConverter);
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
		return typeConversionContext.compiled;
	}

	public Map<String, ISymbol> getGlobals() {
		return typeConversionContext.globals;
	}

	public HighLevelDefinition addClass(Class<?> cls) {
		if (typeConversionContext.definitionByClass.containsKey(cls)) {
			return typeConversionContext.definitionByClass.get(cls);
		}

		if ((cls.getModifiers() & Modifier.PUBLIC) == 0)
			throw new IllegalArgumentException("Class \" " + cls.getName() + "\" must be public");

		if (cls.isAnnotationPresent(ZenCodeType.Expansion.class)) {
			return expansionConverter.convertExpansion(cls);
		}

		return classConverter.convertClass(cls);
	}

	public void addGlobals(Class<?> cls) {

		final HighLevelDefinition definition = addClass(cls);
		globalConverter.addGlobal(cls, definition);
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
						final JavaMethod methodInfo = typeConversionContext.compiled.optMethodInfo(m);
						return methodInfo != null && methodDescriptor.equals(methodInfo.descriptor);
					})
					.findAny();

			if (matchingMember.isPresent()) {
				return matchingMember.get().ref(registry.getForDefinition(definition));
			}
		}
		MethodMember methodMember = new MethodMember(CodePosition.NATIVE, definition, Modifiers.PUBLIC | Modifiers.STATIC, method.getName(), memberConverter.getHeader(typeConversionContext.context, method), null);
		definition.addMember(methodMember);
		boolean isGenericResult = methodMember.header.getReturnType().isGeneric();
		typeConversionContext.compiled.setMethodInfo(methodMember, new JavaMethod(jcls, JavaMethod.Kind.STATIC, method.getName(), false, org.objectweb.asm.Type.getMethodDescriptor(method), method.getModifiers(), isGenericResult));
		return methodMember.ref(registry.getForDefinition(definition));
	}


	public void registerBEP(BracketExpressionParser bep) {
		memberConverter.setBEP(bep);
		typeConverter.setBEP(bep);
	}

	public Module getModule() {
		return packageInfo.getModule();
	}

	public JavaNativeTypeConversionContext getTypeConversionContext() {
		return typeConversionContext;
	}
}
