/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class BlockStatement extends Statement {
	public final List<Statement> statements;
	
	public BlockStatement(CodePosition position, List<Statement> statements) {
		super(position, getThrownType(statements));
		
		this.statements = statements;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitBlock(this);
	}
	
	private static ITypeID getThrownType(List<Statement> statements) {
		ITypeID result = null;
		for (Statement statement : statements)
			result = Expression.binaryThrow(statement.position, result, statement.thrownType);
		return result;
	}
}
