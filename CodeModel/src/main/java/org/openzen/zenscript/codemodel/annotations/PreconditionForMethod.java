/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionBuilder;
import org.openzen.zenscript.codemodel.expression.PanicExpression;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class PreconditionForMethod implements MemberAnnotation {
	private final CodePosition position;
	private final String enforcement;
	private final Expression condition;
	private final Expression message;
	
	public PreconditionForMethod(
			CodePosition position,
			String enforcement,
			Expression condition,
			Expression message) {
		this.position = position;
		this.enforcement = enforcement;
		this.condition = condition;
		this.message = message;
	}

	@Override
	public void apply(IDefinitionMember member, BaseScope scope) {
		if (member instanceof GetterMember) {
			applyOnOverridingGetter((GetterMember)member, scope);
		} else if (member instanceof SetterMember) {
			applyOnOverridingSetter((SetterMember)member, scope);
		} else if (member instanceof FunctionalMember) {
			applyOnOverridingMethod((FunctionalMember)member, scope);
		}
	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope) {
		member.body = applyOnOverride(member.body, scope);
	}

	@Override
	public void applyOnOverridingGetter(GetterMember member, BaseScope scope) {
		member.body = applyOnOverride(member.body, scope);
	}

	@Override
	public void applyOnOverridingSetter(SetterMember member, BaseScope scope) {
		member.body = applyOnOverride(member.body, scope);
	}
	
	private Statement applyOnOverride(Statement body, BaseScope scope) {
		if (body == null)
			return body;
		
		ExpressionScope expressionScope = new ExpressionScope(scope, BasicTypeID.BOOL);
		List<Statement> statements = new ArrayList<>();
		ExpressionBuilder expressionBuilder = new ExpressionBuilder(position, expressionScope);
		Expression inverseCondition = expressionBuilder.not(condition);
		Statement throwStatement = new ExpressionStatement(position, new PanicExpression(position, BasicTypeID.VOID, message));
		statements.add(new IfStatement(position, inverseCondition, throwStatement, null));
		
		if (body instanceof BlockStatement) {
			statements.addAll(((BlockStatement)body).statements);
		} else {
			statements.add(body);
		}
		return new BlockStatement(position, statements);
	}
}
