package org.openzen.zenscript.compiler.types;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.expression.Expression;

public interface Comparator {
	Expression compare(CodePosition position, Expression left, Expression right);

	Expression compare(CodePosition position, Expression left, Expression right, CompareType type);
}
