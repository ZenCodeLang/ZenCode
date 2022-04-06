package org.openzen.zenscript.parser.definitions;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.TypeBuilder;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedFunctionParameter {
	public final ParsedAnnotation[] annotations;
	public final String name;
	public final IParsedType type;
	public final ParsedExpression defaultValue;
	public final boolean variadic;

	private FunctionParameter compiled;

	public ParsedFunctionParameter(ParsedAnnotation[] annotations, String name, IParsedType type, ParsedExpression defaultValue, boolean variadic) {
		this.annotations = annotations;
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}

	public FunctionParameter compile(TypeBuilder typeBuilder) {
		if (compiled != null)
			return compiled;

		TypeID cType = type.compile(typeBuilder);
		this.compiled = new FunctionParameter(cType, name, null, variadic);
		if (defaultValue != null) {
			this.compiled.defaultValue = defaultValue.compile(typeBuilder.getDefaultValueCompiler()).as(cType);
		}
		return compiled;
	}
}
