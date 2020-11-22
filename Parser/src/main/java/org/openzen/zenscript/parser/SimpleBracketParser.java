package org.openzen.zenscript.parser;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallStaticExpression;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.parser.expression.ParsedExpression;

public class SimpleBracketParser implements BracketExpressionParser {
	private final FunctionalMemberRef method;
	private final TypeID targetType;
	
	public SimpleBracketParser(GlobalTypeRegistry registry, FunctionalMemberRef method) {
		if (!method.isStatic())
			throw new IllegalArgumentException("Method must be static");
		if (method.getHeader().getNumberOfTypeParameters() > 0)
			throw new IllegalArgumentException("Method cannot have type parameters");
		
		this.method = method;
		this.targetType = registry.getForDefinition(method.getTarget().definition);
	}

	@Override
	public ParsedExpression parse(CodePosition position, ZSTokenParser tokens) throws ParseException {
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
			return new CallStaticExpression(position, targetType, method, method.getHeader(), new CallArguments(new ConstantStringExpression(position, value)));
		}

		@Override
		public boolean hasStrongType() {
			return true;
		}
	}
}
