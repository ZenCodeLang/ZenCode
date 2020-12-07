package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import stdlib.Strings;

public class PackageProvider {


	private final ZSPackage pkg;
	private final String basePackage;
	private final Module module;

	public PackageProvider(ZSPackage pkg, String basePackage, Module module) {

		this.pkg = pkg;
		this.basePackage = basePackage;
		this.module = module;
	}

	public boolean isInBasePackage(String className) {
		return className.startsWith(module.name) || className.startsWith(basePackage + ".") || className.startsWith("java.lang.") || className.startsWith("java.util.");
	}
	public ZSPackage getPackage(String className) {
		//TODO validate
		if (this.basePackage == null || this.basePackage.isEmpty())
			return pkg;
		//TODO make a lang package?
		if (!className.contains(".") || className.startsWith("java.lang"))
			return pkg;

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
}
