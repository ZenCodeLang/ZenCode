/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.Collections;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class MatchExpression extends Expression {
	public final Expression value;
	public final Case[] cases;
	
	public MatchExpression(CodePosition position, Expression value, ITypeID type, Case[] cases) {
		super(position, type, binaryThrow(position, value.thrownType, getThrownType(position, cases)));
		
		this.value = value;
		this.cases = cases;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitMatch(this);
	}

	@Override
	public Expression transform(ExpressionTransformer transformer) {
		Expression tValue = value.transform(transformer);
		Case[] tCases = new Case[cases.length];
		boolean unmodified = true;
		for (int i = 0; i < tCases.length; i++) {
			tCases[i] = cases[i].transform(transformer);
			unmodified &= tCases[i] == cases[i];
		}
		return unmodified && tValue == value ? this : new MatchExpression(position, tValue, type, tCases);
	}
	
	public SwitchedMatch convertToSwitch(String tempVariable) {
		VarStatement result = new VarStatement(position, tempVariable, type, null, false);
		SwitchStatement switchStatement = new SwitchStatement(position, null, value);
		for (MatchExpression.Case matchCase : cases) {
			Expression caseExpression = new SetLocalVariableExpression(matchCase.value.position, result, matchCase.value);
			SwitchCase switchCase = new SwitchCase(matchCase.key, Collections.singletonList(new ExpressionStatement(matchCase.value.position, caseExpression)));
			switchStatement.cases.add(switchCase);
		}
		return new SwitchedMatch(result, switchStatement);
	}
	
	public static class SwitchedMatch {
		public final VarStatement result;
		public final SwitchStatement switchStatement;
		
		public SwitchedMatch(VarStatement temp, SwitchStatement switchStatement) {
			this.result = temp;
			this.switchStatement = switchStatement;
		}
	}
	
	public static class Case {
		public final SwitchValue key;
		public final Expression value;
		
		public Case(SwitchValue key, Expression value) {
			this.key = key;
			this.value = value;
		}
		
		public Case transform(ExpressionTransformer transformer) {
			Expression tValue = value.transform(transformer);
			return tValue == value ? this : new Case(key, tValue);
		}
	}
	
	private static ITypeID getThrownType(CodePosition position, Case[] cases) {
		if (cases.length == 0)
			return null;
		
		ITypeID result = cases[0].value.thrownType;
		for (int i = 1; i < cases.length; i++)
			result = binaryThrow(position, result, cases[i].value.thrownType);
		
		return result;
	}
}
