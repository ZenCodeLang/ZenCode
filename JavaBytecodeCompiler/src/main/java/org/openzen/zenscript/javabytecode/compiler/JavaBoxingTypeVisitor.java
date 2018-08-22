package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javashared.JavaClass;

public class JavaBoxingTypeVisitor implements ITypeVisitor<JavaMethodInfo> {

	private final JavaWriter writer;

	public JavaBoxingTypeVisitor(JavaWriter writer) {
		this.writer = writer;
	}

	@Override
	public JavaMethodInfo visitBasic(BasicTypeID basic) {
		final JavaMethodInfo info;
		switch (basic) {
			case BOOL:
				writer.newObject(Boolean.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Boolean", JavaClass.Kind.CLASS), "<init>", "(Z)V", -1);
				break;
			case BYTE:
			case SBYTE:
				writer.newObject(Byte.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Byte", JavaClass.Kind.CLASS), "<init>", "(B)V", -1);
				break;
			case SHORT:
			case USHORT:
				writer.newObject(Short.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Short", JavaClass.Kind.CLASS), "<init>", "(S)V", -1);
				break;
			case INT:
			case UINT:
				writer.newObject(Byte.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Byte", JavaClass.Kind.CLASS), "<init>", "(B)V", -1);
				break;
			case LONG:
			case ULONG:
				writer.newObject(Long.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Long", JavaClass.Kind.CLASS), "<init>", "(S)V", -1);
				break;
			case FLOAT:
				writer.newObject(Float.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Float", JavaClass.Kind.CLASS), "<init>", "(F)V", -1);
				break;
			case DOUBLE:
				writer.newObject(Double.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Double", JavaClass.Kind.CLASS), "<init>", "(D)V", -1);
				break;
			case CHAR:
				writer.newObject(Character.class);
				info = new JavaMethodInfo(new JavaClass("java.lang", "Character", JavaClass.Kind.CLASS), "<init>", "(C)V", -1);
				break;
			default:
				return null;
		}
		writer.dup();
		return info;
	}

	@Override
	public JavaMethodInfo visitArray(ArrayTypeID array) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitAssoc(AssocTypeID assoc) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitGenericMap(GenericMapTypeID map) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitIterator(IteratorTypeID iterator) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitFunction(FunctionTypeID function) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitDefinition(DefinitionTypeID definition) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitGeneric(GenericTypeID generic) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitRange(RangeTypeID range) {
		//NO-OP
		return null;
	}

	@Override
	public JavaMethodInfo visitModified(ModifiedTypeID type) {
		//NO-OP
		return null;
	}
}
