package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Stanneke
 */
public class ParsedFunctionParameter {
	public final String name;
	public final IParsedType type;
	public final ParsedExpression defaultValue;
	public final boolean variadic;
	
	public ParsedFunctionParameter(String name, IParsedType type, ParsedExpression defaultValue, boolean variadic) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}
	
	public FunctionParameter compile(BaseScope scope) {
		return new FunctionParameter(type.compile(scope), name);
	}
}
