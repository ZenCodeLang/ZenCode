/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

/**
 *
 * @author Hoofdgebruiker
 */
public interface TypeID {
	static Map<TypeParameter, TypeID> getMapping(TypeParameter[] parameters, TypeID[] arguments) {
		Map<TypeParameter, TypeID> typeArguments = new HashMap<>();
		for (int i = 0; i < parameters.length; i++)
			typeArguments.put(parameters[i], arguments[i]);
		return typeArguments;
	}

	static Map<TypeParameter, TypeID> getSelfMapping(GlobalTypeRegistry registry, TypeParameter[] parameters) {
		Map<TypeParameter, TypeID> typeArguments = new HashMap<>();
		for (TypeParameter parameter : parameters)
			typeArguments.put(parameter, registry.getGeneric(parameter));
		return typeArguments;
	}

	TypeID[] NONE = new TypeID[0];
	
	default TypeID getSuperType(GlobalTypeRegistry registry) {
		return null;
	}
	
	TypeID instance(GenericMapper mapper);
	
	TypeID getNormalized();
	
	boolean isDestructible();
	
	boolean isDestructible(Set<HighLevelDefinition> scanning);
	
	boolean hasDefaultValue();
	
	boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters);
	
	default Expression getDefaultValue() {
		return null;
	}
	
	// Infers type parameters for this type so it matches with targetType
	// returns false if that isn't possible
	default Map<TypeParameter, TypeID> inferTypeParameters(LocalMemberCache cache, TypeID targetType) {
		return TypeMatcher.match(cache, this, targetType);
	}
	
	void extractTypeParameters(List<TypeParameter> typeParameters);
	
	<R> R accept(TypeVisitor<R> visitor);
	
	<C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E;
	
	default boolean isOptional() {
		return false;
	}
	
	default boolean isGeneric() {
		return false;
	}
	
	boolean isValueType();
	
	default TypeID withoutOptional() {
		throw new UnsupportedOperationException("Not an optional type");
	}
	
	default boolean isVariant() {
		return false;
	}
	
	default boolean isEnum() {
		return false;
	}
	
	default boolean isDefinition(HighLevelDefinition definition) {
		return false;
	}
}
