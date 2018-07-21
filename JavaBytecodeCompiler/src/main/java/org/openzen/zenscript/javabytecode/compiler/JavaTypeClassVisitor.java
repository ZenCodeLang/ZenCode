package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javabytecode.JavaModule;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

public class JavaTypeClassVisitor implements ITypeVisitor<Class> {

    public static final JavaTypeClassVisitor INSTANCE = new JavaTypeClassVisitor();
	private final JavaOptionalTypeClassVisitor optional = new JavaOptionalTypeClassVisitor(this);

    @Override
    public Class visitBasic(BasicTypeID basic) {
        switch (basic) {
            case VOID:
                return void.class;
            case NULL:
            case UNDETERMINED:
                return Object.class;
            case BOOL:
                return boolean.class;
            case BYTE:
            case SBYTE:
                return byte.class;
            case SHORT:
            case USHORT:
                return short.class;
            case INT:
            case UINT:
                return int.class;
            case LONG:
            case ULONG:
                return long.class;
            case FLOAT:
                return float.class;
            case DOUBLE:
                return double.class;
            case CHAR:
                return char.class;
            case STRING:
                return String.class;
        }
        return Object.class;
    }

    @Override
    public Class visitArray(ArrayTypeID array) {
        return Array.newInstance(array.elementType.accept(this), 0).getClass();
    }

    @Override
    public Class visitAssoc(AssocTypeID assoc) {
        return Map.class;
    }

    @Override
    public Class visitIterator(IteratorTypeID iterator) {
        return Iterator.class;
    }

    @Override
    public Class visitFunction(FunctionTypeID function) {
        try {
            return new JavaModule().new ScriptClassLoader().loadClass(CompilerUtils.getLambdaInterface(function.header));
        } catch (ClassNotFoundException e) {
            return null;
        }
        //return function.header.returnType.accept(this);
    }

    @Override
    public Class visitDefinition(DefinitionTypeID definition) {
        return null;
    }

    @Override
    public Class visitGeneric(GenericTypeID generic) {
        return null;
    }

    @Override
    public Class visitRange(RangeTypeID range) {
        return null;
    }

    @Override
    public Class visitConst(ConstTypeID type) {
        return type.baseType.accept(this);
    }

    @Override
    public Class visitOptional(OptionalTypeID optional) {
        return optional.baseType.accept(this.optional);
    }

	@Override
	public Class visitGenericMap(GenericMapTypeID map) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
