package org.openzen.zenscript.codemodel.ssa;

import java.util.ArrayList;
import java.util.List;

/**
 * A code block represents a single block of execution: execution will always go from the 1st statement to the last
 * statement, in the given order.
 */
public class CodeBlock {
	private final List<CodeBlock> successors = new ArrayList<>();
	private final List<CodeBlock> predecessors = new ArrayList<>();
	private final List<CodeBlockStatement> statements = new ArrayList<>();

	public CodeBlock createNext() {
		if (statements.isEmpty()) {
			return this;
		} else {
			return createNextAlways();
		}
	}

	public CodeBlock createNextAlways() {
		CodeBlock result = new CodeBlock();
		addSuccessor(result);
		return result;
	}

	public List<CodeBlockStatement> getStatements() {
		return statements;
	}

	public void add(CodeBlockStatement statement) {
		statements.add(statement);
	}

	public void addSuccessor(CodeBlock block) {
		successors.add(block);
		block.predecessors.add(this);
	}

	public List<CodeBlock> getSuccessors() {
		return successors;
	}

	public List<CodeBlock> getPredecessors() {
		return predecessors;
	}
}
