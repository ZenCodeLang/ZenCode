/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.ConcatMap;

/**
 *
 * @author Hoofdgebruiker
 */
public class VarStatement extends Statement {
	public final String name;
	public final ITypeID type;
	public final Expression initializer;
	public final boolean isFinal;
	
	public VarStatement(CodePosition position, String name, ITypeID type, Expression initializer, boolean isFinal) {
		super(position, initializer == null ? null : initializer.thrownType);
		
		this.name = name;
		this.type = type;
		this.initializer = initializer;
		this.isFinal = isFinal;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitVar(this);
	}

	@Override
	public VarStatement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tInitializer = initializer == null ? null : initializer.transform(transformer);
		return tInitializer == initializer ? this : new VarStatement(position, name, type, tInitializer, isFinal);
	}
}
