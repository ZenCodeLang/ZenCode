package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedTypeExpression extends ParsedExpression {
	private final IParsedType type;
	
	public ParsedTypeExpression(CodePosition position, IParsedType type) {
		super(position);
		
		this.type = type;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new PartialTypeExpression(position, type.compile(scope), null);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
