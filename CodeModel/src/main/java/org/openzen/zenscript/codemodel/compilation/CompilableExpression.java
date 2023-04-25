package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.switchvalue.ErrorSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface CompilableExpression {
	CodePosition getPosition();

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
	 * @return
	 */
	default CompilingExpression compileKey(ExpressionCompiler compiler) {
		return compile(compiler);
	}

	default CompilingSwitchValue compileSwitchValue(ExpressionCompiler compiler) {
		return type -> new ErrorSwitchValue(getPosition(), CompileErrors.invalidSwitchCaseExpression());
	}

	/**
	 * Converts this expression to a lambda expression header, if possible.
	 *
	 * @return
	 */
	default Optional<CompilableLambdaHeader> asLambdaHeader() {
		return Optional.empty();
	}

	/**
	 * Converts this expression to a lambda header parameter, if possible.
	 *
	 * @return
	 */
	default Optional<CompilableLambdaHeader.Parameter> asLambdaHeaderParameter() {
		return Optional.empty();
	}
}
