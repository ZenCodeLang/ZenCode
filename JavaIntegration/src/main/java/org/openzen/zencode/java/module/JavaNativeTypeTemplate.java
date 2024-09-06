package org.openzen.zencode.java.module;

import org.objectweb.asm.Type;
import org.openzen.zencode.java.TypeVariableContext;
import org.openzen.zencode.java.ZenCodeGlobals;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.impl.conversion.ConversionUtils;
import org.openzen.zencode.java.impl.conversion.JavaNativeHeaderConverter;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.JavaNativeField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

public class JavaNativeTypeTemplate {
	protected final JavaRuntimeClass class_;
	protected final TypeID target;
	protected final TypeVariableContext typeVariableContext;
	protected final boolean expansion;

	protected List<MethodSymbol> constructors;
	private Map<String, JavaRuntimeField> fields;
	private Map<MethodID, List<MethodSymbol>> methods;
	private Map<String, JavaRuntimeClass> innerTypes;

	public List<MethodSymbol> getConstructors() {
		if (constructors == null) {
			List<MethodSymbol> result = new ArrayList<>();
			for (Constructor<?> constructor : class_.cls.getConstructors()) {
				if (constructor.isAnnotationPresent(ZenCodeType.Constructor.class)) {
					result.add(loadJavaMethod(constructor));
				}
			}
			this.constructors = result;
		}
		return this.constructors;
	}

	public Stream<MethodSymbol> getAllMethods() {
		if (methods == null) {
			loadMethods();
		}
		return methods.values().stream().flatMap(List::stream);
	}

	public JavaNativeTypeTemplate(TypeID target, JavaRuntimeClass class_, TypeVariableContext typeVariableContext, boolean expansion) {
		this.target = target;
		this.class_ = class_;
		this.typeVariableContext = typeVariableContext;
		this.expansion = expansion;
	}

	public List<MethodSymbol> getMethod(MethodID name) {
		if (methods == null) {
			loadMethods();
		}
		return methods.getOrDefault(name, Collections.emptyList());
	}

	public Optional<JavaRuntimeField> getField(String name) {
		if (fields == null) {
			if (expansion) {
				fields = Collections.emptyMap();
			} else {
				loadFields();
			}
		}
		return Optional.ofNullable(fields.get(name));
	}

	public Optional<JavaRuntimeClass> getInnerType(String name) {
		if (innerTypes == null) {
			loadInnerTypes();
		}

		return Optional.ofNullable(innerTypes.get(name));
	}

	public Optional<CompilableExpression> getContextMember(String name) {
		return getField(name)
				.filter(JavaRuntimeField::isEnumConstant)
				.map(EnumField::new);
	}

	private void loadFields() {
		fields = new HashMap<>();

		for (Field field : class_.cls.getFields()) {
			if (!Modifier.isPublic(field.getModifiers()))
				continue;

			final String zenCodeName;
			if (field.isAnnotationPresent(ZenCodeType.Field.class)) {
				ZenCodeType.Field fieldAnnotation = field.getAnnotation(ZenCodeType.Field.class);
				zenCodeName = fieldAnnotation.value().isEmpty() ? field.getName() : fieldAnnotation.value();
			} else if (field.isAnnotationPresent(ZenCodeGlobals.Global.class) && JavaModifiers.isStatic(field.getModifiers())) {
				ZenCodeGlobals.Global fieldAnnotation = field.getAnnotation(ZenCodeGlobals.Global.class);
				zenCodeName = fieldAnnotation.value().isEmpty() ? field.getName() : fieldAnnotation.value();
			} else if (field.isEnumConstant()) {
				zenCodeName = field.getName();
			} else {
				continue;
			}

			TypeID type = class_.module.getTypeConverter().getType(typeVariableContext, field.getAnnotatedType());
			JavaNativeField nativeField = new JavaNativeField(class_.javaClass, field.getName(), Type.getDescriptor(field.getType()));
			JavaRuntimeField runtimeField = new JavaRuntimeField(class_, zenCodeName, nativeField, type, field);

			fields.put(zenCodeName, runtimeField);
			class_.module.getCompiled().setFieldInfo(runtimeField, nativeField);
		}
	}

	private void loadMethods() {
		JavaNativeHeaderConverter headerConverter = class_.module.getHeaderConverter();
		methods = new HashMap<>();

		for (Method method : class_.cls.getMethods()) {
			if (isNotAccessible(method) || isOverridden(class_.cls, method))
				continue;
			if (expansion && !JavaModifiers.isStatic(method.getModifiers()))
				continue;

			Collection<MethodID> ids = new LinkedList<>();
			boolean isStaticExpansion = false;
			boolean implicit = false;
			boolean isStaticMethod = false;
			if (method.isAnnotationPresent(ZenCodeType.Operator.class)) {
				ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
				MethodID id = MethodID.operator(OperatorType.valueOf(operator.value().toString()));
				ids.add(id);
			}
			if (method.isAnnotationPresent(ZenCodeType.Getter.class)) {
				ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
				String name = getter.value().isEmpty() ? method.getName() : getter.value();
				MethodID id = MethodID.getter(name);
				ids.add(id);
			}
			if (method.isAnnotationPresent(ZenCodeType.Setter.class)) {
				ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
				String name = setter.value().isEmpty() ? method.getName() : setter.value();
				MethodID id = MethodID.setter(name);
				ids.add(id);
			}
			if (method.isAnnotationPresent(ZenCodeType.Caster.class)) {
				ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
				implicit = caster.implicit();
				MethodID id = MethodID.caster(headerConverter.getHeader(typeVariableContext, method).getReturnType());
				ids.add(id);
			}
			if (method.isAnnotationPresent(ZenCodeType.Method.class)) {
				ZenCodeType.Method methodAnnotation = method.getAnnotation(ZenCodeType.Method.class);
				String name = methodAnnotation.value().isEmpty() ? method.getName() : methodAnnotation.value();
				boolean hasStaticModifier = JavaModifiers.isStatic(method.getModifiers());
				MethodID id = hasStaticModifier && !expansion ? MethodID.staticMethod(name) : MethodID.instanceMethod(name);
				implicit |= methodAnnotation.implicit();
				isStaticMethod |= hasStaticModifier;
				ids.add(id);
			}
			if (expansion && method.isAnnotationPresent(ZenCodeType.StaticExpansionMethod.class)) {
				ZenCodeType.StaticExpansionMethod methodAnnotation = method.getAnnotation(ZenCodeType.StaticExpansionMethod.class);
				String name = methodAnnotation.value().isEmpty() ? method.getName() : methodAnnotation.value();
				MethodID id = MethodID.staticMethod(name);
				ids.add(id);
				isStaticExpansion = true;
			}
			if (!isStaticMethod && method.isAnnotationPresent(ZenCodeGlobals.Global.class) && JavaModifiers.isStatic(method.getModifiers())) {
				ZenCodeGlobals.Global methodAnnotation = method.getAnnotation(ZenCodeGlobals.Global.class);
				String name = methodAnnotation.value().isEmpty() ? method.getName() : methodAnnotation.value();
				MethodID id = MethodID.staticMethod(name);
				ids.add(id);
			}
			if (ids.isEmpty())
				continue;

			FunctionHeader header = headerConverter.getHeader(typeVariableContext, method);

			if (expansion && !isStaticExpansion) {
				FunctionParameter[] withoutFirst = Arrays.copyOfRange(header.parameters, 1, header.parameters.length);
				header = new FunctionHeader(header.typeParameters, header.getReturnType(), header.thrownType, withoutFirst);
			}

			for (MethodID id : ids) {
				JavaRuntimeMethod runtimeMethod = new JavaRuntimeMethod(class_, target, method, id, header, implicit, expansion && !isStaticExpansion);
				methods.computeIfAbsent(id, x -> new ArrayList<>()).add(runtimeMethod);
				class_.module.getCompiled().setMethodInfo(runtimeMethod, runtimeMethod);
			}
		}
	}

	private void loadInnerTypes() {
		innerTypes = new HashMap<>();

		for (Class<?> cls : class_.cls.getDeclaredClasses()) {
			ZenCodeType.Inner innerType = cls.getAnnotation(ZenCodeType.Inner.class);
			String name = innerType.value().isEmpty() ? cls.getSimpleName() : innerType.value();
			JavaClass.Kind kind = ConversionUtils.getKindFromAnnotations(cls);
			JavaRuntimeClass innerClass = new JavaAnnotatedRuntimeClass(class_.module, cls, name, null, kind);
			innerTypes.put(name, innerClass);
		}
	}

	private boolean isNotAccessible(Method method) {
		return !Modifier.isPublic(method.getModifiers());
	}

	private boolean isOverridden(Class<?> cls, Method method) {
		return !method.getDeclaringClass().equals(cls) || method.isBridge();
	}

	protected MethodSymbol loadJavaMethod(Constructor<?> constructor) {
		JavaNativeHeaderConverter headerConverter = class_.module.getHeaderConverter();
		FunctionHeader header = headerConverter.getHeader(typeVariableContext, constructor);
		header.setReturnType(target); // In ZC, .ctors return the instantiated type
		JavaRuntimeMethod method = new JavaRuntimeMethod(class_, target, constructor, header, false);
		class_.module.getCompiled().setMethodInfo(method, method);
		return method;
	}

	private static class EnumField implements CompilableExpression {
		private final JavaRuntimeField field;

		public EnumField(JavaRuntimeField field) {
			this.field = field;
		}

		@Override
		public CodePosition getPosition() {
			return CodePosition.UNKNOWN;
		}

		@Override
		public CompilingExpression compile(ExpressionCompiler compiler) {
			return new EnumCompilingField(field, compiler);
		}
	}

	private static class EnumCompilingField extends AbstractCompilingExpression {
		private final JavaRuntimeField field;

		public EnumCompilingField(JavaRuntimeField field, ExpressionCompiler compiler) {
			super(compiler, CodePosition.UNKNOWN);
			this.field = field;
		}

		@Override
		public Expression eval() {
			return compiler.at(CodePosition.UNKNOWN).getStaticField(new FieldInstance(field));
		}

		@Override
		public void collect(SSAVariableCollector collector) {

		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {

		}
	}
}
