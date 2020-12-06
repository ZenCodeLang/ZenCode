/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;

/**
 * @author Hoofdgebruiker
 */
public interface JavaSourceBuiltin {
	ExpressionString compile(JavaSourceStatementScope scope, CallExpression expression);
}
