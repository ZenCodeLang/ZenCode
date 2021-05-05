package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.codemodel.type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class JavaTypeGenericVisitor implements TypeVisitor<String> {

	private final JavaContext context;

	public JavaTypeGenericVisitor(JavaContext context) {
		this.context = context;
	}

	public String getGenericSignature(TypeID... types) {
		if (types == null || types.length == 0)
			return "";
		final StringBuilder builder = new StringBuilder();
		for (TypeID type : types)
			builder.append(type.accept(this));

		return builder.toString();
	}

	public String getGenericSignature(TypeParameter... parameters) {
		if (parameters == null || parameters.length == 0)
			return "";

		final StringBuilder builder = new StringBuilder();
		for (TypeParameter parameter : parameters) {
			builder.append(parameter.name).append(":").append(getGenericBounds(parameter.bounds));
		}
		return builder.toString();
	}

	public String getSignatureWithBound(TypeID type) {
		if (type instanceof GenericTypeID) {
			final TypeParameter parameter = ((GenericTypeID) type).parameter;
			return parameter.name + ":" + getGenericBounds(parameter.bounds);
		}
		throw new IllegalStateException("Type " + type + " is of the wrong class");
	}

	private String getGenericSignature(FunctionParameter... parameters) {
		if (parameters == null || parameters.length == 0)
			return "";
		final StringBuilder builder = new StringBuilder();
		for (FunctionParameter parameter : parameters) {
			builder.append(parameter.type.accept(this));
		}
		return builder.toString();
	}

	public String getGenericMethodSignature(FunctionHeader header) {
		return "(" + getGenericSignature(header.parameters) + ")" +
				getGenericSignature(header.getReturnType());
	}

	public String getGenericMethodSignature(FunctionHeader header, boolean addGenerics) {
		final StringBuilder sb = new StringBuilder();
		final boolean doGenerics = addGenerics && header.typeParameters.length > 0;

		if (doGenerics) {
			sb.append("<");
			for (TypeParameter typeParameter : header.typeParameters) {
				//TODO: Eventually replace with upper bound
				sb.append(typeParameter.name).append(":").append("Ljava/lang/Object;");
			}
			sb.append(">");
		}


		sb.append("(");
		if (doGenerics) {
			for (TypeParameter typeParameter : header.typeParameters) {
				//TODO: Eventually replace with -TT; or +TT; for "? super T" and "? extends T"
				sb.append("Ljava/lang/Class<T").append(typeParameter.name).append(";>;");
			}
		}

		sb.append(getGenericSignature(header.parameters));
		sb.append(")");
		sb.append(getGenericSignature(header.getReturnType()));
		return sb.toString();
	}


	public String getGenericBounds(Collection<TypeParameterBound> collection) {
		if (collection == null)
			return "";
		for (TypeParameterBound parameterBound : collection) {
			String s = parameterBound.accept(new GenericParameterBoundVisitor<String>() {
				@Override
				public String visitSuper(ParameterSuperBound bound) {
					return null;
				}

				@Override
				public String visitType(ParameterTypeBound bound) {
					return bound.type.accept(JavaTypeGenericVisitor.this);
				}
			});
			if (s != null)
				return s;
		}
		return "Ljava/lang/Object;";
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		return this.context.getDescriptor(basic);
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		final char[] dim = new char[array.dimension];
		Arrays.fill(dim, '[');
		return new String(dim) + this.context.getSignature(array.elementType);
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return "Ljava/util/Map<"
				+ assoc.keyType.accept(this)
				+ assoc.valueType.accept(this)
				+ ">;";
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return this.context.getDescriptor(map);
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		return this.context.getDescriptor(iterator);
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		final JavaSynthesizedFunctionInstance function1 = this.context.getFunction(function);
		if (function1.typeArguments == null || function1.typeArguments.length == 0) {
			return this.context.getDescriptor(function);
		}

		StringBuilder sb = new StringBuilder("L").append(function1.getCls().internalName).append("<");
		for (TypeID typeArgument : function1.typeArguments) {
			final String n = typeArgument instanceof GenericTypeID
					? ((GenericTypeID) typeArgument).parameter.name
					: "Ljava/lang/Object"; //Can latter even happen?

			sb.append("T").append(n).append(";");
		}

		return sb.append(">;").toString();
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		JavaClass cls = this.context.getJavaClass(definition.definition);
		StringBuilder builder = new StringBuilder("L").append(cls.internalName);

		if (definition.typeArguments.length > 0) {
			builder.append("<");
			for (TypeID typeParameter : definition.typeArguments) {
				builder.append(typeParameter.accept(this));
			}
			builder.append(">");
		}

		return builder.append(";").toString();
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		return "T" + generic.parameter.name + ";";
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return this.context.getDescriptor(range);
	}

	@Override
	public String visitOptional(OptionalTypeID type) {
		return type.baseType.accept(this);
	}

	public String getMethodSignatureExpansion(FunctionHeader header, TypeID expandedClass) {
		final StringBuilder stringBuilder = new StringBuilder();
		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);
		for (TypeParameter typeParameter : header.typeParameters) {
			if (!typeParameters.contains(typeParameter)) {
				typeParameters.add(typeParameter);
			}
		}

		if (typeParameters.size() != 0) {
			stringBuilder.append("<");
			for (TypeParameter typeParameter : typeParameters) {
				stringBuilder.append(typeParameter.name);
				stringBuilder.append(":Ljava/lang/Object;");
			}
			stringBuilder.append(">");
		}
		stringBuilder.append("(");
		stringBuilder.append(context.getSignature(expandedClass));
		for (TypeParameter typeParameter : typeParameters) {
			stringBuilder.append("Ljava/lang/Class<T");
			stringBuilder.append(typeParameter.name);
			stringBuilder.append(";>;");
		}
		stringBuilder.append(getGenericSignature(header.parameters));
		stringBuilder.append(")");
		stringBuilder.append(context.getSignature(header.getReturnType()));

		return stringBuilder.toString();
	}
}
