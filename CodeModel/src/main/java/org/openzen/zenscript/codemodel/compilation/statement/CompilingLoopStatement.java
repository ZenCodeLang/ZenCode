package org.openzen.zenscript.codemodel.compilation.statement;

import org.openzen.zenscript.codemodel.compilation.CompilingVariable;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.List;

public interface CompilingLoopStatement {
	List<String> getLabels();

	List<CompilingVariable> getLoopVariables();

	CodeBlock getContinueTarget();

	CodeBlock getBreakTarget();

	LoopStatement getCompiled();
}
