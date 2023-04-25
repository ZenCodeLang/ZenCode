package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;

public interface CompilingSwitchValue {
	SwitchValue as(TypeID type);

	default List<CompilingVariable> getBindings() {
		return Collections.emptyList();
	}
}
