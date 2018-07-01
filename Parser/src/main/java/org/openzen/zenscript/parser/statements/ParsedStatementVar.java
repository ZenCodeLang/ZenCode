/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedStatementVar extends ParsedStatement {
	private final String name;
	private final IParsedType type;
	private final ParsedExpression initializer;
	private final boolean isFinal;

	public ParsedStatementVar(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name, IParsedType type, ParsedExpression initializer, boolean isFinal) {
		super(position, annotations, whitespace);

		this.name = name;
		this.type = type;
		this.initializer = initializer;
		this.isFinal = isFinal;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression initializer;
		ITypeID type;
		if (this.type == null) {
			initializer = this.initializer == null ? null : this.initializer.compile(new ExpressionScope(scope)).eval();
			type = initializer == null ? BasicTypeID.ANY : initializer.type;
		} else {
			type = this.type.compile(scope);
			initializer = this.initializer == null ? null : this.initializer.compile(new ExpressionScope(scope, type)).eval();
		}
		VarStatement result = new VarStatement(position, name, type, initializer, isFinal);
		scope.defineVariable(result);
		return result(result, scope);
	}
}
