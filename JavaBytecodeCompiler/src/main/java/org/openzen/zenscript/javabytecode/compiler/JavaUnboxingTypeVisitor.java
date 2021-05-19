package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaMethod;

public class JavaUnboxingTypeVisitor implements TypeVisitorWithContext<TypeID, Void, RuntimeException> {

	private static final JavaMethod UNBOX_BOOLEAN = JavaMethod.getNativeVirtual(JavaClass.BOOLEAN, "booleanValue", "()Z");
	private static final JavaMethod UNBOX_BYTE = JavaMethod.getNativeVirtual(JavaClass.BYTE, "byteValue", "()B");
	private static final JavaMethod UNBOX_SHORT = JavaMethod.getNativeVirtual(JavaClass.SHORT, "shortValue", "()S");
	private static final JavaMethod UNBOX_INTEGER = JavaMethod.getNativeVirtual(JavaClass.INTEGER, "intValue", "()I");
	private static final JavaMethod UNBOX_LONG = JavaMethod.getNativeVirtual(JavaClass.LONG, "longValue", "()J");
	private static final JavaMethod UNBOX_FLOAT = JavaMethod.getNativeVirtual(JavaClass.FLOAT, "floatValue", "()F");
	private static final JavaMethod UNBOX_DOUBLE = JavaMethod.getNativeVirtual(JavaClass.DOUBLE, "doubleValue", "()D");
	private static final JavaMethod UNBOX_CHARACTER = JavaMethod.getNativeVirtual(JavaClass.CHARACTER, "charValue", "()C");

	private final JavaWriter writer;

	public JavaUnboxingTypeVisitor(JavaWriter writer) {
		this.writer = writer;
	}


	@Override
	public Void visitBasic(TypeID context, BasicTypeID basic) throws RuntimeException {
		final JavaMethod method;

		switch (basic) {
			case BOOL:
				writer.checkCast(JavaClass.BOOLEAN.internalName);
				method = UNBOX_BOOLEAN;
				break;
			case BYTE:
			case SBYTE:
				writer.checkCast(JavaClass.BYTE.internalName);
				method = UNBOX_BYTE;
				break;
			case SHORT:
			case USHORT:
				writer.checkCast(JavaClass.SHORT.internalName);
				method = UNBOX_SHORT;
				break;
			case INT:
			case UINT:
				writer.checkCast(JavaClass.INTEGER.internalName);
				method = UNBOX_INTEGER;
				break;
			case LONG:
			case ULONG:
			case USIZE:
				writer.checkCast(JavaClass.LONG.internalName);
				method = UNBOX_LONG;
				break;
			case FLOAT:
				writer.checkCast(JavaClass.FLOAT.internalName);
				method = UNBOX_FLOAT;
				break;
			case DOUBLE:
				writer.checkCast(JavaClass.DOUBLE.internalName);
				method = UNBOX_DOUBLE;
				break;
			case CHAR:
				writer.checkCast(JavaClass.CHARACTER.internalName);
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
