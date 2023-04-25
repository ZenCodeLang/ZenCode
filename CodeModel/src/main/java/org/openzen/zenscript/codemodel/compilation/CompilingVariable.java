package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.ssa.SSAVariable;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class CompilingVariable {
	public final VariableID id;
	public final String name;
	public TypeID type; // may be null if the type is not yet known
	public final boolean isFinal;

	public CompilingVariable(VariableID id, String name, TypeID type, boolean isFinal) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.isFinal = isFinal;
	}

	public VariableDefinition complete(SSAVariable variable) {
		return new VariableDefinition(id, name, type, isFinal);
	}
}
