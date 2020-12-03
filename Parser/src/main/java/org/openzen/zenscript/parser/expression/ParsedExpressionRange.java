package org.openzen.zenscript.parser.expression;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.RangeExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ParsedExpressionRange extends ParsedExpression {
	private final ParsedExpression from;
	private final ParsedExpression to;
	
	public ParsedExpressionRange(CodePosition position, ParsedExpression from, ParsedExpression to) {
		super(position);
		
		this.from = from;
		this.to = to;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		List<TypeID> fromHints = new ArrayList<>();
		List<TypeID> toHints = new ArrayList<>();
		
		for (TypeID hint : scope.hints) {
			if (hint instanceof RangeTypeID) {
				RangeTypeID rangeHint = (RangeTypeID) hint;
				if (!fromHints.contains(rangeHint.baseType))
					fromHints.add(rangeHint.baseType);
				if (!toHints.contains(rangeHint.baseType))
					toHints.add(rangeHint.baseType);
			}
		}
		
		Expression from = this.from.compile(scope.withHints(fromHints)).eval();
		Expression to = this.to.compile(scope.withHints(toHints)).eval();

		TypeID baseType = scope.getTypeMembers(from.type).union(to.type);
		return new RangeExpression(position, scope.getTypeRegistry().getRange(baseType), from, to);
	}

	@Override
	public boolean hasStrongType() {
		return from.hasStrongType() && to.hasStrongType();
	}
}
