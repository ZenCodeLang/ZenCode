/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formattershared;

import java.util.List;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatementFormattingSubBlock {
	public final String header;
	public final List<String> literalStatements;
	public final List<Statement> statements;
	
	public StatementFormattingSubBlock(String header, List<String> literalStatements, List<Statement> statements) {
		this.header = header;
		this.literalStatements = literalStatements;
		this.statements = statements;
	}
}
