package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.codemodel.type.*;

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
		return this.context.getDescriptor(array);
	}

	@Override
	public String visitAssoc(StoredType context, AssocTypeID assoc) {
		return this.context.getDescriptor(assoc);
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
		return this.context.getDescriptor(function);
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
