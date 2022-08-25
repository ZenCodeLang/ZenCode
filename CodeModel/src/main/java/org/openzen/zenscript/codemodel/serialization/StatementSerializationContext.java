package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatementSerializationContext {
	private final TypeSerializationContext types;
	private final StatementSerializationContext parent;
	private final FunctionHeader header;
	private final List<VarStatement> variables = new ArrayList<>();
	private final LoopStatement loop;

	public StatementSerializationContext(TypeSerializationContext types, FunctionHeader header) {
		this.types = types;
		this.parent = null;
		this.header = header;
		this.loop = null;
	}

	private StatementSerializationContext(StatementSerializationContext parent, LoopStatement loop) {
		this.types = parent.types;
		this.parent = parent;
		this.header = parent.header;
		this.loop = loop;
	}

	public TypeSerializationContext types() {
		return types;
	}

	public StatementSerializationContext forBlock() {
		return new StatementSerializationContext(this, null);
	}

	public StatementSerializationContext forLoop(LoopStatement loop) {
		return new StatementSerializationContext(this, loop);
	}

	public FunctionParameter getParameter(int id) {
		return header.parameters[id];
	}

	public Optional<LoopStatement> getLoop(int id) {
		if (loop != null && id == 0) {
			return Optional.of(loop);
		} else if (parent == null) {
			return Optional.empty();
		} else {
			return parent.getLoop(loop == null ? id : id - 1);
		}
	}

	public void add(VarStatement variable) {
		variables.add(variable);
	}
}
