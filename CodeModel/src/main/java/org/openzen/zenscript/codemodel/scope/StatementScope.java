package org.openzen.zenscript.codemodel.scope;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.VarStatement;

import java.util.HashMap;
import java.util.Map;

public abstract class StatementScope extends BaseScope {
	private final Map<String, VarStatement> variables = new HashMap<>();

	public void defineVariable(VarStatement variable) {
		variables.put(variable.name, variable);
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		if (variables.containsKey(name.name) && name.hasNoArguments())
			return new GetLocalVariableExpression(position, variables.get(name.name));

		return null;
	}
}
