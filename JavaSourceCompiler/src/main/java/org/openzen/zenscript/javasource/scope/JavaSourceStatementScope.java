/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.scope;

import java.util.HashSet;
import java.util.Set;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.formattershared.ExpressionString;
import org.openzen.zenscript.formattershared.StatementFormattingTarget;
import org.openzen.zenscript.javasource.ExpressionHoistingChecker;
import org.openzen.zenscript.javasource.JavaSourceFormattingSettings;
import org.openzen.zenscript.javasource.JavaSourceExpressionFormatter;
import org.openzen.zenscript.javasource.JavaSourceStatementFormatter;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javasource.JavaSourceContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceStatementScope {
	public final JavaSourceFileScope fileScope;
	public final JavaSourceFormattingSettings settings;
	public final String indent;
	public final LoopStatement innerLoop;
	public final boolean isExpansion;
	public final ITypeID thisType;
	public final JavaSourceContext context;
	
	private final JavaSourceStatementScope outer;
	private final Set<String> localVariables = new HashSet<>();
	private int tempVariableCounter = 0;
	
	public JavaSourceStatementScope(
			JavaSourceFileScope fileScope,
			JavaSourceFormattingSettings settings,
			FunctionHeader header,
			String indent,
			LoopStatement innerLoop,
			JavaSourceStatementScope outer,
			boolean isExpansion) {
		this.fileScope = fileScope;
		this.settings = settings;
		this.indent = indent;
		this.innerLoop = innerLoop;
		this.outer = outer;
		this.isExpansion = isExpansion;
		this.thisType = fileScope.thisType;
		this.context = fileScope.context;
		
		if (header != null) {
			for (FunctionParameter parameter : header.parameters)
				localVariables.add(parameter.name);
		}
	}
	
	public JavaSourceStatementScope createBlockScope(LoopStatement loop) {
		return new JavaSourceStatementScope(fileScope, settings, null, indent + settings.indent, loop == null ? innerLoop : loop, this, isExpansion);
	}
	
	public JavaSourceStatementScope createBlockScope(LoopStatement loop, String indent) {
		return new JavaSourceStatementScope(fileScope, settings, null, this.indent + indent, loop == null ? innerLoop : loop, this, isExpansion);
	}
	
	public ExpressionString expression(StatementFormattingTarget target, Expression expression) {
		return expression.accept(new JavaSourceExpressionFormatter(target, this));
	}
	
	public String type(ITypeID type) {
		return fileScope.type(type);
	}
	
	public String type(ITypeID type, JavaClass renamed) {
		return fileScope.type(type, renamed);
	}
	
	public String type(JavaClass cls) {
		return fileScope.importer.importType(cls);
	}
	
	public void addLocalVariable(String localVariable) {
		localVariables.add(localVariable);
	}
	
	public boolean hasLocalVariable(String localVariable) {
		return localVariables.contains(localVariable) || (outer != null && outer.hasLocalVariable(localVariable));
	}
	
	public String createTempVariable() {
		String result = "temp" + (++tempVariableCounter);
		addLocalVariable(result);
		return result;
	}
	
	public Expression duplicable(StatementFormattingTarget target, Expression expression) {
		boolean shouldHoist = expression.accept(ExpressionHoistingChecker.INSTANCE);
		if (!shouldHoist)
			return expression;
		
		VarStatement temp = new VarStatement(expression.position, createTempVariable(), expression.type, expression, true);
		new JavaSourceStatementFormatter(this).formatVar(target, temp);
		return new GetLocalVariableExpression(expression.position, temp);
	}
}
