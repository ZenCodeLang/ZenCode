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
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BinaryOperator;

public final class LambdaFactory {
	private static final class LambdaClassLoader extends ClassLoader {
		private static final Map<ClassLoader, WeakReference<LambdaClassLoader>> KNOWN_LOADERS = new WeakHashMap<>();

		static {
			ClassLoader.registerAsParallelCapable();
		}

		private final Lock lock;
		private final Map<String, byte[]> knownLambdas;

		private LambdaClassLoader(final ClassLoader parent) {
			super(parent);
			this.lock = new ReentrantLock();
			this.knownLambdas = new HashMap<>();
		}

		private static LambdaClassLoader findLoader(final MethodHandles.Lookup lookup) {
			final ClassLoader lookupClassLoader = lookup.lookupClass().getClassLoader();
			final WeakReference<LambdaClassLoader> loaderRef = KNOWN_LOADERS.get(lookupClassLoader);
			if (loaderRef == null || loaderRef.get() == null) {
				// LambdaClassLoader reference was lost (or it never existed), so recreate
				final LambdaClassLoader loader = new LambdaClassLoader(lookupClassLoader);
				KNOWN_LOADERS.put(loader, new WeakReference<>(loader));
				return loader;
			}
			return loaderRef.get();
		}

		LambdaClassLoader registerLambda(final String name, final byte[] data) {
			this.lock.lock();
			try {
				if (this.knownLambdas.containsKey(name)) {
					throw new IllegalStateException("Lambda with name '" + name + "' already exists");
				}
				this.knownLambdas.put(name, data);
			} finally {
				this.lock.unlock();
			}
			return this;
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			// Step 1: try loading the class directly without locking; there should never be an instance where a thread
			//         is trying to load a class that is being registered on another thread
			Class<?> lambdaClass = this.tryLoadLambdaClass(name);
			if (lambdaClass != null) {
				return lambdaClass;
			}

			// Step 2: if the previous step failed, let's retry with locking just in case the above situation happened
			this.lock.lock();
			try {
				lambdaClass = this.tryLoadLambdaClass(name);
				if (lambdaClass != null) {
					return lambdaClass;
				}
			} finally {
				this.lock.unlock();
			}

			// Step 3: Defer to default behavior
			return super.findClass(name);
		}

		private Class<?> tryLoadLambdaClass(final String name) {
			final byte[] lambdaBytes = this.knownLambdas.get(name);
			if (lambdaBytes != null) {
				return this.defineClass(name, lambdaBytes, 0, lambdaBytes.length);
			}
			return null;
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

		LambdaFlags(final int flags) {
			this.generateBridge = (flags & FLAG_GENERATE_BRIDGE) != 0;
		}

		boolean generateBridge() {
			return this.generateBridge;
		}
	}

	private static final class LambdaTypeConverters {
		@FunctionalInterface
		interface Operation {
			void run(final MethodVisitor visitor, final Type fromType, final Type toType);
		}

		private static final class TypedOperation {
			private final int fromSort;
			private final int toSort;
			private final Operation operation;

			TypedOperation(final int fromSort, final int toSort, final Operation operation) {
				this.fromSort = fromSort;
				this.toSort = toSort;
				this.operation = operation;
			}

			int from() {
				return this.fromSort;
			}

			int to() {
				return this.toSort;
			}

			Operation op() {
				return this.operation;
			}
		}

		private static final Operation[][] OPERATIONS = build(
				conv(Type.VOID, Type.VOID, nop()),
				conv(Type.VOID, Type.BOOLEAN, load(Opcodes.ICONST_0)),
				conv(Type.VOID, Type.CHAR, load(Opcodes.ICONST_0)),
				conv(Type.VOID, Type.BYTE, load(Opcodes.ICONST_0)),
				conv(Type.VOID, Type.SHORT, load(Opcodes.ICONST_0)),
				conv(Type.VOID, Type.INT, load(Opcodes.ICONST_0)),
				conv(Type.VOID, Type.FLOAT, load(Opcodes.FCONST_0)),
				conv(Type.VOID, Type.LONG, load(Opcodes.LCONST_0)),
				conv(Type.VOID, Type.DOUBLE, load(Opcodes.DCONST_0)),
				conv(Type.VOID, Type.ARRAY, load(Opcodes.ACONST_NULL)),
				conv(Type.VOID, Type.OBJECT, load(Opcodes.ACONST_NULL)),
				conv(Type.VOID, Type.METHOD, nonsense()),

				conv(Type.BOOLEAN, Type.VOID, pop()),
				conv(Type.BOOLEAN, Type.BOOLEAN, nop()),
				conv(Type.BOOLEAN, Type.CHAR, nop()),
				conv(Type.BOOLEAN, Type.BYTE, nop()),
				conv(Type.BOOLEAN, Type.SHORT, nop()),
				conv(Type.BOOLEAN, Type.INT, nop()),
				conv(Type.BOOLEAN, Type.FLOAT, widen(Opcodes.I2F)),
				conv(Type.BOOLEAN, Type.LONG, widen(Opcodes.I2L)),
				conv(Type.BOOLEAN, Type.DOUBLE, widen(Opcodes.I2D)),
				conv(Type.BOOLEAN, Type.ARRAY, nonsense()),
				conv(Type.BOOLEAN, Type.OBJECT, all(box(Boolean.class, "valueOf"), cast(toType()))),
				conv(Type.BOOLEAN, Type.METHOD, nonsense()),

				conv(Type.CHAR, Type.VOID, pop()),
				conv(Type.CHAR, Type.BOOLEAN, all(load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.CHAR, Type.CHAR, nop()),
				conv(Type.CHAR, Type.BYTE, narrow(Opcodes.I2B)),
				conv(Type.CHAR, Type.SHORT, nop()),
				conv(Type.CHAR, Type.INT, nop()),
				conv(Type.CHAR, Type.FLOAT, widen(Opcodes.I2F)),
				conv(Type.CHAR, Type.LONG, widen(Opcodes.I2L)),
				conv(Type.CHAR, Type.DOUBLE, widen(Opcodes.I2D)),
				conv(Type.CHAR, Type.ARRAY, nonsense()),
				conv(Type.CHAR, Type.OBJECT, all(box(Character.class, "valueOf"), cast(toType()))),
				conv(Type.CHAR, Type.METHOD, nonsense()),

				conv(Type.BYTE, Type.VOID, pop()),
				conv(Type.BYTE, Type.BOOLEAN, all(load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.BYTE, Type.CHAR, nop()),
				conv(Type.BYTE, Type.BYTE, nop()),
				conv(Type.BYTE, Type.SHORT, nop()),
				conv(Type.BYTE, Type.INT, nop()),
				conv(Type.BYTE, Type.FLOAT, widen(Opcodes.I2F)),
				conv(Type.BYTE, Type.LONG, widen(Opcodes.I2L)),
				conv(Type.BYTE, Type.DOUBLE, widen(Opcodes.I2D)),
				conv(Type.BYTE, Type.ARRAY, nonsense()),
				conv(Type.BYTE, Type.OBJECT, all(box(Byte.class, "valueOf"), cast(toType()))),
				conv(Type.BYTE, Type.METHOD, nonsense()),

				conv(Type.SHORT, Type.VOID, pop()),
				conv(Type.SHORT, Type.BOOLEAN, all(load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.SHORT, Type.CHAR, nop()),
				conv(Type.SHORT, Type.BYTE, narrow(Opcodes.I2B)),
				conv(Type.SHORT, Type.SHORT, nop()),
				conv(Type.SHORT, Type.INT, nop()),
				conv(Type.SHORT, Type.FLOAT, widen(Opcodes.I2F)),
				conv(Type.SHORT, Type.LONG, widen(Opcodes.I2L)),
				conv(Type.SHORT, Type.DOUBLE, widen(Opcodes.I2D)),
				conv(Type.SHORT, Type.ARRAY, nonsense()),
				conv(Type.SHORT, Type.OBJECT, all(box(Short.class, "valueOf"), cast(toType()))),
				conv(Type.SHORT, Type.METHOD, nonsense()),

				conv(Type.INT, Type.VOID, pop()),
				conv(Type.INT, Type.BOOLEAN, all(load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.INT, Type.CHAR, narrow(Opcodes.I2C)),
				conv(Type.INT, Type.BYTE, narrow(Opcodes.I2B)),
				conv(Type.INT, Type.SHORT, narrow(Opcodes.I2S)),
				conv(Type.INT, Type.INT, nop()),
				conv(Type.INT, Type.FLOAT, widen(Opcodes.I2F)),
				conv(Type.INT, Type.LONG, widen(Opcodes.I2L)),
				conv(Type.INT, Type.DOUBLE, widen(Opcodes.I2D)),
				conv(Type.INT, Type.ARRAY, nonsense()),
				conv(Type.INT, Type.OBJECT, all(box(Integer.class, "valueOf"), cast(toType()))),
				conv(Type.INT, Type.METHOD, nonsense()),

				conv(Type.FLOAT, Type.VOID, pop()),
				conv(Type.FLOAT, Type.BOOLEAN, all(narrow(Opcodes.F2I), load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.FLOAT, Type.CHAR, all(narrow(Opcodes.F2I), narrow(Opcodes.I2C))),
				conv(Type.FLOAT, Type.BYTE, all(narrow(Opcodes.F2I), narrow(Opcodes.I2B))),
				conv(Type.FLOAT, Type.SHORT, all(narrow(Opcodes.F2I), narrow(Opcodes.I2S))),
				conv(Type.FLOAT, Type.INT, narrow(Opcodes.F2I)),
				conv(Type.FLOAT, Type.FLOAT, nop()),
				conv(Type.FLOAT, Type.LONG, narrow(Opcodes.F2L)),
				conv(Type.FLOAT, Type.DOUBLE, widen(Opcodes.F2D)),
				conv(Type.FLOAT, Type.ARRAY, nonsense()),
				conv(Type.FLOAT, Type.OBJECT, all(box(Float.class, "valueOf"), cast(toType()))),
				conv(Type.FLOAT, Type.METHOD, nonsense()),

				conv(Type.LONG, Type.VOID, pop2()),
				conv(Type.LONG, Type.BOOLEAN, all(narrow(Opcodes.L2I), load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.LONG, Type.CHAR, all(narrow(Opcodes.L2I), narrow(Opcodes.I2C))),
				conv(Type.LONG, Type.BYTE, all(narrow(Opcodes.L2I), narrow(Opcodes.I2B))),
				conv(Type.LONG, Type.SHORT, all(narrow(Opcodes.L2I), narrow(Opcodes.I2S))),
				conv(Type.LONG, Type.INT, narrow(Opcodes.L2I)),
				conv(Type.LONG, Type.FLOAT, widen(Opcodes.L2F)),
				conv(Type.LONG, Type.LONG, nop()),
				conv(Type.LONG, Type.DOUBLE, widen(Opcodes.L2D)),
				conv(Type.LONG, Type.ARRAY, nonsense()),
				conv(Type.LONG, Type.OBJECT, all(box(Long.class, "valueOf"), cast(toType()))),
				conv(Type.LONG, Type.METHOD, nonsense()),

				conv(Type.DOUBLE, Type.VOID, pop2()),
				conv(Type.DOUBLE, Type.BOOLEAN, all(narrow(Opcodes.D2I), load(Opcodes.ICONST_1), opcode(Opcodes.IAND))),
				conv(Type.DOUBLE, Type.CHAR, all(narrow(Opcodes.D2I), narrow(Opcodes.I2C))),
				conv(Type.DOUBLE, Type.BYTE, all(narrow(Opcodes.D2I), narrow(Opcodes.I2B))),
				conv(Type.DOUBLE, Type.SHORT, all(narrow(Opcodes.D2I), narrow(Opcodes.I2S))),
				conv(Type.DOUBLE, Type.INT, narrow(Opcodes.D2I)),
				conv(Type.DOUBLE, Type.FLOAT, narrow(Opcodes.D2F)),
				conv(Type.DOUBLE, Type.LONG, narrow(Opcodes.D2L)),
				conv(Type.DOUBLE, Type.DOUBLE, nop()),
				conv(Type.DOUBLE, Type.OBJECT, all(box(Double.class, "valueOf"), cast(toType()))),
				conv(Type.DOUBLE, Type.METHOD, nonsense()),

				conv(Type.ARRAY, Type.VOID, pop()),
				conv(Type.ARRAY, Type.BOOLEAN, nonsense()),
				conv(Type.ARRAY, Type.CHAR, nonsense()),
				conv(Type.ARRAY, Type.BYTE, nonsense()),
				conv(Type.ARRAY, Type.SHORT, nonsense()),
				conv(Type.ARRAY, Type.INT, nonsense()),
				conv(Type.ARRAY, Type.FLOAT, nonsense()),
				conv(Type.ARRAY, Type.LONG, nonsense()),
				conv(Type.ARRAY, Type.DOUBLE, nonsense()),
				conv(Type.ARRAY, Type.OBJECT, cast(toType())),
				conv(Type.ARRAY, Type.ARRAY, cast(toType())),
				conv(Type.ARRAY, Type.METHOD, nonsense()),

				conv(Type.OBJECT, Type.VOID, pop()),
				conv(Type.OBJECT, Type.BOOLEAN, all(cast(Boolean.class), unbox(Boolean.class, "booleanValue"))),
				conv(Type.OBJECT, Type.CHAR, all(cast(Character.class), unbox(Character.class, "charValue"))),
				conv(Type.OBJECT, Type.BYTE, all(cast(Number.class), unbox(Number.class, "byteValue"))),
				conv(Type.OBJECT, Type.SHORT, all(cast(Number.class), unbox(Number.class, "shortValue"))),
				conv(Type.OBJECT, Type.INT, all(cast(Number.class), unbox(Number.class, "intValue"))),
				conv(Type.OBJECT, Type.FLOAT, all(cast(Number.class), unbox(Number.class, "floatValue"))),
				conv(Type.OBJECT, Type.LONG, all(cast(Number.class), unbox(Number.class, "longValue"))),
				conv(Type.OBJECT, Type.DOUBLE, all(cast(Number.class), unbox(Number.class, "doubleValue"))),
				conv(Type.OBJECT, Type.OBJECT, cast(toType())),
				conv(Type.OBJECT, Type.ARRAY, cast(toType())),
				conv(Type.OBJECT, Type.METHOD, nonsense()),

				conv(Type.METHOD, Type.VOID, nonsense()),
				conv(Type.METHOD, Type.BOOLEAN, nonsense()),
				conv(Type.METHOD, Type.CHAR, nonsense()),
				conv(Type.METHOD, Type.BYTE, nonsense()),
				conv(Type.METHOD, Type.SHORT, nonsense()),
				conv(Type.METHOD, Type.INT, nonsense()),
				conv(Type.METHOD, Type.FLOAT, nonsense()),
				conv(Type.METHOD, Type.LONG, nonsense()),
				conv(Type.METHOD, Type.DOUBLE, nonsense()),
				conv(Type.METHOD, Type.OBJECT, nonsense()),
				conv(Type.METHOD, Type.ARRAY, nonsense()),
				conv(Type.METHOD, Type.METHOD, nop())
		);

		private LambdaTypeConverters() {}

		static void convert(final Type fromType, final Type toType, final MethodVisitor visitor) {
			OPERATIONS[fromType.getSort()][toType.getSort()].run(visitor, fromType, toType);
		}

		private static Operation[][] build(final TypedOperation... ops) {
			final Operation[][] operations = new Operation[12][12];
			for (final TypedOperation op : ops) {
				operations[op.from()][op.to()] = op.op();
			}
			return operations;
		}

		private static TypedOperation conv(final int from, final int to, final Operation operation) {
			return new TypedOperation(from, to, operation);
		}

		private static Operation nop() {
			return (visitor, fromType, toType) -> {};
		}

		private static Operation widen(final int opcode) {
			return opcode(opcode);
		}

		private static Operation narrow(final int opcode) {
			return opcode(opcode);
		}

		private static Operation load(final int opcode) {
			return opcode(opcode);
		}

		private static Operation pop() {
			return opcode(Opcodes.POP);
		}

		private static Operation pop2() {
			return opcode(Opcodes.POP2);
		}

		private static Operation opcode(final int opcode) {
			return (visitor, fromType, toType) -> visitor.visitInsn(opcode);
		}

		private static Operation nonsense() {
			return (visitor, fromType, toType) -> {
				throw new IllegalStateException("Conversion between " + fromType + " to " + toType + " cannot be meaningfully carried out");
			};
		}

		private static Operation box(final Class<?> owner, @SuppressWarnings("SameParameterValue") final String name) {
			return (visitor, fromType, toType) -> visitor.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(owner),
					name,
					Type.getMethodDescriptor(Type.getType(owner), fromType),
					owner.isInterface()
			);
		}

		private static Operation cast(final Class<?> target) {
			return cast((a, b) -> Type.getType(target));
		}

		private static Operation cast(final BinaryOperator<Type> chooser) {
			return (visitor, fromType, toType) -> visitor.visitTypeInsn(Opcodes.CHECKCAST, chooser.apply(fromType, toType).getInternalName());
		}

		@SuppressWarnings("unused")
		private static BinaryOperator<Type> fromType() {
			return (a, b) -> a;
		}

		private static BinaryOperator<Type> toType() {
			return (a, b) -> b;
		}

		private static Operation unbox(final Class<?> owner, final String name) {
			return (visitor, fromType, toType) -> visitor.visitMethodInsn(
					owner.isInterface()? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
					Type.getInternalName(owner),
					name,
					Type.getMethodDescriptor(toType),
					false
			);
		}

		private static Operation all(final Operation... ops) {
			return (visitor, fromType, toType) -> {
				for (final Operation op : ops) {
					op.run(visitor, fromType, toType);
				}
			};
		}
	}

	public interface LambdaMarker {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface LambdaForwarder {}

	private static final String HANDLE_FIELD_NAME = "$handle";
	private static final String THIS_FIELD_NAME = "$this";
	private static final String CAPTURE_FIELD_NAME_PREFIX = "$capture$";

	private static final String CONSTRUCTOR_NAME = "<init>";
	private static final String FACTORY_METHOD_NAME = "$init";

	public static final int FLAG_GENERATE_BRIDGE = 0b1;
	private static final int FLAG_ALL_FLAGS = FLAG_GENERATE_BRIDGE;

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
	// bridgeInterfaceSignature -> (Object)Object --> ignored if FLAG_GENERATE_BRIDGE unset, otherwise used only if it differs from interfaceSignature
	@SuppressWarnings("unused") // Used via Indy
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

		final MethodType lambdaMethodType = lambdaMethod.type();
		final LambdaFlags flags = new LambdaFlags(packedFlags);

		final Class<?> targetInterface = callSiteSignature.returnType();
		final Class<?>[] callSiteParams = callSiteSignature.parameterArray();

		final Class<?> lambdaReturn = lambdaMethodType.returnType();
		final Class<?>[] lambdaArguments = lambdaMethodType.parameterArray();

		final Class<?> interfaceReturn = interfaceSignature.returnType();
		final Class<?>[] interfaceArguments = interfaceSignature.parameterArray();

		if (!targetInterface.isInterface()) {
			throw new IllegalArgumentException("Return type of the call site was " + targetInterface.getName() + ", which is not an interface");
		}

		if (callSiteParams.length == 0) {
			throw new IllegalArgumentException("Call site needs to have at least one param, namely the receiver of the lambda");
		}

		if (!interfaceReturn.isAssignableFrom(lambdaReturn)) {
			throw new IllegalArgumentException("Lambda does not return a subtype of the interface return type");
		}

		final int totalArguments = interfaceArguments.length + callSiteParams.length;

		if (lambdaArguments.length != totalArguments) {
			throw new IllegalArgumentException("Lambda argument quantity is different than expected: " + lambdaArguments.length + " instead of " + totalArguments);
		}

		// TODO("Relax validation requirements for assignment compatibilities between the various signatures")
		for (int i = callSiteParams.length - 1, d = lambdaArguments.length - callSiteParams.length; i > 1; --i) {
			final Class<?> callSiteTarget = callSiteParams[i];
			final Class<?> lambdaTarget = lambdaArguments[d + i];
			if (callSiteTarget != lambdaTarget) {
				throw new IllegalArgumentException("Captured argument does not match expected type between call site and lambda");
			}
		}

		for (int i = 0, s = interfaceArguments.length; i < s; ++i) {
			final Class<?> interfaceTarget = interfaceArguments[i];
			final Class<?> lambdaTarget = lambdaArguments[i + 1];
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
		final LambdaClassLoader lookupLoader = LambdaClassLoader.findLoader(callerLookup);
		final LambdaClassLoader lambdaLoader = lookupLoader.registerLambda(binaryName, classData);
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

		generateInterfaceClassFields(writer, callSiteSignature);
		generateInterfaceClassConstructor(writer, callSiteSignature, className);
		generateInterfaceClassFactory(writer, callSiteSignature, className);
		generateInterfaceClassLambdaInvoker(writer, targetMethodName, callSiteSignature, interfaceSignature, lambdaMethod, className);
		generateInterfaceClassBridge(writer, targetMethodName, bridgeInterfaceSignature, interfaceSignature, flags, className);

		writer.visitEnd();
	}

	private static void generateInterfaceClassFields(final ClassWriter classWriter, final MethodType callSiteSignature) {
		classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, HANDLE_FIELD_NAME, Type.getDescriptor(MethodHandle.class), null, null);

		final Class<?> ownerField = callSiteSignature.parameterType(0);
		classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, THIS_FIELD_NAME, Type.getDescriptor(ownerField), null, null);

		for (int i = 1, s = callSiteSignature.parameterCount(); i < s; ++i) {
			final Class<?> fieldType = callSiteSignature.parameterType(i);
			final int captureIndex = i - 1;
			classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, CAPTURE_FIELD_NAME_PREFIX + captureIndex, Type.getDescriptor(fieldType), null, null);
		}
	}

	private static void generateInterfaceClassConstructor(final ClassWriter classWriter, final MethodType callSiteSignature, final String className) {
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
			} else {
				fieldName = (CAPTURE_FIELD_NAME_PREFIX + (i - 2));
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

		int localIndex = 1;
		for (int i = 0, s = interfaceSignature.parameterCount(); i < s; ++i) {
			final Type type = Type.getType(interfaceSignature.parameterType(i));
			writer.visitVarInsn(type.getOpcode(Opcodes.ILOAD), localIndex);
			localIndex += type.getSize();
		}

		for (int i = 1, s = callSiteSignature.parameterCount(); i < s; ++i) {
			final String fieldDescriptor = Type.getDescriptor(callSiteSignature.parameterType(i));
			final int captureIndex = i - 1;
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

			writer.visitVarInsn(bridgeType.getOpcode(Opcodes.ILOAD), localIndex);
			LambdaTypeConverters.convert(bridgeType, targetType, writer);

			localIndex += bridgeType.getSize();
		}

		writer.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, targetMethodName, interfaceSignature.toMethodDescriptorString(), false);

		final Type bridgeReturnType = Type.getType(bridgeInterfaceSignature.returnType());
		final Type targetReturnType = Type.getType(interfaceSignature.returnType());

		LambdaTypeConverters.convert(targetReturnType, bridgeReturnType, writer);
		writer.visitInsn(bridgeReturnType.getOpcode(Opcodes.IRETURN));

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
