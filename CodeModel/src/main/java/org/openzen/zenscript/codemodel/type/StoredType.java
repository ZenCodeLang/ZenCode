/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class StoredType {
	public static final StoredType[] NONE = new StoredType[0];
	
	public final TypeID type;
	public final StorageTag storage;
	
	public StoredType(TypeID type, StorageTag storage) {
		if (storage == null)
			throw new NullPointerException();
		
		if (type.isValueType())
			storage = ValueStorageTag.INSTANCE;
		else if (storage == ValueStorageTag.INSTANCE)
			throw new IllegalArgumentException("storage of a nonvalue type cannot be value");
		
		this.type = type;
		this.storage = storage;
	}
	
	public StoredType getNormalized() {
		return type.getNormalized() == type ? this : new StoredType(type, storage);
	}
	
	public StoredType getSuperType(GlobalTypeRegistry registry) {
		TypeID superType = type.getSuperType(registry);
		return superType == null ? null : superType.stored(storage);
	}
	
	public StoredType instance(GenericMapper mapper) {
		TypeArgument result = type.instance(mapper, storage);
		return result == type ? this : result.stored();
	}
	
	public boolean isDestructible() {
		return type.isDestructible() && storage.isDestructible();
	}
	
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return type.isDestructible(scanning) && storage.isDestructible();
	}
	
	public boolean hasDefaultValue() {
		return type.hasDefaultValue();
	}
	
	public boolean isOptional() {
		return type.isOptional();
	}
	
	public boolean isConst() {
		return storage.isConst();
	}
	
	public boolean isImmutable() {
		return storage.isImmutable();
	}
	
	public boolean isBasic(BasicTypeID type) {
		return this.type == type;
	}
	
	public boolean isGeneric() {
		return type.isGeneric();
	}
	
	public StoredType withoutOptional() {
		return new StoredType(type.withoutOptional(), storage);
	}
	
	public StoredType withoutConst() {
		return new StoredType(type.withoutConst(), storage);
	}
	
	public StoredType withoutImmutable() {
		return new StoredType(type.withoutImmutable(), storage);
	}
	
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return type.hasInferenceBlockingTypeParameters(parameters);
	}
	
	// Infers type parameters for this type so it matches with targetType
	// returns false if that isn't possible
	public Map<TypeParameter, TypeArgument> inferTypeParameters(LocalMemberCache cache, TypeArgument targetType) {
		return type.inferTypeParameters(cache, targetType);
	}
	
	public boolean isVariant() {
		return type.isVariant();
	}
	
	public boolean isEnum() {
		return type.isEnum();
	}
	
	public boolean isDefinition(HighLevelDefinition definition) {
		return type.isDefinition(definition);
	}
	
	public DefinitionTypeID asDefinition() {
		return (DefinitionTypeID)type;
	}
	
	public TypeArgument asArgument() {
		return new TypeArgument(type, storage);
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + Objects.hashCode(this.type);
		hash = 41 * hash + Objects.hashCode(this.storage);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		
		final StoredType other = (StoredType) obj;
		return Objects.equals(this.type, other.type)
				&& Objects.equals(this.storage, other.storage);
	}
	
	@Override
	public String toString() {
		return type.toString(storage);
	}
	
	public static class MatchingTypeVisitor implements TypeVisitor<Boolean> {
		private final TypeID type;
		private final Map<TypeParameter, TypeArgument> mapping;
		private final LocalMemberCache cache;
		
		public MatchingTypeVisitor(LocalMemberCache cache, TypeID type, Map<TypeParameter, TypeArgument> mapping) {
			this.type = type;
			this.mapping = mapping;
			this.cache = cache;
		}

		@Override
		public Boolean visitBasic(BasicTypeID basic) {
			return basic == type;
		}
		
		@Override
		public Boolean visitString(StringTypeID string) {
			return string == type;
		}

		@Override
		public Boolean visitArray(ArrayTypeID array) {
			if (type instanceof ArrayTypeID) {
				ArrayTypeID arrayType = (ArrayTypeID) type;
				if (arrayType.dimension != array.dimension)
					return false;
				
				return match(arrayType.elementType, array.elementType.asArgument());
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitAssoc(AssocTypeID assoc) {
			if (type instanceof AssocTypeID) {
				AssocTypeID assocType = (AssocTypeID) type;
				return match(assocType.keyType, assoc.keyType.asArgument())
						&& match(assocType.valueType, assoc.valueType.asArgument());
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
					result = result && match(iterator.iteratorTypes[i], iteratorType.iteratorTypes[i].asArgument());
				
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
				
				if (!match(functionType.header.getReturnType(), function.header.getReturnType().asArgument()))
					return false;
				
				for (int i = 0; i < function.header.parameters.length; i++) {
					if (!match(functionType.header.parameters[i].type, function.header.parameters[i].type.asArgument()))
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
				
				if (definition.typeArguments != null) {
					for (int i = 0; i < definitionType.typeArguments.length; i++) {
						if (!match(definitionType.typeArguments[i].stored(), definition.typeArguments[i]))
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
			} else if (type == generic || generic.matches(cache, new TypeArgument(type, null))) {
				mapping.put(generic.parameter, new TypeArgument(type, null));
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitRange(RangeTypeID range) {
			if (type instanceof RangeTypeID) {
				RangeTypeID rangeType = (RangeTypeID) type;
				return match(rangeType.baseType, range.baseType.asArgument());
			} else {
				return false;
			}
		}

		@Override
		public Boolean visitModified(ModifiedTypeID type) {
			if (this.type instanceof ModifiedTypeID) {
				ModifiedTypeID constType = (ModifiedTypeID) this.type;
				return match(constType.baseType, new TypeArgument(type.baseType, null));
			} else {
				return false;
			}
		}
		
		private boolean match(StoredType type, TypeArgument pattern) {
			if (pattern.storage != null && type.storage != pattern.storage)
				return false;
			
			return TypeMatcher.match(cache, type.asArgument(), pattern) != null;
		}
		
		private boolean match(TypeID type, TypeArgument pattern) {
			return TypeMatcher.match(cache, new TypeArgument(type, null), pattern) != null;
		}

		@Override
		public Boolean visitGenericMap(GenericMapTypeID map) {
			return map == type; // TODO: improve this
		}
	}
}
