/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaTypeParameterInfo;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Stan Hebben
 */
public class JavaTypeExpressionVisitor implements TypeVisitorWithContext<JavaWriter, Void, RuntimeException> {
	private final JavaBytecodeContext context;

	public JavaTypeExpressionVisitor(JavaBytecodeContext context) {
		this.context = context;
	}

	@Override
	public Void visitBasic(JavaWriter writer, BasicTypeID basic) {
		switch (basic) {
			case BOOL:
				writer.constant(Boolean.class);
				return null;
			case SBYTE:
				writer.constant(Byte.class);
				return null;
			case SHORT:
				writer.constant(Short.class);
				return null;
			case BYTE:
			case USHORT:
			case INT:
			case UINT:
			case USIZE:
				writer.constant(Integer.class);
				return null;
			case LONG:
			case ULONG:
				writer.constant(Long.class);
				return null;
			case FLOAT:
				writer.constant(Float.class);
				return null;
			case DOUBLE:
				writer.constant(Double.class);
				return null;
			case CHAR:
				writer.constant(Character.class);
				return null;
			case STRING:
				writer.constant(String.class);
				return null;
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public Void visitArray(JavaWriter writer, ArrayTypeID array) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Void visitAssoc(JavaWriter writer, AssocTypeID assoc) {
		writer.constant(Map.class);
		return null;
	}

	@Override
	public Void visitGenericMap(JavaWriter writer, GenericMapTypeID map) {
		writer.constant(Map.class);
		return null;
	}

	@Override
	public Void visitIterator(JavaWriter writer, IteratorTypeID iterator) {
		writer.constant(Iterator.class);
		return null;
	}

	@Override
	public Void visitFunction(JavaWriter writer, FunctionTypeID function) {
		writer.constantClass(context.getFunction(function).getCls());
		return null;
	}

	@Override
	public Void visitDefinition(JavaWriter writer, DefinitionTypeID definition) {
		writer.constantClass(context.getJavaClass(definition.definition));
		return null;
	}

	@Override
	public Void visitGeneric(JavaWriter writer, GenericTypeID generic) {
		JavaTypeParameterInfo info = context.target.getTypeParameterInfo(generic.parameter);
		if (info.field != null) {
			writer.loadObject(0); // this
			writer.getField(info.field);
		} else {
			writer.loadObject(info.parameterIndex);
		}
		return null;
	}

	@Override
	public Void visitRange(JavaWriter writer, RangeTypeID range) {
		writer.constantClass(context.getRange(range).cls);
		return null;
	}

	@Override
	public Void visitOptional(JavaWriter writer, OptionalTypeID type) {
		return type.baseType.accept(writer, this);
	}
}
