package org.openzen.zenscript.javabytecode.compiler;

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
        return Type.getType("L" + definition + ";");
    }

    @Override
    public Type visitGeneric(GenericTypeID generic) {
        return Type.getType(generic.accept(JavaTypeClassVisitor.INSTANCE));
    }

    @Override
    public Type visitRange(RangeTypeID range) {
        return Type.getType(range.accept(JavaTypeClassVisitor.INSTANCE));
    }

    @Override
    public Type visitConst(ConstTypeID type) {
        return Type.getType(type.accept(JavaTypeClassVisitor.INSTANCE));
    }

    @Override
    public Type visitOptional(OptionalTypeID optional) {
        return Type.getType(optional.accept(JavaTypeClassVisitor.INSTANCE));
    }

	@Override
	public Type visitGenericMap(GenericMapTypeID map) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
