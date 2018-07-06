/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.MatchExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedMatchExpression extends ParsedExpression {
	public final ParsedExpression value;
	public final List<Case> cases;
	
	public ParsedMatchExpression(CodePosition position, ParsedExpression value, List<Case> cases) {
		super(position);
		
		this.value = value;
		this.cases = cases;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		Expression cValue = value.compile(scope).eval();
		MatchExpression.Case[] cCases = new MatchExpression.Case[cases.size()];
		for (int i = 0; i < cases.size(); i++) {
			Case matchCase = cases.get(i);
			cCases[i] = matchCase.compile(cValue.type, scope);
		}
		
		ITypeID result = cCases[0].value.type;
		for (int i = 1; i < cCases.length; i++) {
			ITypeID oldResult = result;
			result = scope.getTypeMembers(result).union(cCases[i].value.type);
			if (result == null)
				throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "Matches have different types: " + oldResult + " and " + cCases[i].value.type);
		}
		
		return new MatchExpression(position, cValue, result, cCases);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
	
	public static class Case {
		public final ParsedExpression name;
		public final ParsedExpression value;
		
		public Case(ParsedExpression name, ParsedExpression body) {
			this.name = name;
			this.value = body;
		}
		
		public MatchExpression.Case compile(ITypeID valueType, ExpressionScope scope) {
			if (name == null) {
				ExpressionScope innerScope = scope.createInner(scope.hints, scope.getDollar());
				Expression value = this.value.compile(innerScope).eval();
				return new MatchExpression.Case(null, value);
			}
			
			SwitchValue switchValue = name.compileToSwitchValue(valueType, scope.withHint(valueType));
			ExpressionScope innerScope = scope.createInner(scope.hints, scope.getDollar());
			if (switchValue instanceof VariantOptionSwitchValue) {
				VariantOptionSwitchValue variantSwitchValue = (VariantOptionSwitchValue)switchValue;
				
				for (VarStatement var : variantSwitchValue.parameters)
					innerScope.addInnerVariable(var);
			}
			
			Expression value = this.value.compile(innerScope).eval();
			return new MatchExpression.Case(switchValue, value);
		}
	}
}
