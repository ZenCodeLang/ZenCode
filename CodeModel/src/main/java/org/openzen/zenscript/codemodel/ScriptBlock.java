/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.List;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class ScriptBlock extends Taggable {
	public final AccessScope access;
	public final List<Statement> statements;
	
	public ScriptBlock(AccessScope access, List<Statement> statements) {
		this.access = access;
		this.statements = statements;
	}
	
	public ScriptBlock withStatements(List<Statement> newStatements) {
		ScriptBlock result = new ScriptBlock(access, newStatements);
		result.addAllTagsFrom(this);
		return result;
	}
}
