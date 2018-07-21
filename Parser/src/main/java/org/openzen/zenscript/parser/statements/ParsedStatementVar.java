/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

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
			if (this.initializer == null)
				throw new CompileException(position, CompileExceptionCode.VAR_WITHOUT_TYPE_OR_INITIALIZER, "Local variables must have either a type or an initializer");
			
			initializer = this.initializer.compile(new ExpressionScope(scope)).eval();
			type = initializer.type;
		} else {
			type = this.type.compile(scope);
			initializer = this.initializer == null ? null : this.initializer.compile(new ExpressionScope(scope, type)).eval();
		}
		VarStatement result = new VarStatement(position, name, type, initializer, isFinal);
		scope.defineVariable(result);
		return result(result, scope);
	}

	@Override
	public ITypeID precompileForResultType(StatementScope scope, PrecompilationState precompileState) {
		ITypeID type = null;
		if (this.type == null) {
			if (this.initializer != null)
				type = initializer.precompileForType(new ExpressionScope(scope), precompileState);
		} else {
			type = this.type.compile(scope);
		}
		VarStatement result = new VarStatement(position, name, type, null, isFinal);
		scope.defineVariable(result);
		return null;
	}
}
