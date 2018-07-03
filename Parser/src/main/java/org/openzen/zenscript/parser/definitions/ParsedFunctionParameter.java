package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Stanneke
 */
public class ParsedFunctionParameter {
	public final ParsedAnnotation[] annotations;
	public final String name;
	public final IParsedType type;
	public final ParsedExpression defaultValue;
	public final boolean variadic;
	
	public ParsedFunctionParameter(ParsedAnnotation[] annotations, String name, IParsedType type, ParsedExpression defaultValue, boolean variadic) {
		this.annotations = annotations;
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}
	
	public FunctionParameter compile(BaseScope scope) {
		ITypeID cType = type.compile(scope);
		Expression cDefaultValue = null;
		if (defaultValue != null) {
			cDefaultValue = defaultValue.compile(new ExpressionScope(scope, cType)).eval();
		}
		return new FunctionParameter(cType, name, cDefaultValue, variadic);
	}
}
