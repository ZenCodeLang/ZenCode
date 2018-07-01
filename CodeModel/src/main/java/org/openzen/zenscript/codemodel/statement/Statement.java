/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.annotations.StatementAnnotation;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.shared.ConcatMap;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class Statement extends Taggable {
	public final CodePosition position;
	public final ITypeID thrownType;
	public StatementAnnotation[] annotations = StatementAnnotation.NONE;
	
	public Statement(CodePosition position, ITypeID thrownType) {
		this.position = position;
		this.thrownType = thrownType;
	}
	
	public ITypeID getReturnType() {
		return null;
	}
	
	public Statement withReturnType(TypeScope scope, ITypeID returnType) {
		return this;
	}
	
	public abstract <T> T accept(StatementVisitor<T> visitor);
	
	public abstract void forEachStatement(Consumer<Statement> consumer);
	
	public final Statement transform(StatementTransformer transformer) {
		return transform(transformer, ConcatMap.empty());
	}
	
	public abstract Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified);
	
	public abstract Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified);
	
	public static List<Statement> withReturnType(TypeScope scope, List<Statement> statements, ITypeID returnType) {
		return statements.stream()
				.map(statement -> statement.withReturnType(scope, returnType))
				.collect(Collectors.toList());
	}
}
