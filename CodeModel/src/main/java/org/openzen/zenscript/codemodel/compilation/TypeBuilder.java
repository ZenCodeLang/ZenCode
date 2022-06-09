package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.*;

import java.util.List;
import java.util.Optional;

public interface TypeBuilder {
	TypeID definitionOf(TypeSymbol definition, TypeID... arguments);

	OptionalTypeID optionalOf(TypeID type);

	ArrayTypeID arrayOf(TypeID elementType);

	ArrayTypeID arrayOf(TypeID elementType, int dimension);

	AssocTypeID associativeOf(TypeID keyType, TypeID valueType);

	RangeTypeID rangeOf(TypeID type);

	FunctionTypeID functionOf(FunctionHeader header);

	GenericMapTypeBuilder withGeneric(TypeParameter... parameters);

	Optional<TypeID> resolve(CodePosition position, List<GenericName> name);

	ExpressionCompiler getDefaultValueCompiler();

	interface GenericMapTypeBuilder extends TypeBuilder {
		TypeID ofValue(TypeID valueType);
	}
}
