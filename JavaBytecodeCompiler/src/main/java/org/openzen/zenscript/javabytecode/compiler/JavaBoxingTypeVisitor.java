package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

public class JavaBoxingTypeVisitor implements TypeVisitorWithContext<TypeID, Void, RuntimeException> {
	private static final JavaNativeMethod BOOLEAN_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "valueOf", "(Z)Ljava/lang/Boolean;");
	private static final JavaNativeMethod BYTE_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "valueOf", "(B)Ljava/lang/Byte;");
	private static final JavaNativeMethod SHORT_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "valueOf", "(S)Ljava/lang/Short;");
	private static final JavaNativeMethod INTEGER_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "valueOf", "(I)Ljava/lang/Integer;");
	private static final JavaNativeMethod LONG_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "valueOf", "(J)Ljava/lang/Long;");
	private static final JavaNativeMethod FLOAT_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "valueOf", "(F)Ljava/lang/Float;");
	private static final JavaNativeMethod DOUBLE_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "valueOf", "(D)Ljava/lang/Double;");
	private static final JavaNativeMethod CHARACTER_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.CHARACTER, "valueOf", "(C)Ljava/lang/Character;");

	private final JavaWriter writer;

	public JavaBoxingTypeVisitor(JavaWriter writer) {
		this.writer = writer;
	}

	@Override
	public Void visitBasic(TypeID context, BasicTypeID basic) {
		final JavaNativeMethod method;
		switch (basic) {
			case BOOL:
				method = BOOLEAN_VALUEOF;
				break;
			case BYTE:
				method = INTEGER_VALUEOF;
				break;
			case SBYTE:
				method = BYTE_VALUEOF;
				break;
			case SHORT:
				method = SHORT_VALUEOF;
				break;
			case USHORT:
				method = INTEGER_VALUEOF;
				break;
			case INT:
			case UINT:
			case USIZE:
				method = INTEGER_VALUEOF;
				break;
			case LONG:
			case ULONG:
				method = LONG_VALUEOF;
				break;
			case FLOAT:
				method = FLOAT_VALUEOF;
				break;
			case DOUBLE:
				method = DOUBLE_VALUEOF;
				break;
			case CHAR:
				method = CHARACTER_VALUEOF;
				break;
			case STRING:
				// NO-OP
				return null;
			default:
				return null;
		}

		if (method != null)
			writer.invokeStatic(method);
		return null;
	}

	@Override
	public Void visitArray(TypeID context, ArrayTypeID array) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitAssoc(TypeID context, AssocTypeID assoc) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitGenericMap(TypeID context, GenericMapTypeID map) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitIterator(TypeID context, IteratorTypeID iterator) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitFunction(TypeID context, FunctionTypeID function) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitDefinition(TypeID context, DefinitionTypeID definition) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitGeneric(TypeID context, GenericTypeID generic) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitRange(TypeID context, RangeTypeID range) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitOptional(TypeID context, OptionalTypeID type) {
		//NO-OP
		return null;
	}
}
