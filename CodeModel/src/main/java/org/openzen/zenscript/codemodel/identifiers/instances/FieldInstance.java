package org.openzen.zenscript.codemodel.identifiers.instances;

import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public final class FieldInstance {
	public final FieldSymbol field;
	private final TypeID type;

	public FieldInstance(FieldSymbol field) {
		this.field = field;
		this.type = field.getType();
	}

	public FieldInstance(FieldSymbol field, TypeID type) {
		this.field = field;
		this.type = type;
	}

	public String getName() {
		return field.getName();
	}

	public TypeID getType() {
		return type;
	}
}
