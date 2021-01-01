package org.openzen.zencode.java.module;

import org.openzen.zencode.java.module.converters.JavaNativePackageInfo;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.javashared.JavaCompiledModule;

import java.util.HashMap;
import java.util.Map;

public class JavaNativeTypeConversionContext {
	public final Map<Class<?>, HighLevelDefinition> definitionByClass;
	public final Map<String, ISymbol> globals;
	public final JavaCompiledModule compiled;
	public final TypeVariableContext context;
	public final GlobalTypeRegistry registry;
	public final PackageDefinitions packageDefinitions;

	public JavaNativeTypeConversionContext(JavaNativePackageInfo packageInfo, JavaNativeModule[] dependencies, GlobalTypeRegistry registry) {
		this.registry = registry;
		this.definitionByClass = new HashMap<>();
		this.globals = new HashMap<>();
		this.context = new TypeVariableContext();
		this.packageDefinitions = new PackageDefinitions();

		this.compiled = new JavaCompiledModule(packageInfo.getModule(), FunctionParameter.NONE);

		for (JavaNativeModule dependency : dependencies) {
			final JavaNativeTypeConversionContext dependencyTypeConversionContext = dependency.getTypeConversionContext();

			definitionByClass.putAll(dependencyTypeConversionContext.definitionByClass);
			context.putAllFrom(dependencyTypeConversionContext.context);
			compiled.addAllFrom(dependencyTypeConversionContext.compiled);
		}
	}
}
