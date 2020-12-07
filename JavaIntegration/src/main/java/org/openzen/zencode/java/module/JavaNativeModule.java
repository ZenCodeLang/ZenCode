/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java.module;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.converters.*;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;


/**
 * @author Stan Hebben
 */
public class JavaNativeModule {


	private final PackageDefinitions definitions = new PackageDefinitions();
	private final IZSLogger logger;

	private final JavaNativePackageInfo packageInfo;
	private final JavaNativeTypeConversionContext typeConversionContext;

	private final JavaNativeTypeConverter typeConverter;
	private final JavaNativeHeaderConverter headerConverter;
	private final JavaNativeMemberConverter memberConverter;

	private final JavaNativeClassConverter classConverter;
	private final JavaNativeGlobalConverter globalConverter;
	private final JavaNativeExpansionConverter expansionConverter;

	public JavaNativeModule(
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage,
			GlobalTypeRegistry registry,
			JavaNativeModule[] dependencies) {
		this.packageInfo = new JavaNativePackageInfo(pkg, basePackage, new Module(name));
		this.logger = logger;
		this.typeConversionContext = new JavaNativeTypeConversionContext(packageInfo, dependencies);

		this.typeConverter = new JavaNativeTypeConverter(typeConversionContext, registry, packageInfo, this);
		this.headerConverter = new JavaNativeHeaderConverter(typeConverter, packageInfo, registry, typeConversionContext);
		this.memberConverter = new JavaNativeMemberConverter(typeConverter, packageInfo, typeConversionContext, registry, headerConverter);
		this.classConverter = new JavaNativeClassConverter(typeConverter, memberConverter, packageInfo, typeConversionContext, headerConverter, registry);
		this.globalConverter = new JavaNativeGlobalConverter(typeConversionContext, registry, typeConverter, memberConverter);
		this.expansionConverter = new JavaNativeExpansionConverter(typeConverter, logger, packageInfo, memberConverter, typeConversionContext, definitions, headerConverter);
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
		final HighLevelDefinition definition = addClass(method.getDeclaringClass());
		return memberConverter.loadStaticMethod(method, definition);
	}


	public void registerBEP(BracketExpressionParser bep) {
		headerConverter.setBEP(bep);
		typeConverter.setBEP(bep);
	}

	public Module getModule() {
		return packageInfo.getModule();
	}

	public JavaNativeTypeConversionContext getTypeConversionContext() {
		return typeConversionContext;
	}
}
