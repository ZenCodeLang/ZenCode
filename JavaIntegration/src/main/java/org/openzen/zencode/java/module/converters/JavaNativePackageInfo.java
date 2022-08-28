package org.openzen.zencode.java.module.converters;

import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import stdlib.Strings;

import java.util.Optional;

public class JavaNativePackageInfo {


	private final ZSPackage pkg;
	private final String basePackage;
	private final ModuleSymbol module;

	public JavaNativePackageInfo(ZSPackage pkg, String basePackage, ModuleSymbol module) {

		this.pkg = pkg;
		this.basePackage = basePackage;
		this.module = module;
	}

	public boolean isInBasePackage(String className) {
		return className.startsWith(module.name) || className.startsWith(basePackage + ".") || className.startsWith("java.lang.") || className.startsWith("java.util.");
	}
	public ZSPackage getPackage(String className) {
		//TODO make a lang package?
		if (!className.contains(".") || className.startsWith("java.lang"))
			return pkg;

		//TODO validate
		if (this.basePackage == null || this.basePackage.isEmpty()) {
			if(!className.startsWith(".") && className.contains(".")) {
				return getPackageFromTopLevelFor(className).orElse(pkg);
			} else {
				return pkg;
			}
		}

		if (className.startsWith("."))
			className = className.substring(1);
		else if (className.startsWith(basePackage + "."))
			className = className.substring(basePackage.length() + 1);
		else
			throw new IllegalArgumentException("Invalid class name: \"" + className + "\" not in the given base package: \"" + basePackage + "\"");

		String[] classNameParts = Strings.split(className, '.');
		ZSPackage classPkg = pkg;
		for (int i = 0; i < classNameParts.length - 1; i++)
			classPkg = classPkg.getOrCreatePackage(classNameParts[i]);

		return classPkg;
	}

	private Optional<ZSPackage> getPackageFromTopLevelFor(String className) {
		ZSPackage zsPackage = pkg;
		while (zsPackage.parent != null) {
			zsPackage = zsPackage.parent;
		}
		final int index = className.lastIndexOf('.');
		return zsPackage.getOptionalRecursive(className.substring(0, index));
	}

	public ModuleSymbol getModule() {
		return module;
	}

	public String getBasePackage() {
		return basePackage;
	}

	public ZSPackage getPkg() {
		return pkg;
	}
}
