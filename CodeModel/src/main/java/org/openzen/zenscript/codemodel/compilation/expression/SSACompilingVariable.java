package org.openzen.zenscript.codemodel.compilation.expression;

import org.openzen.zenscript.codemodel.ssa.SSAVariable;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface SSACompilingVariable {
	SSAVariable as(TypeID type);
}
