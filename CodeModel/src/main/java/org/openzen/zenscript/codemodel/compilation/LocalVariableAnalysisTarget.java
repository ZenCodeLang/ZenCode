package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.LocalVariable;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface LocalVariableAnalysisTarget {
	void register(LocalVariable variable, TypeID type);
}
