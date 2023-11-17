package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.compilation.expression.SSACompilingVariable;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CompilingVariable {
	public final VariableID id;
	public final String name;
	public TypeID type; // may be null if the type is not yet known
	public final boolean isFinal;
	public SSACompilingVariable ssaCompilingVariable;

	public CompilingVariable(VariableID id, String name, TypeID type, boolean isFinal) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.isFinal = isFinal;
	}

	public VariableDefinition eval() {
		if (type == null)
			throw new IllegalStateException("Variable type must be known");

		return new VariableDefinition(id, name, type, isFinal, ssaCompilingVariable.as(type));
	}

	public VariableDefinition asType(TypeID type) {
		if (this.type != null && !this.type.equals(type))
			throw new IllegalStateException("Variable type must be unambiguous");

		this.type = type;
		return new VariableDefinition(id, name, type, isFinal, ssaCompilingVariable.as(type));
	}
}
