/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.expression.MatchExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.LambdaScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
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
		
		ITypeID result = cCases[0].value.header.returnType;
		for (int i = 1; i < cCases.length; i++) {
			result = scope.getTypeMembers(result).union(cCases[i].value.header.returnType);
		}
		
		return new MatchExpression(position, cValue, result, cCases);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
	
	public static class Case {
		public final ParsedExpression name;
		public final ParsedFunctionBody body;
		
		public Case(ParsedExpression name, ParsedFunctionBody body) {
			this.name = name;
			this.body = body;
		}
		
		public MatchExpression.Case compile(ITypeID valueType, ExpressionScope scope) {
			ITypeID result = BasicTypeID.ANY;
			if (scope.getResultTypeHints().size() == 1)
				result = scope.getResultTypeHints().get(0);
			
			if (name == null) {
				FunctionHeader header = new FunctionHeader(result, FunctionParameter.NONE);
				LambdaClosure closure = new LambdaClosure();
				StatementScope innerScope = new LambdaScope(scope, closure, header);
				Statement contents = body.compile(innerScope, header);
				return new MatchExpression.Case(null, new FunctionExpression(null, scope.getTypeRegistry().getFunction(header), closure, contents));
			}
			
			SwitchValue switchValue = name.compileToSwitchValue(valueType, scope.withHint(valueType));
			FunctionParameter[] parameters = FunctionParameter.NONE;
			if (switchValue instanceof VariantOptionSwitchValue) {
				VariantOptionSwitchValue variantSwitchValue = (VariantOptionSwitchValue)switchValue;
				
				parameters = new FunctionParameter[variantSwitchValue.option.types.length];
				for (int i = 0; i < parameters.length; i++)
					parameters[i] = new FunctionParameter(variantSwitchValue.option.types[i], variantSwitchValue.parameters[i]);
			}
			
			FunctionHeader header = new FunctionHeader(result, parameters);
			LambdaClosure closure = new LambdaClosure();
			StatementScope innerScope = new LambdaScope(scope, closure, header);
			Statement contents = body.compile(innerScope, header);
			return new MatchExpression.Case(switchValue, new FunctionExpression(name.position, scope.getTypeRegistry().getFunction(header), closure, contents));
		}
	}
}
