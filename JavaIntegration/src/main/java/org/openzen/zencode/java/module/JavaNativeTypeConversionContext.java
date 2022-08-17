package org.openzen.zencode.java.module;

import org.openzen.zencode.java.module.converters.JavaNativePackageInfo;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.PackageDefinitions;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.javashared.JavaCompiledModule;

import java.util.HashMap;
import java.util.Map;

public class JavaNativeTypeConversionContext {
	public final Map<Class<?>, TypeSymbol> definitionByClass;
	public final Map<String, IGlobal> globals;
	public final JavaCompiledModule compiled;
	public final TypeVariableContext context;
	public final PackageDefinitions packageDefinitions;
	public final ZSPackage root;

	public JavaNativeTypeConversionContext(JavaNativePackageInfo packageInfo, JavaNativeModule[] dependencies, ZSPackage root) {
		this.definitionByClass = new HashMap<>();
		this.globals = new HashMap<>();
		this.context = new TypeVariableContext();
		this.packageDefinitions = new PackageDefinitions();
		this.root = root;

		this.compiled = new JavaCompiledModule(packageInfo.getModule(), FunctionParameter.NONE);

		for (JavaNativeModule dependency : dependencies) {
			final JavaNativeTypeConversionContext dependencyTypeConversionContext = dependency.getTypeConversionContext();

			definitionByClass.putAll(dependencyTypeConversionContext.definitionByClass);
			context.putAllFrom(dependencyTypeConversionContext.context);
			compiled.addAllFrom(dependencyTypeConversionContext.compiled);
		}
	}
}
