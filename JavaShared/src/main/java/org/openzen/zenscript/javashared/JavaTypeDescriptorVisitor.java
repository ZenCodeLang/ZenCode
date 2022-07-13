package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

import java.util.Arrays;

public class JavaTypeDescriptorVisitor implements TypeVisitor<String> {
	private final JavaTypeDescriptorVisitor forOptional;
	private final JavaContext context;
	private final boolean optional;

	public JavaTypeDescriptorVisitor(JavaContext context) {
		this(context, false);
	}

	private JavaTypeDescriptorVisitor(JavaContext context, boolean optional) {
		this.optional = optional;
		this.context = context;
		forOptional = optional ? this : new JavaTypeDescriptorVisitor(context, true);
	}

	public String process(TypeID type) {
		return type.accept(this);
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		if (optional) {
			switch (basic) {
				case BOOL:
					return "Ljava/lang/Boolean;";
				case CHAR:
					return "Ljava/lang/Character;";
				case BYTE:
					return "Ljava/lang/Integer;";
				case SBYTE:
					return "Ljava/lang/Byte;";
				case SHORT:
					return "Ljava/lang/Short;";
				case USHORT:
					return "Ljava/lang/Integer;";
				case INT:
					return "Ljava/lang/Integer;";
				case UINT:
					return "Ljava/lang/Integer;";
				case LONG:
					return "Ljava/lang/Long;";
				case ULONG:
					return "Ljava/lang/Long;";
				case USIZE:
					return "I"; // special case: optional usize fits in an int where null = -1
				case FLOAT:
					return "Ljava/lang/Float;";
				case DOUBLE:
					return "Ljava/lang/Double;";
				case STRING:
					return "Ljava/lang/String;";
				default:
					throw new IllegalArgumentException("Not a valid type: " + basic);
			}
		} else {
			switch (basic) {
				case VOID:
					return "V";
				case BOOL:
					return "Z";
				case CHAR:
					return "C";
				case BYTE:
					return "I";
				case SBYTE:
					return "B";
				case SHORT:
					return "S";
				case USHORT:
					return "I";
				case INT:
					return "I";
				case UINT:
					return "I";
				case LONG:
					return "J";
				case ULONG:
					return "J";
				case USIZE:
					return "I";
				case FLOAT:
					return "F";
				case DOUBLE:
					return "D";
				case STRING:
					return "Ljava/lang/String;";
				default:
					throw new IllegalArgumentException("Not a valid type: " + basic);
			}
		}
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		if (array.elementType == BasicTypeID.BYTE)
			return "[B"; // instead of int[], save memory, save compatibility
		else if (array.elementType == BasicTypeID.USHORT)
			return "[S"; // instead of int[], save memory
		else {
			char[] arrayDepth = new char[array.dimension];
			Arrays.fill(arrayDepth, '[');
			return new String(arrayDepth) + this.context.getDescriptor(array.elementType);
		}
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return "Ljava/util/Map;";
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		return "Ljava/lang/Iterator;";
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		if (function instanceof JavaFunctionalInterfaceTypeID) {
			return "L" + ((JavaFunctionalInterfaceTypeID) function).method.cls.internalName + ";";
		} else {
			return "L" + this.context.getFunction(function).getCls().internalName + ";";
		}
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		JavaClass cls = context.getJavaClass(definition.definition);
		return "L" + cls.internalName + ";";
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		for (TypeParameterBound bound : generic.parameter.bounds) {
			if (bound instanceof ParameterTypeBound)
				return process(((ParameterTypeBound) bound).type);
		}

		return "Ljava/lang/Object;";
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return "L" + this.context.getRange(range).cls.internalName + ";";
	}

	@Override
	public String visitOptional(OptionalTypeID modified) {
		return modified.withoutOptional().accept(forOptional);
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return "Ljava/util/Map;";
	}
}
