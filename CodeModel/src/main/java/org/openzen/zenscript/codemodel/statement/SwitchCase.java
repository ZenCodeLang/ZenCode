/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.List;
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
}
