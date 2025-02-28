package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaNativeMethod;

public class JavaBoxingTypeVisitor implements TypeVisitor<Void> {
	private static final JavaNativeMethod BOOLEAN_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.BOOLEAN, "valueOf", "(Z)Ljava/lang/Boolean;");
	private static final JavaNativeMethod BYTE_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.BYTE, "valueOf", "(B)Ljava/lang/Byte;");
	private static final JavaNativeMethod SHORT_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.SHORT, "valueOf", "(S)Ljava/lang/Short;");
	private static final JavaNativeMethod INTEGER_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.INTEGER, "valueOf", "(I)Ljava/lang/Integer;");
	private static final JavaNativeMethod LONG_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.LONG, "valueOf", "(J)Ljava/lang/Long;");
	private static final JavaNativeMethod FLOAT_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.FLOAT, "valueOf", "(F)Ljava/lang/Float;");
	private static final JavaNativeMethod DOUBLE_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.DOUBLE, "valueOf", "(D)Ljava/lang/Double;");
	private static final JavaNativeMethod CHARACTER_VALUEOF = JavaNativeMethod.getNativeStatic(JavaClass.CHARACTER, "valueOf", "(C)Ljava/lang/Character;");

	private final JavaWriter writer;
	private final boolean wrapping;

	public static JavaBoxingTypeVisitor forJavaBoxing(JavaWriter writer) {
		return new JavaBoxingTypeVisitor(writer, false);
	}

	public static JavaBoxingTypeVisitor forOptionalWrapping(JavaWriter writer) {
		return new JavaBoxingTypeVisitor(writer, true);
	}

	private JavaBoxingTypeVisitor(JavaWriter writer, boolean wrapping) {
		this.writer = writer;
		this.wrapping = wrapping;
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
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
				method = INTEGER_VALUEOF;
				break;
			case USIZE:
				if (!wrapping) {
					method = INTEGER_VALUEOF;
				} else {
					return null;
				}
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

		writer.invokeStatic(method);
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitOptional(OptionalTypeID type) {
		if (type.baseType == BasicTypeID.USIZE) {
			writer.invokeStatic(INTEGER_VALUEOF);
		}
		//NO-OP
		return null;
	}

	@Override
	public Void visitWildcardIn(WildcardInTypeID type) {
		//NO-OP
		return null;
	}

	@Override
	public Void visitWildcardOut(WildcardOutTypeID type) {
		//NO-OP
		return null;
	}
}
