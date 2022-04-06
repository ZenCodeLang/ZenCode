package org.openzen.zenscript.compiler.types;

import org.openzen.zenscript.codemodel.expression.Expression;

public interface Caster {
	Expression apply(Expression value, boolean optional);
}
