/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java.module;

import org.openzen.zencode.java.JavaRuntimeTypeConverter;
import org.openzen.zencode.java.TypeVariableContext;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.impl.JavaNativeModuleSpace;
import org.openzen.zencode.java.impl.conversion.ConversionUtils;
import org.openzen.zencode.java.impl.conversion.JavaNativeHeaderConverter;
import org.openzen.zencode.java.impl.conversion.JavaRuntimeTypeConverterImpl;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.expression.StaticMemberCompilingExpression;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.globals.ExpressionGlobal;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaNativeField;
import org.openzen.zenscript.parser.BracketExpressionParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author Stan Hebben
 */
public class JavaNativeModule {
	private final IZSLogger logger;
	private final ModuleSpace space;
	private final JavaNativePackageInfo packageInfo;
	private final Map<Class<?>, JavaRuntimeClass> classes = new HashMap<>();
	private final JavaRuntimeTypeConverterImpl typeConverter;
	private final JavaNativeHeaderConverter headerConverter;

	private final Map<String, IGlobal> globals = new HashMap<>();
	private final PackageDefinitions packageDefinitions = new PackageDefinitions();
	private final JavaCompiledModule compiled;

	public JavaNativeModule(
			ModuleSpace space,
			JavaNativeModuleSpace nativeModuleSpace,
			IZSLogger logger,
			ZSPackage pkg,
			String name,
			String basePackage
	) {
		this.space = space;
		this.packageInfo = new JavaNativePackageInfo(space.rootPackage, pkg, basePackage, new ModuleSymbol(name));
		this.logger = logger;

		this.compiled = new JavaCompiledModule(packageInfo.getModule(), FunctionParameter.NONE);

		JavaRuntimeTypeConverterImpl typeConverter = new JavaRuntimeTypeConverterImpl(nativeModuleSpace, packageInfo);
		this.typeConverter = typeConverter;
		headerConverter = new JavaNativeHeaderConverter(typeConverter, packageInfo, this);
		typeConverter.setHeaderConverter(headerConverter);
	}

	public SemanticModule toSemantic() {
		return new SemanticModule(
				packageInfo.getModule(),
				SemanticModule.NONE,
				FunctionParameter.NONE,
				SemanticModule.State.NORMALIZED,
				space.rootPackage,
				packageInfo.getPkg(),
				packageDefinitions,
				Collections.emptyList(),
				space.collectExpansions(),
				space.getAnnotations().toArray(new AnnotationDefinition[0]),
				logger);
	}

	public JavaRuntimeClass addClass(Class<?> cls) {
		if (classes.containsKey(cls)) {
			return classes.get(cls);
		}

		JavaClass.Kind kind = ConversionUtils.getKindFromAnnotations(cls);
		TypeID target = null;
		if (kind == JavaClass.Kind.EXPANSION) {
			ZenCodeType.Expansion expansion = cls.getAnnotation(ZenCodeType.Expansion.class);
			target = typeConverter.parseType(expansion.value());
		}

		ParsedName name = getClassName(packageInfo.getPkg(), cls);
		JavaRuntimeClass class_ = new JavaRuntimeClass(this, cls, name.name, target, kind);
		classes.put(cls, class_);
		name.pkg.register(class_);
		return class_;
	}

	public void addGlobals(Class<?> cls) {
		JavaRuntimeClass class_ = addClass(cls);
		getCompiled().setClassInfo(class_, class_.javaClass);

		TypeID thisType = DefinitionTypeID.createThis(class_);
		TypeVariableContext context = new TypeVariableContext();

		for (Field field : cls.getDeclaredFields()) {
			if (!field.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(field.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = field.getAnnotation(ZenCodeGlobals.Global.class);
			TypeID type = typeConverter.getType(context, field.getAnnotatedType());
			String name = global.value().isEmpty() ? field.getName() : global.value();

			JavaNativeField javaField = new JavaNativeField(class_.javaClass, field.getName(), org.objectweb.asm.Type.getDescriptor(field.getType()));
			JavaRuntimeField nativeField = new JavaRuntimeField(class_, name, javaField, type, field);
			getCompiled().setFieldInfo(nativeField, javaField);

			globals.put(name, new ExpressionGlobal((compiler, position, typeArguments) ->
					compiler.at(position).getStaticField(new FieldInstance(nativeField)).wrap(compiler)));
		}

		for (Method method : cls.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(ZenCodeGlobals.Global.class))
				continue;
			if (!Modifier.isStatic(method.getModifiers()))
				continue;

			ZenCodeGlobals.Global global = method.getAnnotation(ZenCodeGlobals.Global.class);
			String name = global.value().isEmpty() ? method.getName() : global.value();

			FunctionHeader header = getHeaderConverter().getHeader(TypeVariableContext.EMPTY, method);
			JavaRuntimeMethod runtimeMethod = new JavaRuntimeMethod(class_, thisType, method, MethodID.staticMethod(name), header);
			getCompiled().setMethodInfo(runtimeMethod, runtimeMethod.getNative());

			globals.put(name, new ExpressionGlobal((compiler, position, typeArguments) ->
					new StaticMemberCompilingExpression(compiler, position, thisType, new GenericName(name, typeArguments))));
		}
	}

	public void addGlobal(String name, IGlobal global) {
		globals.put(name, global);
	}

	public void registerBEP(BracketExpressionParser bep) {
		headerConverter.setBEP(bep);
	}

	public JavaCompiledModule getCompiled() {
		return compiled;
	}

	public Map<String, IGlobal> getGlobals() {
		return globals;
	}

	public ModuleSymbol getModule() {
		return packageInfo.getModule();
	}

	public String getBasePackage() {
		return packageInfo.getBasePackage();
	}

	public Optional<TypeSymbol> findClass(Class<?> cls) {
		return Optional.ofNullable(classes.get(cls));
	}

	public JavaRuntimeTypeConverter getTypeConverter() {
		return typeConverter;
	}

	public JavaNativeHeaderConverter getHeaderConverter() {
		return headerConverter;
	}

	private ParsedName getClassName(ZSPackage pkg, Class<?> cls) {
		boolean isStruct = cls.isAnnotationPresent(ZenCodeType.Struct.class);
		final String specifiedName = getNameForScripts(cls);

		ZSPackage classPkg;
		boolean hasAnnotation = cls.isAnnotationPresent(ZenCodeType.Name.class);
		String className = specifiedName.contains(".") ? specifiedName.substring(specifiedName.lastIndexOf('.') + 1) : specifiedName;
		if (!hasAnnotation) {
			if (!specifiedName.startsWith(packageInfo.getPkg().fullName)) {
				classPkg = packageInfo.getPackage(className);
			} else {
				classPkg = packageInfo.getPackage(packageInfo.getBasePackage() + specifiedName.substring(packageInfo.getPkg().fullName.length()));
				className = specifiedName.substring(specifiedName.lastIndexOf('.') + 1);
			}
		} else {
			if (specifiedName.startsWith(".")) {
				classPkg = packageInfo.getPackage(specifiedName);
				className = specifiedName.substring(specifiedName.lastIndexOf('.') + 1);
			} else if (specifiedName.indexOf('.') >= 0) {
				if (!specifiedName.startsWith(packageInfo.getPkg().fullName))
					throw new IllegalArgumentException("Specified @Name as \"" + specifiedName + "\" for class: \"" + cls
							.toString() + "\" but it's not in the module root package: \"" + packageInfo.getPkg().fullName + "\"");

				classPkg = packageInfo.getPackage(packageInfo.getBasePackage() + specifiedName.substring(packageInfo.getPkg().fullName.length()));
				className = specifiedName.substring(specifiedName.lastIndexOf('.') + 1);
			} else {
				classPkg = packageInfo.getPackage(specifiedName);
				className = specifiedName;
			}
		}

		return new ParsedName(classPkg, className);
	}

	private static String getNameForScripts(Class<?> cls) {
		if (cls.isAnnotationPresent(ZenCodeType.Name.class)) {
			return cls.getAnnotation(ZenCodeType.Name.class).value();
		}
		return cls.getName();
	}

	private static class ParsedName {
		private final ZSPackage pkg;
		private final String name;

		public ParsedName(ZSPackage pkg, String name) {
			this.pkg = pkg;
			this.name = name;
		}
	}
}
