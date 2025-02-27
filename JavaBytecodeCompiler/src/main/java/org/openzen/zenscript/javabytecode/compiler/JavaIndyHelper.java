package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class JavaIndyHelper {
	public static final class Builder {
		static final BsmData BUILDING = new BsmData(null, null);

		private final Constructor constructor;

		private String callSiteMethodName;
		private String callSiteMethodDesc;
		private BsmData bsm;

		Builder(final Constructor constructor) {
			this.constructor = constructor;
			this.callSiteMethodName = null;
			this.callSiteMethodDesc = null;
			this.bsm = null;
		}

		public Builder callSite(final String name, final Type desc) {
			Objects.requireNonNull(name, "Call site name cannot be null");
			Objects.requireNonNull(desc, "Call site descriptor cannot be null");
			if (desc.getSort() != Type.METHOD) {
				throw new IllegalArgumentException("Call site type must be a valid method descriptor");
			}
			if (this.callSiteMethodName != null) {
				throw new IllegalStateException("Call site has already been set");
			}
			this.callSiteMethodName = name;
			this.callSiteMethodDesc = desc.getDescriptor();
			return this;
		}

		public Builder bootstrapMethod(final UnaryOperator<BsmDataBuilder> bsmDataBuilder) {
			if (this.bsm != null) {
				throw new IllegalStateException("BSM has already been specified or is being currently built");
			}
			this.bsm = BUILDING;
			return this.bootstrapMethod(bsmDataBuilder.apply(new BsmDataBuilder(BsmDataBuilder.Kind.METHOD)).build());
		}

		private Builder bootstrapMethod(final BsmData data) {
			if (this.bsm != BUILDING) {
				throw new IllegalStateException("Unable to set BSM without building it");
			}
			this.bsm = data;
			return this;
		}

		JavaIndyHelper build() {
			if (this.callSiteMethodName == null) {
				throw new IllegalStateException("Call site has not been specified");
			}
			if (this.bsm == null || this.bsm == BUILDING) {
				throw new IllegalStateException("BSM data has not been specified or has not been built");
			}
			return this.constructor.of(this.callSiteMethodName, this.callSiteMethodDesc, this.bsm.method(), this.bsm.args());
		}
	}

	public static final class BsmDataBuilder {
		public enum InvocationKind {
			STATIC,
			VIRTUAL,
			SPECIAL,
			INTERFACE
		}

		private enum Kind {
			METHOD(it -> IndyHelpers.isValidBsm(it, false), it -> IndyHelpers.autoFillBsmDesc(false, it)),
			CONSTANT(it -> IndyHelpers.isValidBsm(it, true), it -> IndyHelpers.autoFillBsmDesc(true, it));

			private final Predicate<JavaNativeMethod> bsmVerifier;
			private final Function<List<?>, String> descAutoFiller;

			Kind(final Predicate<JavaNativeMethod> bsmVerifier, final Function<List<?>, String> descAutoFiller) {
				this.bsmVerifier = bsmVerifier;
				this.descAutoFiller = descAutoFiller;
			}

			boolean verify(final JavaNativeMethod method) {
				return this.bsmVerifier.test(method);
			}

			String autoFillDesc(final List<?> args) {
				return this.descAutoFiller.apply(args);
			}
		}

		private final Kind kind;
		private final List<Object> args;

		private JavaNativeMethod bsmMethod;
		private JavaClass owner;
		private String name;

		BsmDataBuilder(final Kind kind) {
			this.kind = kind;
			this.args = new ArrayList<>();

			this.bsmMethod = null;
			this.owner = null;
			this.name = null;
		}

		public BsmDataBuilder method(final JavaNativeMethod bsmMethod) {
			Objects.requireNonNull(bsmMethod, "BSM Method specified cannot be null");
			if (!this.kind.verify(bsmMethod)) {
				throw new IllegalArgumentException("Provided BSM must be a valid BSM");
			}
			if (this.owner != null) {
				throw new IllegalStateException("BSM has already been specified in 'autodetect' mode");
			}
			if (this.bsmMethod != null) {
				throw new IllegalStateException("BSM has already been specified");
			}
			this.bsmMethod = bsmMethod;
			return this;
		}

		public BsmDataBuilder method(final JavaClass owner, final String name) {
			Objects.requireNonNull(owner, "BSM owner cannot be null");
			Objects.requireNonNull(name, "BSM name cannot be null");
			if (this.bsmMethod != null) {
				throw new IllegalStateException("BSM has already been specified in 'fixed' mode");
			}
			if (this.owner != null) {
				throw new IllegalStateException("BSM has already been specified");
			}
			this.owner = owner;
			this.name = name;
			return this;
		}

		public BsmDataBuilder arg(final int argument) {
			return this.arg((Object) argument);
		}

		public BsmDataBuilder arg(final float argument) {
			return this.arg((Object) argument);
		}

		public BsmDataBuilder arg(final long argument) {
			return this.arg((Object) argument);
		}

		public BsmDataBuilder arg(final double argument) {
			return this.arg((Object) argument);
		}

		public BsmDataBuilder arg(final String argument) {
			return this.arg((Object) argument);
		}

		public BsmDataBuilder arg(final JavaClass clazz) {
			return this.arg((Object) clazz);
		}

		public BsmDataBuilder arg(final Class<?> clazz) {
			return this.arg((Object) clazz);
		}

		public BsmDataBuilder arg(final Type type) {
			return this.arg((Object) type);
		}

		public BsmDataBuilder arg(final Type returnType, final Type... arguments) {
			return this.arg(Type.getMethodType(returnType, arguments));
		}

		public BsmDataBuilder arg(final String returnType, final String... arguments) {
			return this.arg(Type.getType(returnType), Stream.of(arguments).map(Type::getType).toArray(Type[]::new));
		}

		public BsmDataBuilder arg(final JavaCompilingMethod method) {
			return this.arg((Object) method);
		}

		public BsmDataBuilder arg(final JavaNativeMethod method) {
			return this.arg((Object) method);
		}

		public BsmDataBuilder arg(final InvocationKind kind, final JavaClass owner, final String name, final Type desc) {
			switch (kind) {
				case STATIC: return this.arg(JavaNativeMethod.getStatic(owner, name, desc.getDescriptor(), JavaModifiers.STATIC));
				case SPECIAL: {
					if ("<init>".equals(name)) {
						return this.arg(JavaNativeMethod.getConstructor(owner, desc.getDescriptor(), 0));
					} else {
						return this.arg(JavaNativeMethod.getVirtual(owner, name, desc.getDescriptor(), 0));
					}
				}
				case VIRTUAL: return this.arg(JavaNativeMethod.getVirtual(owner, name, desc.getDescriptor(), 0));
				case INTERFACE: return this.arg(JavaNativeMethod.getInterface(owner, name, desc.getDescriptor()));
			}
			throw new IllegalArgumentException(String.valueOf(kind));
		}

		public BsmDataBuilder arg(final String name, final JavaClass type, final UnaryOperator<BsmDataBuilder> constantBuilder) {
			return this.arg(name, Type.getType(type.internalName), constantBuilder);
		}

		public BsmDataBuilder arg(final String name, final Class<?> type, final UnaryOperator<BsmDataBuilder> constantBuilder) {
			return this.arg(name, Type.getType(type), constantBuilder);
		}

		public BsmDataBuilder arg(final String name, final Type type, final UnaryOperator<BsmDataBuilder> constantBuilder) {
			return this.arg(new ConstantDynamicData(name, type, constantBuilder.apply(new BsmDataBuilder(Kind.CONSTANT)).build()));
		}

		private BsmDataBuilder arg(final Object argument) {
			Objects.requireNonNull(argument, "Null arguments are not supported when invoking BSMs");
			this.args.add(IndyHelpers.toBsmArg(argument));
			return this;
		}

		BsmData build() {
			if (this.bsmMethod == null && this.owner == null) {
				throw new IllegalStateException("BSM has not been specified");
			}
			final JavaNativeMethod method = this.findBsmMethod();
			return new BsmData(method, this.args);
		}

		private JavaNativeMethod findBsmMethod() {
			if (this.bsmMethod != null) {
				return this.bsmMethod;
			}

			return JavaNativeMethod.getStatic(this.owner, this.name, this.kind.autoFillDesc(this.args), JavaModifiers.PUBLIC | JavaModifiers.STATIC);
		}
	}

	private static final class BsmData {
		private final JavaNativeMethod method;
		private final List<?> args;

		BsmData(final JavaNativeMethod method, final List<?> args) {
			this.method = method;
			this.args = args;
		}

		JavaNativeMethod method() {
			return this.method;
		}

		List<?> args() {
			return this.args;
		}
	}

	private static final class ConstantDynamicData {
		private final String name;
		private final Type type;
		private final BsmData bsmData;

		ConstantDynamicData(final String name, final Type type, final BsmData bsmData) {
			this.name = name;
			this.type = type;
			this.bsmData = bsmData;
		}

		String name() {
			return this.name;
		}

		Type type() {
			return this.type;
		}

		BsmData bsm() {
			return this.bsmData;
		}
	}

	@FunctionalInterface
	interface IndyVisitor {
		void visitInvokeDynamic(final String methodName, final String methodDesc, final Handle bsmMethod, final Object... bsmArgs);
	}

	@FunctionalInterface
	private interface Constructor {
		JavaIndyHelper of(final String callSiteMethodName, final String callSiteMethodDesc, final JavaNativeMethod bsmMethod, final List<?> bsmArgs);
	}

	private static final class IndyHelpers {
		private static final Type CALL_SITE_TYPE = Type.getType(CallSite.class);
		private static final Type METHOD_HANDLES_LOOKUP_TYPE = Type.getType(MethodHandles.Lookup.class);
		private static final Type STRING_TYPE = Type.getType(String.class);
		private static final Type METHOD_TYPE_TYPE = Type.getType(MethodType.class);
		private static final Type CLASS_TYPE = Type.getType(Class.class);
		private static final Type METHOD_HANDLE_TYPE = Type.getType(MethodHandle.class);

		private IndyHelpers() {}

		static boolean isValidBsm(final JavaNativeMethod method, final boolean constant) {
			final int modifiers = method.modifiers;
			if ((modifiers & JavaModifiers.PUBLIC) == 0) {
				return false;
			}
			if ((modifiers & JavaModifiers.STATIC) == 0) {
				return false;
			}

			final Type type = Type.getMethodType(method.descriptor);
			if (!CALL_SITE_TYPE.equals(type.getReturnType())) {
				return false;
			}

			final Type[] args = type.getArgumentTypes();
			final Type lastType = constant? CLASS_TYPE : METHOD_TYPE_TYPE;
			return args.length >= 3 && METHOD_HANDLES_LOOKUP_TYPE.equals(args[0]) && STRING_TYPE.equals(args[1]) && lastType.equals(args[2]);
		}

		static String autoFillBsmDesc(final boolean constant, final List<?> args) {
			return Type.getMethodDescriptor(
					CALL_SITE_TYPE,
					Stream.concat(
							Stream.of(METHOD_HANDLES_LOOKUP_TYPE, STRING_TYPE, constant? CLASS_TYPE : METHOD_TYPE_TYPE),
							args.stream().map(IndyHelpers::toBsmArg).map(IndyHelpers::toBsmArgType)
					).toArray(Type[]::new)
			);
		}

		static Handle toBsmHandle(final JavaNativeMethod method) {
			return new Handle(
					"<init>".equals(method.name)? Opcodes.H_INVOKESPECIAL : Opcodes.H_INVOKESTATIC,
					method.cls.internalName,
					method.name,
					method.descriptor,
					method.cls.isInterface()
			);
		}

		static Object[] toBsmArgs(final List<?> args) {
			return args.stream().map(IndyHelpers::toBsmArg).toArray(Object[]::new);
		}

		static Object toBsmArg(final Object object) {
			if (object instanceof Integer || object instanceof Float || object instanceof Double || object instanceof Long || object instanceof String) {
				return object;
			}
			if (object instanceof Type || object instanceof Handle || object instanceof ConstantDynamic) {
				return object;
			}
			if (object instanceof JavaClass) {
				return Type.getType(((JavaClass) object).internalName);
			}
			if (object instanceof Class<?>) {
				return Type.getType((Class<?>) object);
			}
			if (object instanceof JavaNativeMethod || object instanceof JavaCompilingMethod) {
				final JavaNativeMethod method = object instanceof JavaCompilingMethod? ((JavaCompilingMethod) object).compiled : (JavaNativeMethod) object;

				final int invokeType;
				if ((method.modifiers & JavaModifiers.STATIC) != 0) {
					invokeType = Opcodes.H_INVOKESTATIC;
				} else if (method.cls.isInterface()) {
					invokeType = Opcodes.H_INVOKEINTERFACE;
				} else if ("<init>".equals(method.name) || (method.modifiers & JavaModifiers.PRIVATE) != 0) {
					invokeType = Opcodes.H_INVOKESPECIAL;
				} else {
					invokeType = Opcodes.H_INVOKEVIRTUAL;
				}

				return new Handle(
						invokeType,
						method.cls.internalName,
						method.name,
						method.descriptor,
						method.cls.isInterface()
				);
			}
			if (object instanceof ConstantDynamicData) {
				final ConstantDynamicData data = (ConstantDynamicData) object;
				final BsmData bsmData = data.bsm();
				return new ConstantDynamic(data.name(), data.type().getDescriptor(), toBsmHandle(bsmData.method()), toBsmArgs(bsmData.args()));
			}
			throw new IllegalArgumentException("Unrecognized or incompatible indy construct " + object.getClass().getName());
		}

		static Type toBsmArgType(final Object object) {
			final Object realObject = toBsmArg(object);
			if (realObject instanceof Integer) {
				return Type.INT_TYPE;
			}
			if (realObject instanceof Float) {
				return Type.FLOAT_TYPE;
			}
			if (realObject instanceof Double) {
				return Type.DOUBLE_TYPE;
			}
			if (realObject instanceof Long) {
				return Type.LONG_TYPE;
			}
			if (realObject instanceof String) {
				return STRING_TYPE;
			}
			if (realObject instanceof Type) {
				final int sort = ((Type) realObject).getSort();
				if (sort == Type.METHOD) {
					return METHOD_TYPE_TYPE;
				} else {
					return CLASS_TYPE;
				}
			}
			if (realObject instanceof Handle) {
				return METHOD_HANDLE_TYPE;
			}
			if (realObject instanceof ConstantDynamicData) {
				final ConstantDynamicData data = (ConstantDynamicData) realObject;
				return data.type();
			}
			throw new IllegalArgumentException("Unrecognized or incompatible indy construct " + realObject.getClass().getName());
		}
	}

	private final String callSiteMethodName;
	private final String callSiteMethodDesc;
	private final JavaNativeMethod bsmMethod;
	private final List<?> bsmArgs;

	private JavaIndyHelper(final String callSiteMethodName, final String callSiteMethodDesc, final JavaNativeMethod bsmMethod, final List<?> bsmArgs) {
		this.callSiteMethodName = callSiteMethodName;
		this.callSiteMethodDesc = callSiteMethodDesc;
		this.bsmMethod = bsmMethod;
		this.bsmArgs = bsmArgs;
	}

	static JavaIndyHelper.Builder builder() {
		return new Builder(JavaIndyHelper::new);
	}

	void visit(final IndyVisitor visitor) {
		visitor.visitInvokeDynamic(
				this.callSiteMethodName,
				this.callSiteMethodDesc,
				IndyHelpers.toBsmHandle(this.bsmMethod),
				IndyHelpers.toBsmArgs(this.bsmArgs)
		);
	}

	@Override
	public String toString() {
		return this.callSiteMethodName + this.callSiteMethodDesc + " (through " + this.bsmMethod.cls.internalName + '.' + this.bsmMethod.name + this.bsmMethod.descriptor + ')';
	}
}
