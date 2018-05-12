/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.StatementScope;
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
	
	public SwitchCase compile(StatementScope scope) {
		Expression cValue = value == null ? null : value.compile(new ExpressionScope(scope)).eval();
		List<Statement> cStatements = new ArrayList<>();
		for (ParsedStatement statement : statements) {
			cStatements.add(statement.compile(scope));
		}
		return new SwitchCase(cValue, cStatements);
	}
}
