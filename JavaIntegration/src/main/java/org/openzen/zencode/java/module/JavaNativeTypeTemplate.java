package org.openzen.zencode.java.module;

import org.objectweb.asm.Type;
import org.openzen.zencode.java.TypeVariableContext;
import org.openzen.zencode.java.ZenCodeType;
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
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.JavaNativeField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class JavaNativeTypeTemplate {
	private final JavaRuntimeClass class_;
	private final TypeID target;
	private final TypeVariableContext typeVariableContext;
	private final boolean expansion;

	private List<MethodSymbol> constructors;
	private Map<String, JavaRuntimeField> fields;
	private Map<MethodID, List<MethodSymbol>> methods;

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

	public Optional<CompilableExpression> getContextMember(String name) {
		return getField(name)
				.filter(JavaRuntimeField::isEnumConstant)
				.map(EnumField::new);
	}

	private void loadFields() {
		fields = new HashMap<>();

		for (Field field : class_.cls.getFields()) {
			if (!field.isAccessible())
				continue;

			if (field.isAnnotationPresent(ZenCodeType.Field.class)) {
				ZenCodeType.Field fieldAnnotation = field.getAnnotation(ZenCodeType.Field.class);
				String name = fieldAnnotation.value() == null ? field.getName() : fieldAnnotation.value();
				TypeID type = class_.module.getTypeConverter().getType(typeVariableContext, field.getAnnotatedType());
				JavaNativeField nativeField = new JavaNativeField(class_.javaClass, field.getName(), Type.getDescriptor(field.getType()));
				fields.put(name, new JavaRuntimeField(class_, name, nativeField, type, field));
			} else if (field.isEnumConstant()) {
				TypeID type = class_.module.getTypeConverter().getType(typeVariableContext, field.getAnnotatedType());
				JavaNativeField nativeField = new JavaNativeField(class_.javaClass, field.getName(), Type.getDescriptor(field.getType()));
				fields.put(field.getName(), new JavaRuntimeField(class_, field.getName(), nativeField, type, field));
			}
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

			MethodID id = null;
			FunctionHeader header = headerConverter.getHeader(typeVariableContext, method);
			boolean isStaticExpansion = false;
			if (method.isAnnotationPresent(ZenCodeType.Operator.class)) {
				ZenCodeType.Operator operator = method.getAnnotation(ZenCodeType.Operator.class);
				id = MethodID.operator(OperatorType.valueOf(operator.value().toString()));
			} else if (method.isAnnotationPresent(ZenCodeType.Getter.class)) {
				ZenCodeType.Getter getter = method.getAnnotation(ZenCodeType.Getter.class);
				String name = getter.value().isEmpty() ? method.getName() : getter.value();
				id = MethodID.getter(name);
			} else if (method.isAnnotationPresent(ZenCodeType.Setter.class)) {
				ZenCodeType.Setter setter = method.getAnnotation(ZenCodeType.Setter.class);
				String name = setter.value().isEmpty() ? method.getName() : setter.value();
				id = MethodID.setter(name);
			} else if (method.isAnnotationPresent(ZenCodeType.Caster.class)) {
				ZenCodeType.Caster caster = method.getAnnotation(ZenCodeType.Caster.class);
				// TODO: implicit flag!
				id = MethodID.caster(header.getReturnType());
			} else if (method.isAnnotationPresent(ZenCodeType.Method.class)) {
				ZenCodeType.Method methodAnnotation = method.getAnnotation(ZenCodeType.Method.class);
				String name = methodAnnotation.value().isEmpty() ? method.getName() : methodAnnotation.value();
				id = JavaModifiers.isStatic(method.getModifiers()) ? MethodID.staticMethod(name) : MethodID.instanceMethod(name);
			} else if (expansion && method.isAnnotationPresent(ZenCodeType.StaticExpansionMethod.class)) {
				ZenCodeType.StaticExpansionMethod methodAnnotation = method.getAnnotation(ZenCodeType.StaticExpansionMethod.class);
				String name = methodAnnotation.value().isEmpty() ? method.getName() : methodAnnotation.value();
				id = MethodID.staticMethod(name);
				isStaticExpansion = true;
			}
			if (id == null)
				continue;

			if (expansion && !isStaticExpansion) {
				FunctionParameter[] withoutFirst = Arrays.copyOfRange(header.parameters, 1, header.parameters.length);
				header = new FunctionHeader(header.getReturnType(), withoutFirst);
			}

			JavaRuntimeMethod runtimeMethod = new JavaRuntimeMethod(class_, target, method, id, header);
			methods.computeIfAbsent(id, x -> new ArrayList<>()).add(runtimeMethod);
			class_.module.getCompiled().setMethodInfo(runtimeMethod, runtimeMethod);
		}
	}

	private boolean isNotAccessible(Method method) {
		return !Modifier.isPublic(method.getModifiers());
	}

	private boolean isOverridden(Class<?> cls, Method method) {
		return !method.getDeclaringClass().equals(cls) || method.isBridge();
	}

	private MethodSymbol loadJavaMethod(Constructor<?> constructor) {
		JavaNativeHeaderConverter headerConverter = class_.module.getHeaderConverter();
		FunctionHeader header = headerConverter.getHeader(typeVariableContext, constructor);
		JavaRuntimeMethod method = new JavaRuntimeMethod(class_, target, constructor, header);
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
