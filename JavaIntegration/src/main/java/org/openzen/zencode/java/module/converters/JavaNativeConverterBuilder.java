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

		return getNativeConverter(typeConversionContext, typeConverter, headerConverter, memberConverter, classConverter, globalConverter, expansionConverter);
	}

	protected JavaNativeConverter getNativeConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter, JavaNativeMemberConverter memberConverter, JavaNativeClassConverter classConverter, JavaNativeGlobalConverter globalConverter, JavaNativeExpansionConverter expansionConverter) {
		return new JavaNativeConverter(
				typeConverter,
				headerConverter,
				memberConverter,
				classConverter,
				globalConverter,
				expansionConverter,
				typeConversionContext
		);
	}

	protected JavaNativeExpansionConverter getExpansionConverter(JavaNativePackageInfo packageInfo, IZSLogger logger, JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter, JavaNativeMemberConverter memberConverter) {
		return new JavaNativeExpansionConverter(typeConverter, logger, packageInfo, memberConverter, typeConversionContext, headerConverter);
	}

	protected JavaNativeGlobalConverter getGlobalConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeMemberConverter memberConverter) {
		return new JavaNativeGlobalConverter(typeConversionContext, typeConverter, memberConverter);
	}

	protected JavaNativeClassConverter getClassConverter(JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter, JavaNativeMemberConverter memberConverter) {
		return new JavaNativeClassConverter(typeConverter, memberConverter, packageInfo, typeConversionContext, headerConverter);
	}

	protected JavaNativeMemberConverter getMemberConverter(JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter, JavaNativeHeaderConverter headerConverter) {
		return new JavaNativeMemberConverter(typeConverter, typeConversionContext, headerConverter);
	}

	protected JavaNativeHeaderConverter getHeaderConverter(JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeTypeConverter typeConverter) {
		return new JavaNativeHeaderConverter(typeConverter, packageInfo, typeConversionContext);
	}

	protected JavaNativeTypeConverter getTypeConverter(JavaNativePackageInfo packageInfo, JavaNativeTypeConversionContext typeConversionContext, JavaNativeModule module) {
		return new JavaNativeTypeConverter(typeConversionContext, packageInfo, module);
	}
}
