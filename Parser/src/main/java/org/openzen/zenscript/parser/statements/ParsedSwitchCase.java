/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.StoredType;
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
	
	public SwitchCase compile(StoredType type, StatementScope scope) throws CompileException {
		SwitchValue cValue = value == null ? null : value.compileToSwitchValue(type, new ExpressionScope(scope));
		Statement[] cStatements = new Statement[statements.size()];
		int i = 0;
		for (ParsedStatement statement : statements) {
			cStatements[i++] = statement.compile(scope);
		}
		return new SwitchCase(cValue, cStatements);
	}
}
