package org.openzen.zenscript.codemodel;

import org.openzen.zenscript.codemodel.ssa.SSAVariable;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class VariableDefinition {
	public final VariableID id;
	public final String name;
	public final TypeID type;
	public final boolean isFinal;
	public final SSAVariable variable;

	public VariableDefinition(VariableID id, String name, TypeID type, boolean isFinal, SSAVariable variable) {
		if (type == null)
			throw new IllegalArgumentException("Variable type must be known");

		this.id = id;
		this.name = name;
		this.type = type;
		this.isFinal = isFinal;
		this.variable = variable;
	}
}
