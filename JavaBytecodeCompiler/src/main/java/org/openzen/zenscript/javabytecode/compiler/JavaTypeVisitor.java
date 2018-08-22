package org.openzen.zenscript.javabytecode.compiler;

import java.util.Map;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.type.*;

public class JavaTypeVisitor implements ITypeVisitor<Type> {
	public static final JavaTypeVisitor INSTANCE = new JavaTypeVisitor();

	@Override
	public Type visitBasic(BasicTypeID basic) {
		return Type.getType(basic.accept(JavaTypeClassVisitor.INSTANCE));
	}

	@Override
	public Type visitArray(ArrayTypeID array) {
		return Type.getType(array.accept(JavaTypeClassVisitor.INSTANCE));
	}

	@Override
	public Type visitAssoc(AssocTypeID assoc) {
		return Type.getType(assoc.accept(JavaTypeClassVisitor.INSTANCE));
	}

	@Override
	public Type visitIterator(IteratorTypeID iterator) {
		return Type.getType(iterator.accept(JavaTypeClassVisitor.INSTANCE));
	}

	@Override
	public Type visitFunction(FunctionTypeID function) {
		Class clazz = function.accept(JavaTypeClassVisitor.INSTANCE);
		return clazz != null ? Type.getType(clazz) : Type.getType("L" + CompilerUtils.getLambdaInterface(function.header) + ";");
	}

	@Override
	public Type visitDefinition(DefinitionTypeID definition) {
		return Type.getType("L" + definition.definition.name + ";");
	}

	@Override
	public Type visitGeneric(GenericTypeID generic) {
		final Class<?> clazz = generic.accept(JavaTypeClassVisitor.INSTANCE);
		return Type.getType(clazz == null ? Object.class : clazz);
	}

	@Override
	public Type visitRange(RangeTypeID range) {
		return Type.getType(range.accept(JavaTypeClassVisitor.INSTANCE));
	}

	@Override
	public Type visitModified(ModifiedTypeID optional) {
		return Type.getType(optional.accept(JavaTypeClassVisitor.INSTANCE));
	}

	@Override
	public Type visitGenericMap(GenericMapTypeID map) {
		return Type.getType(Map.class);
	}
}
