/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedSwitchCase {
	public final ParsedExpression value; // null for default
	public final List<ParsedStatement> statements = new ArrayList<>();
	
	public ParsedSwitchCase(ParsedExpression value) {
		this.value = value;
	}
	
	public SwitchCase compile(ITypeID type, StatementScope scope) {
		SwitchValue cValue = value == null ? null : value.compileToSwitchValue(type, new ExpressionScope(scope));
		List<Statement> cStatements = new ArrayList<>();
		for (ParsedStatement statement : statements) {
			cStatements.add(statement.compile(scope));
		}
		return new SwitchCase(cValue, cStatements);
	}
}
