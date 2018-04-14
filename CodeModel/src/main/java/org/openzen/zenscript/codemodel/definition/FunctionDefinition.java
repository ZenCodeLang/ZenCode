/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionDefinition extends HighLevelDefinition {
	public FunctionHeader header;
	public List<Statement> statements;
	
	public FunctionDefinition(String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(name, modifiers, outerDefinition);
	}
	
	public void setHeader(FunctionHeader header) {
		this.header = header;
	}
	
	public void setCode(List<Statement> statements) {
		this.statements = statements;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitFunction(this);
	}
}
