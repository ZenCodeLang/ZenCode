package org.openzen.zenscript.javabytecode.compiler.indy;

import org.objectweb.asm.Type;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class BsmData {
	public enum InvokeType {
		STATIC,
		VIRTUAL,
		SPECIAL,
		INTERFACE
	}

	public static final class Builder {
		// TODO("Think about this")
		private static final boolean ALLOW_RELAX = false;

		private final IndyTarget indyTarget;
		private final Constructor constructor;
		private final List<Object> args;

		private JavaNativeMethod bsmMethod;
		private JavaClass owner;
		private String name;

		private Builder(final IndyTarget indyTarget, final Constructor constructor) {
			this.indyTarget = indyTarget;
			this.constructor = constructor;
			this.args = new ArrayList<>();
			this.bsmMethod = null;
			this.owner = null;
			this.name = null;
		}

		static Builder of(final IndyTarget indyTarget, final Constructor constructor) {
			return new Builder(indyTarget, constructor);
		}

		public Builder method(final JavaNativeMethod bsmMethod) {
			Objects.requireNonNull(bsmMethod, "BSM Method specified cannot be null");
			if (!IndyHelpers.isValidBsm(bsmMethod, this.indyTarget, ALLOW_RELAX)) {
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

		public Builder method(final JavaClass owner, final String name) {
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

		public Builder arg(final int argument) {
			return this.arg((Object) argument);
		}

		public Builder arg(final float argument) {
			return this.arg((Object) argument);
		}

		public Builder arg(final long argument) {
			return this.arg((Object) argument);
		}

		public Builder arg(final double argument) {
			return this.arg((Object) argument);
		}

		public Builder arg(final String argument) {
			return this.arg((Object) argument);
		}

		public Builder arg(final JavaClass clazz) {
			return this.arg((Object) clazz);
		}

		public Builder arg(final Class<?> clazz) {
			return this.arg((Object) clazz);
		}

		public Builder arg(final Type type) {
			return this.arg((Object) type);
		}

		public Builder arg(final Type returnType, final Type... arguments) {
			return this.arg(Type.getMethodType(returnType, arguments));
		}

		public Builder arg(final String returnType, final String... arguments) {
			return this.arg(Type.getType(returnType), Stream.of(arguments).map(Type::getType).toArray(Type[]::new));
		}

		public Builder arg(final JavaCompilingMethod method) {
			return this.arg((Object) method);
		}

		public Builder arg(final JavaNativeMethod method) {
			return this.arg((Object) method);
		}

		public Builder arg(final InvokeType invokeType, final JavaClass owner, final String name, final Type desc) {
			switch (invokeType) {
				case STATIC: return this.arg(JavaNativeMethod.getStatic(owner, name, desc.getDescriptor(), JavaModifiers.STATIC));
				case SPECIAL: {
					if (IndyHelpers.CONSTRUCTOR_NAME.equals(name)) {
						return this.arg(JavaNativeMethod.getConstructor(owner, desc.getDescriptor(), 0));
					} else {
						return this.arg(JavaNativeMethod.getVirtual(owner, name, desc.getDescriptor(), 0));
					}
				}
				case VIRTUAL: return this.arg(JavaNativeMethod.getVirtual(owner, name, desc.getDescriptor(), 0));
				case INTERFACE: return this.arg(JavaNativeMethod.getInterface(owner, name, desc.getDescriptor()));
			}
			throw new IllegalArgumentException(String.valueOf(invokeType));
		}

		@SuppressWarnings("SpellCheckingInspection")
		public Builder arg(final UnaryOperator<JavaCondy.Builder> condyBuilder) {
			return this.arg(condyBuilder.apply(JavaCondy.builder()).build());
		}

		@SuppressWarnings("SpellCheckingInspection")
		public Builder arg(final JavaCondy condy) {
			return this.arg((Object) condy);
		}

		private Builder arg(final Object argument) {
			Objects.requireNonNull(argument, "Null arguments are not supported when invoking BSMs");
			this.args.add(argument);
			return this;
		}

		BsmData build() {
			if (this.bsmMethod == null && this.owner == null) {
				throw new IllegalStateException("BSM has not been specified");
			}
			final JavaNativeMethod method = this.findBsmMethod();
			return this.constructor.of(method, this.args);
		}

		private JavaNativeMethod findBsmMethod() {
			if (this.bsmMethod != null) {
				return this.bsmMethod;
			}

			return JavaNativeMethod.getStatic(this.owner, this.name, IndyHelpers.autoFillBsmDesc(this.indyTarget, this.args), JavaModifiers.PUBLIC | JavaModifiers.STATIC);
		}
	}

	@FunctionalInterface
	interface Constructor {
		BsmData of(final JavaNativeMethod bsm, final List<?> args);
	}

	private final JavaNativeMethod bsm;
	private final List<?> args;

	private BsmData(final JavaNativeMethod bsm, final List<?> args) {
		this.bsm = bsm;
		this.args = args;
	}

	static BsmData.Builder builder(final IndyTarget target) {
		return Builder.of(target, BsmData::new);
	}

	JavaNativeMethod bsm() {
		return this.bsm;
	}

	List<?> args() {
		return this.args;
	}

	@Override
	public String toString() {
		return String.format("%s.%s%s, invoked with %s", this.bsm.cls.internalName, this.bsm.name, this.bsm.descriptor, this.args);
	}
}
