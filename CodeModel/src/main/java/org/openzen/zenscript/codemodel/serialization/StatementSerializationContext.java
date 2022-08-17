package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.StaticSetterExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.ArrayList;
import java.util.List;

public class StatementSerializationContext {
	private final TypeSerializationContext types;
	private final StatementSerializationContext parent;
	private final FunctionHeader header;
	private final List<VarStatement> variables = new ArrayList<>();

	public StatementSerializationContext(TypeSerializationContext types, FunctionHeader header) {
		this.types = types;
		this.parent = null;
		this.header = header;
	}

	private StatementSerializationContext(StatementSerializationContext parent) {
		this.types = parent.types;
		this.parent = parent;
		this.header = parent.header;
	}

	public TypeSerializationContext types() {
		return types;
	}

	public StatementSerializationContext forBlock() {
		return new StatementSerializationContext(this);
	}

	public StatementSerializationContext forLoop(LoopStatement loop) {

	}

	public FunctionParameter getParameter(int id) {
		return header.parameters[id];
	}

	public LoopStatement getLoop(int id) {

	}

	public void add(VarStatement variable) {
		variables.add(variable);
	}
}
