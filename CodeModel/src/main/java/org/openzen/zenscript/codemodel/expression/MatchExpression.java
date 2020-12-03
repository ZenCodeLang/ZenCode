package org.openzen.zenscript.codemodel.expression;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.BreakStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class MatchExpression extends Expression {
	public final Expression value;
	public final Case[] cases;
	
	public MatchExpression(CodePosition position, Expression value, TypeID type, Case[] cases) {
		super(position, type, binaryThrow(position, value.thrownType, getThrownType(position, cases)));
		
		this.value = value;
		this.cases = cases;
	}

	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitMatch(this);
	}

	@Override
	public <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor) {
		return visitor.visitMatch(context, this);
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

	@Override
	public Expression normalize(TypeScope scope) {
		Case[] normalizedCases = new Case[cases.length];
		for (int i = 0; i < cases.length; i++)
			normalizedCases[i] = cases[i].normalize(scope);
		return new MatchExpression(position, value.normalize(scope), type, normalizedCases);
	}
	
	public SwitchedMatch convertToSwitch(String tempVariable) {
		VarStatement result = new VarStatement(position, new VariableID(), tempVariable, type, null, false);
		SwitchStatement switchStatement = new SwitchStatement(position, null, value);
		boolean hasDefault = false;
		for (MatchExpression.Case matchCase : cases) {
			Expression caseExpression;
			boolean reachable = true;
			if (matchCase.value instanceof ThrowExpression || matchCase.value instanceof PanicExpression) {
				caseExpression = matchCase.value;
				reachable = false;
			} else {
				caseExpression = new SetLocalVariableExpression(matchCase.value.position, result, matchCase.value);
			}
			List<Statement> statements = new ArrayList<>();
			statements.add(new ExpressionStatement(matchCase.value.position, caseExpression));
			if (reachable)
				statements.add(new BreakStatement(matchCase.value.position, switchStatement));
			SwitchCase switchCase = new SwitchCase(matchCase.key, statements.toArray(new Statement[statements.size()]));
			switchStatement.cases.add(switchCase);
			
			if (matchCase.key == null)
				hasDefault = true;
		}
		if (!hasDefault) {
			Statement defaultCase = new ExpressionStatement(position, new PanicExpression(position, BasicTypeID.VOID, new ConstantStringExpression(position, "Missing case")));
			switchStatement.cases.add(new SwitchCase(null, new Statement[] { defaultCase }));
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
		
		public Case normalize(TypeScope scope) {
			return new Case(key, value.normalize(scope));
		}
	}
	
	private static TypeID getThrownType(CodePosition position, Case[] cases) {
		if (cases.length == 0)
			return null;

		TypeID result = cases[0].value.thrownType;
		for (int i = 1; i < cases.length; i++)
			result = binaryThrow(position, result, cases[i].value.thrownType);
		
		return result;
	}
}
