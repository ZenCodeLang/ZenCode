package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

public class JavaUnboxingTypeVisitor implements TypeVisitorWithContext<TypeID, Void, RuntimeException> {

	private static final JavaNativeMethod UNBOX_BOOLEAN = JavaNativeMethod.getNativeVirtual(JavaClass.BOOLEAN, "booleanValue", "()Z", "()Z");
	private static final JavaNativeMethod UNBOX_BYTE = JavaNativeMethod.getNativeVirtual(JavaClass.BYTE, "byteValue", "()B", "()B");
	private static final JavaNativeMethod UNBOX_SHORT = JavaNativeMethod.getNativeVirtual(JavaClass.SHORT, "shortValue", "()S", "()S");
	private static final JavaNativeMethod UNBOX_INTEGER = JavaNativeMethod.getNativeVirtual(JavaClass.INTEGER, "intValue", "()I", "()I");
	private static final JavaNativeMethod UNBOX_LONG = JavaNativeMethod.getNativeVirtual(JavaClass.LONG, "longValue", "()J", "()J");
	private static final JavaNativeMethod UNBOX_FLOAT = JavaNativeMethod.getNativeVirtual(JavaClass.FLOAT, "floatValue", "()F", "()F");
	private static final JavaNativeMethod UNBOX_DOUBLE = JavaNativeMethod.getNativeVirtual(JavaClass.DOUBLE, "doubleValue", "()D", "()D");
	private static final JavaNativeMethod UNBOX_CHARACTER = JavaNativeMethod.getNativeVirtual(JavaClass.CHARACTER, "charValue", "()C", "()C");

	private final JavaWriter writer;

	public JavaUnboxingTypeVisitor(JavaWriter writer) {
		this.writer = writer;
	}


	@Override
	public Void visitBasic(TypeID context, BasicTypeID basic) throws RuntimeException {
		final JavaNativeMethod method;

		switch (basic) {
			case BOOL:
				method = UNBOX_BOOLEAN;
				break;
			case BYTE:
			case SBYTE:
				method = UNBOX_BYTE;
				break;
			case SHORT:
			case USHORT:
				method = UNBOX_SHORT;
				break;
			case INT:
			case UINT:
				method = UNBOX_INTEGER;
				break;
			case LONG:
			case ULONG:
			case USIZE:
				method = UNBOX_LONG;
				break;
			case FLOAT:
				method = UNBOX_FLOAT;
				break;
			case DOUBLE:
				method = UNBOX_DOUBLE;
				break;
			case CHAR:
				method = UNBOX_CHARACTER;
				break;
			case VOID:
			case UNDETERMINED:
			case NULL:
			default:
				return null;
		}
		writer.invokeVirtual(method);
		return null;
	}

	@Override
	public Void visitArray(TypeID context, ArrayTypeID array) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitAssoc(TypeID context, AssocTypeID assoc) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitGenericMap(TypeID context, GenericMapTypeID map) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitIterator(TypeID context, IteratorTypeID iterator) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitFunction(TypeID context, FunctionTypeID function) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitDefinition(TypeID context, DefinitionTypeID definition) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitGeneric(TypeID context, GenericTypeID generic) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitRange(TypeID context, RangeTypeID range) throws RuntimeException {
		//NO-OP
		return null;
	}

	@Override
	public Void visitOptional(TypeID context, OptionalTypeID type) throws RuntimeException {
		//NO-OP
		return null;
	}
}
