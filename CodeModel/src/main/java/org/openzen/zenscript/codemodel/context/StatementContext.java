package org.openzen.zenscript.codemodel.context;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatementContext extends TypeContext {
	private final List<VarStatement> variables = new ArrayList<>();
	private final LoopStatement[] loops;
	private final FunctionHeader header;
	private final LambdaClosure closure;
	private final StatementContext lambdaOuter;

	public VariantOptionSwitchValue variantOptionSwitchValue;

	public StatementContext(CodePosition position, ModuleContext module, TypeID thisType) {
		super(position, module, TypeParameter.NONE, thisType);

		loops = LoopStatement.NONE;
		header = null;
		closure = null;
		lambdaOuter = null;
	}

	public StatementContext(CodePosition position, ModuleContext module, TypeID thisType, FunctionHeader header) {
		super(position, module, header.typeParameters, thisType);

		loops = LoopStatement.NONE;
		this.header = header;
		this.closure = null;
		lambdaOuter = null;
	}

	public StatementContext(CodePosition position, TypeContext outer) {
		super(position, outer.moduleContext, outer.typeParameters, outer.thisType);

		loops = LoopStatement.NONE;
		header = null;
		closure = null;
		lambdaOuter = null;
	}

	@Deprecated
	public StatementContext(TypeContext outer) {
		this(outer.getPosition(), outer);
	}

	@Deprecated
	public StatementContext(TypeContext outer, FunctionHeader header) {
		this(outer.getPosition(), outer, header);
	}

	public StatementContext(CodePosition position, TypeContext outer, FunctionHeader header) {
		super(position, outer, outer.thisType, header == null ? TypeParameter.NONE : header.typeParameters);

		loops = LoopStatement.NONE;
		this.header = header;
		closure = null;
		lambdaOuter = null;
	}

	public StatementContext(StatementContext outer) {
		super(outer.position, outer.moduleContext, outer.typeParameters, outer.thisType);

		variables.addAll(outer.variables);
		loops = outer.loops;
		header = outer.header;
		closure = outer.closure;
		lambdaOuter = outer.lambdaOuter;
	}

	public StatementContext(StatementContext outer, LoopStatement loop) {
		super(outer.position, outer.moduleContext, outer.typeParameters, outer.thisType);

		variables.addAll(outer.variables);
		loops = Arrays.copyOf(outer.loops, outer.loops.length + 1);
		loops[loops.length - 1] = loop;
		header = outer.header;
		closure = outer.closure;
		lambdaOuter = outer.lambdaOuter;
	}

	public StatementContext(StatementContext outer, FunctionHeader lambdaHeader, LambdaClosure lambdaClosure) {
		super(outer.position, outer, outer.thisType, lambdaHeader.typeParameters);

		loops = LoopStatement.NONE;
		header = lambdaHeader;
		this.closure = lambdaClosure;
		lambdaOuter = outer;
	}

	public void add(VarStatement variable) {
		variables.add(variable);
	}

	public int getVariableId(VarStatement variable) {
		int id = variables.indexOf(variable);
		if (id < 0)
			throw new IllegalArgumentException("Variable not in scope: " + variable.name);
		return id;
	}

	public int getLoopId(LoopStatement loop) {
		for (int i = 0; i < loops.length; i++)
			if (loops[i] == loop)
				return i;

		throw new IllegalArgumentException("Loop@" + loop.position + " not in scope");
	}

	public int getParameterIndex(FunctionParameter parameter) {
		if (header == null)
			throw new IllegalStateException("No parameters available");

		for (int i = 0; i < header.parameters.length; i++)
			if (header.parameters[i] == parameter)
				return i;

		throw new IllegalArgumentException("Parameter" + parameter.name + " not in scope");
	}

	public VarStatement getVariable(int id) {
		return variables.get(id);
	}

	public LoopStatement getLoop(int id) {
		return loops[id];
	}

	public FunctionParameter getParameter(int id) {
		return header.parameters[id];
	}

	public LambdaClosure getLambdaClosure() {
		return closure;
	}

	public StatementContext getLambdaOuter() {
		return lambdaOuter;
	}
}
