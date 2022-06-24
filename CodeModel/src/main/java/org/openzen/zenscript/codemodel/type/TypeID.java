package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TypeID {
	TypeID[] NONE = new TypeID[0];

	static Map<TypeParameter, TypeID> getMapping(TypeParameter[] parameters, TypeID[] arguments) {
		Map<TypeParameter, TypeID> typeArguments = new HashMap<>();
		for (int i = 0; i < parameters.length; i++)
			typeArguments.put(parameters[i], arguments[i]);
		return typeArguments;
	}

	static Map<TypeParameter, TypeID> getSelfMapping(TypeParameter[] parameters) {
		Map<TypeParameter, TypeID> typeArguments = new HashMap<>();
		for (TypeParameter parameter : parameters)
			typeArguments.put(parameter, new GenericTypeID(parameter));
		return typeArguments;
	}

	default TypeID getSuperType() {
		return null;
	}

	TypeID instance(GenericMapper mapper);

	boolean hasDefaultValue();

	default Expression getDefaultValue() {
		return null;
	}

	/**
	 * Infers type parameters for this type so it matches with targetType
	 *
	 * @return inferred type parameters, or null if no match was found
 	 */
	default Map<TypeParameter, TypeID> inferTypeParameters(TypeID targetType) {
		return TypeMatcher.match(this, targetType);
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
		return this;
	}

	/**
	 * Strips non-essential information from a type. (such as it being const or optional)
	 *
	 * @return simplified type
	 */
	default TypeID simplified() {
		return withoutOptional();
	}

	default boolean isVariant() {
		return false;
	}

	default boolean isEnum() {
		return false;
	}

	default boolean canCastImplicitTo(TypeID other) {
		return false;
	}

	default boolean canCastExplicitTo(TypeID other) {
		return false;
	}

	default boolean canCastImplicitFrom(TypeID other) {
		return false;
	}

	default boolean canCastExplicitFrom(TypeID other) {
		return false;
	}

	default Expression castImplicitTo(CodePosition position, Expression value, TypeID toType) {
		return null;
	}

	default Expression castExplicitTo(CodePosition position, Expression value, TypeID toOther) {
		return null;
	}

	default Expression castImplicitFrom(CodePosition position, Expression value) {
		return null;
	}

	default Expression castExplicitFrom(CodePosition position, Expression value) {
		return null;
	}

	default Optional<OptionalTypeID> asOptional() {
		return Optional.empty();
	}

	default Optional<AssocTypeID> asAssoc() {
		return Optional.empty();
	}

	default Optional<ArrayTypeID> asArray() {
		return Optional.empty();
	}

	default Optional<GenericMapTypeID> asGenericMap() {
		return Optional.empty();
	}

	default Optional<DefinitionTypeID> asDefinition() {
		return Optional.empty();
	}

	default Optional<FunctionTypeID> asFunction() {
		return Optional.empty();
	}

	default Optional<RangeTypeID> asRange() {
		return Optional.empty();
	}

	default Optional<GenericTypeID> asGeneric() {
		return Optional.empty();
	}

	ResolvedType resolve();
}
