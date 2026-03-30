package org.openzen.zenscript.codemodel.type.builtin;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CastedExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.expression.CompareExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class BasicTypeMembers {
	private static final MethodID CONSTRUCTOR = MethodID.staticOperator(OperatorType.CONSTRUCTOR);
	private static final MethodID COMPARE = MethodID.operator(OperatorType.COMPARE);

	public static ResolvingType get(BasicTypeID type) {
		switch (type) {
			case VOID:
			case NULL:
			case UNDETERMINED:
			default:
				MemberSet.Builder builder = MemberSet.create(type);
				setup(builder, type);
				return builder.build();
		}
	}

	private static void setup(MemberSet.Builder builder, BasicTypeID type) {
		for (BuiltinMethodSymbol method : BuiltinMethodSymbol.values()) {
			if (method.getDefiningType().equals(type) && method.getID().equals(COMPARE)) {
				comparator(builder);
			} else if (method.getDefiningType().equals(type) && method.getID().equals(CONSTRUCTOR)) {
				builder.constructor(new MethodInstance(method));
			}/* else if (method.getDefiningType() == type) {
				builder.method(new MethodInstance(method));
			}*/
			if (method.getDefiningType() == type) {
				builder.method(new MethodInstance(method));
				if (method.useWideningConversions) {
					for (MethodInstance widened : getWideningMethodInstances(method)) {
						builder.method(widened);
					}
				}
			}
			if (method.useWideningConversions) {
				method.getDefiningType().asType().ifPresent(definingType -> {
					BasicTypeID basicDefiningType = (BasicTypeID) definingType;
					TypeID[] wideningSources = getWideningSources(basicDefiningType);
					for (TypeID source : wideningSources) {
						if (source == type) {
							builder.method(new MethodInstance(method, method.getHeader(), basicDefiningType, true));
						}
					}
				});
			}
		}

		for (BuiltinFieldSymbol field : BuiltinFieldSymbol.values()) {
			if (field.getDefiningType() == type) {
				builder.field(new FieldInstance(field));
			}
		}
	}

	private static void comparator(MemberSet.Builder builder) {
		builder.comparator(((compiler, position, left, right, type) -> {
			Expression rightCompiled = right.eval();
			Optional<TypeID> union = compiler.union(left.type, rightCompiled.type);
			return union.map(typeID -> {
				CastedEval cast = new CastedEval(compiler, left.position, typeID, false, false);
				Expression castedLeft = cast.of(left).value;
				Expression castedRight = cast.of(rightCompiled).value;
				Expression value = new CompareExpression(
						position,
						castedLeft,
						castedRight,
						new MethodInstance(getComparator((BasicTypeID) typeID)),
						type);
				return new CastedExpression(CastedExpression.Level.EXACT, value);
			}).orElseGet(() -> new CastedExpression(CastedExpression.Level.INVALID, compiler.at(position).invalid(CompileErrors.cannotCompare(left.type, rightCompiled.type))));
		}));
	}

	private static BuiltinMethodSymbol getComparator(BasicTypeID type) {
		switch (type) {
			case BYTE: return BuiltinMethodSymbol.BYTE_COMPARE;
			case SBYTE: return BuiltinMethodSymbol.SBYTE_COMPARE;
			case SHORT: return BuiltinMethodSymbol.SHORT_COMPARE;
			case USHORT: return BuiltinMethodSymbol.USHORT_COMPARE;
			case INT: return BuiltinMethodSymbol.INT_COMPARE;
			case UINT: return BuiltinMethodSymbol.UINT_COMPARE;
			case LONG: return BuiltinMethodSymbol.LONG_COMPARE;
			case ULONG: return BuiltinMethodSymbol.ULONG_COMPARE;
			case USIZE: return BuiltinMethodSymbol.USIZE_COMPARE;
			case FLOAT: return BuiltinMethodSymbol.FLOAT_COMPARE;
			case DOUBLE: return BuiltinMethodSymbol.DOUBLE_COMPARE;
			case CHAR: return BuiltinMethodSymbol.CHAR_COMPARE;
			case STRING: return BuiltinMethodSymbol.STRING_COMPARE;
			default: throw new IllegalArgumentException("No comparator for " + type);
		}
	}

	private static MethodInstance[] getWideningMethodInstances(BuiltinMethodSymbol method) {
		FunctionHeader original = method.getHeader();
		if (original.parameters.length != 1)
			throw new IllegalArgumentException("Not doing widening on multiple method arguments");

		FunctionParameter originalParameter = original.parameters[0];
		TypeID[] wideningSources = getWideningSources((BasicTypeID) originalParameter.type);
		MethodInstance[] wideningMethodInstances = new MethodInstance[wideningSources.length];
		for (int i = 0; i < wideningSources.length; i++) {
			FunctionParameter parameter = new FunctionParameter(wideningSources[i], originalParameter.name, false);
			FunctionHeader header = new FunctionHeader(original.typeParameters, original.getReturnType(), original.thrownType, parameter);
			wideningMethodInstances[i] = new MethodInstance(method, header, method.getTargetType(), true);
		}
		return wideningMethodInstances;
	}

	private static TypeID[] getWideningSources(BasicTypeID type) {
		switch (type) {
			case BYTE:
			case SBYTE:
				return TypeID.NONE;
			case SHORT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE};
			case USHORT:
				return new TypeID[]{BasicTypeID.BYTE};
			case INT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT};
			case UINT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.USHORT};
			case USIZE:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.USHORT, BasicTypeID.UINT};
			case LONG:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT, BasicTypeID.INT, BasicTypeID.USIZE};
			case ULONG:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.USHORT, BasicTypeID.UINT, BasicTypeID.USIZE};
			case FLOAT:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT, BasicTypeID.INT, BasicTypeID.UINT, BasicTypeID.LONG, BasicTypeID.ULONG, BasicTypeID.USIZE};
			case DOUBLE:
				return new TypeID[]{BasicTypeID.BYTE, BasicTypeID.SBYTE, BasicTypeID.SHORT, BasicTypeID.USHORT, BasicTypeID.INT, BasicTypeID.UINT, BasicTypeID.LONG, BasicTypeID.ULONG, BasicTypeID.USIZE, BasicTypeID.FLOAT};
			default:
				return TypeID.NONE;
		}
	}

	private BasicTypeMembers() {
	}
}
