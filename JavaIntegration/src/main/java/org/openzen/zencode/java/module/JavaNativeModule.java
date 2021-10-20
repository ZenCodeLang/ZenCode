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
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;


/**
 * @author Stan Hebben
 */
public class JavaNativeModule {


	private final IZSLogger logger;

	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;
	private final JavaNativeConverter nativeConverter;

	public JavaNativeModule(
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage,
			GlobalTypeRegistry registry,
			JavaNativeModule[] dependencies) {
		this(logger,
				pkg,
				name,
				basePackage,
				registry,
				dependencies,
				new JavaNativeConverterBuilder()
		);
	}

	public JavaNativeModule(
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage,
			GlobalTypeRegistry registry,
			JavaNativeModule[] dependencies,
			JavaNativeConverterBuilder nativeConverterBuilder) {
		this.packageInfo = new JavaNativePackageInfo(pkg, basePackage, new Module(name));
		this.logger = logger;
		this.typeConversionContext = new JavaNativeTypeConversionContext(packageInfo, dependencies, registry);

		this.nativeConverter = nativeConverterBuilder.build(packageInfo, logger, typeConversionContext, this);
	}

	public SemanticModule toSemantic(ModuleSpace space) {
		return new SemanticModule(
				packageInfo.getModule(),
				SemanticModule.NONE,
				FunctionParameter.NONE,
				SemanticModule.State.NORMALIZED,
				space.rootPackage,
				packageInfo.getPkg(),
				typeConversionContext.packageDefinitions,
				Collections.emptyList(),
				space.registry,
				space.collectExpansions(),
				space.getAnnotations(),
				logger);
	}

	public HighLevelDefinition addClass(Class<?> cls) {
		return nativeConverter.addClass(cls);
	}

	public void addGlobals(Class<?> cls) {
		final HighLevelDefinition definition = addClass(cls);
		nativeConverter.globalConverter.addGlobal(cls, definition);
	}

	public FunctionalMemberRef loadStaticMethod(Method method) {
		final HighLevelDefinition definition = addClass(method.getDeclaringClass());
		return nativeConverter.memberConverter.loadStaticMethod(method, definition);
	}


	public void registerBEP(BracketExpressionParser bep) {
		nativeConverter.registerBEP(bep);
	}

	public JavaCompiledModule getCompiled() {
		return typeConversionContext.compiled;
	}

	public Map<String, ISymbol> getGlobals() {
		return typeConversionContext.globals;
	}

	public Module getModule() {
		return packageInfo.getModule();
	}

	JavaNativeTypeConversionContext getTypeConversionContext() {
		return typeConversionContext;
	}
}
