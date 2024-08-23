package org.openzen.zenscript.codemodel;

import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;
import java.util.Optional;

public class GenericName {
	public final String name;
	public final TypeID[] arguments;

	public GenericName(String name) {
		this(name, TypeID.NONE);
	}

	public GenericName(String name, TypeID[] arguments) {
		if (arguments == null)
			throw new NullPointerException("Arguments cannot be null");

		this.name = name;
		this.arguments = arguments;
	}

	public static Optional<TypeID> getInnerType(TypeID type, List<GenericName> name, int index, List<ExpansionSymbol> expansions) {
		while (index < name.size()) {
			GenericName innerName = name.get(index++);
			ResolvedType members = type.resolve(expansions);
			Optional<TypeID> inner = members
					.findInnerType(innerName.name)
					.map(t -> DefinitionTypeID.create(t, innerName.arguments));
			if (!inner.isPresent())
				return Optional.empty();

			type = inner.get();
		}

		return Optional.of(type);
	}

	public int getNumberOfArguments() {
		return arguments.length;
	}

	public boolean hasArguments() {
		return arguments.length > 0;
	}

	public boolean hasNoArguments() {
		return arguments.length == 0;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(name);
		if (hasArguments()) {
			result.append("<");
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0)
					result.append(", ");
				result.append(arguments[i].toString());
			}
			result.append(">");
		}
		return result.toString();
	}
}
