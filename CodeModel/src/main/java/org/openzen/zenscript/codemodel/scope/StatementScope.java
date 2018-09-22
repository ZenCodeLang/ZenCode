/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.GenericName;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class StatementScope extends BaseScope {
	private final Map<String, VarStatement> variables = new HashMap<>();
	
	public void defineVariable(VarStatement variable) {
		variables.put(variable.name, variable);
	}
	
	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		if (variables.containsKey(name.name) && name.hasNoArguments())
			return new GetLocalVariableExpression(position, variables.get(name.name));
		
		return null;
	}
}
