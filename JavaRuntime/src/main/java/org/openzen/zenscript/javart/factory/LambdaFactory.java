package org.openzen.zenscript.javart.factory;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LambdaFactory {
	private static final class LambdaClassLoader extends ClassLoader {
		static {
			ClassLoader.registerAsParallelCapable();
		}

		private final String name;
		private final byte[] data;

		private LambdaClassLoader(final String name, final byte[] data, final ClassLoader parent) {
			super(parent);
			this.name = name;
			this.data = data;
		}

		static ClassLoader spinLoader(final String name, final byte[] data, final MethodHandles.Lookup lookup) {
			return new LambdaClassLoader(name, data, lookup.lookupClass().getClassLoader());
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			if (this.name.equals(name)) {
				return this.defineClass(name, this.data, 0, this.data.length);
			}
			return super.findClass(name);
		}


	}

	private static final class LambdaCounters {
		private static final ConcurrentMap<String, Integer> COUNTERS = new ConcurrentHashMap<>();

		static int get(final Class<?> owner, final Class<?> funType) {
			final String key = owner.getName() + '/' + funType.getName();
			// TODO("Verify concurrency")
			return COUNTERS.compute(key, (k, old) -> old == null? 0 : (old + 1));
		}
	}

	private static final class LambdaFlags {
		private final boolean generateBridge;
		private final boolean differentiateReceiver;

		LambdaFlags(final int flags) {
			this.generateBridge = (flags & FLAG_GENERATE_BRIDGE) != 0;
			this.differentiateReceiver = (flags & FLAG_DIFFERENTIATE_RECEIVER) != 0;
		}

		boolean generateBridge() {
			return this.generateBridge;
		}

		boolean differentiateReceiver() {
			return this.differentiateReceiver;
		}
	}

	public interface LambdaMarker {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface LambdaForwarder {}

	private static final String HANDLE_FIELD_NAME = "$handle";
	private static final String THIS_FIELD_NAME = "$this";
	private static final String RECEIVER_FIELD_NAME = "$receiver";
	private static final String CAPTURE_FIELD_NAME_PREFIX = "$capture$";

	private static final String CONSTRUCTOR_NAME = "<init>";
	private static final String FACTORY_METHOD_NAME = "$init";

	public static final int FLAG_GENERATE_BRIDGE = 0b01;
	public static final int FLAG_DIFFERENTIATE_RECEIVER = 0b10;
	private static final int FLAG_ALL_FLAGS = FLAG_GENERATE_BRIDGE | FLAG_DIFFERENTIATE_RECEIVER;

	private LambdaFactory() {}

	// Assume a lambda such as (foo) => foo + bar, where bar is a captured variable and this is implicitly captured,
	// and that the target "Functional Interface" is meant to be an on-the-fly Function1<FooBar, Foo> with the following
	// definition:
	// interface Function1<R, T> { R invoke(T t); }
	//
	// The parameter types
	// will be filled according to the following table:
	// targetMethodName -> invoke
	// callSiteSignature -> (ThisType,BarType)Function1
	// lambdaMethod -> handle to the method that "foo + bar" has been compiled to in its declaring class
	//                 expected signature: (ThisType,FooType,BarType)FooBarType
	// interfaceSignature -> (FooType)FooBarType
	// packedFlags -> a set of information representing special behavior of this method
	//          FLAG_GENERATE_BRIDGE => generate a method bridge if necessary
	//          FLAG_DIFFERENTIATE_RECEIVER => differentiates the 'this' context from the 'this' receiver; used for expansions
	// bridgeInterfaceSignature -> (Object)Object --> ignored if FLAG_GENERATE_BRIDGE unset, otherwise used only if it differs from interfaceSignature
	public static CallSite buildLambda(
			final MethodHandles.Lookup callerLookup,
			final String targetMethodName,
			final MethodType callSiteSignature,
			final MethodHandle lambdaMethod,
			final MethodType interfaceSignature,
			final int packedFlags,
			final MethodType bridgeInterfaceSignature
	) {
		final LambdaFlags flags = checkArgumentValidity(callerLookup, targetMethodName, callSiteSignature, lambdaMethod, interfaceSignature, packedFlags, bridgeInterfaceSignature);
		final Class<?> lambdaImplementation = generateInterfaceImplementation(callerLookup, targetMethodName, callSiteSignature, lambdaMethod, interfaceSignature, flags, bridgeInterfaceSignature);
		final MethodHandle handle = constructClassHandle(callerLookup, lambdaImplementation, lambdaMethod, callSiteSignature);
		return bindCallSite(handle);
	}

	private static LambdaFlags checkArgumentValidity(
			final MethodHandles.Lookup callerLookup,
			final String targetMethodName,
			final MethodType callSiteSignature,
			final MethodHandle lambdaMethod,
			final MethodType interfaceSignature,
			final int packedFlags,
			final MethodType bridgeInterfaceSignature
	) {
		Objects.requireNonNull(callerLookup, "Caller lookup is required to generate lambda class");
		Objects.requireNonNull(targetMethodName, "Name of the method that should be implemented must be provided");
		Objects.requireNonNull(callSiteSignature, "Signature of the expected CallSite object must be provided");
		Objects.requireNonNull(lambdaMethod, "Handle to the lambda method is missing");
		Objects.requireNonNull(interfaceSignature, "Signature of method in the target interface must be provided");
		Objects.requireNonNull(bridgeInterfaceSignature, "Signature of bridge method must be provided");

		if ((callerLookup.lookupModes() & MethodHandles.Lookup.PRIVATE) == 0) {
			throw new IllegalArgumentException("Caller lookup does not have private access in the target class: cannot be used to invoke lambdas");
		}

		if ((packedFlags & ~FLAG_ALL_FLAGS) != 0) {
			throw new IllegalArgumentException("Unknown flag set: " + packedFlags);
		}

		final Class<?> lookupClass = callerLookup.lookupClass();
		final MethodType lambdaMethodType = lambdaMethod.type();
		final LambdaFlags flags = new LambdaFlags(packedFlags);

		final Class<?> targetInterface = callSiteSignature.returnType();
		final Class<?>[] callSiteParams = callSiteSignature.parameterArray();

		final Class<?> lambdaReturn = lambdaMethodType.returnType();
		final Class<?>[] lambdaArguments = lambdaMethodType.parameterArray();

		final Class<?> interfaceReturn = interfaceSignature.returnType();
		final Class<?>[] interfaceArguments = interfaceSignature.parameterArray();

		final int lastReceiver = flags.differentiateReceiver()? 2 : 1;

		if (!targetInterface.isInterface()) {
			throw new IllegalArgumentException("Return type of the call site was " + targetInterface.getName() + ", which is not an interface");
		}

		if (callSiteParams.length == 0) {
			throw new IllegalArgumentException("Call site needs to have at least one param, namely the receiver of the lambda");
		}

		if (callSiteParams[0] != lookupClass) {
			throw new IllegalArgumentException("Argument 0 of call site must be the same class as the lookup class");
		}

		if (!interfaceReturn.isAssignableFrom(lambdaReturn)) {
			throw new IllegalArgumentException("Lambda does not return a subtype of the interface return type");
		}

		final int totalArguments = interfaceArguments.length + callSiteParams.length;

		if (lambdaArguments.length != totalArguments) {
			throw new IllegalArgumentException("Lambda argument quantity is different than expected: " + lambdaArguments.length + " instead of " + totalArguments);
		}

		// TODO("Relax validation requirements for assignment compatibilities between the various signatures")
		for (int i = callSiteParams.length - 1, d = lambdaArguments.length - callSiteParams.length; i > lastReceiver; --i) {
			final Class<?> callSiteTarget = callSiteParams[i];
			final Class<?> lambdaTarget = lambdaArguments[d + i];
			if (callSiteTarget != lambdaTarget) {
				throw new IllegalArgumentException("Captured argument does not match expected type between call site and lambda");
			}
		}

		for (int i = 0, s = interfaceArguments.length; i < s; ++i) {
			final Class<?> interfaceTarget = interfaceArguments[i];
			final Class<?> lambdaTarget = lambdaArguments[i + lastReceiver];
			if (interfaceTarget != lambdaTarget) {
				throw new IllegalArgumentException("Interface argument does not match expected type between interface and lambda");
			}
		}

		if (flags.generateBridge()) {
			final Class<?>[] bridgeInterfaceArguments = bridgeInterfaceSignature.parameterArray();

			if (bridgeInterfaceArguments.length != interfaceArguments.length) {
				throw new IllegalArgumentException("Interface bridge has a different amount of arguments than the interface");
			}
		}

		return flags;
	}

	private static Class<?> generateInterfaceImplementation(
			final MethodHandles.Lookup callerLookup,
			final String targetMethodName,
			final MethodType callSiteSignature,
			final MethodHandle lambdaMethod,
			final MethodType interfaceSignature,
			final LambdaFlags flags,
			final MethodType bridgeInterfaceSignature
	) {
		final String className = obtainLambdaClassName(callerLookup, callSiteSignature);
		final String binaryName = className.replace('/', '.');
		final byte[] classData = generateInterfaceClassData(targetMethodName, callSiteSignature, lambdaMethod, interfaceSignature, flags, bridgeInterfaceSignature, className);

		// Java 8 forces us to use a custom classloader for this; ideally we'd be leveraging hidden classes
		final ClassLoader lambdaLoader = LambdaClassLoader.spinLoader(binaryName, classData, callerLookup);
		try {
			return Class.forName(binaryName, false, lambdaLoader);
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException("An error occurred while trying to load lambda class", e);
		}
	}

	private static String obtainLambdaClassName(final MethodHandles.Lookup callerLookup, final MethodType callSiteSignature) {
		final Class<?> owner = callerLookup.lookupClass();
		final Class<?> interfaceType = callSiteSignature.returnType();

		// $$ is required because such a construct is impossible to obtain via normal means in source, so no conflict
		// can arise
		return String.format(
				"%s$$Lambda$%s$%s",
				owner.getName().replace('.', '/'),
				interfaceType.getName().replace('.', '_'),
				LambdaCounters.get(owner, interfaceType)
		);
	}

	private static byte[] generateInterfaceClassData(
			final String targetMethodName,
			final MethodType callSiteSignature,
			final MethodHandle lambdaMethod,
			final MethodType interfaceSignature,
			final LambdaFlags flags,
			final MethodType bridgeInterfaceSignature,
			final String className
	) {
		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); // No if jumps means we don't need frames
		generateInterfaceClass(writer, targetMethodName, callSiteSignature, lambdaMethod, interfaceSignature, flags, bridgeInterfaceSignature, className);
		return writer.toByteArray();
	}

	private static void generateInterfaceClass(
			final ClassWriter writer,
			final String targetMethodName,
			final MethodType callSiteSignature,
			final MethodHandle lambdaMethod,
			final MethodType interfaceSignature,
			final LambdaFlags flags,
			final MethodType bridgeInterfaceSignature,
			final String className
	) {
		writer.visit(
				52, // Java 8
				Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER | Opcodes.ACC_SYNTHETIC,
				className,
				null,
				Type.getInternalName(Object.class),
				new String[] { Type.getInternalName(callSiteSignature.returnType()), Type.getInternalName(LambdaMarker.class) }
		);
		writer.visitSource("dynamically generated by ZenCode", null);

		generateInterfaceClassFields(writer, callSiteSignature, flags);
		generateInterfaceClassConstructor(writer, callSiteSignature, flags, className);
		generateInterfaceClassFactory(writer, callSiteSignature, className);
		generateInterfaceClassLambdaInvoker(writer, targetMethodName, callSiteSignature, interfaceSignature, lambdaMethod, flags, className);
		generateInterfaceClassBridge(writer, targetMethodName, bridgeInterfaceSignature, interfaceSignature, flags, className);

		writer.visitEnd();
	}

	private static void generateInterfaceClassFields(final ClassWriter classWriter, final MethodType callSiteSignature, final LambdaFlags flags) {
		classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, HANDLE_FIELD_NAME, Type.getDescriptor(MethodHandle.class), null, null);

		final Class<?> ownerField = callSiteSignature.parameterType(0);
		classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, THIS_FIELD_NAME, Type.getDescriptor(ownerField), null, null);

		if (flags.differentiateReceiver()) {
			final Class<?> receiverField = callSiteSignature.parameterType(1);
			classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, RECEIVER_FIELD_NAME, Type.getDescriptor(receiverField), null, null);
		}

		final int captureBeginIndex = flags.differentiateReceiver()? 2 : 1;
		for (int i = captureBeginIndex, s = callSiteSignature.parameterCount(); i < s; ++i) {
			final Class<?> fieldType = callSiteSignature.parameterType(i);
			final int captureIndex = i - captureBeginIndex;
			classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, CAPTURE_FIELD_NAME_PREFIX + captureIndex, Type.getDescriptor(fieldType), null, null);
		}
	}

	private static void generateInterfaceClassConstructor(final ClassWriter classWriter, final MethodType callSiteSignature, final LambdaFlags flags, final String className) {
		final Type[] constructorArguments = new Type[callSiteSignature.parameterCount() + 1];
		constructorArguments[0] = Type.getType(MethodHandle.class);
		for (int i = 0, s = callSiteSignature.parameterCount(); i < s; ++i) {
			constructorArguments[i + 1] = Type.getType(callSiteSignature.parameterType(i));
		}

		final String constructorDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, constructorArguments);
		final MethodVisitor constructorWriter = classWriter.visitMethod(Opcodes.ACC_PRIVATE, CONSTRUCTOR_NAME, constructorDescriptor, null, null);
		constructorWriter.visitCode();

		constructorWriter.visitVarInsn(Opcodes.ALOAD, 0);
		constructorWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), CONSTRUCTOR_NAME, "()V", false);

		int localIndex = 1;
		for (int i = 0, s = constructorArguments.length; i < s; ++i) {
			final Type fieldType = constructorArguments[i];
			final String fieldName;
			if (i == 0) {
				fieldName = HANDLE_FIELD_NAME;
			} else if (i == 1) {
				fieldName = THIS_FIELD_NAME;
			} else if (flags.differentiateReceiver() && i == 2) {
				fieldName = RECEIVER_FIELD_NAME;
			} else {
				fieldName = (CAPTURE_FIELD_NAME_PREFIX + (i - (flags.differentiateReceiver()? 3 : 2)));
			}

			constructorWriter.visitVarInsn(Opcodes.ALOAD, 0);
			constructorWriter.visitVarInsn(fieldType.getOpcode(Opcodes.ILOAD), localIndex);
			constructorWriter.visitFieldInsn(Opcodes.PUTFIELD, className, fieldName, fieldType.getDescriptor());

			localIndex += fieldType.getSize();
		}

		constructorWriter.visitInsn(Opcodes.RETURN);
		constructorWriter.visitMaxs(3, localIndex);
		constructorWriter.visitEnd();
	}

	private static void generateInterfaceClassFactory(final ClassWriter classWriter, final MethodType callSiteSignature, final String className) {
		final Type[] factoryArguments = new Type[callSiteSignature.parameterCount() + 1];
		factoryArguments[0] = Type.getType(MethodHandle.class);
		for (int i = 0, s = callSiteSignature.parameterCount(); i < s; ++i) {
			factoryArguments[i + 1] = Type.getType(callSiteSignature.parameterType(i));
		}

		final String factoryDescriptor = Type.getMethodDescriptor(Type.getType(callSiteSignature.returnType()), factoryArguments);
		final String constructorDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, factoryArguments);
		final MethodVisitor factoryWriter = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, FACTORY_METHOD_NAME, factoryDescriptor, null, null);
		factoryWriter.visitCode();

		factoryWriter.visitTypeInsn(Opcodes.NEW, className);
		factoryWriter.visitInsn(Opcodes.DUP);

		int localIndex = 0;
		for (final Type fieldType : factoryArguments) {
			factoryWriter.visitVarInsn(fieldType.getOpcode(Opcodes.ILOAD), localIndex);
			localIndex += fieldType.getSize();
		}

		factoryWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, className, CONSTRUCTOR_NAME, constructorDescriptor, false);
		factoryWriter.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(callSiteSignature.returnType()));
		factoryWriter.visitInsn(Opcodes.ARETURN);
		factoryWriter.visitMaxs(localIndex, localIndex);
		factoryWriter.visitEnd();
	}

	private static void generateInterfaceClassLambdaInvoker(
			final ClassWriter classWriter,
			final String targetMethodName,
			final MethodType callSiteSignature,
			final MethodType interfaceSignature,
			final MethodHandle lambdaMethod,
			final LambdaFlags flags,
			final String className
	) {
		final String descriptor = interfaceSignature.toMethodDescriptorString();
		final MethodVisitor writer = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, targetMethodName, descriptor, null, null);
		writer.visitAnnotation(Type.getDescriptor(LambdaForwarder.class), true); // For inspection via reflection in case someone needs to

		writer.visitCode();

		writer.visitVarInsn(Opcodes.ALOAD, 0);
		writer.visitFieldInsn(Opcodes.GETFIELD, className, HANDLE_FIELD_NAME, Type.getDescriptor(MethodHandle.class));

		final String thisTypeDescriptor = Type.getDescriptor(callSiteSignature.parameterType(0));
		writer.visitVarInsn(Opcodes.ALOAD, 0);
		writer.visitFieldInsn(Opcodes.GETFIELD, className, THIS_FIELD_NAME, thisTypeDescriptor);

		if (flags.differentiateReceiver()) {
			final String receiverTypeDescriptor = Type.getDescriptor(callSiteSignature.parameterType(1));
			writer.visitVarInsn(Opcodes.ALOAD, 0);
			writer.visitFieldInsn(Opcodes.GETFIELD, className, RECEIVER_FIELD_NAME, receiverTypeDescriptor);
		}

		int localIndex = 1;
		for (int i = 0, s = interfaceSignature.parameterCount(); i < s; ++i) {
			final Type type = Type.getType(interfaceSignature.parameterType(i));
			writer.visitVarInsn(type.getOpcode(Opcodes.ILOAD), localIndex);
			localIndex += type.getSize();
		}

		final int firstCaptureIndex = flags.differentiateReceiver()? 2 : 1;
		for (int i = firstCaptureIndex, s = callSiteSignature.parameterCount(); i < s; ++i) {
			final String fieldDescriptor = Type.getDescriptor(callSiteSignature.parameterType(i));
			final int captureIndex = i - firstCaptureIndex;
			writer.visitVarInsn(Opcodes.ALOAD, 0);
			writer.visitFieldInsn(Opcodes.GETFIELD, className, CAPTURE_FIELD_NAME_PREFIX + captureIndex, fieldDescriptor);
		}

		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(MethodHandle.class), "invoke", lambdaMethod.type().toMethodDescriptorString(), false);

		final Class<?> returnType = interfaceSignature.returnType();
		if (returnType == void.class) {
			writer.visitInsn(Opcodes.RETURN);
		} else {
			writer.visitInsn(Type.getType(returnType).getOpcode(Opcodes.IRETURN));
		}

		writer.visitMaxs(lambdaMethod.type().parameterCount() + 1, interfaceSignature.parameterCount());
		writer.visitEnd();
	}

	private static void generateInterfaceClassBridge(
			final ClassWriter classWriter,
			final String targetMethodName,
			final MethodType bridgeInterfaceSignature,
			final MethodType interfaceSignature,
			final LambdaFlags flags,
			final String className
	) {
		if (!flags.generateBridge()) {
			return;
		}

		if (bridgeInterfaceSignature.equals(interfaceSignature)) {
			return;
		}

		final String descriptor = bridgeInterfaceSignature.toMethodDescriptorString();
		final MethodVisitor writer = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_BRIDGE, targetMethodName, descriptor, null, null);
		writer.visitCode();

		writer.visitVarInsn(Opcodes.ALOAD, 0);

		int localIndex = 1;
		for (int i = 0, s = bridgeInterfaceSignature.parameterCount(); i < s; ++i) {
			final Type bridgeType = Type.getType(bridgeInterfaceSignature.parameterType(i));
			final Type targetType = Type.getType(interfaceSignature.parameterType(i));

			if (bridgeType.getSort() == targetType.getSort()) {
				writer.visitVarInsn(bridgeType.getOpcode(Opcodes.ILOAD), localIndex);

				if (bridgeType.getSort() == Type.OBJECT) {
					writer.visitTypeInsn(Opcodes.CHECKCAST, targetType.getInternalName());
				}
			} else {
				throw new UnsupportedOperationException("Not yet implemented");
			}

			localIndex += bridgeType.getSize();
		}

		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, targetMethodName, interfaceSignature.toMethodDescriptorString(), false);

		final Type bridgeReturnType = Type.getType(bridgeInterfaceSignature.returnType());
		final Type targetReturnType = Type.getType(interfaceSignature.returnType());

		if (bridgeReturnType.getSort() == targetReturnType.getSort()) {
			if (targetReturnType.getSort() == Type.OBJECT) {
				writer.visitTypeInsn(Opcodes.CHECKCAST, bridgeReturnType.getInternalName());
			}

			writer.visitInsn(targetReturnType.getOpcode(Opcodes.IRETURN));
		} else {
			throw new UnsupportedOperationException("Not yet implemented");
		}

		writer.visitMaxs(localIndex, localIndex);
		writer.visitEnd();
	}

	private static MethodHandle constructClassHandle(
			final MethodHandles.Lookup lookup,
			final Class<?> clazz,
			final MethodHandle lambdaMethod,
			final MethodType callSite
	) {
		final MethodType factoryType = callSite.insertParameterTypes(0, MethodHandle.class);
		try {
			final MethodHandle type = lookup.findStatic(clazz, FACTORY_METHOD_NAME, factoryType);
			return MethodHandles.insertArguments(type, 0, lambdaMethod);
		} catch (final NoSuchMethodException | IllegalAccessException e) {
			throw new IllegalStateException("Unable to find factory method for lambda", e);
		}
	}

	private static CallSite bindCallSite(final MethodHandle methodHandle) {
		return new ConstantCallSite(methodHandle);
	}
}
