/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.List;
import java.util.stream.Collectors;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class Statement extends Taggable {
	public final CodePosition position;
	
	public Statement(CodePosition position) {
		this.position = position;
	}
	
	public ITypeID getReturnType() {
		return null;
	}
	
	public Statement withReturnType(TypeScope scope, ITypeID returnType) {
		return this;
	}
	
	public abstract <T> T accept(StatementVisitor<T> visitor);
	
	public static List<Statement> withReturnType(TypeScope scope, List<Statement> statements, ITypeID returnType) {
		return statements.stream()
				.map(statement -> statement.withReturnType(scope, returnType))
				.collect(Collectors.toList());
	}
}
