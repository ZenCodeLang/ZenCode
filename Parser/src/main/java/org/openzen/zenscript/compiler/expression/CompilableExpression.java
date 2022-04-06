package org.openzen.zenscript.compiler.expression;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface CompilableExpression {

	/**
	 * Compiles the given parsed expression to a high-level expression or
	 * partial expression.
	 *
	 * @param compiler expression compiler
	 * @return
	 */
	CompilingExpression compile(ExpressionCompiler compiler);

	/**
	 * Compiles the given parsed expression to a map key value.
	 *
	 * @param compiler expression compiler
	 * @param type resulting type
	 * @return
	 */
	default Expression compileKey(ExpressionCompiler compiler, TypeID type) {
		return compile(compiler).as(type);
	}
}
