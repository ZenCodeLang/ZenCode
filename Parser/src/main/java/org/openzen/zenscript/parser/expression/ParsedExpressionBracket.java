/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionBracket extends ParsedExpression {
	public List<ParsedExpression> expressions;
	
	public ParsedExpressionBracket(CodePosition position, List<ParsedExpression> expressions) {
		super(position);
		
		this.expressions = expressions;
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		if (expressions.size() != 1) {
			throw new CompileException(position, CompileExceptionCode.BRACKET_MULTIPLE_EXPRESSIONS, "Bracket expression may have only one expression");
		} else {
			return expressions.get(0).compile(scope);
		}
	}
	
	@Override
	public ParsedFunctionHeader toLambdaHeader() {
		List<ParsedFunctionParameter> parameters = new ArrayList<>();
		for (ParsedExpression expression : expressions)
			parameters.add(expression.toLambdaParameter());
		
		return new ParsedFunctionHeader(parameters, ParsedTypeBasic.ANY, null);
	}

	@Override
	public boolean hasStrongType() {
		return expressions.get(0).hasStrongType();
	}
}
