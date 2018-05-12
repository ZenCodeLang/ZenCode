/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionVariable extends ParsedExpression {
	private final String name;
	private final List<IParsedType> genericParameters;
	
	public ParsedExpressionVariable(CodePosition position, String name, List<IParsedType> genericParameters) {
		super(position);

		this.name = name;
		this.genericParameters = genericParameters;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID[] genericArguments = null;
		if (genericParameters != null) {
			genericArguments = new ITypeID[genericParameters.size()];
			for (int i = 0; i < genericParameters.size(); i++) {
				genericArguments[i] = genericParameters.get(i).compile(scope);
			}
		}
		
		IPartialExpression result = scope.get(position, new GenericName(name, genericArguments));
		if (result == null) {
			for (ITypeID hint : scope.hints) {
				EnumConstantMember member = scope.getTypeMembers(hint).getEnumMember(name);
				if (member != null)
					return new EnumConstantExpression(position, hint, member);
			}
			
			throw new CompileException(position, CompileExceptionCode.UNDEFINED_VARIABLE, "No such symbol: " + name);
		} else {
			return result;
		}
	}

	@Override
	public Expression compileKey(ExpressionScope scope) {
		return new ConstantStringExpression(position, name);
	}
	
	@Override
	public ParsedFunctionHeader toLambdaHeader() {
		return new ParsedFunctionHeader(Collections.singletonList(toLambdaParameter()), ParsedTypeBasic.ANY);
	}
	
	@Override
	public ParsedFunctionParameter toLambdaParameter() {
		return new ParsedFunctionParameter(name, ParsedTypeBasic.ANY, null, false);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
