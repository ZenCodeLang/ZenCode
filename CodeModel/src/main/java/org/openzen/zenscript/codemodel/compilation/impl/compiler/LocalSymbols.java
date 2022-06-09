package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LocalSymbols {
	public static LocalSymbols empty() {
		return new LocalSymbols(null);
	}

	private final LocalSymbols parent;
	private final String[] loopName;
	private final LoopStatement loop;
	private final Map<String, VarStatement> localVariables = new HashMap<>();

	public LocalSymbols(LocalSymbols parent) {
		this.parent = parent;
		this.loop = null;
		this.loopName = null;
	}

	public LocalSymbols(LocalSymbols parent, LoopStatement loop, String... loopName) {
		this.parent = parent;
		this.loop = loop;
		this.loopName = loopName;
	}

	public Optional<LoopStatement> findLoop(String name) {
		if (loop != null) {
			if (name == null || (loop.label != null && loop.label.equals(name)))
				return Optional.of(loop);
			if (loopName != null) {
				for (String s : loopName) {
					if (s.equals(name))
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

	public void add(VarStatement localVariable) {
		localVariables.put(localVariable.name, localVariable);
	}

	public Optional<VarStatement> findLocalVariable(String name) {
		if (localVariables.containsKey(name))
			return Optional.of(localVariables.get(name));

		if (parent == null)
			return Optional.empty();

		return parent.findLocalVariable(name);
	}
}
