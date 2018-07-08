/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwitchCase {
	public final SwitchValue value;
	public final List<Statement> statements;
	
	public SwitchCase(SwitchValue value, List<Statement> statements) {
		this.value = value;
		this.statements = statements;
	}
	
	public SwitchCase transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		List<Statement> tStatements = new ArrayList<>();
		for (Statement statement : statements) {
			tStatements.add(statement.transform(transformer, modified));
		}
		return new SwitchCase(value, tStatements);
	}
	
	public SwitchCase transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		List<Statement> tStatements = new ArrayList<>();
		for (Statement statement : statements) {
			tStatements.add(statement.transform(transformer, modified));
		}
		return new SwitchCase(value, tStatements);
	}
}
