/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeMatcher implements TypeVisitorWithContext<TypeMatcher.Matching, Boolean, RuntimeException> {
	public static Map<TypeParameter, TypeID> match(LocalMemberCache cache, TypeID type, TypeID pattern) {
		Matching matching = new Matching(cache, type);
		if (pattern.accept(matching, INSTANCE))
			return matching.mapping;
		
		return null;
	}
	
	private static final TypeMatcher INSTANCE = new TypeMatcher();
	
	private TypeMatcher() {}
	
	@Override
	public Boolean visitBasic(Matching context, BasicTypeID basic) {
		return context.type == basic;
	}

	@Override
	public Boolean visitArray(Matching context, ArrayTypeID array) {
		if (context.type instanceof ArrayTypeID) {
			ArrayTypeID arrayType = (ArrayTypeID) context.type;
			if (arrayType.dimension != array.dimension)
				return false;

			return match(context, arrayType.elementType, array.elementType);
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitAssoc(Matching context, AssocTypeID assoc) {
		if (context.type instanceof AssocTypeID) {
			AssocTypeID assocType = (AssocTypeID) context.type;
			return match(context, assocType.keyType, assoc.keyType)
					&& match(context, assocType.valueType, assoc.valueType);
		} else {
			return false;
		}
	}
	
	@Override
	public Boolean visitInvalid(Matching context, InvalidTypeID invalid) {
		return false;
	}

	@Override
	public Boolean visitIterator(Matching context, IteratorTypeID iterator) {
		if (context.type instanceof IteratorTypeID) {
			IteratorTypeID iteratorType = (IteratorTypeID) context.type;
			if (iteratorType.iteratorTypes.length != iterator.iteratorTypes.length)
				return false;

			boolean result = true;
			for (int i = 0; i < iteratorType.iteratorTypes.length; i++)
				result = result && match(context, iterator.iteratorTypes[i], iteratorType.iteratorTypes[i]);

			return result;
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitFunction(Matching context, FunctionTypeID function) {
		if (context.type instanceof FunctionTypeID) {
			FunctionTypeID functionType = (FunctionTypeID) context.type;
			if (functionType.header.parameters.length != function.header.parameters.length)
				return false;

			if (!match(context, functionType.header.getReturnType(), function.header.getReturnType()))
				return false;

			for (int i = 0; i < function.header.parameters.length; i++) {
				if (!match(context, functionType.header.parameters[i].type, function.header.parameters[i].type))
					return false;
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitDefinition(Matching context, DefinitionTypeID definition) {
		if (context.type instanceof DefinitionTypeID) {
			DefinitionTypeID definitionType = (DefinitionTypeID) context.type;
			if (definitionType.definition != definition.definition)
				return false;

			if (definition.typeArguments != null) {
				for (int i = 0; i < definitionType.typeArguments.length; i++) {
					if (!match(context, definitionType.typeArguments[i], definition.typeArguments[i]))
						return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitGeneric(Matching context, GenericTypeID generic) {
		if (context.mapping.containsKey(generic.parameter)) {
			TypeID argument = context.mapping.get(generic.parameter);
			return argument == context.type;
		} else if (context.type == generic || generic.matches(context.cache, context.type)) {
			context.mapping.put(generic.parameter, context.type);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitRange(Matching context, RangeTypeID range) {
		if (context.type instanceof RangeTypeID) {
			RangeTypeID rangeType = (RangeTypeID) context.type;
			return match(context, rangeType.baseType, range.baseType);
		} else {
			return false;
		}
	}

	@Override
	public Boolean visitOptional(Matching context, OptionalTypeID type) {
		if (context.type instanceof OptionalTypeID) {
			OptionalTypeID modified = (OptionalTypeID) context.type;
			return match(context, modified.baseType, type.baseType);
		} else {
			return false;
		}
	}

	private boolean match(Matching context, TypeID type, TypeID pattern) {
		return pattern.accept(context.withType(type), this);
	}

	@Override
	public Boolean visitGenericMap(Matching context, GenericMapTypeID map) {
		return map == context.type; // TODO: improve this
	}
		
	public static final class Matching {
		public final LocalMemberCache cache;
		public final TypeID type;
		public final Map<TypeParameter, TypeID> mapping;
		
		public Matching(LocalMemberCache cache, TypeID type) {
			this.cache = cache;
			this.type = type;
			mapping = new HashMap<>();
		}
		
		private Matching(LocalMemberCache cache, TypeID type, Map<TypeParameter, TypeID> mapping) {
			this.cache = cache;
			this.type = type;
			this.mapping = mapping;
		}
		
		public Matching withType(TypeID type) {
			return new Matching(cache, type, mapping);
		}
	}
}
