/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ITypeID {
	public static final ITypeID[] NONE = new ITypeID[0];
	
	public ITypeID getUnmodified();
	
	public ITypeID getNormalized();
	
	public <T> T accept(ITypeVisitor<T> visitor);
	
	public default ITypeID getSuperType(GlobalTypeRegistry registry) {
		return null;
	}
	
	public boolean isOptional();
	
	public default ITypeID getOptionalBase() {
		return this;
	}
	
	public boolean isConst();
	
	public default ITypeID unwrap() {
		return this;
	}
	
	public boolean hasDefaultValue();
	
	public boolean isObjectType();
	
	public default boolean isEnum() {
		return false;
	}
	
	public default boolean isVariant() {
		return false;
	}
	
	public default boolean isDefinition(HighLevelDefinition definition) {
		return false;
	}
	
	public ITypeID instance(GenericMapper mapper);
	
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters);
	
	// Infers type parameters for this type so it matches with targetType
	// returns false if that isn't possible
	public default boolean inferTypeParameters(LocalMemberCache cache, ITypeID targetType, Map<TypeParameter, ITypeID> mapping) {
		return accept(new MatchingTypeVisitor(cache, targetType, mapping));
	}
	
	public void extractTypeParameters(List<TypeParameter> typeParameters);

	public default boolean isDestructible() {
		return false;
	}
	
	public static class MatchingTypeVisitor implements ITypeVisitor<Boolean> {
		private final ITypeID type;
		private final Map<TypeParameter, ITypeID> mapping;
		private final LocalMemberCache cache;
		
		public MatchingTypeVisitor(LocalMemberCache cache, ITypeID type, Map<TypeParameter, ITypeID> mapping) {
			this.type = type;
			this.mapping = mapping;
			this.cache = cache;
		}

		@Override
		public Boolean visitBasic(BasicTypeID basic) {
			return basic == type;
		}

		@Override
		public Boolean visitArray(ArrayTypeID array) {
			if (type instanceof ArrayTypeID) {
				ArrayTypeID arrayType = (ArrayTypeID) type;
				if (arrayType.dimension != array.dimension)
					return false;
				
				return match(arrayType.elementType, array.elementType);
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitAssoc(AssocTypeID assoc) {
			if (type instanceof AssocTypeID) {
				AssocTypeID assocType = (AssocTypeID) type;
				return match(assoc.keyType, assocType.keyType)
						&& match(assoc.valueType, assocType.valueType);
			} else {
				return false;
			}
		}
		
		@Override
		public Boolean visitIterator(IteratorTypeID iterator) {
			if (type instanceof IteratorTypeID) {
				IteratorTypeID iteratorType = (IteratorTypeID) type;
				if (iteratorType.iteratorTypes.length != iterator.iteratorTypes.length)
					return false;
				
				boolean result = true;
				for (int i = 0; i < iteratorType.iteratorTypes.length; i++)
					result = result && match(iterator.iteratorTypes[i], iteratorType.iteratorTypes[i]);
				
				return result;
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitFunction(FunctionTypeID function) {
			if (type instanceof FunctionTypeID) {
				FunctionTypeID functionType = (FunctionTypeID) type;
				if (functionType.header.parameters.length != function.header.parameters.length)
					return false;
				
				if (!match(functionType.header.returnType, function.header.returnType))
					return false;
				
				for (int i = 0; i < function.header.parameters.length; i++) {
					if (!match(functionType.header.parameters[i].type, function.header.parameters[i].type))
						return false;
				}
				
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitDefinition(DefinitionTypeID definition) {
			if (type instanceof DefinitionTypeID) {
				DefinitionTypeID definitionType = (DefinitionTypeID) type;
				if (definitionType.definition != definition.definition)
					return false;
				
				if (definition.typeParameters != null) {
					for (int i = 0; i < definitionType.typeParameters.length; i++) {
						if (!match(definitionType.typeParameters[i], definition.typeParameters[i]))
							return false;
					}
				}
				
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitGeneric(GenericTypeID generic) {
			if (mapping.containsKey(generic.parameter)) {
				return mapping.get(generic.parameter) == type;
			} else if (generic.matches(cache, type)) {
				mapping.put(generic.parameter, type);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitRange(RangeTypeID range) {
			if (type instanceof RangeTypeID) {
				RangeTypeID rangeType = (RangeTypeID) type;
				return match(rangeType.from, range.from) && match(rangeType.to, range.to);
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitModified(ModifiedTypeID type) {
			if (this.type instanceof ModifiedTypeID) {
				ModifiedTypeID constType = (ModifiedTypeID) this.type;
				return match(constType.baseType, type.baseType);
			} else {
				return false;
			}
		}
		
		private boolean match(ITypeID type, ITypeID pattern) {
			if (type == pattern)
				return true;
			
			return pattern.accept(new MatchingTypeVisitor(cache, type, mapping));
		}

		@Override
		public Boolean visitGenericMap(GenericMapTypeID map) {
			return map == type; // TODO: improve this
		}
	}
}
