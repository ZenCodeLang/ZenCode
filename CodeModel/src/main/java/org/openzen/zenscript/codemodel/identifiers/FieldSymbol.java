package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

/**
 * Uniquely identifies a field and allows a limited set of information to be retrieved from it.
 */
public interface FieldSymbol {
	TypeSymbol getDefiningType();

	String getName();

	TypeID getType();

	/* Calculates the compile-time value of this constant field, if applicable */
	Optional<CompileTimeConstant> evaluate();
}
