package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.type.*;

public class JavaTypeInternalNameVisitor implements TypeVisitorWithContext<StoredType, String, RuntimeException> {
	private final JavaTypeInternalNameVisitor forOptional;
	private final JavaContext context;
	private final boolean optional;
	
	public JavaTypeInternalNameVisitor(JavaContext context) {
		this(context, false);
	}
	
	private JavaTypeInternalNameVisitor(JavaContext context, boolean optional) {
		this.optional = optional;
		this.context = context;
		forOptional = optional ? this : new JavaTypeInternalNameVisitor(context, true);
	}
	
    @Override
    public String visitBasic(StoredType context, BasicTypeID basic) {
		if (optional) {
			switch (basic) {
				case BOOL: return "java/lang/Boolean";
				case CHAR: return "java/lang/Character";
				case BYTE: return "java/lang/Integer";
				case SBYTE: return "java/lang/Byte";
				case SHORT: return "java/lang/Short";
				case USHORT: return "java/lang/Integer";
				case INT: return "java/lang/Integer";
				case UINT: return "java/lang/Integer";
				case LONG: return "java/lang/Long";
				case ULONG: return "java/lang/Long";
				case USIZE: return "java/lang/Integer";
				case FLOAT: return "java/lang/Float";
				case DOUBLE: return "java/lang/Double";
				default:
					throw new IllegalArgumentException("Not a valid type: " + basic);
			}
		} else {
			switch (basic) {
				case VOID: return "V";
				case BOOL: return "Z";
				case CHAR: return "C";
				case BYTE: return "I";
				case SBYTE: return "B";
				case SHORT: return "S";
				case USHORT: return "I";
				case INT: return "I";
				case UINT: return "I";
				case LONG: return "J";
				case ULONG: return "J";
				case USIZE: return "I";
				case FLOAT: return "F";
				case DOUBLE: return "D";
				default:
					throw new IllegalArgumentException("Not a valid type: " + basic);
			}
		}
    }
	
	@Override
	public String visitString(StoredType context, StringTypeID string) {
		return "java/lang/String";
	}

    @Override
    public String visitArray(StoredType context, ArrayTypeID array) {
		return "[" + array.elementType.type.accept(array.elementType, this);
    }

    @Override
    public String visitAssoc(StoredType context, AssocTypeID assoc) {
		return "java/util/Map;";
    }

    @Override
    public String visitIterator(StoredType context, IteratorTypeID iterator) {
		return "java/lang/Iterator;";
    }

    @Override
    public String visitFunction(StoredType context, FunctionTypeID function) {
        return this.context.getFunction(function).getCls().internalName;
    }

    @Override
    public String visitDefinition(StoredType context, DefinitionTypeID definition) {
		return this.context.getJavaClass(definition.definition).internalName;
    }

    @Override
    public String visitGeneric(StoredType context, GenericTypeID generic) {
		for (TypeParameterBound bound : generic.parameter.bounds) {
			if (bound instanceof ParameterTypeBound) {
				return ((ParameterTypeBound) bound).type.accept(null, this);
			}
		}
		
		return "java/lang/Object";
    }

    @Override
    public String visitRange(StoredType context, RangeTypeID range) {
		return this.context.getRange(range).cls.internalName;
    }

    @Override
    public String visitOptional(StoredType context, OptionalTypeID modified) {
		if (modified.isOptional())
			return modified.withoutOptional().accept(context, forOptional);
		
		return modified.baseType.accept(context, this);
    }

	@Override
	public String visitGenericMap(StoredType context, GenericMapTypeID map) {
		return "java/util/Map";
	}
}
