/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.formattershared.StatementFormattingTarget;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;

/**
 * @author Hoofdgebruiker
 */
public class JavaSourceLambdaStatementFormatter extends JavaSourceStatementFormatter {
	public JavaSourceLambdaStatementFormatter(JavaSourceStatementScope scope) {
		super(scope);
	}

	@Override
	public void formatEmpty(StatementFormattingTarget target, EmptyStatement statement) {
		target.writeLine("{}");
	}

	@Override
	public void formatExpression(StatementFormattingTarget target, ExpressionStatement statement) {
		target.writeLine(scope.expression(target, statement.expression).value);
	}

	@Override
	public void formatReturn(StatementFormattingTarget target, ReturnStatement statement) {
		if (statement.value == null)
			target.writeLine("{}");
		else
			target.writeLine(scope.expression(target, statement.value).value);
	}
}
