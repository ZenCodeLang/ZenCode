/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwitchCase {
	public final Expression value;
	public final List<Statement> statements;
	
	public SwitchCase(Expression value, List<Statement> statements) {
		this.value = value;
		this.statements = statements;
	}
}
