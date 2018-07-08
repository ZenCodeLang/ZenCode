/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;

/**
 *
 * @author Hoofdgebruiker
 */
public class CatchClause {
	public final CodePosition position;
	public final Statement content;
	public final VarStatement exceptionVariable;
	
	public CatchClause(CodePosition position, VarStatement exceptionVariable, Statement content) {
		this.position = position;
		this.exceptionVariable = exceptionVariable;
		this.content = content;
	}
	
	public CatchClause transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Statement tContent = content.transform(transformer, modified);
		return content == tContent ? this : new CatchClause(position, exceptionVariable, tContent);
	}
	
	public CatchClause transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Statement tContent = content.transform(transformer, modified);
		return content == tContent ? this : new CatchClause(position, exceptionVariable, tContent);
	}
}
