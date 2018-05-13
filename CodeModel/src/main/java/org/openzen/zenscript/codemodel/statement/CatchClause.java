/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CatchClause {
	public final Statement content;
	public final VarStatement exceptionVariable;
	
	public CatchClause(CodePosition position, VarStatement exceptionVariable, Statement content) {
		this.exceptionVariable = exceptionVariable;
		this.content = content;
	}
}
