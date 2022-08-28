package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinFieldSymbol;
import org.openzen.zenscript.javashared.*;

public class JavaFieldBytecodeCompiler implements JavaFieldCompiler<Void> {
	private final JavaWriter javaWriter;
	private final JavaExpressionVisitor expressionVisitor;
	private final boolean pushing;

	public JavaFieldBytecodeCompiler(JavaWriter javaWriter, JavaExpressionVisitor expressionVisitor, boolean pushing) {
		this.javaWriter = javaWriter;
		this.expressionVisitor = expressionVisitor;
		this.pushing = pushing;
	}

	@Override
	public Void nativeInstanceGet(JavaNativeField field, Expression instance) {
		instance.accept(expressionVisitor);
		javaWriter.getField(field);
		return null;
	}

	@Override
	public Void nativeInstanceSet(JavaNativeField field, Expression instance, Expression value) {
		if (pushing) {
			value.accept(expressionVisitor);
			instance.accept(expressionVisitor);
			javaWriter.dupX1(false, CompilerUtils.isLarge(value.type));
		} else {
			instance.accept(expressionVisitor);
			value.accept(expressionVisitor);
		}
		javaWriter.putField(field);
		return null;
	}

	@Override
	public Void nativeStaticGet(JavaNativeField field) {
		javaWriter.getStaticField(field);
		return null;
	}

	@Override
	public Void nativeStaticSet(JavaNativeField field, Expression value) {
		value.accept(expressionVisitor);
		if (pushing) {
			javaWriter.dup(CompilerUtils.isLarge(value.type));
		}
		javaWriter.putStaticField(field);
		return null;
	}

	@Override
	public Void builtinInstanceGet(BuiltinFieldSymbol field, Expression instance) {
		throw new UnsupportedOperationException("Unknown builtin: " + field);
	}

	@Override
	public Void builtinInstanceSet(BuiltinFieldSymbol field, Expression instance, Expression value) {
		throw new UnsupportedOperationException("Unknown builtin: " + field);
	}

	@Override
	public Void builtinStaticGet(BuiltinFieldSymbol field) {
		if (!pushing) {
			return null;
		}

		switch (field) {
			case BYTE_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case BYTE_MAX_VALUE:
				javaWriter.constant(0xFF);
				break;
			case USHORT_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USHORT_MAX_VALUE:
				javaWriter.constant(0xFFFF);
				break;
			case UINT_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case UINT_MAX_VALUE:
				javaWriter.constant(-1);
				break;
			case ULONG_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case ULONG_MAX_VALUE:
				javaWriter.constant(-1L);
				break;
			case USIZE_MIN_VALUE:
				javaWriter.iConst0();
				break;
			case USIZE_MAX_VALUE:
				javaWriter.constant(-2);
				break;
			case USIZE_BITS:
				javaWriter.constant(32);
				break;
/*			case ENUM_VALUES: {
				DefinitionTypeID type = (DefinitionTypeID) ((ArrayTypeID) expression.type).elementType;
				JavaClass cls = context.getJavaClass(type.definition);
				javaWriter.invokeStatic(JavaNativeMethod.getNativeStatic(cls, "values", "()[L" + cls.internalName + ";"));
				break;
			}*/
			default:
				throw new UnsupportedOperationException("Unknown builtin: " + field);
		}
		return null;
	}

	@Override
	public Void builtinStaticSet(BuiltinFieldSymbol field, Expression value) {
		throw new UnsupportedOperationException("Unknown builtin: " + field);
	}
}
