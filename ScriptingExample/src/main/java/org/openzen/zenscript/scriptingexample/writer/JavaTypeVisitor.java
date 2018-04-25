package org.openzen.zenscript.scriptingexample.writer;

import org.openzen.zenscript.codemodel.type.*;

public class JavaTypeVisitor implements ITypeVisitor<String> {
    @Override
    public String visitBasic(BasicTypeID basic) {
        switch (basic) {
            case VOID:
                return "V";
            case NULL:
            case ANY:
            case UNDETERMINED:
                return "Ljava/lang/Object;";
            case BOOL:
                return "Z";
            case BYTE:
            case SBYTE:
                return "B";
            case SHORT:
            case USHORT:
                return "S";
            case INT:
            case UINT:
                return "I";
            case LONG:
            case ULONG:
                return "J";
            case FLOAT:
                return "F";
            case DOUBLE:
                return "D";
            case CHAR:
                return "C";
            case STRING:
                return "Ljava/lang/String;";
        }
        return "";
    }

    @Override
    public String visitArray(ArrayTypeID array) {
        return "[" + array.elementType.accept(this);
    }

    @Override
    public String visitAssoc(AssocTypeID assoc) {
        return "Ljava/util/Map;";
    }

    @Override
    public String visitIterator(IteratorTypeID iterator) {
        return "java/lang/Iterable";
    }

    @Override
    public String visitFunction(FunctionTypeID function) {
        return null;
    }

    @Override
    public String visitDefinition(DefinitionTypeID definition) {

        return definition.definition.name.replaceAll("\"", "");
    }

    @Override
    public String visitGeneric(GenericTypeID generic) {
        return null;
    }

    @Override
    public String visitRange(RangeTypeID range) {
        return null;
    }

    @Override
    public String visitConst(ConstTypeID type) {
        return null;
    }

    @Override
    public String visitOptional(OptionalTypeID optional) {
        return null;
    }
}
