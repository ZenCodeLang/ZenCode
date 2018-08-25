package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.generic.GenericParameterBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.type.*;

public class JavaTypeInternalNameVisitor implements ITypeVisitor<String> {
	private final JavaTypeInternalNameVisitor forOptional;
	private final JavaSyntheticClassGenerator generator;
	private final boolean optional;
	
	public JavaTypeInternalNameVisitor(JavaSyntheticClassGenerator generator) {
		this(generator, false);
	}
	
	private JavaTypeInternalNameVisitor(JavaSyntheticClassGenerator generator, boolean optional) {
		this.optional = optional;
		this.generator = generator;
		forOptional = optional ? this : new JavaTypeInternalNameVisitor(generator, true);
	}
	
    @Override
    public String visitBasic(BasicTypeID basic) {
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
				case STRING: return "java/lang/String";
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
				case STRING: return "java/lang/String";
				default:
					throw new IllegalArgumentException("Not a valid type: " + basic);
			}
		}
    }

    @Override
    public String visitArray(ArrayTypeID array) {
		return "[" + array.elementType.accept(this);
    }

    @Override
    public String visitAssoc(AssocTypeID assoc) {
		return "java/util/Map;";
    }

    @Override
    public String visitIterator(IteratorTypeID iterator) {
		return "java/lang/Iterator;";
    }

    @Override
    public String visitFunction(FunctionTypeID function) {
        return generator.synthesizeFunction(function).cls.internalName;
    }

    @Override
    public String visitDefinition(DefinitionTypeID definition) {
		return definition.definition.getTag(JavaClass.class).internalName;
    }

    @Override
    public String visitGeneric(GenericTypeID generic) {
		for (GenericParameterBound bound : generic.parameter.bounds) {
			if (bound instanceof ParameterTypeBound) {
				return ((ParameterTypeBound) bound).type.accept(this);
			}
		}
		
		return "java/lang/Object";
    }

    @Override
    public String visitRange(RangeTypeID range) {
		return generator.synthesizeRange(range).cls.internalName;
    }

    @Override
    public String visitModified(ModifiedTypeID modified) {
		if (modified.isOptional())
			return modified.withoutOptional().accept(forOptional);
		
		return modified.baseType.accept(this);
    }

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return "java/util/Map";
	}
}
