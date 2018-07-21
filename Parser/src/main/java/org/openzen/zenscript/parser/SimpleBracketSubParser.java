/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class SimpleBracketSubParser implements BracketExpressionParser {
	private final FunctionalMemberRef method;
	private final ITypeID targetType;
	
	public SimpleBracketSubParser(GlobalTypeRegistry registry, FunctionalMemberRef method) {
		if (!method.isStatic())
			throw new IllegalArgumentException("Method must be static");
		if (method.header.getNumberOfTypeParameters() > 0)
			throw new IllegalArgumentException("Method cannot have type parameters");
		
		this.method = method;
		this.targetType = registry.getForDefinition(method.getTarget().definition);
	}

	@Override
	public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) {
		StringBuilder string = new StringBuilder();
		while (tokens.optional(ZSTokenType.T_GREATER) == null) {
			string.append(tokens.next().content);
			string.append(tokens.getLastWhitespace());
		}
		return new StaticMethodCallExpression(position, string.toString());
	}
	
	private class StaticMethodCallExpression extends ParsedExpression {
		private final String value;
		
		public StaticMethodCallExpression(CodePosition position, String value) {
			super(position);
			
			this.value = value;
		}

		@Override
		public IPartialExpression compile(ExpressionScope scope) {
			return new CallStaticExpression(position, targetType, method, method.header, new CallArguments(new ConstantStringExpression(position, value)), scope);
		}

		@Override
		public boolean hasStrongType() {
			return true;
		}

		@Override
		public ITypeID precompileForType(ExpressionScope scope, PrecompilationState state) {
			return method.header.returnType;
		}
	}
}
