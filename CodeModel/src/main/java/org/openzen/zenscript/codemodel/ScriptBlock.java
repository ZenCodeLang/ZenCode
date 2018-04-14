/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.List;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class ScriptBlock {
	public final List<Statement> statements;
	
	public ScriptBlock(List<Statement> statements) {
		this.statements = statements;
	}
}
