package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.compiler.JavaModificationExpressionVisitor.PushOption;
import org.openzen.zenscript.javashared.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class JavaExpressionVisitor implements ExpressionVisitor<Void>, JavaNativeTranslator<Void> {
    private static final JavaMethod OBJECTS_TOSTRING = JavaMethod.getNativeStatic(new JavaClass("java.util", "Objects", JavaClass.Kind.CLASS), "toString", "(Ljava/lang/Object;)Ljava/lang/String;");
    
	private static final JavaMethod BOOLEAN_PARSE = JavaMethod.getNativeStatic(JavaClass.BOOLEAN, "parseBoolean", "(Ljava/lang/String;)Z");
	private static final JavaMethod BOOLEAN_TO_STRING = JavaMethod.getNativeStatic(JavaClass.BOOLEAN, "toString", "(Z)Ljava/lang/String;");
	private static final JavaMethod BYTE_PARSE = JavaMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;)B");
	private static final JavaMethod BYTE_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.BYTE, "parseByte", "(Ljava/lang/String;I)B");
	private static final JavaField BYTE_MIN_VALUE = new JavaField(JavaClass.BYTE, "MIN_VALUE", "B");
	private static final JavaField BYTE_MAX_VALUE = new JavaField(JavaClass.BYTE, "MAX_VALUE", "B");
	private static final JavaMethod BYTE_TO_STRING = JavaMethod.getNativeStatic(JavaClass.BYTE, "toString", "(B)Ljava/lang/String;");
	private static final JavaMethod SHORT_PARSE = JavaMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;)S");
	private static final JavaMethod SHORT_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.SHORT, "parseShort", "(Ljava/lang/String;I)S");
	private static final JavaField SHORT_MIN_VALUE = new JavaField(JavaClass.SHORT, "MIN_VALUE", "S");
	private static final JavaField SHORT_MAX_VALUE = new JavaField(JavaClass.SHORT, "MAX_VALUE", "S");
	private static final JavaMethod SHORT_TO_STRING = JavaMethod.getNativeStatic(JavaClass.SHORT, "toString", "(S)Ljava/lang/String;");
	private static final JavaMethod INTEGER_COMPARE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "compareUnsigned", "(II)I");
	private static final JavaMethod INTEGER_DIVIDE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "divideUnsigned", "(II)I");
	private static final JavaMethod INTEGER_REMAINDER_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "remainderUnsigned", "(II)I");
	private static final JavaMethod INTEGER_NUMBER_OF_TRAILING_ZEROS = JavaMethod.getNativeStatic(JavaClass.INTEGER, "numberOfTrailingZeros", "(I)I");
	private static final JavaMethod INTEGER_NUMBER_OF_LEADING_ZEROS = JavaMethod.getNativeStatic(JavaClass.INTEGER, "numberOfLeadingZeros", "(I)I");
	private static final JavaMethod INTEGER_PARSE = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseInt", "(Ljava/lang/String;)I");
	private static final JavaMethod INTEGER_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseInt", "(Ljava/lang/String;I)I");
	private static final JavaMethod INTEGER_PARSE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseUnsignedInt", "(Ljava/lang/String;)I");
	private static final JavaMethod INTEGER_PARSE_UNSIGNED_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.INTEGER, "parseUnsignedInt", "(Ljava/lang/String;I)I");
	private static final JavaMethod INTEGER_HIGHEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.INTEGER, "highestOneBit", "(I)I");
	private static final JavaMethod INTEGER_LOWEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.INTEGER, "lowestOneBit", "(I)I");
	private static final JavaMethod INTEGER_BIT_COUNT = JavaMethod.getNativeStatic(JavaClass.INTEGER, "bitCount", "(I)I");
	private static final JavaField INTEGER_MIN_VALUE = new JavaField(JavaClass.INTEGER, "MIN_VALUE", "I");
	private static final JavaField INTEGER_MAX_VALUE = new JavaField(JavaClass.INTEGER, "MAX_VALUE", "I");
	private static final JavaMethod INTEGER_TO_STRING = JavaMethod.getNativeStatic(JavaClass.INTEGER, "toString", "(I)Ljava/lang/String;");
	private static final JavaMethod INTEGER_TO_UNSIGNED_STRING = JavaMethod.getNativeStatic(JavaClass.INTEGER, "toUnsignedString", "(I)Ljava/lang/String;");
	private static final JavaMethod LONG_COMPARE = JavaMethod.getNativeStatic(JavaClass.LONG, "compare", "(JJ)I");
	private static final JavaMethod LONG_COMPARE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "compareUnsigned", "(JJ)I");
	private static final JavaMethod LONG_DIVIDE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "divideUnsigned", "(JJ)J");
	private static final JavaMethod LONG_REMAINDER_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "remainderUnsigned", "(JJ)J");
	private static final JavaMethod LONG_NUMBER_OF_TRAILING_ZEROS = JavaMethod.getNativeStatic(JavaClass.LONG, "numberOfTrailingZeros", "(J)I");
	private static final JavaMethod LONG_NUMBER_OF_LEADING_ZEROS = JavaMethod.getNativeStatic(JavaClass.LONG, "numberOfLeadingZeros", "(J)I");
	private static final JavaMethod LONG_PARSE = JavaMethod.getNativeStatic(JavaClass.LONG, "parseLong", "(Ljava/lang/String;)J");
	private static final JavaMethod LONG_PARSE_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.LONG, "parseLong", "(Ljava/lang/String;I)J");
	private static final JavaMethod LONG_PARSE_UNSIGNED = JavaMethod.getNativeStatic(JavaClass.LONG, "parseUnsignedLong", "(Ljava/lang/String;)J");
	private static final JavaMethod LONG_PARSE_UNSIGNED_WITH_BASE = JavaMethod.getNativeStatic(JavaClass.LONG, "parseUnsignedLong", "(Ljava/lang/String;I)J");
	private static final JavaMethod LONG_HIGHEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.LONG, "highestOneBit", "(J)J");
	private static final JavaMethod LONG_LOWEST_ONE_BIT = JavaMethod.getNativeStatic(JavaClass.LONG, "lowestOneBit", "(J)J");
	private static final JavaMethod LONG_BIT_COUNT = JavaMethod.getNativeStatic(JavaClass.LONG, "bitCount", "(J)I");
	private static final JavaField LONG_MIN_VALUE = new JavaField(JavaClass.LONG, "MIN_VALUE", "J");
	private static final JavaField LONG_MAX_VALUE = new JavaField(JavaClass.LONG, "MAX_VALUE", "J");
	private static final JavaMethod LONG_TO_STRING = JavaMethod.getNativeStatic(JavaClass.LONG, "toString", "(J)Ljava/lang/String;");
	private static final JavaMethod LONG_TO_UNSIGNED_STRING = JavaMethod.getNativeStatic(JavaClass.LONG, "toUnsignedString", "(J)Ljava/lang/String;");
	private static final JavaMethod FLOAT_COMPARE = JavaMethod.getNativeStatic(JavaClass.FLOAT, "compare", "(FF)I");
	private static final JavaMethod FLOAT_PARSE = JavaMethod.getNativeStatic(JavaClass.FLOAT, "parseFloat", "(Ljava/lang/String;)F");
	private static final JavaMethod FLOAT_FROM_BITS = JavaMethod.getNativeStatic(JavaClass.FLOAT, "intBitsToFloat", "(I)F");
	private static final JavaMethod FLOAT_BITS = JavaMethod.getNativeStatic(JavaClass.FLOAT, "floatToRawIntBits", "(F)I");
	private static final JavaField FLOAT_MIN_VALUE = new JavaField(JavaClass.FLOAT, "MIN_VALUE", "F");
	private static final JavaField FLOAT_MAX_VALUE = new JavaField(JavaClass.FLOAT, "MAX_VALUE", "F");
	private static final JavaMethod FLOAT_TO_STRING = JavaMethod.getNativeStatic(JavaClass.FLOAT, "toString", "(F)Ljava/lang/String;");
	private static final JavaMethod DOUBLE_COMPARE = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "compare", "(DD)I");
	private static final JavaMethod DOUBLE_PARSE = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "parseDouble", "(Ljava/lang/String;)D");
	private static final JavaMethod DOUBLE_FROM_BITS = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "longBitsToDouble", "(J)D");
	private static final JavaMethod DOUBLE_BITS = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "doubleToRawLongBits", "(D)J");
	private static final JavaField DOUBLE_MIN_VALUE = new JavaField(JavaClass.DOUBLE, "MIN_VALUE", "D");
	private static final JavaField DOUBLE_MAX_VALUE = new JavaField(JavaClass.DOUBLE, "MAX_VALUE", "D");
	private static final JavaMethod DOUBLE_TO_STRING = JavaMethod.getNativeStatic(JavaClass.DOUBLE, "toString", "(D)Ljava/lang/String;");
	private static final JavaMethod CHARACTER_TO_LOWER_CASE = JavaMethod.getNativeVirtual(JavaClass.CHARACTER, "toLowerCase", "()C");
	private static final JavaMethod CHARACTER_TO_UPPER_CASE = JavaMethod.getNativeVirtual(JavaClass.CHARACTER, "toUpperCase", "()C");
	private static final JavaField CHARACTER_MIN_VALUE = new JavaField(JavaClass.CHARACTER, "MIN_VALUE", "C");
	private static final JavaField CHARACTER_MAX_VALUE = new JavaField(JavaClass.CHARACTER, "MAX_VALUE", "C");
	private static final JavaMethod CHARACTER_TO_STRING = JavaMethod.getNativeStatic(JavaClass.CHARACTER, "toString", "(C)Ljava/lang/String;");
	private static final JavaMethod STRING_INIT_CHARACTERS = JavaMethod.getNativeConstructor(JavaClass.STRING, "([C)V");
	private static final JavaMethod STRING_COMPARETO = JavaMethod.getNativeVirtual(JavaClass.STRING, "compareTo", "(Ljava/lang/String;)I");
	private static final JavaMethod STRING_CONCAT = JavaMethod.getNativeVirtual(JavaClass.STRING, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
	private static final JavaMethod STRING_CHAR_AT = JavaMethod.getNativeVirtual(JavaClass.STRING, "charAt", "(I)C");
	private static final JavaMethod STRING_SUBSTRING = JavaMethod.getNativeVirtual(JavaClass.STRING, "substring", "(II)Ljava/lang/String;");
	private static final JavaMethod STRING_TRIM = JavaMethod.getNativeVirtual(JavaClass.STRING, "trim", "()Ljava/lang/String;");
	private static final JavaMethod STRING_TO_LOWER_CASE = JavaMethod.getNativeVirtual(JavaClass.STRING, "toLowerCase", "()Ljava/lang/String;");
	private static final JavaMethod STRING_TO_UPPER_CASE = JavaMethod.getNativeVirtual(JavaClass.STRING, "toUpperCase", "()Ljava/lang/String;");
	private static final JavaMethod STRING_LENGTH = JavaMethod.getNativeVirtual(JavaClass.STRING, "length", "()I");
	private static final JavaMethod STRING_CHARACTERS = JavaMethod.getNativeVirtual(JavaClass.STRING, "toCharArray", "()[C");
	private static final JavaMethod STRING_ISEMPTY = JavaMethod.getNativeVirtual(JavaClass.STRING, "isEmpty", "()Z");
	private static final JavaMethod ENUM_COMPARETO = JavaMethod.getNativeVirtual(JavaClass.ENUM, "compareTo", "(Ljava/lang/Enum;)I");
	private static final JavaMethod ENUM_NAME = JavaMethod.getNativeVirtual(JavaClass.ENUM, "name", "()Ljava/lang/String;");
	private static final JavaMethod ENUM_ORDINAL = JavaMethod.getNativeVirtual(JavaClass.ENUM, "ordinal", "()I");
	private static final JavaMethod HASHMAP_INIT = JavaMethod.getNativeConstructor(JavaClass.HASHMAP, "()V");
	private static final JavaMethod MAP_GET = JavaMethod.getInterface(JavaClass.MAP, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaMethod MAP_PUT = JavaMethod.getInterface(JavaClass.MAP, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
	private static final JavaMethod MAP_PUT_ALL = JavaMethod.getInterface(JavaClass.MAP, "putAll", "(Ljava/util/Map;)V");
	private static final JavaMethod MAP_CONTAINS_KEY = JavaMethod.getInterface(JavaClass.MAP, "containsKey", "(Ljava/lang/Object;)Z");
	private static final JavaMethod MAP_SIZE = JavaMethod.getInterface(JavaClass.MAP, "size", "()I");
	private static final JavaMethod MAP_ISEMPTY = JavaMethod.getInterface(JavaClass.MAP, "isEmpty", "()Z");
	private static final JavaMethod MAP_KEYS = JavaMethod.getInterface(JavaClass.MAP, "keys", "()Ljava/lang/Object;");
	private static final JavaMethod MAP_VALUES = JavaMethod.getInterface(JavaClass.MAP, "values", "()Ljava/lang/Object;");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_OBJECTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([Ljava/lang/Object;II)[Ljava/lang/Object;");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_BOOLS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([ZII)[Z");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_BYTES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([BII)[B");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_SHORTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([SII)[S");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_INTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([III)[I");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_LONGS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([JII)[J");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_FLOATS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([FII)[F");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_DOUBLES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([DII)[D");
	private static final JavaMethod ARRAYS_COPY_OF_RANGE_CHARS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "copyOfRange", "([CII)[C");
	private static final JavaMethod ARRAYS_EQUALS_OBJECTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Ljava/lang/Object[Ljava/lang/Object)Z");
	private static final JavaMethod ARRAYS_EQUALS_BOOLS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([Z[Z)Z");
	private static final JavaMethod ARRAYS_EQUALS_BYTES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([B[B)Z");
	private static final JavaMethod ARRAYS_EQUALS_SHORTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([S[S)Z");
	private static final JavaMethod ARRAYS_EQUALS_INTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([I[I)Z");
	private static final JavaMethod ARRAYS_EQUALS_LONGS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([L[L)Z");
	private static final JavaMethod ARRAYS_EQUALS_FLOATS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([F[F)Z");
	private static final JavaMethod ARRAYS_EQUALS_DOUBLES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([D[D)Z");
	private static final JavaMethod ARRAYS_EQUALS_CHARS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "equals", "([C[C)Z");
	private static final JavaMethod ARRAYS_DEEPHASHCODE = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "deepHashCode", "([Ljava/lang/Object;)");
	private static final JavaMethod ARRAYS_HASHCODE_BOOLS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([Z)I");
	private static final JavaMethod ARRAYS_HASHCODE_BYTES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([B)I");
	private static final JavaMethod ARRAYS_HASHCODE_SHORTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([S)I");
	private static final JavaMethod ARRAYS_HASHCODE_INTS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([I)I");
	private static final JavaMethod ARRAYS_HASHCODE_LONGS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([L)I");
	private static final JavaMethod ARRAYS_HASHCODE_FLOATS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([F)I");
	private static final JavaMethod ARRAYS_HASHCODE_DOUBLES = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([D)I");
	private static final JavaMethod ARRAYS_HASHCODE_CHARS = JavaMethod.getNativeStatic(JavaClass.ARRAYS, "hashCode", "([C)I");
	public static final JavaMethod OBJECT_HASHCODE = JavaMethod.getNativeVirtual(JavaClass.OBJECT, "hashCode", "()I");
	public static final JavaMethod OBJECT_EQUALS = JavaMethod.getNativeVirtual(JavaClass.OBJECT, "equals", "(Ljava/lang/Object)Z");
	private static final JavaMethod COLLECTION_SIZE = JavaMethod.getNativeVirtual(JavaClass.COLLECTION, "size", "()I");
	private static final JavaMethod COLLECTION_TOARRAY = JavaMethod.getNativeVirtual(JavaClass.COLLECTION, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;");

	private static final JavaMethod SHARED_INIT = JavaMethod.getConstructor(JavaClass.SHARED, "(Ljava/lang/Object;)V", Modifier.PUBLIC);
	private static final JavaMethod SHARED_GET = JavaMethod.getNativeVirtual(JavaClass.SHARED, "get", "()Ljava/lang/Object;");
	private static final JavaMethod SHARED_ADDREF = JavaMethod.getNativeVirtual(JavaClass.SHARED, "addRef", "()V");
	private static final JavaMethod SHARED_RELEASE = JavaMethod.getNativeVirtual(JavaClass.SHARED, "release", "()V");

	final JavaWriter javaWriter;
    private final JavaBoxingTypeVisitor boxingTypeVisitor;
    private final JavaUnboxingTypeVisitor unboxingTypeVisitor;
    private final JavaCapturedExpressionVisitor capturedExpressionVisitor = new JavaCapturedExpressionVisitor(this);
    final JavaBytecodeContext context;
    final JavaCompiledModule module;

	public JavaExpressionVisitor(JavaBytecodeContext context, JavaCompiledModule module, JavaWriter javaWriter) {
		this.javaWriter = javaWriter;
		this.context = context;
		this.module = module;
        boxingTypeVisitor = new JavaBoxingTypeVisitor(javaWriter);
        unboxingTypeVisitor = new JavaUnboxingTypeVisitor(javaWriter);
    }

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		Label end = new Label();
		Label onFalse = new Label();

		expression.left.accept(this);

		javaWriter.ifEQ(onFalse);
		expression.right.accept(this);

		// //these two calls are redundant but make decompiled code look better. Keep?
		// javaWriter.ifEQ(onFalse);
		// javaWriter.iConst1();

		javaWriter.goTo(end);

		javaWriter.label(onFalse);
		javaWriter.iConst0();


		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		javaWriter.constant(expression.expressions.length);
		Type type = context.getType(((ArrayTypeID) expression.type.type).elementType);
		javaWriter.newArray(type);
		for (int i = 0; i < expression.expressions.length; i++) {
			javaWriter.dup();
			javaWriter.constant(i);
			expression.expressions[i].accept(this);
			javaWriter.arrayStore(type);
		}
		return null;
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		if (expression.operator.getBuiltin() != null) {
			switch (expression.operator.getBuiltin()) {
				case BYTE_COMPARE:
					expression.left.accept(this);
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					expression.right.accept(this);
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
					compareInt(expression.comparison);
					break;
				case USHORT_COMPARE:
					expression.left.accept(this);
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					expression.right.accept(this);
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
					compareInt(expression.comparison);
					break;
				case SBYTE_COMPARE:
				case SHORT_COMPARE:
				case INT_COMPARE:
				case CHAR_COMPARE:
				case USIZE_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					compareInt(expression.comparison);
					break;
				case UINT_COMPARE:
				case USIZE_COMPARE_UINT:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(INTEGER_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case LONG_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(LONG_COMPARE);
					compareGeneric(expression.comparison);
					break;
				case ULONG_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case ULONG_COMPARE_UINT:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.i2l();
					javaWriter.constant(0xFFFF_FFFFL);
					javaWriter.lAnd();
					javaWriter.invokeStatic(LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case ULONG_COMPARE_USIZE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.i2l();
					javaWriter.invokeStatic(LONG_COMPARE_UNSIGNED);
					compareGeneric(expression.comparison);
					break;
				case FLOAT_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(FLOAT_COMPARE);
					compareGeneric(expression.comparison);
					break;
				case DOUBLE_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeStatic(DOUBLE_COMPARE);
					compareGeneric(expression.comparison);
					break;
				case STRING_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeVirtual(STRING_COMPARETO);
					compareGeneric(expression.comparison);
					break;
				case ENUM_COMPARE:
					expression.left.accept(this);
					expression.right.accept(this);
					javaWriter.invokeVirtual(ENUM_COMPARETO);
					compareGeneric(expression.comparison);
					break;
				default:
					throw new UnsupportedOperationException("Unknown builtin comparator: " + expression.operator.getBuiltin());
			}
		} else {
			if (!checkAndExecuteMethodInfo(expression.operator, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");

			expression.left.accept(this);
			expression.right.accept(this);
			compareGeneric(expression.comparison);
		}

		return null;
	}

	private void compareInt(CompareType comparator) {
		Label exit = new Label();
		Label isTrue = new Label();
		switch (comparator) {
			case EQ:
				javaWriter.ifICmpEQ(isTrue);
				break;
			case NE:
				javaWriter.ifICmpNE(isTrue);
				break;
			case GT:
				javaWriter.ifICmpGT(isTrue);
				break;
			case GE:
				javaWriter.ifICmpGE(isTrue);
				break;
			case LT:
				javaWriter.ifICmpLT(isTrue);
				break;
			case LE:
				javaWriter.ifICmpLE(isTrue);
				break;
			default:
				throw new IllegalStateException("Invalid comparator: " + comparator);
		}
		javaWriter.iConst0();
		javaWriter.goTo(exit);
		javaWriter.label(isTrue);
		javaWriter.iConst1();
		javaWriter.label(exit);
	}

	private void compareGeneric(CompareType comparator) {
		Label exit = new Label();
		Label isTrue = new Label();
		switch (comparator) {
			case EQ:
				javaWriter.ifEQ(isTrue);
				break;
			case NE:
				javaWriter.ifNE(isTrue);
				break;
			case GT:
				javaWriter.ifGT(isTrue);
				break;
			case GE:
				javaWriter.ifGE(isTrue);
				break;
			case LT:
				javaWriter.ifLT(isTrue);
				break;
			case LE:
				javaWriter.ifLE(isTrue);
				break;
			default:
				throw new IllegalStateException("Invalid comparator: " + comparator);
		}
		javaWriter.iConst0();
		javaWriter.goTo(exit);
		javaWriter.label(isTrue);
		javaWriter.iConst1();
		javaWriter.label(exit);
	}

	@Override
	public Void visitCall(CallExpression expression) {
		BuiltinID builtin = expression.member.getBuiltin();
		if (builtin == null) {
			expression.target.accept(this);

			final List<TypeParameter> typeParameters;
			{

				final List<TypeParameter> parameters = new ArrayList<>();
				if(expression.member.getTarget().definition.isExpansion()) {
					parameters.addAll(Arrays.asList(expression.member.getTarget().definition.typeParameters));
				}

				//expression.member.getOwnerType().type.extractTypeParameters(parameters);
				//expression.instancedHeader.typeParameters
				for (TypeParameter typeParameter : expression.member.getTarget().getHeader().typeParameters) {
					if(!parameters.contains(typeParameter)) {
						parameters.add(typeParameter);
					}
				}
				typeParameters = parameters.stream().distinct().collect(Collectors.toList());
			}

			JavaMethod methodInfo = context.getJavaMethod(expression.member);

			if(methodInfo.compile) {
				for (TypeParameter typeParameter : typeParameters) {
					javaWriter.aConstNull(); // TODO: Replace with actual class
					javaWriter.checkCast("java/lang/Class");
				}
			}

			final Expression[] arguments = expression.arguments.arguments;
			final FunctionParameter[] parameters = expression.instancedHeader.parameters;

			final boolean variadic = expression.instancedHeader.isVariadicCall(expression.arguments) && ((arguments.length != parameters.length) || !parameters[parameters.length - 1].type.type
					.equals(arguments[arguments.length - 1].type.type));

			if(variadic) {
				for (int i = 0; i < parameters.length - 1; i++) {
					arguments[i].accept(this);
				}

				final int arrayCount = (arguments.length - parameters.length) + 1;
				javaWriter.constant(arrayCount);
				javaWriter.newArray(context.getType(parameters[parameters.length - 1].type).getElementType());
				for (int i = 0; i < arrayCount; i++) {
					javaWriter.dup();
					javaWriter.constant(i);
					arguments[i + parameters.length - 1].accept(this);
					javaWriter.arrayStore(context.getType(arguments[i].type));
				}


			} else {
 				for (Expression argument : arguments) {
					argument.accept(this);
				}
			}



			if (!checkAndExecuteMethodInfo(expression.member, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");

			if (expression.member.getTarget().header.getReturnType().isGeneric())
				javaWriter.checkCast(context.getInternalName(expression.type));

			return null;
		}

		switch (builtin) {
			case STRING_RANGEGET:
			case ARRAY_INDEXGETRANGE:
			case ARRAY_INDEXGET:
			case ARRAY_CONTAINS:
				break;
			case BYTE_INC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
				}, PushOption.AFTER);
				return null;
			case BYTE_DEC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.constant(0xFF);
					javaWriter.iAnd();
				}, PushOption.AFTER);
				return null;
			case SBYTE_INC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.i2b();
				}, PushOption.AFTER);
				return null;
			case SBYTE_DEC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.i2b();
				}, PushOption.AFTER);
				return null;
			case SHORT_INC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.i2s();
				}, PushOption.AFTER);
				return null;
			case SHORT_DEC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.i2s();
				}, PushOption.AFTER);
				return null;
			case USHORT_INC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iAdd();
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
				}, PushOption.AFTER);
				return null;
			case USHORT_DEC:
				modify(expression.target, () -> {
					javaWriter.iConst1();
					javaWriter.iSub();
					javaWriter.constant(0xFFFF);
					javaWriter.iAnd();
				}, PushOption.AFTER);
				return null;
			case INT_INC:
			case UINT_INC:
			case USIZE_INC:
				if (expression.target instanceof GetLocalVariableExpression) {
					JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.variable);
					javaWriter.iinc(local.local);
					javaWriter.load(local);
				} else {
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
					}, PushOption.AFTER);
				}
				return null;
			case INT_DEC:
			case UINT_DEC:
			case USIZE_DEC:
				if (expression.target instanceof GetLocalVariableExpression) {
					JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.variable);
					javaWriter.iinc(local.local, -1);
					javaWriter.load(local);
				} else {
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
					}, PushOption.AFTER);
				}
				return null;
			case LONG_INC:
			case ULONG_INC:
				modify(expression.target, () -> {
					javaWriter.constant(1l);
					javaWriter.iAdd();
				}, PushOption.AFTER);
				return null;
			case LONG_DEC:
			case ULONG_DEC:
				modify(expression.target, () -> {
					javaWriter.constant(1l);
					javaWriter.iSub();
				}, PushOption.AFTER);
				return null;
			case FLOAT_INC:
				modify(expression.target, () -> {
					javaWriter.constant(1f);
					javaWriter.iAdd();
				}, PushOption.AFTER);
				return null;
			case FLOAT_DEC:
				modify(expression.target, () -> {
					javaWriter.constant(1f);
					javaWriter.iSub();
				}, PushOption.AFTER);
				return null;
			case DOUBLE_INC:
				modify(expression.target, () -> {
					javaWriter.constant(1d);
					javaWriter.iAdd();
				}, PushOption.AFTER);
				return null;
			case DOUBLE_DEC:
				modify(expression.target, () -> {
					javaWriter.constant(1d);
					javaWriter.iSub();
				}, PushOption.AFTER);
				return null;
			case OPTIONAL_IS_NULL:
			case OPTIONAL_IS_NOT_NULL:
				expression.target.accept(this);
				final Label isFalse = new Label();
				final Label end = new Label();

				if (builtin == BuiltinID.OPTIONAL_IS_NULL)
					javaWriter.ifNonNull(isFalse);
				else
					javaWriter.ifNull(isFalse);
				javaWriter.iConst1();
				javaWriter.goTo(end);
				javaWriter.label(isFalse);
				javaWriter.iConst0();
				javaWriter.label(end);
				return null;
			default:
				expression.target.accept(this);
				for (Expression argument : expression.arguments.arguments) {
					argument.accept(this);
				}
		}

		switch (builtin) {
			case BOOL_NOT:
				javaWriter.iConst1();
				javaWriter.iXor();
				break;
			case BOOL_AND:
				javaWriter.iAnd();
				break;
			case BOOL_OR:
				javaWriter.iOr();
				break;
			case BOOL_XOR:
				javaWriter.iXor();
				break;
			case BOOL_EQUALS:
				javaWriter.iXor();
				javaWriter.iConst1();
				javaWriter.iXor();
				break;
			case BOOL_NOTEQUALS:
				javaWriter.iXor();
				break;
			case BYTE_NOT:
			case SBYTE_NOT:
			case SHORT_NOT:
			case USHORT_NOT:
			case INT_NOT:
			case UINT_NOT:
			case USIZE_NOT:
				javaWriter.iNot();
				break;
			case SBYTE_NEG:
			case SHORT_NEG:
			case INT_NEG:
				javaWriter.iNeg();
				break;
			case BYTE_ADD_BYTE:
			case SBYTE_ADD_SBYTE:
			case SHORT_ADD_SHORT:
			case USHORT_ADD_USHORT:
			case INT_ADD_INT:
			case UINT_ADD_UINT:
			case USIZE_ADD_USIZE:
				javaWriter.iAdd();
				break;
			case BYTE_SUB_BYTE:
			case SBYTE_SUB_SBYTE:
			case SHORT_SUB_SHORT:
			case USHORT_SUB_USHORT:
			case INT_SUB_INT:
			case UINT_SUB_UINT:
			case USIZE_SUB_USIZE:
				javaWriter.iSub();
				break;
			case BYTE_MUL_BYTE:
			case SBYTE_MUL_SBYTE:
			case SHORT_MUL_SHORT:
			case USHORT_MUL_USHORT:
			case INT_MUL_INT:
			case UINT_MUL_UINT:
			case USIZE_MUL_USIZE:
				javaWriter.iMul();
				break;
			case SBYTE_DIV_SBYTE:
			case SHORT_DIV_SHORT:
			case INT_DIV_INT:
			case USIZE_DIV_USIZE:
				javaWriter.iDiv();
				break;
			case SBYTE_MOD_SBYTE:
			case SHORT_MOD_SHORT:
			case INT_MOD_INT:
			case USIZE_MOD_USIZE:
				javaWriter.iRem();
				break;
			case BYTE_DIV_BYTE:
			case USHORT_DIV_USHORT:
			case UINT_DIV_UINT:
				javaWriter.invokeStatic(INTEGER_DIVIDE_UNSIGNED);
				break;
			case BYTE_MOD_BYTE:
			case USHORT_MOD_USHORT:
			case UINT_MOD_UINT:
				javaWriter.invokeStatic(INTEGER_REMAINDER_UNSIGNED);
				break;
			case BYTE_AND_BYTE:
			case SBYTE_AND_SBYTE:
			case SHORT_AND_SHORT:
			case USHORT_AND_USHORT:
			case INT_AND_INT:
			case UINT_AND_UINT:
			case USIZE_AND_USIZE:
				javaWriter.iAnd();
				break;
			case BYTE_OR_BYTE:
			case SBYTE_OR_SBYTE:
			case SHORT_OR_SHORT:
			case USHORT_OR_USHORT:
			case INT_OR_INT:
			case UINT_OR_UINT:
			case USIZE_OR_USIZE:
				javaWriter.iOr();
				break;
			case BYTE_XOR_BYTE:
			case SBYTE_XOR_SBYTE:
			case SHORT_XOR_SHORT:
			case USHORT_XOR_USHORT:
			case INT_XOR_INT:
			case UINT_XOR_UINT:
			case USIZE_XOR_USIZE:
				javaWriter.iXor();
				break;
			case BYTE_SHL:
			case SBYTE_SHL:
			case SHORT_SHL:
			case USHORT_SHL:
			case INT_SHL:
			case UINT_SHL:
			case USIZE_SHL:
				javaWriter.iShl();
				break;
			case SBYTE_SHR:
			case SHORT_SHR:
			case INT_SHR:
				javaWriter.iShr();
				break;
			case BYTE_SHR:
			case SBYTE_USHR:
			case USHORT_SHR:
			case SHORT_USHR:
			case INT_USHR:
			case UINT_SHR:
			case USIZE_SHR:
				javaWriter.iUShr();
				break;
			case INT_COUNT_LOW_ZEROES:
			case UINT_COUNT_LOW_ZEROES:
			case USIZE_COUNT_LOW_ZEROES:
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_TRAILING_ZEROS);
				break;
			case INT_COUNT_HIGH_ZEROES:
			case UINT_COUNT_HIGH_ZEROES:
			case USIZE_COUNT_HIGH_ZEROES:
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_LEADING_ZEROS);
				break;
			case INT_COUNT_LOW_ONES:
			case UINT_COUNT_LOW_ONES:
			case USIZE_COUNT_LOW_ONES:
				javaWriter.iNot();
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_TRAILING_ZEROS);
				break;
			case INT_COUNT_HIGH_ONES:
			case UINT_COUNT_HIGH_ONES:
			case USIZE_COUNT_HIGH_ONES:
				javaWriter.iNot();
				javaWriter.invokeStatic(INTEGER_NUMBER_OF_LEADING_ZEROS);
				break;
			case LONG_NOT:
			case ULONG_NOT:
				javaWriter.lNot();
				break;
			case LONG_NEG:
				javaWriter.lNeg();
				break;
			case LONG_ADD_LONG:
			case ULONG_ADD_ULONG:
				javaWriter.lAdd();
				break;
			case LONG_SUB_LONG:
			case ULONG_SUB_ULONG:
				javaWriter.lSub();
				break;
			case LONG_MUL_LONG:
			case ULONG_MUL_ULONG:
				javaWriter.lMul();
				break;
			case LONG_DIV_LONG:
				javaWriter.lDiv();
				break;
			case LONG_MOD_LONG:
				javaWriter.lRem();
				break;
			case LONG_AND_LONG:
			case ULONG_AND_ULONG:
				javaWriter.lAnd();
				break;
			case LONG_OR_LONG:
			case ULONG_OR_ULONG:
				javaWriter.lOr();
				break;
			case LONG_XOR_LONG:
			case ULONG_XOR_ULONG:
				javaWriter.lXor();
				break;
			case LONG_SHL:
			case ULONG_SHL:
				javaWriter.lShl();
				break;
			case LONG_SHR:
				javaWriter.lShr();
				break;
			case LONG_USHR:
			case ULONG_SHR:
				javaWriter.lUShr();
				break;
			case LONG_COUNT_LOW_ZEROES:
			case ULONG_COUNT_LOW_ZEROES:
				javaWriter.invokeStatic(LONG_NUMBER_OF_TRAILING_ZEROS);
				break;
			case LONG_COUNT_HIGH_ZEROES:
			case ULONG_COUNT_HIGH_ZEROES:
				javaWriter.invokeStatic(LONG_NUMBER_OF_LEADING_ZEROS);
				break;
			case LONG_COUNT_LOW_ONES:
			case ULONG_COUNT_LOW_ONES:
				javaWriter.lNot();
				javaWriter.invokeStatic(LONG_NUMBER_OF_TRAILING_ZEROS);
				break;
			case LONG_COUNT_HIGH_ONES:
			case ULONG_COUNT_HIGH_ONES:
				javaWriter.lNot();
				javaWriter.invokeStatic(LONG_NUMBER_OF_LEADING_ZEROS);
				break;
			case ULONG_DIV_ULONG:
				javaWriter.invokeStatic(LONG_DIVIDE_UNSIGNED);
				break;
			case ULONG_MOD_ULONG:
				javaWriter.invokeStatic(LONG_REMAINDER_UNSIGNED);
				break;
			case FLOAT_NEG:
				javaWriter.fNeg();
				break;
			case FLOAT_ADD_FLOAT:
				javaWriter.fAdd();
				break;
			case FLOAT_SUB_FLOAT:
				javaWriter.fSub();
				break;
			case FLOAT_MUL_FLOAT:
				javaWriter.fMul();
				break;
			case FLOAT_DIV_FLOAT:
				javaWriter.fDiv();
				break;
			case FLOAT_MOD_FLOAT:
				javaWriter.fRem();
				break;
			case DOUBLE_NEG:
				javaWriter.dNeg();
				break;
			case DOUBLE_ADD_DOUBLE:
				javaWriter.dAdd();
				break;
			case DOUBLE_SUB_DOUBLE:
				javaWriter.dSub();
				break;
			case DOUBLE_MUL_DOUBLE:
				javaWriter.dMul();
				break;
			case DOUBLE_DIV_DOUBLE:
				javaWriter.dDiv();
				break;
			case DOUBLE_MOD_DOUBLE:
				javaWriter.dRem();
				break;
			case CHAR_ADD_INT:
				javaWriter.iAdd();
				break;
			case CHAR_SUB_INT:
			case CHAR_SUB_CHAR:
				javaWriter.iSub();
				break;
			case CHAR_REMOVE_DIACRITICS:
				throw new UnsupportedOperationException("Not yet supported!");
			case CHAR_TO_LOWER_CASE:
				javaWriter.invokeStatic(CHARACTER_TO_LOWER_CASE);
				break;
			case CHAR_TO_UPPER_CASE:
				javaWriter.invokeStatic(CHARACTER_TO_UPPER_CASE);
				break;
			case STRING_ADD_STRING:
				javaWriter.invokeVirtual(STRING_CONCAT);
				break;
			case STRING_INDEXGET:
				javaWriter.invokeVirtual(STRING_CHAR_AT);
				break;
			case STRING_RANGEGET: {
				expression.target.accept(this);
				Expression argument = expression.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					rangeArgument.from.accept(this);
					rangeArgument.to.accept(this); // TODO: is this string.length ? if so, use the other substring method
				} else {
					argument.accept(this);
					javaWriter.dup();
					final String owner;
					if (argument.type.type instanceof RangeTypeID) {
						owner = context.getInternalName(argument.type);
					} else {
						owner = "zsynthetic/IntRange";
					}
					int tmp = javaWriter.local(Type.getType(owner));
					javaWriter.storeInt(tmp);
					javaWriter.getField(owner, "from", "I");
					javaWriter.loadInt(tmp);
					javaWriter.getField(owner, "to", "I");
				}
				javaWriter.invokeVirtual(STRING_SUBSTRING);
				break;
			}
			case STRING_REMOVE_DIACRITICS:
				throw new UnsupportedOperationException("Not yet supported!");
			case STRING_TRIM:
				javaWriter.invokeVirtual(STRING_TRIM);
				break;
			case STRING_TO_LOWER_CASE:
				javaWriter.invokeVirtual(STRING_TO_LOWER_CASE);
				break;
			case STRING_TO_UPPER_CASE:
				javaWriter.invokeVirtual(STRING_TO_UPPER_CASE);
				break;
			case ASSOC_INDEXGET:
			case ASSOC_GETORDEFAULT: {
				javaWriter.invokeInterface(MAP_GET);

				AssocTypeID type = (AssocTypeID) expression.target.type.type;
				type.valueType.type.accept(type.valueType, unboxingTypeVisitor);
				break;
			}
			case ASSOC_INDEXSET:
				javaWriter.invokeVirtual(MAP_PUT);
				javaWriter.pop();
				break;
			case ASSOC_CONTAINS:
				javaWriter.invokeVirtual(MAP_CONTAINS_KEY);
				break;
			case ASSOC_EQUALS:
				javaWriter.invokeVirtual(OBJECT_EQUALS);
				break;
			case ASSOC_NOTEQUALS:
				javaWriter.invokeVirtual(OBJECT_EQUALS);
				javaWriter.iXorVs1();
				break;
			case ASSOC_SAME:
			case GENERICMAP_SAME:
			case ARRAY_SAME:
			case FUNCTION_SAME:
			case OBJECT_SAME: {
				Label exit = new Label();
				javaWriter.iConst0();
				javaWriter.ifACmpNe(exit);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			case ASSOC_NOTSAME:
			case GENERICMAP_NOTSAME:
			case ARRAY_NOTSAME:
			case FUNCTION_NOTSAME:
			case OBJECT_NOTSAME: {
				Label exit = new Label();
				javaWriter.iConst0();
				javaWriter.ifACmpEq(exit);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			}
			case GENERICMAP_GETOPTIONAL: {
				javaWriter.invokeVirtual(MAP_GET);
				break;
			}
			case GENERICMAP_PUT: {
				//FIXME dirty check for typeOfT
				if (expression.arguments.arguments.length == 1) {
					javaWriter.dup();
					javaWriter.invokeVirtual(JavaMethod.getVirtual(JavaClass.OBJECT, "getClass", "()Ljava/lang/Class;", 0));
					javaWriter.swap();
				}

				javaWriter.invokeVirtual(MAP_PUT);
				javaWriter.pop();
				break;
			}
			case GENERICMAP_CONTAINS:
				javaWriter.invokeVirtual(MAP_CONTAINS_KEY);
				break;
			case GENERICMAP_EQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case GENERICMAP_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");
			case GENERICMAP_ADDALL:
				javaWriter.invokeInterface(MAP_PUT_ALL);
				break;
			case ARRAY_INDEXGET: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type.type;
				expression.target.accept(this);

				final Expression[] arguments = expression.arguments.arguments;
				Type asmType = context.getType(expression.target.type);
				for (Expression argument : arguments) {
					asmType = Type.getType(asmType.getDescriptor().substring(1));
					argument.accept(this);
					javaWriter.arrayLoad(asmType);
				}
				break;
			}
			case ARRAY_INDEXSET: {
				//TODO multi-dim arrays?
				ArrayTypeID type = (ArrayTypeID) expression.target.type.type;
				javaWriter.arrayStore(context.getType(type.elementType));
				break;
			}
			case ARRAY_INDEXGETRANGE: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type.type;

				expression.target.accept(this);
				Expression argument = expression.arguments.arguments[0];
				if (argument instanceof RangeExpression) {
					RangeExpression rangeArgument = (RangeExpression) argument;
					rangeArgument.from.accept(this);
					rangeArgument.to.accept(this);
				} else {
					argument.accept(this);
					javaWriter.dup();
					final String owner;
					if (argument.type.type instanceof RangeTypeID) {
						owner = context.getInternalName(argument.type);
					} else {
						owner = "zsynthetic/IntRange";
					}
					int tmp = javaWriter.local(Type.getType(owner));
					javaWriter.storeInt(tmp);
					javaWriter.getField(owner, "from", "I");
					javaWriter.loadInt(tmp);
					javaWriter.getField(owner, "to", "I");
				}

				if (type.elementType.type instanceof BasicTypeID) {
					switch ((BasicTypeID) type.elementType.type) {
						case BOOL:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_BOOLS);
							break;
						case BYTE:
						case SBYTE:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_BYTES);
							break;
						case SHORT:
						case USHORT:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_SHORTS);
							break;
						case INT:
						case UINT:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_INTS);
							break;
						case LONG:
						case ULONG:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_LONGS);
							break;
						case FLOAT:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_FLOATS);
							break;
						case DOUBLE:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_DOUBLES);
							break;
						case CHAR:
							javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_CHARS);
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_COPY_OF_RANGE_OBJECTS);
					javaWriter.checkCast(context.getInternalName(expression.target.type));
				}
				break;
			}
			case ARRAY_CONTAINS:
				expression.target.accept(this);
				final Label loopStart = new Label();
				final Label loopEnd = new Label();
				final Label isTrue = new Label();
				final Label expressionEnd = new Label();

				final int counterLocation = javaWriter.local(int.class);
				javaWriter.iConst0();
				javaWriter.storeInt(counterLocation);

				javaWriter.label(loopStart);
				javaWriter.dup();
				javaWriter.arrayLength();

				javaWriter.loadInt(counterLocation);

				javaWriter.ifICmpLE(loopEnd);
				javaWriter.dup();
				javaWriter.loadInt(counterLocation);
				final StoredType itemType = expression.arguments.arguments[0].type;
				javaWriter.arrayLoad(context.getType(itemType));
				javaWriter.iinc(counterLocation);
				expression.arguments.arguments[0].accept(this);


				if (CompilerUtils.isPrimitive(itemType.type)) {
					//Compare non-int types beforehand
					if (itemType.type == BasicTypeID.LONG || itemType.type == BasicTypeID.ULONG) {
						javaWriter.lCmp();
						javaWriter.ifNE(loopStart);
					} else if (itemType.type == BasicTypeID.FLOAT) {
						javaWriter.fCmp();
						javaWriter.ifNE(loopStart);
					} else if (itemType.type == BasicTypeID.DOUBLE) {
						javaWriter.dCmp();
						javaWriter.ifNE(loopStart);
					} else
						javaWriter.ifICmpNE(loopStart);
				} else {
					//If equals, use Object.equals in case of null
					javaWriter.invokeStatic(new JavaMethod(JavaClass.fromInternalName("java/util/Objects", JavaClass.Kind.CLASS), JavaMethod.Kind.STATIC, "equals", false, "(Ljava/lang/Object;Ljava/lang/Object;)Z", 0, false));
					javaWriter.ifNE(loopStart);
					// If ==
					// javaWriter.ifACmpNe(loopStart);
				}

				javaWriter.label(isTrue);

				javaWriter.pop();
				javaWriter.iConst1();
				javaWriter.goTo(expressionEnd);

				javaWriter.label(loopEnd);
				javaWriter.pop();
				javaWriter.iConst0();
				javaWriter.label(expressionEnd);

				break;
			case ARRAY_EQUALS:
			case ARRAY_NOTEQUALS: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type.type;
				if (type.elementType.type instanceof BasicTypeID) {
					switch ((BasicTypeID) type.elementType.type) {
						case BOOL:
							javaWriter.invokeStatic(ARRAYS_EQUALS_BOOLS);
							break;
						case BYTE:
						case SBYTE:
							javaWriter.invokeStatic(ARRAYS_EQUALS_BYTES);
							break;
						case SHORT:
						case USHORT:
							javaWriter.invokeStatic(ARRAYS_EQUALS_SHORTS);
							break;
						case INT:
						case UINT:
							javaWriter.invokeStatic(ARRAYS_EQUALS_INTS);
							break;
						case LONG:
						case ULONG:
							javaWriter.invokeStatic(ARRAYS_EQUALS_LONGS);
							break;
						case FLOAT:
							javaWriter.invokeStatic(ARRAYS_EQUALS_FLOATS);
							break;
						case DOUBLE:
							javaWriter.invokeStatic(ARRAYS_EQUALS_DOUBLES);
							break;
						case CHAR:
							javaWriter.invokeStatic(ARRAYS_EQUALS_CHARS);
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_EQUALS_OBJECTS);
				}

				if (builtin == BuiltinID.ARRAY_NOTEQUALS) {
					javaWriter.iConst1();
					javaWriter.iXor();
				}
				break;
			}
			case FUNCTION_CALL:
				javaWriter.invokeInterface(context.getFunctionalInterface(expression.target.type));
				break;
			case AUTOOP_NOTEQUALS:
				throw new UnsupportedOperationException("Not yet supported!");

			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		return null;
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		for (Expression argument : expression.arguments.arguments)
			argument.accept(this);

		BuiltinID builtin = expression.member.getBuiltin();
		if (builtin == null) {
			if (!checkAndExecuteMethodInfo(expression.member, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case BOOL_PARSE:
				javaWriter.invokeStatic(BOOLEAN_PARSE);
				break;
			case BYTE_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case BYTE_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED_WITH_BASE);
				break;
			case SBYTE_PARSE:
				javaWriter.invokeStatic(BYTE_PARSE);
				break;
			case SBYTE_PARSE_WITH_BASE:
				javaWriter.invokeStatic(BYTE_PARSE_WITH_BASE);
				break;
			case SHORT_PARSE:
				javaWriter.invokeStatic(SHORT_PARSE);
				break;
			case SHORT_PARSE_WITH_BASE:
				javaWriter.invokeStatic(SHORT_PARSE_WITH_BASE);
				break;
			case USHORT_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case USHORT_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED_WITH_BASE);
				break;
			case INT_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE);
				break;
			case INT_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_WITH_BASE);
				break;
			case UINT_PARSE:
			case USIZE_PARSE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED);
				break;
			case UINT_PARSE_WITH_BASE:
			case USIZE_PARSE_WITH_BASE:
				javaWriter.invokeStatic(INTEGER_PARSE_UNSIGNED_WITH_BASE);
				break;
			case LONG_PARSE:
				javaWriter.invokeStatic(LONG_PARSE);
				break;
			case LONG_PARSE_WITH_BASE:
				javaWriter.invokeStatic(LONG_PARSE_WITH_BASE);
				break;
			case ULONG_PARSE:
				javaWriter.invokeStatic(LONG_PARSE_UNSIGNED);
				break;
			case ULONG_PARSE_WITH_BASE:
				javaWriter.invokeStatic(LONG_PARSE_UNSIGNED_WITH_BASE);
				break;
			case FLOAT_FROM_BITS:
				javaWriter.invokeStatic(FLOAT_FROM_BITS);
				break;
			case FLOAT_PARSE:
				javaWriter.invokeStatic(FLOAT_PARSE);
				break;
			case DOUBLE_FROM_BITS:
				javaWriter.invokeStatic(DOUBLE_FROM_BITS);
				break;
			case DOUBLE_PARSE:
				javaWriter.invokeStatic(DOUBLE_PARSE);
				break;
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}
		return null;
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedDirect(CapturedDirectExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		return expression.accept(capturedExpressionVisitor);
	}

	@Override
	public Void visitCast(CastExpression expression) {
		expression.target.accept(this);

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>(Arrays.asList(expression.member.member.definition.typeParameters));
		//expression.member.type.type.extractTypeParameters(typeParameters);
		expression.member.toType.type.extractTypeParameters(typeParameters);

		if (expression.member.member.definition.isExpansion()) {
			for (TypeParameter typeParameter : typeParameters) {
				javaWriter.aConstNull(); //Todo: Replace with actual Type
				javaWriter.checkCast("java/lang/Class");
			}
		}

		BuiltinID builtin = expression.member.member.builtin;
		if (builtin == null) {
			if (!checkAndExecuteMethodInfo(expression.member, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case BOOL_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(BOOLEAN_TO_STRING);
                }
				break;
			case BYTE_TO_SBYTE:
				javaWriter.i2b();
				break;
			case BYTE_TO_SHORT:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2s();
				break;
			case BYTE_TO_USHORT:
			case BYTE_TO_INT:
			case BYTE_TO_UINT:
			case BYTE_TO_USIZE:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				break;
			case BYTE_TO_LONG:
			case BYTE_TO_ULONG:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2l();
				break;
			case BYTE_TO_FLOAT:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2f();
				break;
			case BYTE_TO_DOUBLE:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				javaWriter.i2d();
				break;
			case BYTE_TO_CHAR:
				javaWriter.constant(0xFF);
				javaWriter.iAnd();
				break;
			case BYTE_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.constant(0xFF);
                    javaWriter.iAnd();
                    javaWriter.invokeStatic(INTEGER_TO_STRING);
                }
				break;
			case SBYTE_TO_BYTE:
			case SBYTE_TO_SHORT:
			case SBYTE_TO_USHORT:
			case SBYTE_TO_INT:
			case SBYTE_TO_UINT:
			case SBYTE_TO_USIZE:
				break;
			case SBYTE_TO_LONG:
			case SBYTE_TO_ULONG:
				javaWriter.i2l();
				break;
			case SBYTE_TO_FLOAT:
				javaWriter.i2f();
				break;
			case SBYTE_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case SBYTE_TO_CHAR:
				break;
			case SBYTE_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(INTEGER_TO_STRING);
                }
				break;
			case SHORT_TO_BYTE:
				break;
			case SHORT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case SHORT_TO_USHORT:
			case SHORT_TO_INT:
			case SHORT_TO_UINT:
			case SHORT_TO_USIZE:
				break;
			case SHORT_TO_LONG:
			case SHORT_TO_ULONG:
				javaWriter.i2l();
				break;
			case SHORT_TO_FLOAT:
				javaWriter.i2f();
				break;
			case SHORT_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case SHORT_TO_CHAR:
				break;
			case SHORT_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(SHORT_TO_STRING);
                }
				break;
			case USHORT_TO_BYTE:
				break;
			case USHORT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case USHORT_TO_SHORT:
				javaWriter.i2s();
				break;
			case USHORT_TO_INT:
			case USHORT_TO_UINT:
			case USHORT_TO_USIZE:
				javaWriter.constant(0xFFFF);
				javaWriter.iAnd();
				break;
			case USHORT_TO_LONG:
			case USHORT_TO_ULONG:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				break;
			case USHORT_TO_FLOAT:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				javaWriter.i2f();
				break;
			case USHORT_TO_DOUBLE:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				javaWriter.i2d();
				break;
			case USHORT_TO_CHAR:
				javaWriter.constant(0xFFFFL);
				javaWriter.iAnd();
				break;
			case USHORT_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.constant(0xFFFFL);
                    javaWriter.iAnd();
                    javaWriter.invokeStatic(INTEGER_TO_STRING);
                }
                break;
			case INT_TO_BYTE:
			case USIZE_TO_BYTE:
				break;
			case INT_TO_SBYTE:
			case USIZE_TO_SBYTE:
				javaWriter.i2b();
				break;
			case INT_TO_SHORT:
			case USIZE_TO_SHORT:
				javaWriter.i2s();
				break;
			case INT_TO_USHORT:
			case USIZE_TO_USHORT:
				break;
			case INT_TO_UINT:
			case USIZE_TO_INT:
			case USIZE_TO_UINT:
			case INT_TO_USIZE:
				break;
			case INT_TO_LONG:
			case INT_TO_ULONG:
			case USIZE_TO_LONG:
			case USIZE_TO_ULONG:
				javaWriter.i2l();
				break;
			case INT_TO_FLOAT:
			case USIZE_TO_FLOAT:
				javaWriter.i2f();
				break;
			case INT_TO_DOUBLE:
			case USIZE_TO_DOUBLE:
				javaWriter.i2d();
				break;
			case INT_TO_CHAR:
			case USIZE_TO_CHAR:
				javaWriter.i2s();
				break;
			case INT_TO_STRING:
			case USIZE_TO_STRING:
			    if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(INTEGER_TO_STRING);
                }
				break;
			case UINT_TO_BYTE:
				break;
			case UINT_TO_SBYTE:
				javaWriter.i2b();
				break;
			case UINT_TO_SHORT:
				javaWriter.i2s();
				break;
			case UINT_TO_USHORT:
			case UINT_TO_INT:
			case UINT_TO_USIZE:
				break;
			case UINT_TO_LONG:
				javaWriter.i2l();
				break;
			case UINT_TO_ULONG:
				javaWriter.i2l();
				javaWriter.constant(0xFFFFFFFFL);
				javaWriter.lAnd();
				break;
			case UINT_TO_FLOAT:
				javaWriter.i2l();
				javaWriter.constant(0xFFFFFFFFL);
				javaWriter.lAnd();
				javaWriter.l2f();
				break;
			case UINT_TO_DOUBLE:
				javaWriter.i2l();
				javaWriter.constant(0xFFFFFFFFL);
				javaWriter.lAnd();
				javaWriter.l2d();
				break;
			case UINT_TO_CHAR:
				javaWriter.i2s();
				break;
			case UINT_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(INTEGER_TO_UNSIGNED_STRING);
                }
				break;
			case LONG_TO_BYTE:
				javaWriter.l2i();
				break;
			case LONG_TO_SBYTE:
				javaWriter.l2i();
				javaWriter.i2b();
				break;
			case LONG_TO_SHORT:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case LONG_TO_USHORT:
			case LONG_TO_INT:
			case LONG_TO_UINT:
			case LONG_TO_USIZE:
				javaWriter.l2i();
				break;
			case LONG_TO_ULONG:
				break;
			case LONG_TO_FLOAT:
				javaWriter.l2f();
				break;
			case LONG_TO_DOUBLE:
				javaWriter.l2d();
				break;
			case LONG_TO_CHAR:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case LONG_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(LONG_TO_STRING);
                }
				break;
			case ULONG_TO_BYTE:
				javaWriter.l2i();
				break;
			case ULONG_TO_SBYTE:
				javaWriter.l2i();
				javaWriter.i2b();
				break;
			case ULONG_TO_SHORT:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case ULONG_TO_USHORT:
			case ULONG_TO_INT:
			case ULONG_TO_UINT:
			case ULONG_TO_USIZE:
				javaWriter.l2i();
				break;
			case ULONG_TO_LONG:
				break;
			case ULONG_TO_FLOAT:
				javaWriter.l2f(); // TODO: this is incorrect
				break;
			case ULONG_TO_DOUBLE:
				javaWriter.l2d(); // TODO: this is incorrect
			case ULONG_TO_CHAR:
				javaWriter.l2i();
				javaWriter.i2s();
				break;
			case ULONG_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(LONG_TO_UNSIGNED_STRING);
                }
				break;
			case FLOAT_TO_BYTE:
				javaWriter.f2i();
				break;
			case FLOAT_TO_SBYTE:
				javaWriter.f2i();
				javaWriter.i2b();
				break;
			case FLOAT_TO_SHORT:
				javaWriter.f2i();
				javaWriter.i2s();
				break;
			case FLOAT_TO_USHORT:
			case FLOAT_TO_UINT:
			case FLOAT_TO_INT:
			case FLOAT_TO_USIZE:
				javaWriter.f2i();
				break;
			case FLOAT_TO_LONG:
			case FLOAT_TO_ULONG:
				javaWriter.f2l();
				break;
			case FLOAT_TO_DOUBLE:
				javaWriter.f2d();
				break;
			case FLOAT_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(FLOAT_TO_STRING);
                }
				break;
			case DOUBLE_TO_BYTE:
				javaWriter.d2i();
				break;
			case DOUBLE_TO_SBYTE:
				javaWriter.d2i();
				javaWriter.i2b();
				break;
			case DOUBLE_TO_SHORT:
				javaWriter.d2i();
				javaWriter.i2s();
				break;
			case DOUBLE_TO_USHORT:
			case DOUBLE_TO_INT:
			case DOUBLE_TO_UINT:
			case DOUBLE_TO_USIZE:
				javaWriter.d2i();
				break;
			case DOUBLE_TO_LONG:
			case DOUBLE_TO_ULONG:
				javaWriter.d2l();
				break;
			case DOUBLE_TO_FLOAT:
				javaWriter.d2f();
				break;
			case DOUBLE_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(DOUBLE_TO_STRING);
                }
				break;
			case CHAR_TO_BYTE:
				break;
			case CHAR_TO_SBYTE:
				javaWriter.i2s();
				break;
			case CHAR_TO_SHORT:
			case CHAR_TO_USHORT:
			case CHAR_TO_INT:
			case CHAR_TO_UINT:
			case CHAR_TO_USIZE:
				break;
			case CHAR_TO_LONG:
			case CHAR_TO_ULONG:
				javaWriter.i2l();
				break;
			case CHAR_TO_STRING:
                if(expression.target.type.isOptional()) {
                    javaWriter.invokeStatic(OBJECTS_TOSTRING);
                } else {
                    javaWriter.invokeStatic(CHARACTER_TO_STRING);
                }
				break;
			case ENUM_TO_STRING:
				javaWriter.invokeVirtual(ENUM_NAME);
				break;
			default:
				throw new UnsupportedOperationException("Unknown builtin cast: " + builtin);
		}

		return null;
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		final Label end = new Label();
		expression.value.accept(this);
		javaWriter.dup();
		javaWriter.ifNonNull(end);
		javaWriter.pop();
		javaWriter.newObject("java/lang/NullPointerException");
		javaWriter.dup();
		javaWriter.constant("Tried to convert a null value to nonnull type " + context.getType(expression.type).getClassName());
		javaWriter.invokeSpecial(NullPointerException.class, "<init>", "(Ljava/lang/String;)V");
		javaWriter.aThrow();
		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		final Label end = new Label();
		expression.left.accept(this);
		javaWriter.dup();
		javaWriter.ifNonNull(end);
		javaWriter.pop();
		expression.right.accept(this);
		expression.right.type.type.accept(expression.right.type, boxingTypeVisitor);
		javaWriter.label(end);
		expression.type.type.accept(expression.type, unboxingTypeVisitor);
		return null;
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		final Label end = new Label();
		final Label onElse = new Label();
		expression.condition.accept(this);
		javaWriter.ifEQ(onElse);
		expression.ifThen.accept(this);
		javaWriter.goTo(end);
		javaWriter.label(onElse);
		expression.ifElse.accept(this);
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitConst(ConstExpression expression) {
		BuiltinID builtin = expression.constant.member.builtin;
		if (builtin == null) {
			javaWriter.getStaticField(context.getJavaField(expression.constant));
			return null;
		}

		switch (builtin) {
			case BYTE_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case BYTE_GET_MAX_VALUE:
				javaWriter.constant(0xFF);
				break;
			case SBYTE_GET_MIN_VALUE:
				javaWriter.getStaticField(BYTE_MIN_VALUE);
				break;
			case SBYTE_GET_MAX_VALUE:
				javaWriter.getStaticField(BYTE_MAX_VALUE);
				break;
			case SHORT_GET_MIN_VALUE:
				javaWriter.getStaticField(SHORT_MIN_VALUE);
				break;
			case SHORT_GET_MAX_VALUE:
				javaWriter.getStaticField(SHORT_MAX_VALUE);
				break;
			case USHORT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USHORT_GET_MAX_VALUE:
				javaWriter.constant(0xFFFF);
				break;
			case INT_GET_MIN_VALUE:
				javaWriter.getStaticField(INTEGER_MIN_VALUE);
				break;
			case INT_GET_MAX_VALUE:
				javaWriter.getStaticField(INTEGER_MAX_VALUE);
				break;
			case UINT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case UINT_GET_MAX_VALUE:
				javaWriter.constant(-1);
				break;
			case LONG_GET_MIN_VALUE:
				javaWriter.getStaticField(LONG_MIN_VALUE);
				break;
			case LONG_GET_MAX_VALUE:
				javaWriter.getStaticField(LONG_MAX_VALUE);
				break;
			case ULONG_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case ULONG_GET_MAX_VALUE:
				javaWriter.constant(-1L);
				break;
			case USIZE_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USIZE_GET_MAX_VALUE:
				javaWriter.getStaticField(INTEGER_MAX_VALUE);
				break;
			case USIZE_BITS:
				javaWriter.constant(32);
				break;
			case FLOAT_GET_MIN_VALUE:
				javaWriter.getStaticField(FLOAT_MIN_VALUE);
				break;
			case FLOAT_GET_MAX_VALUE:
				javaWriter.getStaticField(FLOAT_MAX_VALUE);
				break;
			case DOUBLE_GET_MIN_VALUE:
				javaWriter.getStaticField(DOUBLE_MIN_VALUE);
				break;
			case DOUBLE_GET_MAX_VALUE:
				javaWriter.getStaticField(DOUBLE_MAX_VALUE);
				break;
			case CHAR_GET_MIN_VALUE:
				javaWriter.getStaticField(CHARACTER_MIN_VALUE);
				break;
			case CHAR_GET_MAX_VALUE:
				javaWriter.getStaticField(CHARACTER_MAX_VALUE);
				break;
			case ENUM_VALUES: {
				DefinitionTypeID type = (DefinitionTypeID) expression.type.type;
				JavaClass cls = context.getJavaClass(type.definition);
				javaWriter.invokeStatic(JavaMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		return null;
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		if (expression.value)
			javaWriter.iConst1();
		else
			javaWriter.iConst0();
		return null;
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		getJavaWriter().siPush(expression.value);
		return null;
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		getJavaWriter().constant(expression.value);
		return null;
	}

	@Override
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
		getJavaWriter().constant((int) expression.value);
		return null;
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		throw new UnsupportedOperationException("Invalid usage");
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		throw new UnsupportedOperationException("Invalid usage");
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		javaWriter.getStaticField(context.getInternalName(expression.type), expression.value.name, context.getDescriptor(expression.type));
		return null;
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		CompilerUtils.tagMethodParameters(context, module, expression.header, false, Collections.emptyList());

        /*if (expression.header.parameters.length == 0 && expression.body instanceof ReturnStatement && expression.body.hasTag(MatchExpression.class) && expression.closure.captures.isEmpty()) {
            ((ReturnStatement) expression.body).value.accept(this);
            return null;
        }*/

		final String signature;
		final String[] interfaces;
		final String className = context.getLambdaCounter();
		final String descriptor;
		
		{//Fill the info above
			final StorageTag actualStorage = expression.type.getActualStorage();
			if (actualStorage instanceof JavaFunctionalInterfaceStorageTag) {
				//Let's implement the functional Interface instead
				final Method functionalInterfaceMethod = ((JavaFunctionalInterfaceStorageTag) actualStorage).functionalInterfaceMethod;
				
				//Should be the same, should it not?
				signature = context.getMethodSignature(expression.header, true);
				descriptor = context.getMethodDescriptor(expression.header);
				interfaces = new String[]{Type.getInternalName(functionalInterfaceMethod.getDeclaringClass())};
			} else {
				//Normal way, no casting to functional interface
				signature = context.getMethodSignature(expression.header, true);
				descriptor = context.getMethodDescriptor(expression.header);
				interfaces = new String[]{context.getInternalName(new FunctionTypeID(null, expression.header).stored(UniqueStorageTag.INSTANCE))};
			}
		}
		
		
		final JavaMethod methodInfo;
		{
			final JavaMethod m = context.getFunctionalInterface(expression.type);
			methodInfo = new JavaMethod(m.cls, m.kind, m.name, m.compile, m.descriptor, m.modifiers & ~JavaModifiers.ABSTRACT, m.genericResult, m.typeParameterArguments);
		}
		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", interfaces);
		final JavaWriter functionWriter;
		
		//Bridge method!!!
		if (!Objects.equals(methodInfo.descriptor, descriptor)) {
			final JavaMethod bridgeMethodInfo = new JavaMethod(methodInfo.cls, methodInfo.kind, methodInfo.name, methodInfo.compile, methodInfo.descriptor, methodInfo.modifiers | JavaModifiers.BRIDGE | JavaModifiers.SYNTHETIC, methodInfo.genericResult, methodInfo.typeParameterArguments);
			final JavaWriter bridgeWriter = new JavaWriter(expression.position, lambdaCW, bridgeMethodInfo, null, methodInfo.descriptor, null, "java/lang/Override");
			bridgeWriter.start();
			
			//This.name(parameters, casted)
			bridgeWriter.loadObject(0);
			
			for (int i = 0; i < expression.header.parameters.length; i++) {
				final FunctionParameter functionParameter = expression.header.parameters[i];
				final Type type = context.getType(functionParameter.type);
				bridgeWriter.load(type, i + 1);
				if (!CompilerUtils.isPrimitive(functionParameter.type.type)) {
					bridgeWriter.checkCast(type);
				}
			}
			
			bridgeWriter.invokeVirtual(new JavaMethod(JavaClass.fromInternalName(className, JavaClass.Kind.CLASS), JavaMethod.Kind.INSTANCE, methodInfo.name, methodInfo.compile, descriptor, methodInfo.modifiers, methodInfo.genericResult));
			if(expression.header.getReturnType().type != BasicTypeID.VOID) {
				bridgeWriter.returnType(context.getType(expression.header.getReturnType()));
			}
			
			bridgeWriter.ret();
			bridgeWriter.end();
			
			
			
			final JavaMethod actualMethod = new JavaMethod(methodInfo.cls, methodInfo.kind, methodInfo.name, methodInfo.compile, context.getMethodDescriptor(expression.header), methodInfo.modifiers, methodInfo.genericResult, methodInfo.typeParameterArguments);
			//No @Override
			functionWriter = new JavaWriter(expression.position, lambdaCW, actualMethod, null, signature, null);
		} else {
			functionWriter = new JavaWriter(expression.position, lambdaCW, methodInfo, null, signature, null, "java/lang/Override");
		}

		javaWriter.newObject(className);
		javaWriter.dup();

		final String constructorDesc = calcFunctionSignature(expression.closure);


		final JavaWriter constructorWriter = new JavaWriter(expression.position, lambdaCW, JavaMethod.getConstructor(javaWriter.method.cls, constructorDesc, Opcodes.ACC_PUBLIC), null, null, null);
		constructorWriter.start();
		constructorWriter.loadObject(0);
		constructorWriter.dup();
		constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

		int i = 0;
		for (CapturedExpression capture : expression.closure.captures) {
			constructorWriter.dup();
			Type type = context.getType(capture.type);
			lambdaCW.visitField(Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE, "captured" + ++i, type.getDescriptor(), null, null).visitEnd();

			capture.accept(this);

			constructorWriter.load(type, i);
			constructorWriter.putField(className, "captured" + i, type.getDescriptor());
		}

		constructorWriter.pop();

		javaWriter.invokeSpecial(className, "<init>", constructorDesc);

		constructorWriter.ret();
		constructorWriter.end();


		functionWriter.start();


		final JavaStatementVisitor CSV = new JavaStatementVisitor(context, new JavaExpressionVisitor(context, module, functionWriter) {
			@Override
			public Void visitGetLocalVariable(GetLocalVariableExpression varExpression) {
				final JavaLocalVariableInfo localVariable = functionWriter.tryGetLocalVariable(varExpression.variable.variable);
				if(localVariable != null) {
					final Label label = new Label();
					localVariable.end = label;
					functionWriter.label(label);
					functionWriter.load(localVariable);
					return null;
				}



				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(className, "captured" + position, context.getDescriptor(varExpression.variable.type));
				return null;
			}

			@Override
			public Void visitCapturedParameter(CapturedParameterExpression varExpression) {
				final int position = calculateMemberPosition(varExpression, expression);
				functionWriter.loadObject(0);
				functionWriter.getField(className, "captured" + position, context.getDescriptor(varExpression.parameter.type));
				return null;
			}
		});

		expression.body.accept(CSV);

		functionWriter.ret();
		functionWriter.end();
		lambdaCW.visitEnd();

		context.register(className, lambdaCW.toByteArray());

		return null;
	}

	//TODO replace with visitor?
	private static int calculateMemberPosition(GetLocalVariableExpression localVariableExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) capture).variable == localVariableExpression.variable)
				return h;
			if (capture instanceof CapturedClosureExpression && ((CapturedClosureExpression) capture).value instanceof CapturedLocalVariableExpression && ((CapturedLocalVariableExpression) ((CapturedClosureExpression) capture).value).variable == localVariableExpression.variable)
				return h;
			h++;
		}
		throw new RuntimeException(localVariableExpression.position.toString() + ": Captured Statement error");
	}

	private static int calculateMemberPosition(CapturedParameterExpression functionParameterExpression, FunctionExpression expression) {
		int h = 1;//expression.header.parameters.length;
		for (CapturedExpression capture : expression.closure.captures) {
			if (capture instanceof CapturedParameterExpression && ((CapturedParameterExpression) capture).parameter == functionParameterExpression.parameter)
				return h;
			h++;
		}
		throw new RuntimeException(functionParameterExpression.position.toString() + ": Captured Statement error");
	}

	private String calcFunctionSignature(LambdaClosure closure) {
		StringJoiner joiner = new StringJoiner("", "(", ")V");
		for (CapturedExpression capture : closure.captures) {
			String descriptor = context.getDescriptor(capture.type);
			joiner.add(descriptor);
		}
		return joiner.toString();
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		expression.target.accept(this);
		getField(expression.field);
		if(!CompilerUtils.isPrimitive(expression.field.member.getType().type)) {
			javaWriter.checkCast(context.getType(expression.field.getType()));
		}
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);

		if (parameter == null)
			throw new RuntimeException(expression.position.toString() + ": Could not resolve lambda parameter" + expression.parameter);

		javaWriter.load(context.getType(expression.parameter.type), parameter.index);
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		final Label label = new Label();
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.variable);

		tag.end = label;
		javaWriter.load(tag.type, tag.local);
		javaWriter.label(label);
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		javaWriter.loadObject(0);
		final StoredType type = expression.value.option.getParameterType(expression.index);
		final JavaVariantOption tag = context.getJavaVariantOption(expression.value.option);
		javaWriter.checkCast(tag.variantOptionClass.internalName);
		javaWriter.getField(new JavaField(tag.variantOptionClass, "field" + expression.index, context.getDescriptor(type)));
		return null;
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		javaWriter.getStaticField(context.getJavaField(expression.field));
		return null;
	}

	@Override
	public Void visitGetter(GetterExpression expression) {
		expression.target.accept(this);

		BuiltinID builtin = expression.getter.member.builtin;
		if (builtin == null) {
			if (context.hasJavaField(expression.getter)) {
				javaWriter.getField(context.getJavaField(expression.getter));
				if(!CompilerUtils.isPrimitive(expression.getter.member.getType().type)) {
					javaWriter.checkCast(context.getType(expression.getter.getType()));
				}
				return null;
			}


			final List<TypeParameter> typeParameters = new ArrayList<>();
			expression.getter.member.getType().type.extractTypeParameters(typeParameters);

			if(expression.getter.member.definition.isExpansion()) {
				for (TypeParameter typeParameter : typeParameters) {
					javaWriter.aConstNull(); //TODO: Replace with actual type
					javaWriter.checkCast("java/lang/Class");
				}
			}

			if (!checkAndExecuteMethodInfo(expression.getter, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");
			if(!CompilerUtils.isPrimitive(expression.getter.member.getType().type)) {
				javaWriter.checkCast(context.getType(expression.getter.getType()));
			}
			return null;
		}

		switch (builtin) {
			case INT_HIGHEST_ONE_BIT:
			case UINT_HIGHEST_ONE_BIT:
			case USIZE_HIGHEST_ONE_BIT:
				javaWriter.invokeStatic(INTEGER_HIGHEST_ONE_BIT);
				break;
			case INT_LOWEST_ONE_BIT:
			case UINT_LOWEST_ONE_BIT:
			case USIZE_LOWEST_ONE_BIT:
				javaWriter.invokeStatic(INTEGER_LOWEST_ONE_BIT);
				break;
			case INT_HIGHEST_ZERO_BIT:
			case UINT_HIGHEST_ZERO_BIT:
			case USIZE_HIGHEST_ZERO_BIT:
				javaWriter.iNeg();
				javaWriter.invokeStatic(INTEGER_HIGHEST_ONE_BIT);
				break;
			case INT_LOWEST_ZERO_BIT:
			case UINT_LOWEST_ZERO_BIT:
			case USIZE_LOWEST_ZERO_BIT:
				javaWriter.iNeg();
				javaWriter.invokeStatic(INTEGER_LOWEST_ONE_BIT);
				break;
			case INT_BIT_COUNT:
			case UINT_BIT_COUNT:
			case USIZE_BIT_COUNT:
				javaWriter.invokeStatic(INTEGER_BIT_COUNT);
				break;
			case LONG_HIGHEST_ONE_BIT:
			case ULONG_HIGHEST_ONE_BIT:
				javaWriter.invokeStatic(LONG_HIGHEST_ONE_BIT);
				break;
			case LONG_LOWEST_ONE_BIT:
			case ULONG_LOWEST_ONE_BIT:
				javaWriter.invokeStatic(LONG_LOWEST_ONE_BIT);
				break;
			case LONG_HIGHEST_ZERO_BIT:
			case ULONG_HIGHEST_ZERO_BIT:
				javaWriter.lNeg();
				javaWriter.invokeStatic(LONG_HIGHEST_ONE_BIT);
				break;
			case LONG_LOWEST_ZERO_BIT:
			case ULONG_LOWEST_ZERO_BIT:
				javaWriter.lNeg();
				javaWriter.invokeStatic(LONG_LOWEST_ONE_BIT);
				break;
			case LONG_BIT_COUNT:
			case ULONG_BIT_COUNT:
				javaWriter.invokeStatic(LONG_BIT_COUNT);
				break;
			case FLOAT_BITS:
				javaWriter.invokeStatic(FLOAT_BITS);
				break;
			case DOUBLE_BITS:
				javaWriter.invokeStatic(DOUBLE_BITS);
				break;
			case STRING_LENGTH:
				javaWriter.invokeVirtual(STRING_LENGTH);
				break;
			case STRING_CHARACTERS:
				javaWriter.invokeVirtual(STRING_CHARACTERS);
				break;
			case STRING_ISEMPTY:
				javaWriter.invokeVirtual(STRING_ISEMPTY);
				break;
			case ASSOC_SIZE:
				javaWriter.invokeVirtual(MAP_SIZE);
				break;
			case ASSOC_ISEMPTY:
				javaWriter.invokeVirtual(MAP_ISEMPTY);
				break;
			case ASSOC_KEYS: {
				Type resultType = context.getType(expression.type);

				javaWriter.invokeVirtual(MAP_KEYS);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ASSOC_VALUES: {
				Type resultType = context.getType(expression.type);

				javaWriter.invokeVirtual(MAP_VALUES);
				javaWriter.dup();
				javaWriter.invokeVirtual(COLLECTION_SIZE);
				javaWriter.newArray(resultType);
				javaWriter.invokeVirtual(COLLECTION_TOARRAY);
				javaWriter.checkCast(resultType);
				break;
			}
			case ASSOC_HASHCODE:
				// TODO: we need a content-based hashcode
				javaWriter.invokeVirtual(OBJECT_HASHCODE);
				break;
			case GENERICMAP_HASHCODE:
				// TODO: we need a content-based hashcode
				javaWriter.invokeVirtual(OBJECT_HASHCODE);
				break;
			case ARRAY_LENGTH:
				javaWriter.arrayLength();
				break;
			case ARRAY_HASHCODE: {
				ArrayTypeID type = (ArrayTypeID) expression.target.type.type;
				if (type.elementType.type instanceof BasicTypeID) {
					switch ((BasicTypeID) type.elementType.type) {
						case BOOL:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_BOOLS);
							break;
						case BYTE:
						case SBYTE:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_BYTES);
							break;
						case SHORT:
						case USHORT:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_SHORTS);
							break;
						case INT:
						case UINT:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_INTS);
							break;
						case LONG:
						case ULONG:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_LONGS);
							break;
						case FLOAT:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_FLOATS);
							break;
						case DOUBLE:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_DOUBLES);
							break;
						case CHAR:
							javaWriter.invokeStatic(ARRAYS_HASHCODE_CHARS);
							break;
						default:
							throw new IllegalArgumentException("Unknown basic type: " + type.elementType);
					}
				} else {
					javaWriter.invokeStatic(ARRAYS_DEEPHASHCODE);
				}
				break;
			}
			case ARRAY_ISEMPTY:
				Label isTrue = new Label();
				Label exit = new Label();

				javaWriter.arrayLength();
				javaWriter.ifEQ(isTrue);
				javaWriter.iConst0();
				javaWriter.goTo(exit);
				javaWriter.label(isTrue);
				javaWriter.iConst1();
				javaWriter.label(exit);
				break;
			case ENUM_NAME:
				javaWriter.invokeVirtual(ENUM_NAME);
				break;
			case ENUM_ORDINAL:
				javaWriter.invokeVirtual(ENUM_ORDINAL);
				break;
			case OBJECT_HASHCODE:
				javaWriter.invokeVirtual(OBJECT_HASHCODE);
				break;
			case RANGE_FROM: {
				RangeTypeID type = (RangeTypeID) expression.target.type.type;
				Type jType = context.getType(expression.target.type);
				javaWriter.getField(jType.getInternalName(), "from", context.getDescriptor(type.baseType));
				break;
			}
			case RANGE_TO:
				RangeTypeID type = (RangeTypeID) expression.target.type.type;
				Type jType = context.getType(expression.target.type);
				javaWriter.getField(jType.getInternalName(), "to", context.getDescriptor(type.baseType));
				break;
		}

		return null;
	}

	@Override
	public Void visitGlobal(GlobalExpression expression) {
		return expression.resolution.accept(this);
	}

	@Override
	public Void visitGlobalCall(GlobalCallExpression expression) {
		return expression.resolution.accept(this);
	}

	@Override
	public Void visitInterfaceCast(InterfaceCastExpression expression) {
		expression.value.accept(this);
		javaWriter.checkCast(context.getInternalName(expression.type));
		return null;
	}

	@Override
	public Void visitIs(IsExpression expression) {
		expression.value.accept(this);
		javaWriter.instanceOf(context.getInternalName(expression.isType.stored(BorrowStorageTag.INVOCATION)));
		return null;
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		return null;
	}

	@Override
	public Void visitMap(MapExpression expression) {
		javaWriter.newObject("java/util/HashMap");
		javaWriter.dup();
		javaWriter.invokeSpecial("java/util/HashMap", "<init>", "()V");
        final AssocTypeID type = (AssocTypeID) expression.type.type;
		for (int i = 0; i < expression.keys.length; i++) {
			javaWriter.dup();
			expression.keys[i].accept(this);
            type.keyType.type.accept(type.keyType, boxingTypeVisitor);
            expression.values[i].accept(this);
            type.valueType.type.accept(type.valueType, boxingTypeVisitor);
            javaWriter.invokeInterface(MAP_PUT);
			javaWriter.pop();
		}
		return null;
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		final Label start = new Label();
		final Label end = new Label();

		javaWriter.label(start);
		expression.value.accept(this);

		//TODO replace beforeSwitch visitor or similar
		if (expression.value.type.type instanceof StringTypeID)
			javaWriter.invokeVirtual(OBJECT_HASHCODE);

		//TODO replace with beforeSwitch visitor or similar
		for (MatchExpression.Case aCase : expression.cases) {
			if (aCase.key instanceof VariantOptionSwitchValue) {
				VariantOptionSwitchValue variantOptionSwitchValue = (VariantOptionSwitchValue) aCase.key;
				JavaVariantOption option = context.getJavaVariantOption(variantOptionSwitchValue.option);
				javaWriter.invokeVirtual(JavaMethod.getNativeVirtual(option.variantClass, "getDenominator", "()I"));
				break;
			}
		}

		final boolean hasNoDefault = hasNoDefault(expression);

		final MatchExpression.Case[] cases = expression.cases;
		final JavaSwitchLabel[] switchLabels = new JavaSwitchLabel[hasNoDefault ? cases.length : cases.length - 1];
		final Label defaultLabel = new Label();

		int i = 0;
		for (final MatchExpression.Case matchCase : cases) {
			if (matchCase.key != null) {
				switchLabels[i++] = new JavaSwitchLabel(CompilerUtils.getKeyForSwitch(matchCase.key), new Label());
			}
		}

		JavaSwitchLabel[] sortedSwitchLabels = Arrays.copyOf(switchLabels, switchLabels.length);
		Arrays.sort(sortedSwitchLabels, Comparator.comparingInt(a -> a.key));

		javaWriter.lookupSwitch(defaultLabel, sortedSwitchLabels);

		i = 0;
		for (final MatchExpression.Case switchCase : cases) {
			if (hasNoDefault || switchCase.key != null) {
				javaWriter.label(switchLabels[i++].label);
			} else {
				javaWriter.label(defaultLabel);
			}
			//switchCase.value.body.setTag(MatchExpression.class, expression);
			switchCase.value.accept(this);
			javaWriter.goTo(end);
		}

		if (hasNoDefault) {
			javaWriter.label(defaultLabel);
			if (context.getType(expression.type).getOpcode(Opcodes.ISTORE) == Opcodes.ASTORE)
				javaWriter.aConstNull();
			else
				javaWriter.iConst0();
		}

		javaWriter.label(end);
		return null;
	}

	private static boolean hasNoDefault(MatchExpression switchStatement) {
		for (MatchExpression.Case switchCase : switchStatement.cases)
			if (switchCase.key == null) return false;
		return true;
	}

	@Override
	public Void visitNew(NewExpression expression) {
		if (expression.constructor.getBuiltin() != null) {
			visitBuiltinConstructor(expression);
			return null;
		}

		JavaMethod method = context.getJavaMethod(expression.constructor);
		javaWriter.newObject(method.cls);
		javaWriter.dup();


		if(!expression.constructor.getTarget().hasTag(NativeTag.class)) {
			for (StoredType typeArgument : expression.type.asDefinition().typeArguments) {
				javaWriter.aConstNull();
				javaWriter.checkCast("java/lang/Class");
			}
		}


		for (Expression argument : expression.arguments.arguments) {
			argument.accept(this);
		}

		javaWriter.invokeSpecial(method);
		return null;
	}

	private void visitBuiltinConstructor(NewExpression expression) {
		final BuiltinID builtin = expression.constructor.getBuiltin();
		switch (builtin) {
			case BOOL_NOT:
				break;
			case BOOL_AND:
				break;
			case BOOL_OR:
				break;
			case BOOL_XOR:
				break;
			case BOOL_EQUALS:
				break;
			case BOOL_NOTEQUALS:
				break;
			case BOOL_TO_STRING:
				break;
			case BOOL_PARSE:
				break;
			case BYTE_NOT:
				break;
			case BYTE_INC:
				break;
			case BYTE_DEC:
				break;
			case BYTE_ADD_BYTE:
				break;
			case BYTE_SUB_BYTE:
				break;
			case BYTE_MUL_BYTE:
				break;
			case BYTE_DIV_BYTE:
				break;
			case BYTE_MOD_BYTE:
				break;
			case BYTE_AND_BYTE:
				break;
			case BYTE_OR_BYTE:
				break;
			case BYTE_XOR_BYTE:
				break;
			case BYTE_SHL:
				break;
			case BYTE_SHR:
				break;
			case BYTE_COMPARE:
				break;
			case BYTE_TO_SBYTE:
				break;
			case BYTE_TO_SHORT:
				break;
			case BYTE_TO_USHORT:
				break;
			case BYTE_TO_INT:
				break;
			case BYTE_TO_UINT:
				break;
			case BYTE_TO_LONG:
				break;
			case BYTE_TO_ULONG:
				break;
			case BYTE_TO_USIZE:
				break;
			case BYTE_TO_FLOAT:
				break;
			case BYTE_TO_DOUBLE:
				break;
			case BYTE_TO_CHAR:
				break;
			case BYTE_TO_STRING:
				break;
			case BYTE_PARSE:
				break;
			case BYTE_PARSE_WITH_BASE:
				break;
			case BYTE_GET_MIN_VALUE:
				break;
			case BYTE_GET_MAX_VALUE:
				break;
			case SBYTE_NOT:
				break;
			case SBYTE_NEG:
				break;
			case SBYTE_INC:
				break;
			case SBYTE_DEC:
				break;
			case SBYTE_ADD_SBYTE:
				break;
			case SBYTE_SUB_SBYTE:
				break;
			case SBYTE_MUL_SBYTE:
				break;
			case SBYTE_DIV_SBYTE:
				break;
			case SBYTE_MOD_SBYTE:
				break;
			case SBYTE_AND_SBYTE:
				break;
			case SBYTE_OR_SBYTE:
				break;
			case SBYTE_XOR_SBYTE:
				break;
			case SBYTE_SHL:
				break;
			case SBYTE_SHR:
				break;
			case SBYTE_USHR:
				break;
			case SBYTE_COMPARE:
				break;
			case SBYTE_TO_BYTE:
				break;
			case SBYTE_TO_SHORT:
				break;
			case SBYTE_TO_USHORT:
				break;
			case SBYTE_TO_INT:
				break;
			case SBYTE_TO_UINT:
				break;
			case SBYTE_TO_LONG:
				break;
			case SBYTE_TO_ULONG:
				break;
			case SBYTE_TO_USIZE:
				break;
			case SBYTE_TO_FLOAT:
				break;
			case SBYTE_TO_DOUBLE:
				break;
			case SBYTE_TO_CHAR:
				break;
			case SBYTE_TO_STRING:
				break;
			case SBYTE_PARSE:
				break;
			case SBYTE_PARSE_WITH_BASE:
				break;
			case SBYTE_GET_MIN_VALUE:
				break;
			case SBYTE_GET_MAX_VALUE:
				break;
			case SHORT_NOT:
				break;
			case SHORT_NEG:
				break;
			case SHORT_INC:
				break;
			case SHORT_DEC:
				break;
			case SHORT_ADD_SHORT:
				break;
			case SHORT_SUB_SHORT:
				break;
			case SHORT_MUL_SHORT:
				break;
			case SHORT_DIV_SHORT:
				break;
			case SHORT_MOD_SHORT:
				break;
			case SHORT_AND_SHORT:
				break;
			case SHORT_OR_SHORT:
				break;
			case SHORT_XOR_SHORT:
				break;
			case SHORT_SHL:
				break;
			case SHORT_SHR:
				break;
			case SHORT_USHR:
				break;
			case SHORT_COMPARE:
				break;
			case SHORT_TO_BYTE:
				break;
			case SHORT_TO_SBYTE:
				break;
			case SHORT_TO_USHORT:
				break;
			case SHORT_TO_INT:
				break;
			case SHORT_TO_UINT:
				break;
			case SHORT_TO_LONG:
				break;
			case SHORT_TO_ULONG:
				break;
			case SHORT_TO_USIZE:
				break;
			case SHORT_TO_FLOAT:
				break;
			case SHORT_TO_DOUBLE:
				break;
			case SHORT_TO_CHAR:
				break;
			case SHORT_TO_STRING:
				break;
			case SHORT_PARSE:
				break;
			case SHORT_PARSE_WITH_BASE:
				break;
			case SHORT_GET_MIN_VALUE:
				break;
			case SHORT_GET_MAX_VALUE:
				break;
			case USHORT_NOT:
				break;
			case USHORT_INC:
				break;
			case USHORT_DEC:
				break;
			case USHORT_ADD_USHORT:
				break;
			case USHORT_SUB_USHORT:
				break;
			case USHORT_MUL_USHORT:
				break;
			case USHORT_DIV_USHORT:
				break;
			case USHORT_MOD_USHORT:
				break;
			case USHORT_AND_USHORT:
				break;
			case USHORT_OR_USHORT:
				break;
			case USHORT_XOR_USHORT:
				break;
			case USHORT_SHL:
				break;
			case USHORT_SHR:
				break;
			case USHORT_COMPARE:
				break;
			case USHORT_TO_BYTE:
				break;
			case USHORT_TO_SBYTE:
				break;
			case USHORT_TO_SHORT:
				break;
			case USHORT_TO_INT:
				break;
			case USHORT_TO_UINT:
				break;
			case USHORT_TO_LONG:
				break;
			case USHORT_TO_ULONG:
				break;
			case USHORT_TO_USIZE:
				break;
			case USHORT_TO_FLOAT:
				break;
			case USHORT_TO_DOUBLE:
				break;
			case USHORT_TO_CHAR:
				break;
			case USHORT_TO_STRING:
				break;
			case USHORT_PARSE:
				break;
			case USHORT_PARSE_WITH_BASE:
				break;
			case USHORT_GET_MIN_VALUE:
				break;
			case USHORT_GET_MAX_VALUE:
				break;
			case INT_NOT:
				break;
			case INT_NEG:
				break;
			case INT_INC:
				break;
			case INT_DEC:
				break;
			case INT_ADD_INT:
				break;
			case INT_ADD_USIZE:
				break;
			case INT_SUB_INT:
				break;
			case INT_MUL_INT:
				break;
			case INT_DIV_INT:
				break;
			case INT_MOD_INT:
				break;
			case INT_AND_INT:
				break;
			case INT_OR_INT:
				break;
			case INT_XOR_INT:
				break;
			case INT_SHL:
				break;
			case INT_SHR:
				break;
			case INT_USHR:
				break;
			case INT_COMPARE:
				break;
			case INT_TO_BYTE:
				break;
			case INT_TO_SBYTE:
				break;
			case INT_TO_SHORT:
				break;
			case INT_TO_USHORT:
				break;
			case INT_TO_UINT:
				break;
			case INT_TO_LONG:
				break;
			case INT_TO_ULONG:
				break;
			case INT_TO_USIZE:
				break;
			case INT_TO_FLOAT:
				break;
			case INT_TO_DOUBLE:
				break;
			case INT_TO_CHAR:
				break;
			case INT_TO_STRING:
				break;
			case INT_PARSE:
				break;
			case INT_PARSE_WITH_BASE:
				break;
			case INT_GET_MIN_VALUE:
				break;
			case INT_GET_MAX_VALUE:
				break;
			case INT_COUNT_LOW_ZEROES:
				break;
			case INT_COUNT_HIGH_ZEROES:
				break;
			case INT_COUNT_LOW_ONES:
				break;
			case INT_COUNT_HIGH_ONES:
				break;
			case INT_HIGHEST_ONE_BIT:
				break;
			case INT_LOWEST_ONE_BIT:
				break;
			case INT_HIGHEST_ZERO_BIT:
				break;
			case INT_LOWEST_ZERO_BIT:
				break;
			case INT_BIT_COUNT:
				break;
			case UINT_NOT:
				break;
			case UINT_INC:
				break;
			case UINT_DEC:
				break;
			case UINT_ADD_UINT:
				break;
			case UINT_SUB_UINT:
				break;
			case UINT_MUL_UINT:
				break;
			case UINT_DIV_UINT:
				break;
			case UINT_MOD_UINT:
				break;
			case UINT_AND_UINT:
				break;
			case UINT_OR_UINT:
				break;
			case UINT_XOR_UINT:
				break;
			case UINT_SHL:
				break;
			case UINT_SHR:
				break;
			case UINT_COMPARE:
				break;
			case UINT_TO_BYTE:
				break;
			case UINT_TO_SBYTE:
				break;
			case UINT_TO_SHORT:
				break;
			case UINT_TO_USHORT:
				break;
			case UINT_TO_INT:
				break;
			case UINT_TO_LONG:
				break;
			case UINT_TO_ULONG:
				break;
			case UINT_TO_USIZE:
				break;
			case UINT_TO_FLOAT:
				break;
			case UINT_TO_DOUBLE:
				break;
			case UINT_TO_CHAR:
				break;
			case UINT_TO_STRING:
				break;
			case UINT_PARSE:
				break;
			case UINT_PARSE_WITH_BASE:
				break;
			case UINT_GET_MIN_VALUE:
				break;
			case UINT_GET_MAX_VALUE:
				break;
			case UINT_COUNT_LOW_ZEROES:
				break;
			case UINT_COUNT_HIGH_ZEROES:
				break;
			case UINT_COUNT_LOW_ONES:
				break;
			case UINT_COUNT_HIGH_ONES:
				break;
			case UINT_HIGHEST_ONE_BIT:
				break;
			case UINT_LOWEST_ONE_BIT:
				break;
			case UINT_HIGHEST_ZERO_BIT:
				break;
			case UINT_LOWEST_ZERO_BIT:
				break;
			case UINT_BIT_COUNT:
				break;
			case LONG_NOT:
				break;
			case LONG_NEG:
				break;
			case LONG_INC:
				break;
			case LONG_DEC:
				break;
			case LONG_ADD_LONG:
				break;
			case LONG_SUB_LONG:
				break;
			case LONG_MUL_LONG:
				break;
			case LONG_DIV_LONG:
				break;
			case LONG_MOD_LONG:
				break;
			case LONG_AND_LONG:
				break;
			case LONG_OR_LONG:
				break;
			case LONG_XOR_LONG:
				break;
			case LONG_SHL:
				break;
			case LONG_SHR:
				break;
			case LONG_USHR:
				break;
			case LONG_COMPARE:
				break;
			case LONG_COMPARE_INT:
				break;
			case LONG_TO_BYTE:
				break;
			case LONG_TO_SBYTE:
				break;
			case LONG_TO_SHORT:
				break;
			case LONG_TO_USHORT:
				break;
			case LONG_TO_INT:
				break;
			case LONG_TO_UINT:
				break;
			case LONG_TO_ULONG:
				break;
			case LONG_TO_USIZE:
				break;
			case LONG_TO_FLOAT:
				break;
			case LONG_TO_DOUBLE:
				break;
			case LONG_TO_CHAR:
				break;
			case LONG_TO_STRING:
				break;
			case LONG_PARSE:
				break;
			case LONG_PARSE_WITH_BASE:
				break;
			case LONG_GET_MIN_VALUE:
				break;
			case LONG_GET_MAX_VALUE:
				break;
			case LONG_COUNT_LOW_ZEROES:
				break;
			case LONG_COUNT_HIGH_ZEROES:
				break;
			case LONG_COUNT_LOW_ONES:
				break;
			case LONG_COUNT_HIGH_ONES:
				break;
			case LONG_HIGHEST_ONE_BIT:
				break;
			case LONG_LOWEST_ONE_BIT:
				break;
			case LONG_HIGHEST_ZERO_BIT:
				break;
			case LONG_LOWEST_ZERO_BIT:
				break;
			case LONG_BIT_COUNT:
				break;
			case ULONG_NOT:
				break;
			case ULONG_INC:
				break;
			case ULONG_DEC:
				break;
			case ULONG_ADD_ULONG:
				break;
			case ULONG_SUB_ULONG:
				break;
			case ULONG_MUL_ULONG:
				break;
			case ULONG_DIV_ULONG:
				break;
			case ULONG_MOD_ULONG:
				break;
			case ULONG_AND_ULONG:
				break;
			case ULONG_OR_ULONG:
				break;
			case ULONG_XOR_ULONG:
				break;
			case ULONG_SHL:
				break;
			case ULONG_SHR:
				break;
			case ULONG_COMPARE:
				break;
			case ULONG_COMPARE_UINT:
				break;
			case ULONG_COMPARE_USIZE:
				break;
			case ULONG_TO_BYTE:
				break;
			case ULONG_TO_SBYTE:
				break;
			case ULONG_TO_SHORT:
				break;
			case ULONG_TO_USHORT:
				break;
			case ULONG_TO_INT:
				break;
			case ULONG_TO_UINT:
				break;
			case ULONG_TO_LONG:
				break;
			case ULONG_TO_USIZE:
				break;
			case ULONG_TO_FLOAT:
				break;
			case ULONG_TO_DOUBLE:
				break;
			case ULONG_TO_CHAR:
				break;
			case ULONG_TO_STRING:
				break;
			case ULONG_PARSE:
				break;
			case ULONG_PARSE_WITH_BASE:
				break;
			case ULONG_GET_MIN_VALUE:
				break;
			case ULONG_GET_MAX_VALUE:
				break;
			case ULONG_COUNT_LOW_ZEROES:
				break;
			case ULONG_COUNT_HIGH_ZEROES:
				break;
			case ULONG_COUNT_LOW_ONES:
				break;
			case ULONG_COUNT_HIGH_ONES:
				break;
			case ULONG_HIGHEST_ONE_BIT:
				break;
			case ULONG_LOWEST_ONE_BIT:
				break;
			case ULONG_HIGHEST_ZERO_BIT:
				break;
			case ULONG_LOWEST_ZERO_BIT:
				break;
			case ULONG_BIT_COUNT:
				break;
			case USIZE_NOT:
				break;
			case USIZE_INC:
				break;
			case USIZE_DEC:
				break;
			case USIZE_ADD_USIZE:
				break;
			case USIZE_SUB_USIZE:
				break;
			case USIZE_MUL_USIZE:
				break;
			case USIZE_DIV_USIZE:
				break;
			case USIZE_MOD_USIZE:
				break;
			case USIZE_AND_USIZE:
				break;
			case USIZE_OR_USIZE:
				break;
			case USIZE_XOR_USIZE:
				break;
			case USIZE_SHL:
				break;
			case USIZE_SHR:
				break;
			case USIZE_COMPARE:
				break;
			case USIZE_COMPARE_UINT:
				break;
			case USIZE_TO_BYTE:
				break;
			case USIZE_TO_SBYTE:
				break;
			case USIZE_TO_SHORT:
				break;
			case USIZE_TO_USHORT:
				break;
			case USIZE_TO_INT:
				break;
			case USIZE_TO_UINT:
				break;
			case USIZE_TO_LONG:
				break;
			case USIZE_TO_ULONG:
				break;
			case USIZE_TO_FLOAT:
				break;
			case USIZE_TO_DOUBLE:
				break;
			case USIZE_TO_CHAR:
				break;
			case USIZE_TO_STRING:
				break;
			case USIZE_PARSE:
				break;
			case USIZE_PARSE_WITH_BASE:
				break;
			case USIZE_GET_MIN_VALUE:
				break;
			case USIZE_GET_MAX_VALUE:
				break;
			case USIZE_COUNT_LOW_ZEROES:
				break;
			case USIZE_COUNT_HIGH_ZEROES:
				break;
			case USIZE_COUNT_LOW_ONES:
				break;
			case USIZE_COUNT_HIGH_ONES:
				break;
			case USIZE_HIGHEST_ONE_BIT:
				break;
			case USIZE_LOWEST_ONE_BIT:
				break;
			case USIZE_HIGHEST_ZERO_BIT:
				break;
			case USIZE_LOWEST_ZERO_BIT:
				break;
			case USIZE_BIT_COUNT:
				break;
			case USIZE_BITS:
				break;
			case FLOAT_NEG:
				break;
			case FLOAT_INC:
				break;
			case FLOAT_DEC:
				break;
			case FLOAT_ADD_FLOAT:
				break;
			case FLOAT_SUB_FLOAT:
				break;
			case FLOAT_MUL_FLOAT:
				break;
			case FLOAT_DIV_FLOAT:
				break;
			case FLOAT_MOD_FLOAT:
				break;
			case FLOAT_COMPARE:
				break;
			case FLOAT_TO_BYTE:
				break;
			case FLOAT_TO_SBYTE:
				break;
			case FLOAT_TO_SHORT:
				break;
			case FLOAT_TO_USHORT:
				break;
			case FLOAT_TO_INT:
				break;
			case FLOAT_TO_UINT:
				break;
			case FLOAT_TO_LONG:
				break;
			case FLOAT_TO_ULONG:
				break;
			case FLOAT_TO_USIZE:
				break;
			case FLOAT_TO_DOUBLE:
				break;
			case FLOAT_TO_STRING:
				break;
			case FLOAT_BITS:
				break;
			case FLOAT_FROM_BITS:
				break;
			case FLOAT_PARSE:
				break;
			case FLOAT_GET_MIN_VALUE:
				break;
			case FLOAT_GET_MAX_VALUE:
				break;
			case DOUBLE_NEG:
				break;
			case DOUBLE_INC:
				break;
			case DOUBLE_DEC:
				break;
			case DOUBLE_ADD_DOUBLE:
				break;
			case DOUBLE_SUB_DOUBLE:
				break;
			case DOUBLE_MUL_DOUBLE:
				break;
			case DOUBLE_DIV_DOUBLE:
				break;
			case DOUBLE_MOD_DOUBLE:
				break;
			case DOUBLE_COMPARE:
				break;
			case DOUBLE_TO_BYTE:
				break;
			case DOUBLE_TO_SBYTE:
				break;
			case DOUBLE_TO_SHORT:
				break;
			case DOUBLE_TO_USHORT:
				break;
			case DOUBLE_TO_INT:
				break;
			case DOUBLE_TO_UINT:
				break;
			case DOUBLE_TO_LONG:
				break;
			case DOUBLE_TO_ULONG:
				break;
			case DOUBLE_TO_USIZE:
				break;
			case DOUBLE_TO_FLOAT:
				break;
			case DOUBLE_TO_STRING:
				break;
			case DOUBLE_BITS:
				break;
			case DOUBLE_FROM_BITS:
				break;
			case DOUBLE_PARSE:
				break;
			case DOUBLE_GET_MIN_VALUE:
				break;
			case DOUBLE_GET_MAX_VALUE:
				break;
			case CHAR_ADD_INT:
				break;
			case CHAR_SUB_INT:
				break;
			case CHAR_SUB_CHAR:
				break;
			case CHAR_COMPARE:
				break;
			case CHAR_TO_BYTE:
				break;
			case CHAR_TO_SBYTE:
				break;
			case CHAR_TO_SHORT:
				break;
			case CHAR_TO_USHORT:
				break;
			case CHAR_TO_INT:
				break;
			case CHAR_TO_UINT:
				break;
			case CHAR_TO_LONG:
				break;
			case CHAR_TO_ULONG:
				break;
			case CHAR_TO_USIZE:
				break;
			case CHAR_TO_STRING:
				break;
			case CHAR_GET_MIN_VALUE:
				break;
			case CHAR_GET_MAX_VALUE:
				break;
			case CHAR_REMOVE_DIACRITICS:
				break;
			case CHAR_TO_LOWER_CASE:
				break;
			case CHAR_TO_UPPER_CASE:
				break;
			case STRING_CONSTRUCTOR_CHARACTERS:
				javaWriter.newObject(JavaClass.STRING);
				javaWriter.dup();
				expression.arguments.arguments[0].accept(this);
				javaWriter.invokeSpecial(STRING_INIT_CHARACTERS);
				return;
			case STRING_ADD_STRING:
				break;
			case STRING_COMPARE:
				break;
			case STRING_LENGTH:
				break;
			case STRING_INDEXGET:
				break;
			case STRING_RANGEGET:
				break;
			case STRING_CHARACTERS:
				break;
			case STRING_ISEMPTY:
				break;
			case STRING_REMOVE_DIACRITICS:
				break;
			case STRING_TRIM:
				break;
			case STRING_TO_LOWER_CASE:
				break;
			case STRING_TO_UPPER_CASE:
				break;
			case STRING_CONTAINS_CHAR:
				break;
			case STRING_CONTAINS_STRING:
				break;
			case ASSOC_INDEXGET:
				break;
			case ASSOC_INDEXSET:
				break;
			case ASSOC_CONTAINS:
				break;
			case ASSOC_GETORDEFAULT:
				break;
			case ASSOC_SIZE:
				break;
			case ASSOC_ISEMPTY:
				break;
			case ASSOC_KEYS:
				break;
			case ASSOC_VALUES:
				break;
			case ASSOC_HASHCODE:
				break;
			case ASSOC_EQUALS:
				break;
			case ASSOC_NOTEQUALS:
				break;
			case ASSOC_SAME:
				break;
			case ASSOC_NOTSAME:
				break;
			case ASSOC_CONSTRUCTOR:
			case GENERICMAP_CONSTRUCTOR: {
				javaWriter.newObject(JavaClass.HASHMAP);
				javaWriter.dup();
				javaWriter.invokeSpecial(HASHMAP_INIT);
				return;
			}
			case GENERICMAP_GETOPTIONAL:
				break;
			case GENERICMAP_PUT:
				break;
			case GENERICMAP_CONTAINS:
				break;
			case GENERICMAP_ADDALL:
				break;
			case GENERICMAP_SIZE:
				break;
			case GENERICMAP_ISEMPTY:
				break;
			case GENERICMAP_HASHCODE:
				break;
			case GENERICMAP_EQUALS:
				break;
			case GENERICMAP_NOTEQUALS:
				break;
			case GENERICMAP_SAME:
				break;
			case GENERICMAP_NOTSAME:
				break;
			case ARRAY_CONSTRUCTOR_SIZED:
			case ARRAY_CONSTRUCTOR_INITIAL_VALUE: {
				ArrayTypeID type = (ArrayTypeID) expression.type.type;

				final Type ASMType = context.getType(expression.type);
				final Type ASMElementType = context.getType(type.elementType);

				final Label begin = new Label();
				final Label end = new Label();

				javaWriter.label(begin);
				final int defaultValueLocation = javaWriter.local(ASMElementType);
				javaWriter.addVariableInfo(new JavaLocalVariableInfo(ASMElementType, defaultValueLocation, begin, "defaultValue", end));


				if (builtin == BuiltinID.ARRAY_CONSTRUCTOR_SIZED) {
					type.elementType.type.getDefaultValue().accept(this);
				} else {
					expression.arguments.arguments[expression.arguments.arguments.length - 1].accept(this);
				}
				javaWriter.store(ASMElementType, defaultValueLocation);


				final int[] arraySizes = ArrayInitializerHelper.getArraySizeLocationsFromConstructor(type.dimension, expression.arguments.arguments, this);
				ArrayInitializerHelper.visitMultiDimArrayWithDefaultValue(javaWriter, arraySizes, type.dimension, ASMType, defaultValueLocation);

				javaWriter.label(end);
				return;
			}
			case ARRAY_CONSTRUCTOR_LAMBDA: {
				
				//Labels
				final Label begin = new Label();
				final Label end = new Label();
				javaWriter.label(begin);
				
				final Type ASMElementType = context.getType(expression.type);
				final int dimension = ((ArrayTypeID) expression.type.type).dimension;
				final int[] arraySizes = ArrayInitializerHelper.getArraySizeLocationsFromConstructor(dimension, expression.arguments.arguments, this);
				ArrayInitializerHelper.visitMultiDimArray(javaWriter, arraySizes, new int[dimension], dimension, ASMElementType, (elementType, counterLocations) -> {
					expression.arguments.arguments[dimension].accept(this);
					for (int counterLocation : counterLocations) {
						javaWriter.loadInt(counterLocation);
					}
					javaWriter.invokeInterface(context.getFunctionalInterface(expression.arguments.arguments[dimension].type));
				});
				javaWriter.label(end);
				return;
			}
			case ARRAY_CONSTRUCTOR_PROJECTED:
			case ARRAY_CONSTRUCTOR_PROJECTED_INDEXED: {
				ArrayTypeID type = (ArrayTypeID) expression.type.type;
				
				//Labels
				final Label begin = new Label();
				final Label end = new Label();
				javaWriter.label(begin);
				
				//Origin Array
				expression.arguments.arguments[0].accept(this);
				final Type originArrayType = context.getType(expression.arguments.arguments[0].type);
				final int originArrayLocation = javaWriter.local(originArrayType);
				javaWriter.storeObject(originArrayLocation);
				Type destinationArrayType = context.getType(expression.type);
				
				final boolean indexed = builtin == BuiltinID.ARRAY_CONSTRUCTOR_PROJECTED_INDEXED;
				final boolean canBeInLined = ArrayInitializerHelper.canBeInLined(expression.arguments.arguments[1]);
				if (canBeInLined) {
					//We can inline, so do it
					final int[] arraySizes = ArrayInitializerHelper.getArraySizeLocationsProjected(type.dimension, originArrayType, originArrayLocation, javaWriter);
					final Type projectedElementType = Type.getType(originArrayType.getDescriptor().substring(type.dimension));
					ArrayInitializerHelper.visitProjected(javaWriter, arraySizes, type.dimension, originArrayLocation, originArrayType, destinationArrayType,
							(elementType, counterLocations) -> {
								Label inlineBegin = new Label();
								Label inlineEnd = new Label();
								javaWriter.label(inlineBegin);
								
								final int projectedElementLocal = javaWriter.local(projectedElementType);
								javaWriter.store(projectedElementType, projectedElementLocal);
								
								
								JavaExpressionVisitor visitor = new JavaExpressionVisitor(context, module, javaWriter) {
									@Override
									public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
										if(indexed) {
											final JavaParameterInfo parameterInfo = module.getParameterInfo(expression.parameter);
											if (parameterInfo != null && parameterInfo.index <= type.dimension) {
												javaWriter.loadInt(counterLocations[parameterInfo.index - 1]);
												return null;
											}
										}
										
										javaWriter.load(projectedElementType, projectedElementLocal);
										return null;
									}
								};
								
								Expression funcExpression = expression.arguments.arguments[1];
								while (funcExpression instanceof StorageCastExpression) {
									funcExpression = ((StorageCastExpression) funcExpression).value;
								}
								
								if (funcExpression instanceof FunctionExpression && ((FunctionExpression) funcExpression).body instanceof ReturnStatement) {
									CompilerUtils.tagMethodParameters(context, module, ((FunctionExpression) funcExpression).header, false, Collections
                                            .emptyList());
									((ReturnStatement) ((FunctionExpression) funcExpression).body).value.accept(visitor);
									javaWriter.addVariableInfo(new JavaLocalVariableInfo(projectedElementType, projectedElementLocal, inlineBegin, ((FunctionExpression) funcExpression).header.parameters[0].name, inlineEnd));
									
								} else throw new IllegalStateException("Trying to inline a non-inlineable expression");
								
								
								javaWriter.label(inlineEnd);
							});
				} else {
					//We cannot inline, so get a hold of the function expression and apply it to every
					expression.arguments.arguments[1].accept(this); //Projection Function
					final Type functionType = context.getType(expression.arguments.arguments[1].type);
					final int functionLocation = javaWriter.local(functionType);
					javaWriter.storeObject(functionLocation);
					javaWriter.addVariableInfo(new JavaLocalVariableInfo(functionType, functionLocation, begin, "projectionFunction", end));
					final int[] arraySizes = ArrayInitializerHelper.getArraySizeLocationsProjected(type.dimension, originArrayType, originArrayLocation, javaWriter);
					ArrayInitializerHelper.visitProjected(javaWriter, arraySizes, type.dimension, originArrayLocation, originArrayType, destinationArrayType,
							(elementType, counterLocations) -> {
								//Apply function here
								javaWriter.loadObject(functionLocation);
								javaWriter.swap();
								
								if(indexed) {
									for (int counterLocation : counterLocations) {
										javaWriter.loadInt(counterLocation);
										javaWriter.swap();
									}
								}
								
								javaWriter.invokeInterface(context.getFunctionalInterface(expression.arguments.arguments[1].type));
							});
				}
				
				
				javaWriter.label(end);
				return;
				
			}
			case ARRAY_INDEXGET:
				break;
			case ARRAY_INDEXSET:
				break;
			case ARRAY_INDEXGETRANGE:
				break;
			case ARRAY_CONTAINS:
				break;
			case ARRAY_LENGTH:
				break;
			case ARRAY_ISEMPTY:
				break;
			case ARRAY_HASHCODE:
				break;
			case ARRAY_EQUALS:
				break;
			case ARRAY_NOTEQUALS:
				break;
			case ARRAY_SAME:
				break;
			case ARRAY_NOTSAME:
				break;
			case SBYTE_ARRAY_AS_BYTE_ARRAY:
				break;
			case BYTE_ARRAY_AS_SBYTE_ARRAY:
				break;
			case SHORT_ARRAY_AS_USHORT_ARRAY:
				break;
			case USHORT_ARRAY_AS_SHORT_ARRAY:
				break;
			case INT_ARRAY_AS_UINT_ARRAY:
				break;
			case UINT_ARRAY_AS_INT_ARRAY:
				break;
			case LONG_ARRAY_AS_ULONG_ARRAY:
				break;
			case ULONG_ARRAY_AS_LONG_ARRAY:
				break;
			case FUNCTION_CALL:
				break;
			case FUNCTION_SAME:
				break;
			case FUNCTION_NOTSAME:
				break;
			case CLASS_DEFAULT_CONSTRUCTOR:
				javaWriter.newObject(context.getInternalName(expression.type));
				javaWriter.dup();
				javaWriter.invokeSpecial(context.getJavaMethod(expression.constructor));
				return;
			case STRUCT_EMPTY_CONSTRUCTOR:
				break;
			case STRUCT_VALUE_CONSTRUCTOR:
				break;
			case ENUM_EMPTY_CONSTRUCTOR:
				break;
			case ENUM_NAME:
				break;
			case ENUM_ORDINAL:
				break;
			case ENUM_VALUES:
				break;
			case ENUM_TO_STRING:
				break;
			case ENUM_COMPARE:
				break;
			case OBJECT_HASHCODE:
				break;
			case OBJECT_SAME:
				break;
			case OBJECT_NOTSAME:
				break;
			case RANGE_FROM:
				break;
			case RANGE_TO:
				break;
			case OPTIONAL_IS_NULL:
				break;
			case OPTIONAL_IS_NOT_NULL:
				break;
			case AUTOOP_NOTEQUALS:
				break;
			case ITERATOR_INT_RANGE:
				break;
			case ITERATOR_ARRAY_VALUES:
				break;
			case ITERATOR_ARRAY_KEY_VALUES:
				break;
			case ITERATOR_ASSOC_KEYS:
				break;
			case ITERATOR_ASSOC_KEY_VALUES:
				break;
			case ITERATOR_STRING_CHARS:
				break;
		}

		throw new UnsupportedOperationException("Unknown builtin constructor: " + builtin);
	}


	@Override
	public Void visitNull(NullExpression expression) {
		if (!expression.type.isBasic(BasicTypeID.NULL) && expression.type.type.withoutOptional() == BasicTypeID.USIZE)
			javaWriter.constant(-1); // special case: usize? null = -1
		else
			javaWriter.aConstNull();
		return null;
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		Label end = new Label();
		Label onTrue = new Label();

		expression.left.accept(this);

		javaWriter.ifNE(onTrue);
		expression.right.accept(this);

		// //these two calls are redundant but make decompiled code look better. Keep?
		// javaWriter.ifNE(onTrue);
		// javaWriter.iConst0();

		javaWriter.goTo(end);

		javaWriter.label(onTrue);
		javaWriter.iConst1();


		javaWriter.label(end);

		return null;
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		javaWriter.newObject("java/lang/AssertionError");
		javaWriter.dup();
		expression.value.accept(this);
		javaWriter.invokeSpecial(AssertionError.class, "<init>", "(Ljava/lang/String;)V");
		javaWriter.aThrow();
		return null;
	}

	private void modify(Expression source, Runnable modification, PushOption push) {
		source.accept(new JavaModificationExpressionVisitor(context, module, javaWriter, this, modification, push));
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		if (expression.member.getBuiltin() != null) {
			switch (expression.member.getBuiltin()) {
				case BYTE_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.constant(255);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case BYTE_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.constant(255);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case SBYTE_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.i2b();
					}, PushOption.BEFORE);
					return null;
				case SBYTE_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.i2b();
					}, PushOption.BEFORE);
					return null;
				case SHORT_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.i2s();
					}, PushOption.BEFORE);
					return null;
				case SHORT_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.i2s();
					}, PushOption.BEFORE);
					return null;
				case USHORT_INC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iAdd();
						javaWriter.constant(0xFFFF);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case USHORT_DEC:
					modify(expression.target, () -> {
						javaWriter.iConst1();
						javaWriter.iSub();
						javaWriter.constant(0xFFFF);
						javaWriter.iAnd();
					}, PushOption.BEFORE);
					return null;
				case INT_INC:
				case UINT_INC:
				case USIZE_INC:
					if (expression.target instanceof GetLocalVariableExpression) {
						JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.variable);
						javaWriter.load(local);
						javaWriter.iinc(local.local);
					} else {
						modify(expression.target, () -> {
							javaWriter.iConst1();
							javaWriter.iAdd();
						}, PushOption.BEFORE);
					}
					return null;
				case INT_DEC:
				case UINT_DEC:
				case USIZE_DEC:
					if (expression.target instanceof GetLocalVariableExpression) {
						JavaLocalVariableInfo local = javaWriter.getLocalVariable(((GetLocalVariableExpression) expression.target).variable.variable);
						javaWriter.load(local);
						javaWriter.iinc(local.local, -1);
					} else {
						modify(expression.target, () -> {
							javaWriter.iConst1();
							javaWriter.iSub();
						}, PushOption.BEFORE);
					}
					return null;
				case LONG_INC:
				case ULONG_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1l);
						javaWriter.lAdd();
					}, PushOption.BEFORE);
					return null;
				case LONG_DEC:
				case ULONG_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1l);
						javaWriter.lSub();
					}, PushOption.BEFORE);
					return null;
				case FLOAT_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1f);
						javaWriter.fAdd();
					}, PushOption.BEFORE);
					return null;
				case FLOAT_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1f);
						javaWriter.fSub();
					}, PushOption.BEFORE);
					return null;
				case DOUBLE_INC:
					modify(expression.target, () -> {
						javaWriter.constant(1d);
						javaWriter.dAdd();
					}, PushOption.BEFORE);
					return null;
				case DOUBLE_DEC:
					modify(expression.target, () -> {
						javaWriter.constant(1d);
						javaWriter.dSub();
					}, PushOption.BEFORE);
					return null;
				default:
					throw new IllegalArgumentException("Unknown postcall builtin: " + expression.member.getBuiltin());
			}
		}

		modify(expression.target, () -> {
			if (!checkAndExecuteMethodInfo(expression.member, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");
		}, PushOption.BEFORE);

		return null;
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		RangeTypeID type = (RangeTypeID) expression.type.type;
		Type cls = context.getType(expression.type);
		javaWriter.newObject(cls.getInternalName());
		javaWriter.dup();
		expression.from.accept(this);
		expression.to.accept(this);
		javaWriter.invokeSpecial(cls.getInternalName(), "<init>", "(" + context.getDescriptor(type.baseType) + context.getDescriptor(type.baseType) + ")V");

		return null;
	}


	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		expression.left.accept(this);
		expression.right.accept(this);

		Label end = new Label();
		Label equal = new Label();

		if (expression.inverted)
			javaWriter.ifACmpNe(equal);
		else
			javaWriter.ifACmpEq(equal);

		javaWriter.iConst0();
		javaWriter.goTo(end);
		javaWriter.label(equal);
		javaWriter.iConst1();
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		expression.value.accept(this);
		expression.target.accept(this);
		javaWriter.dupX1(false, CompilerUtils.isLarge(expression.type));
		putField(expression.field);
		return null;
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		expression.value.accept(this);
		javaWriter.dup(CompilerUtils.isLarge(expression.value.type));
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);
		javaWriter.store(context.getType(expression.type), parameter.index);
		return null;
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		expression.value.accept(this);
		Label label = new Label();
		javaWriter.label(label);
		final JavaLocalVariableInfo tag = javaWriter.getLocalVariable(expression.variable.variable);
		tag.end = label;

		javaWriter.dup(CompilerUtils.isLarge(expression.value.type));
		javaWriter.store(tag.type, tag.local);
		return null;
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		expression.value.accept(this);
		javaWriter.dup(CompilerUtils.isLarge(expression.value.type));
		javaWriter.putStaticField(context.getJavaField(expression.field));
		return null;
	}

	@Override
	public Void visitSetter(SetterExpression expression) {
		if(!checkAndExecuteMethodInfo(expression.setter, expression.type, expression)) {
			throw new IllegalStateException("Unknown Setter");
		}
		return null;
	}

	@Override
	public Void visitStaticGetter(StaticGetterExpression expression) {
		BuiltinID builtin = expression.getter.member.builtin;
		if (builtin == null) {
			if (context.hasJavaField(expression.getter)) {
				javaWriter.getStaticField(context.getJavaField(expression.getter));
				return null;
			}

			if (!checkAndExecuteMethodInfo(expression.getter, expression.type, expression))
				throw new IllegalStateException("Call target has no method info!");

			return null;
		}

		switch (builtin) {
			case BYTE_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case BYTE_GET_MAX_VALUE:
				javaWriter.constant(0xFF);
				break;
			case SBYTE_GET_MIN_VALUE:
				javaWriter.getStaticField(BYTE_MIN_VALUE);
				break;
			case SBYTE_GET_MAX_VALUE:
				javaWriter.getStaticField(BYTE_MAX_VALUE);
				break;
			case SHORT_GET_MIN_VALUE:
				javaWriter.getStaticField(SHORT_MIN_VALUE);
				break;
			case SHORT_GET_MAX_VALUE:
				javaWriter.getStaticField(SHORT_MAX_VALUE);
				break;
			case USHORT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USHORT_GET_MAX_VALUE:
				javaWriter.constant(0xFFFF);
				break;
			case INT_GET_MIN_VALUE:
				javaWriter.getStaticField(INTEGER_MIN_VALUE);
				break;
			case INT_GET_MAX_VALUE:
				javaWriter.getStaticField(INTEGER_MAX_VALUE);
				break;
			case UINT_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case UINT_GET_MAX_VALUE:
				javaWriter.constant(-1);
				break;
			case LONG_GET_MIN_VALUE:
				javaWriter.getStaticField(LONG_MIN_VALUE);
				break;
			case LONG_GET_MAX_VALUE:
				javaWriter.getStaticField(LONG_MAX_VALUE);
				break;
			case ULONG_GET_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case ULONG_GET_MAX_VALUE:
				javaWriter.constant(-1L);
				break;
			case FLOAT_GET_MIN_VALUE:
				javaWriter.getStaticField(FLOAT_MIN_VALUE);
				break;
			case FLOAT_GET_MAX_VALUE:
				javaWriter.getStaticField(FLOAT_MAX_VALUE);
				break;
			case DOUBLE_GET_MIN_VALUE:
				javaWriter.getStaticField(DOUBLE_MIN_VALUE);
				break;
			case DOUBLE_GET_MAX_VALUE:
				javaWriter.getStaticField(DOUBLE_MAX_VALUE);
				break;
			case CHAR_GET_MIN_VALUE:
				javaWriter.getStaticField(CHARACTER_MIN_VALUE);
				break;
			case CHAR_GET_MAX_VALUE:
				javaWriter.getStaticField(CHARACTER_MAX_VALUE);
				break;
			case ENUM_VALUES: {
				DefinitionTypeID type = (DefinitionTypeID) expression.type.type;
				JavaClass cls = context.getJavaClass(type.definition);
				javaWriter.invokeStatic(JavaMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + builtin);
		}

		throw new UnsupportedOperationException("Unknown builtin: " + builtin);
	}

	@Override
	public Void visitStaticSetter(StaticSetterExpression expression) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Void visitStorageCast(StorageCastExpression expression) {
		expression.value.accept(this);
		
		{
			final StorageTag specifiedStorage = expression.type.getSpecifiedStorage();
			if(specifiedStorage instanceof JavaFunctionalInterfaceStorageTag) {
				visitFunctionalInterfaceWrapping(expression, (JavaFunctionalInterfaceStorageTag) specifiedStorage);
			}
		}

		if (expression.type.isDestructible()) { // only destructible types matter here; nondestructible types never need conversion
			StorageTag fromTag = expression.value.type.getActualStorage();
			StorageTag toTag = expression.type.getActualStorage();
			if (JavaTypeUtils.isShared(fromTag) && toTag == BorrowStorageTag.INVOCATION) {
				// Shared<T>.get()
				javaWriter.invokeVirtual(SHARED_GET);
			} else if (fromTag == UniqueStorageTag.INSTANCE && JavaTypeUtils.isShared(toTag)) {
				// new Shared<T>(value)
				javaWriter.newObject("zsynthetic/Shared");
				javaWriter.dupX1();
				javaWriter.swap();
				javaWriter.invokeSpecial(SHARED_INIT);
			}
		}

		return null;
	}
	
	private void visitFunctionalInterfaceWrapping(StorageCastExpression expression, JavaFunctionalInterfaceStorageTag tag) {

		final Method functionalInterfaceMethod = tag.functionalInterfaceMethod;

		final JavaMethod wrappedMethod = context.getFunctionalInterface(expression.value.type);
		final String wrappedSignature = context.getDescriptor(expression.type);
		final String constructorDesc = "(" + wrappedSignature + ")V";
		
		final String className = context.getLambdaCounter();
		final String methodDescriptor = Type.getMethodDescriptor(functionalInterfaceMethod);
		final String[] interfaces = new String[]{Type.getInternalName(functionalInterfaceMethod.getDeclaringClass())};

		final ClassWriter lambdaCW = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		lambdaCW.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", interfaces);

		//The field storing the wrapped object
		{
			lambdaCW.visitField(Modifier.PRIVATE | Modifier.FINAL, "wrapped", wrappedSignature, null, null).visitEnd();
		}

		//Constructor
		{
			final JavaWriter constructorWriter = new JavaWriter(expression.position, lambdaCW, JavaMethod.getConstructor(javaWriter.method.cls, constructorDesc, Opcodes.ACC_PUBLIC), null, null, null);
			constructorWriter.start();
			constructorWriter.loadObject(0);
			constructorWriter.dup();
			constructorWriter.invokeSpecial(Object.class, "<init>", "()V");

			constructorWriter.loadObject(1);
			constructorWriter.putField(className, "wrapped", wrappedSignature);

			constructorWriter.ret();
			constructorWriter.end();
		}

		//The actual method
		{
			final JavaMethod actualMethod = new JavaMethod(tag.method.cls, tag.method.kind, tag.method.name, tag.method.compile, tag.method.descriptor, tag.method.modifiers & ~JavaModifiers.ABSTRACT, tag.method.genericResult, tag.method.typeParameterArguments);
			final JavaWriter functionWriter = new JavaWriter(expression.position, lambdaCW, actualMethod, null, methodDescriptor, null, "java/lang/Override");
			functionWriter.start();

			//this.wrapped
			functionWriter.loadObject(0);
			functionWriter.getField(className, "wrapped", wrappedSignature);

			//Load all function parameters
			{
				final Class<?>[] methodParameterTypes = functionalInterfaceMethod.getParameterTypes();
				for (int i = 0; i < methodParameterTypes.length; i++) {
					functionWriter.load(Type.getType(methodParameterTypes[i]), i + 1);
				}
			}

			//Invokes the wrapped interface's method and returns the result
			functionWriter.invokeInterface(wrappedMethod);
			functionWriter.returnType(context.getType(((FunctionTypeID) expression.value.type.type).header.getReturnType()));

			functionWriter.ret();
			functionWriter.end();
		}


		lambdaCW.visitEnd();
		context.register(className, lambdaCW.toByteArray());

		javaWriter.newObject(className);
		javaWriter.dupX1();
		javaWriter.swap();
		javaWriter.invokeSpecial(className, "<init>", constructorDesc);
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		expression.value.accept(this);
		return null; // nothing to do
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		javaWriter.load(context.getType(expression.type), 0);
		return null;
	}

	@Override
	public Void visitThrow(ThrowExpression expression) {
		expression.value.accept(this);
		javaWriter.aThrow();
		return null;
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		expression.value.accept(this);
		javaWriter.dup();
		//FIXME better way of finding the error
		final String internalName = context.getInternalName(expression.value.type) + "$Error";
		javaWriter.instanceOf(internalName);
		final Label end = new Label();
		javaWriter.ifNE(end);
		javaWriter.newObject(Type.getInternalName(Exception.class));
		javaWriter.dup();
		javaWriter.invokeSpecial(Type.getInternalName(Exception.class), "<init>", "()V");
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		expression.value.accept(this);
		javaWriter.dup();
		//FIXME better way of finding the error
		final String internalName = context.getInternalName(expression.value.type) + "$Error";
		javaWriter.instanceOf(internalName);
		final Label end = new Label();
		javaWriter.ifNE(end);
		javaWriter.newObject(internalName);
		javaWriter.dupX1();
		javaWriter.swap();
		javaWriter.invokeSpecial(internalName, "<init>", "(Ljava/lang/Object;)V");
		javaWriter.label(end);
		return null;
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		JavaVariantOption tag = context.getJavaVariantOption(expression.option);
		final String internalName = tag.variantOptionClass.internalName;
		javaWriter.newObject(internalName);
		javaWriter.dup();

		for (Expression argument : expression.arguments) {
			argument.accept(this);
		}

		final StringBuilder builder = new StringBuilder("(");
		for (StoredType type : expression.option.getOption().types) {
			builder.append(context.getDescriptor(type));
		}
		builder.append(")V");


		javaWriter.invokeSpecial(internalName, "<init>", builder.toString());
		return null;
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		//Does nothing if not required to be wrapped
		expression.value.accept(this);
		expression.value.type.type.accept(expression.value.type, boxingTypeVisitor);
		return null;
	}

	public JavaWriter getJavaWriter() {
		return javaWriter;
	}

	//Will return true if a JavaMethodInfo.class tag exists, and will compile that tag
	@SuppressWarnings({"Raw", "unchecked"})
	boolean checkAndExecuteMethodInfo(DefinitionMemberRef member, StoredType resultType, Expression expression) {
		JavaMethod methodInfo = context.getJavaMethod(member);
		if (methodInfo == null)
			return false;

		if (methodInfo.kind == JavaMethod.Kind.STATIC) {
			getJavaWriter().invokeStatic(methodInfo);
		} else if (methodInfo.kind == JavaMethod.Kind.INTERFACE) {
			getJavaWriter().invokeInterface(methodInfo);
		} else if (methodInfo.kind == JavaMethod.Kind.EXPANSION) {
			getJavaWriter().invokeStatic(methodInfo);
		} else if (methodInfo.kind == JavaMethod.Kind.COMPILED) {
			Objects.requireNonNull(methodInfo.translation).translate(expression, this);
		} else if (methodInfo.cls != null && methodInfo.cls.kind == JavaClass.Kind.INTERFACE) {
			getJavaWriter().invokeInterface(methodInfo);
		} else {
			getJavaWriter().invokeVirtual(methodInfo);
		}
		if (methodInfo.genericResult) {
			getJavaWriter().checkCast(context.getInternalName(resultType));
		}

		//Make sure that method results are popped if ZC thinks its a void but it actually is not.
		//Fixes an issue for List#add() returning void in ZC but Z in Java.
		if(resultType.type == BasicTypeID.VOID && !methodInfo.descriptor.equals("") && !methodInfo.descriptor.endsWith(")V")) {
			final boolean isLarge = methodInfo.descriptor.endsWith(")D") && methodInfo.descriptor.endsWith(")J");
			getJavaWriter().pop(isLarge);
		}

		return true;
	}

	//Will return true if a JavaFieldInfo.class tag exists, and will compile that tag
	public void putField(FieldMemberRef field) {
		JavaField fieldInfo = context.getJavaField(field);
		if (field.isStatic()) {
			getJavaWriter().putStaticField(fieldInfo);
		} else {
			getJavaWriter().putField(fieldInfo);
		}
	}

	public void getField(FieldMemberRef field) {
		final JavaField fieldInfo = context.getJavaField(field);
		if (field.isStatic()) {
			getJavaWriter().getStaticField(fieldInfo);
		} else {
			getJavaWriter().getField(fieldInfo);
		}
	}

	@Override
	public Void isEmptyAsLengthZero(Expression value) {
		return null;
	}

	@Override
	public Void listToArray(CastExpression value) {
		//value.target.accept(this);
		javaWriter.iConst0();
		final Type type = context.getType(((ArrayTypeID) value.type.type).elementType);
		javaWriter.newArray(type);
		final JavaMethod toArray = new JavaMethod(JavaClass.COLLECTION, JavaMethod.Kind.INSTANCE, "toArray", true, "([Ljava/lang/Object;)[Ljava/lang/Object;", 0, true);
		javaWriter.invokeInterface(toArray);
		javaWriter.checkCast(context.getType(value.type));
		return null;
	}

	@Override
	public Void containsAsIndexOf(Expression target, Expression value) {
		return null;
	}

	@Override
	public Void sorted(Expression value) {
		return null;
	}

	@Override
	public Void sortedWithComparator(Expression value, Expression comparator) {
		return null;
	}

	@Override
	public Void copy(Expression value) {
		return null;
	}

	@Override
	public Void copyTo(CallExpression call) {
		//Copy this (source) to dest
		//              source.copyTo(dest, sourceOffset, destOffset, length)
		//=> System.arraycopy(source, sourceOffset, dest, destOffset, length);
		javaWriter.dup2X2();
		javaWriter.pop2();
		javaWriter.swap();
		javaWriter.dup2X2();
		javaWriter.pop2();
		final JavaClass system = JavaClass.fromInternalName("java/lang/System", JavaClass.Kind.CLASS);
		final JavaMethod javaMethod = JavaMethod.getStatic(system, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", JavaModifiers.PUBLIC);
		javaWriter.invokeStatic(javaMethod);
		return null;
	}

	@Override
	public Void stringToAscii(Expression value) {
		return null;
	}

	@Override
	public Void stringToUTF8(Expression value) {
		return null;
	}

	@Override
	public Void bytesAsciiToString(Expression value) {
		return null;
	}

	@Override
	public Void bytesUTF8ToString(Expression value) {
		return null;
	}
}
