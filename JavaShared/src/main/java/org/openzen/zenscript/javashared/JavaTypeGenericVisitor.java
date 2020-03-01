package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

import java.util.Arrays;
import java.util.Collection;

public class JavaTypeGenericVisitor implements TypeVisitorWithContext<StoredType, String, RuntimeException> {

	private final JavaContext context;

	public JavaTypeGenericVisitor(JavaContext context) {
		this.context = context;
	}
	
	public String getGenericSignature(StoredType... types) {
		if (types == null || types.length == 0)
			return "";
		final StringBuilder builder = new StringBuilder();
		for (StoredType type : types)
			builder.append(type.type.accept(type, this));

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
		if (type instanceof GenericTypeID){
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
			builder.append(parameter.type.type.accept(parameter.type, this));
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

		if(doGenerics) {
			sb.append("<");
			for (TypeParameter typeParameter : header.typeParameters) {
				//TODO: Eventually replace with upper bound
				sb.append(typeParameter.name).append(":").append("Ljava/lang/Object;");
			}
			sb.append(">");
		}


		sb.append("(");
		if(doGenerics) {
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
					return bound.type.accept(null, JavaTypeGenericVisitor.this);
				}
			});
			if (s != null)
				return s;
		}
		return "Ljava/lang/Object;";
	}

	@Override
	public String visitBasic(StoredType context, BasicTypeID basic) {
		return this.context.getDescriptor(basic);
	}
	
	@Override
	public String visitString(StoredType context, StringTypeID string) {
		return this.context.getDescriptor(string);
	}

	@Override
	public String visitArray(StoredType context, ArrayTypeID array) {
		final char[] dim = new char[array.dimension];
		Arrays.fill(dim, '[');
		return new String(dim) + this.context.getSignature(array.elementType);
	}

	@Override
	public String visitAssoc(StoredType context, AssocTypeID assoc) {
		return "Ljava/util/Map<"
				+ assoc.keyType.type.accept(context, this)
				+ assoc.valueType.type.accept(context, this)
				+ ">;";
	}

	@Override
	public String visitGenericMap(StoredType context, GenericMapTypeID map) {
		return this.context.getDescriptor(map);
	}

	@Override
	public String visitIterator(StoredType context, IteratorTypeID iterator) {
		return this.context.getDescriptor(iterator);
	}

	@Override
	public String visitFunction(StoredType context, FunctionTypeID function) {
		final JavaSynthesizedFunctionInstance function1 = this.context.getFunction(function);
		if(function1.typeArguments == null || function1.typeArguments.length == 0) {
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
	public String visitDefinition(StoredType context, DefinitionTypeID definition) {
		JavaClass cls = this.context.getJavaClass(definition.definition);
		StringBuilder builder = new StringBuilder("L").append(cls.internalName);

		if (definition.typeArguments.length > 0) {
			builder.append("<");
			for (StoredType typeParameter : definition.typeArguments) {
				builder.append(typeParameter.type.accept(null, this));
			}
			builder.append(">");
		}

		return builder.append(";").toString();
	}

	@Override
	public String visitGeneric(StoredType context, GenericTypeID generic) {
		return "T" + generic.parameter.name + ";";
	}

	@Override
	public String visitRange(StoredType context, RangeTypeID range) {
		return this.context.getDescriptor(range);
	}

	@Override
	public String visitOptional(StoredType context, OptionalTypeID type) {
		return type.baseType.accept(context, this);
	}
}
