package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.reflect.Modifier;

public class JavaNativeConverter {
	public final JavaNativeTypeConverter typeConverter;
	public final JavaNativeHeaderConverter headerConverter;
	public final JavaNativeMemberConverter memberConverter;

	public final JavaNativeClassConverter classConverter;
	public final JavaNativeGlobalConverter globalConverter;
	public final JavaNativeExpansionConverter expansionConverter;
	private final JavaNativeTypeConversionContext typeConversionContext;

	public JavaNativeConverter(JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter, JavaNativeMemberConverter memberConverter, JavaNativeClassConverter classConverter, JavaNativeGlobalConverter globalConverter, JavaNativeExpansionConverter expansionConverter, JavaNativeTypeConversionContext typeConversionContext) {
		this.typeConverter = typeConverter;
		this.headerConverter = headerConverter;
		this.memberConverter = memberConverter;
		this.classConverter = classConverter;
		this.globalConverter = globalConverter;
		this.expansionConverter = expansionConverter;
		this.typeConversionContext = typeConversionContext;
	}

	public void registerBEP(BracketExpressionParser bep) {
		headerConverter.setBEP(bep);
		typeConverter.setBEP(bep);
	}

	public TypeSymbol addClass(Class<?> cls) {
		if (typeConversionContext.definitionByClass.containsKey(cls)) {
			return typeConversionContext.definitionByClass.get(cls);
		}

		if (!Modifier.isPublic(cls.getModifiers()))
			throw new IllegalArgumentException("Class \" " + cls.getName() + "\" must be public");

		if (cls.isAnnotationPresent(ZenCodeType.Expansion.class)) {
			return expansionConverter.convertExpansion(cls);
		}

		return classConverter.convertClass(cls);
	}
}
