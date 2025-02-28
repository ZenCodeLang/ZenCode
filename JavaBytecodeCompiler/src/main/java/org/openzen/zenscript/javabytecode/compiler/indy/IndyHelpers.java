package org.openzen.zenscript.javabytecode.compiler.indy;

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
import java.util.List;

final class IndyHelpers {
	static final Type CALL_SITE_TYPE = Type.getType(CallSite.class);
	static final Type CLASS_TYPE = Type.getType(Class.class);
	static final Type METHOD_HANDLE_TYPE = Type.getType(MethodHandle.class);
	static final Type METHOD_HANDLES_LOOKUP_TYPE = Type.getType(MethodHandles.Lookup.class);
	static final Type METHOD_TYPE_TYPE = Type.getType(MethodType.class);
	static final Type OBJECT_TYPE = Type.getType(Object.class);
	static final Type STRING_TYPE = Type.getType(String.class);

	static final String CONSTRUCTOR_NAME = "<init>";

	private IndyHelpers() {}

	static boolean isValidBsm(final JavaNativeMethod method, final IndyTarget target, final boolean relax) {
		final int modifiers = method.modifiers;
		if ((modifiers & JavaModifiers.PUBLIC) == 0) {
			return false;
		}
		if ((modifiers & JavaModifiers.STATIC) == 0 && !CONSTRUCTOR_NAME.equals(method.name)) {
			return false;
		}

		final Type type = Type.getMethodType(method.descriptor);
		return target.verifySignature(type, relax);
	}

	static String autoFillBsmDesc(final IndyTarget target, final List<?> args) {
		return target.appendTypeArguments(args).getDescriptor();
	}

	static Handle toBsmHandle(final JavaNativeMethod method) {
		return new Handle(
				CONSTRUCTOR_NAME.equals(method.name)? Opcodes.H_INVOKESPECIAL : Opcodes.H_INVOKESTATIC,
				method.cls.internalName,
				method.name,
				method.descriptor,
				method.cls.isInterface()
		);
	}

	static Object[] toBsmArgs(final List<?> args) {
		return args.stream().map(IndyHelpers::toBsmArg).toArray(Object[]::new);
	}

	static Type[] toBsmArgTypes(final List<?> args) {
		return args.stream().map(IndyHelpers::toBsmArgType).toArray(Type[]::new);
	}

	private static Object toBsmArg(final Object object) {
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
			} else if (CONSTRUCTOR_NAME.equals(method.name) || (method.modifiers & JavaModifiers.PRIVATE) != 0) {
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
		if (object instanceof JavaCondy) {
			return ((JavaCondy) object).asConstantDynamic();
		}
		throw new IllegalArgumentException("Unrecognized or incompatible indy construct " + object.getClass().getName());
	}

	private static Type toBsmArgType(final Object object) {
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
		if (realObject instanceof ConstantDynamic) {
			return Type.getType(((ConstantDynamic) realObject).getDescriptor());
		}
		throw new IllegalArgumentException("Unrecognized or incompatible indy construct " + realObject.getClass().getName());
	}
}
