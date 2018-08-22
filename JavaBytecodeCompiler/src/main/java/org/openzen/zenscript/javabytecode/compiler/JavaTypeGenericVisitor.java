package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.codemodel.type.*;

import java.util.Collection;

public class JavaTypeGenericVisitor implements ITypeVisitor<String> {

	public static final JavaTypeGenericVisitor INSTANCE = new JavaTypeGenericVisitor();


	public static String getGenericSignature(ITypeID... types) {
		if (types == null || types.length == 0)
			return "";
		final StringBuilder builder = new StringBuilder();
		for (ITypeID type : types) {
			builder.append(type.accept(INSTANCE));
		}

		return builder.toString();
	}


	public static String getGenericSignature(TypeParameter... parameters) {
		if (parameters == null || parameters.length == 0)
			return "";

		final StringBuilder builder = new StringBuilder();
		for (TypeParameter parameter : parameters) {
			builder.append(parameter.name).append(":").append(getGenericBounds(parameter.bounds));
		}
		return builder.toString();
	}

	public static String getSignatureWithBound(ITypeID type) {
		if(type instanceof GenericTypeID){
			final TypeParameter parameter = ((GenericTypeID) type).parameter;
			return parameter.name + ":" + getGenericBounds(parameter.bounds);
		}
		throw new IllegalStateException("Type " + type + " is of the wrong class");
	}

	private static String getGenericSignature(FunctionParameter... parameters) {
		if(parameters == null || parameters.length == 0)
			return "";
		final StringBuilder builder = new StringBuilder();
		for (FunctionParameter parameter : parameters) {
			builder.append(parameter.type.accept(INSTANCE));
		}
		return builder.toString();
	}

	public static String getGenericMethodSignature(FunctionHeader header) {
		return "(" + getGenericSignature(header.parameters) +
				")" +
				getGenericSignature(header.returnType);
	}


	public static String getGenericBounds(Collection<GenericParameterBound> collection) {
		if (collection == null)
			return "";
		for (GenericParameterBound parameterBound : collection) {
			String s = parameterBound.accept(new GenericParameterBoundVisitor<String>() {
				@Override
				public String visitSuper(ParameterSuperBound bound) {
					return null;
				}

				@Override
				public String visitType(ParameterTypeBound bound) {
					return bound.type.accept(INSTANCE);
				}
			});
			if (s != null)
				return s;
		}
		return "Ljava/lang/Object;";
	}


	private JavaTypeGenericVisitor() {
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		return basic.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		return array.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		return assoc.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		return map.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		return iterator.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		return function.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		StringBuilder builder = new StringBuilder("L").append(definition.definition.name);

		if (definition.typeParameters.length > 0) {
			builder.append("<");
			for (ITypeID typeParameter : definition.typeParameters) {
				builder.append(typeParameter.accept(this));
			}
			builder.append(">");
		}

		return builder.append(";").toString();
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		return "T" + generic.parameter.name + ";";
		//return generic.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return range.accept(JavaTypeVisitor.INSTANCE).getDescriptor();
	}

	@Override
	public String visitModified(ModifiedTypeID type) {
		return type.baseType.accept(this);
	}
}
