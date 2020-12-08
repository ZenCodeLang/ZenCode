package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.module.JavaNativeModule;
import org.openzen.zencode.java.module.JavaNativeTypeConversionContext;
import org.openzen.zencode.shared.logging.IZSLogger;

public class JavaNativeConverterBuilder {
	public JavaNativeConverter build(JavaNativePackageInfo packageInfo, IZSLogger logger, JavaNativeTypeConversionContext typeConversionContext, JavaNativeModule module) {
		final JavaNativeTypeConverter typeConverter = getTypeConverter(packageInfo, typeConversionContext, module);
		final JavaNativeHeaderConverter headerConverter = getHeaderConverter(packageInfo, typeConversionContext, typeConverter);
		final JavaNativeMemberConverter memberConverter = getMemberConverter(typeConversionContext, typeConverter, headerConverter);
		final JavaNativeClassConverter classConverter = getClassConverter(packageInfo, typeConversionContext, typeConverter, headerConverter, memberConverter);
		final JavaNativeGlobalConverter globalConverter = getGlobalConverter(typeConversionContext, typeConverter, memberConverter);
		final JavaNativeExpansionConverter expansionConverter = getExpansionConverter(packageInfo, logger, typeConversionContext, typeConverter, headerConverter, memberConverter);

		return new JavaNativeConverter(
				typeConverter,
				headerConverter,
				memberConverter,
				classConverter,
				globalConverter,
				expansionConverter
		);
	}

	public JavaNativeExpansionConverter getExpansionConverter(JavaNativePackageInfo packageInfo, IZSLogger logger, JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter, JavaNativeMemberConverter memberConverter) {
		return new JavaNativeExpansionConverter(typeConverter, logger, packageInfo, memberConverter, typeConversionContext, headerConverter);
	}

	public JavaNativeGlobalConverter getGlobalConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeMemberConverter memberConverter) {
		return new JavaNativeGlobalConverter(typeConversionContext, typeConverter, memberConverter);
	}

	public JavaNativeClassConverter getClassConverter(JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter, JavaNativeMemberConverter memberConverter) {
		return new JavaNativeClassConverter(typeConverter, memberConverter, packageInfo, typeConversionContext, headerConverter);
	}

	public JavaNativeMemberConverter getMemberConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter) {
		return new JavaNativeMemberConverter(typeConverter, typeConversionContext, headerConverter);
	}

	public JavaNativeHeaderConverter getHeaderConverter(JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter) {
		return new JavaNativeHeaderConverter(typeConverter, packageInfo, typeConversionContext);
	}

	public JavaNativeTypeConverter getTypeConverter(JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeModule module) {
		return new JavaNativeTypeConverter(typeConversionContext, packageInfo, module);
	}
}
