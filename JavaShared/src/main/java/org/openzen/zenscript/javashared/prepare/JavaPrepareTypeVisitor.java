package org.openzen.zenscript.javashared.prepare;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.JavaContext;

public class JavaPrepareTypeVisitor implements TypeVisitor<Void> {
	private final JavaContext context;

	public JavaPrepareTypeVisitor(JavaContext context) {
		this.context = context;
	}

	@Override
	public Void visitBasic(BasicTypeID basic) {
		return null;
	}

	@Override
	public Void visitArray(ArrayTypeID array) {
		return array.elementType.accept(this);
	}

	@Override
	public Void visitAssoc(AssocTypeID assoc) {
		assoc.keyType.accept(this);
		assoc.valueType.accept(this);
		return null;
	}

	@Override
	public Void visitGenericMap(GenericMapTypeID map) {
		map.value.accept(this);
		return null;
	}

	@Override
	public Void visitIterator(IteratorTypeID iterator) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionTypeID function) {
		context.getFunction(function);
		return null;
	}

	@Override
	public Void visitDefinition(DefinitionTypeID definition) {
		for (TypeID argument : definition.typeArguments)
			argument.accept(this);
		return null;
	}

	@Override
	public Void visitGeneric(GenericTypeID generic) {
		return null;
	}

	@Override
	public Void visitRange(RangeTypeID range) {
		return null;
	}

	@Override
	public Void visitOptional(OptionalTypeID type) {
		type.baseType.accept(this);
		return null;
	}
}
