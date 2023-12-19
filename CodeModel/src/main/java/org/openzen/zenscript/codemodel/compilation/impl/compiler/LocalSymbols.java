package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingVariable;
import org.openzen.zenscript.codemodel.compilation.impl.capture.LocalExpression;
import org.openzen.zenscript.codemodel.compilation.impl.capture.LocalParameterExpression;
import org.openzen.zenscript.codemodel.compilation.impl.capture.LocalVariableExpression;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingLoopStatement;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LocalSymbols {
	public static LocalSymbols empty() {
		return new LocalSymbols(null);
	}

	private final LocalSymbols parent;
	private final LambdaClosure closure;
	private final String[] loopName;
	private final CompilingLoopStatement loop;
	private final FunctionHeader header;
	private final Map<String, CompilingVariable> localVariables = new HashMap<>();
	private final CompilableExpression dollar;

	public LocalSymbols(FunctionHeader header) {
		this.parent = null;
		this.header = header;
		this.closure = null;
		this.loop = null;
		this.loopName = null;
		this.dollar = null;
	}

	private LocalSymbols(LocalSymbols parent, FunctionHeader header, LambdaClosure closure) {
		this.parent = parent;
		this.loop = null;
		this.loopName = null;
		this.header = header;
		this.closure = closure;
		this.dollar = null;
	}

	private LocalSymbols(LocalSymbols parent, CompilingLoopStatement loop, String... loopName) {
		this.parent = parent;
		this.closure = null;
		this.loop = loop;
		this.loopName = loopName;
		this.header = null;
		this.dollar = null;

		for (CompilingVariable loopVariable : loop.getLoopVariables())
			localVariables.put(loopVariable.name, loopVariable);
	}

	public LocalSymbols forBlock() {
		return new LocalSymbols(this, (FunctionHeader) null, null);
	}

	public LocalSymbols forLambda(LambdaClosure closure, FunctionHeader header) {
		return new LocalSymbols(this, header, closure);
	}

	public LocalSymbols forLoop(CompilingLoopStatement loop, String... loopName) {
		return new LocalSymbols(this, loop, loopName);
	}

	public Optional<CompilingLoopStatement> findLoop(String name) {
		if (loop != null) {
			if (name == null || (loop.getLabels().contains(name)))
				return Optional.of(loop);
			if (loopName != null) {
				for (String s : loopName) {
					if (s != null && s.equals(name))
						return Optional.of(loop);
				}
			}
		}

		if (parent != null) {
			return parent.findLoop(name);
		} else {
			return Optional.empty();
		}
	}

	public void add(CompilingVariable localVariable) {
		localVariables.put(localVariable.name, localVariable);
	}

	public Optional<LocalExpression> findLocalVariable(CodePosition position, String name) {
		if (localVariables.containsKey(name))
			return Optional.of(new LocalVariableExpression(position, localVariables.get(name)));

		if (header != null) {
			for (FunctionParameter parameter : header.parameters) {
				if (parameter.name.equals(name))
					return Optional.of(new LocalParameterExpression(position, parameter));
			}
		}

		if (parent == null)
			return Optional.empty();
		else if (closure != null)
			return parent.findLocalVariable(position, name).map(var -> var.capture(closure));
		else
			return parent.findLocalVariable(position, name);
	}

	public Optional<CompilableExpression> getDollar() {
		return Optional.ofNullable(dollar);
	}
}