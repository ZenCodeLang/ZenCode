/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.formattershared.StatementFormatter;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceLambdaStatementCompiler extends StatementFormatter {
	public JavaSourceLambdaStatementCompiler(JavaSourceStatementScope scope, StringBuilder output, boolean unwrap, boolean newline) {
		super(output, scope.settings, new JavaSourceLambdaStatementFormatter(scope), scope.indent, scope.innerLoop);
	}
}
