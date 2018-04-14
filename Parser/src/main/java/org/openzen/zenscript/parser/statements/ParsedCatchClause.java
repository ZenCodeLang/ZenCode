/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCatchClause {
	public final String exceptionName;
	public final IParsedType exceptionType;
	public final ParsedStatement content;
	
	public ParsedCatchClause(String exceptionName, IParsedType exceptionType, ParsedStatement content) {
		this.exceptionName = exceptionName;
		this.exceptionType = exceptionType;
		this.content = content;
	}
	
	public CatchClause compile(StatementScope scope) {
		return new CatchClause(exceptionName, exceptionType.compile(scope), content.compile(scope));
	}
}
