/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java.module;

import org.openzen.zencode.java.module.converters.JavaNativeConverter;
import org.openzen.zencode.java.module.converters.JavaNativeConverterBuilder;
import org.openzen.zencode.java.module.converters.JavaNativePackageInfo;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;


/**
 * @author Stan Hebben
 */
public class JavaNativeModule {
	private final ModuleSpace space;


	private final IZSLogger logger;

	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;
	public final JavaNativeConverter nativeConverter;

	public JavaNativeModule(
			ModuleSpace space,
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage,
			JavaNativeModule[] dependencies,
			ZSPackage root) {
		this(space, logger,
				pkg,
				name,
				basePackage,
				dependencies,
				new JavaNativeConverterBuilder(),
				root
		);
	}

	public JavaNativeModule(
			ModuleSpace space,
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage,
			JavaNativeModule[] dependencies,
			JavaNativeConverterBuilder nativeConverterBuilder,
			ZSPackage rootPackage) {
		this.space = space;
		this.packageInfo = new JavaNativePackageInfo(pkg, basePackage, new Module(name));
		this.logger = logger;
		this.typeConversionContext = new JavaNativeTypeConversionContext(packageInfo, dependencies, rootPackage);

		this.nativeConverter = nativeConverterBuilder.build(packageInfo, logger, typeConversionContext, this);
	}

	public SemanticModule toSemantic() {
		return new SemanticModule(
				packageInfo.getModule(),
				SemanticModule.NONE,
				FunctionParameter.NONE,
				SemanticModule.State.NORMALIZED,
				space.rootPackage,
				packageInfo.getPkg(),
				typeConversionContext.packageDefinitions,
				Collections.emptyList(),
				space.collectExpansions(),
				space.getAnnotations().toArray(new AnnotationDefinition[0]),
				logger);
	}

	public TypeSymbol addClass(Class<?> cls) {
		return nativeConverter.addClass(cls);
	}

	public void addGlobals(Class<?> cls) {
		final TypeSymbol definition = addClass(cls);
		nativeConverter.globalConverter.addGlobal(cls, definition);
	}

	public MethodInstance loadStaticMethod(Method method) {
		final TypeSymbol definition = addClass(method.getDeclaringClass());
		return nativeConverter.memberConverter.loadStaticMethod(method, definition);
	}

	public FunctionHeader loadHeader(TypeVariableContext typeVariableContext, Method method) {
		return nativeConverter.headerConverter.getHeader(context, method);
	}

	public void registerBEP(BracketExpressionParser bep) {
		nativeConverter.registerBEP(bep);
	}

	public JavaCompiledModule getCompiled() {
		return typeConversionContext.compiled;
	}

	public Map<String, IGlobal> getGlobals() {
		return typeConversionContext.globals;
	}

	public Module getModule() {
		return packageInfo.getModule();
	}

	JavaNativeTypeConversionContext getTypeConversionContext() {
		return typeConversionContext;
	}
}
