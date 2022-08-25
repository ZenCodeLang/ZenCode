package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.compilation.CastedEval;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedFunctionParameter {
	public final CodePosition position;
	public final ParsedAnnotation[] annotations;
	public final String name;
	public final IParsedType type;
	public final CompilableExpression defaultValue;
	public final boolean variadic;

	private FunctionParameter compiled;

	public ParsedFunctionParameter(CodePosition position, ParsedAnnotation[] annotations, String name, IParsedType type, CompilableExpression defaultValue, boolean variadic) {
		this.position = position;
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
			ExpressionCompiler defaultValueCompiler = typeBuilder.getDefaultValueCompiler();
			CastedEval cast = CastedEval.implicit(defaultValueCompiler, position, cType);
			this.compiled.defaultValue = defaultValue.compile(defaultValueCompiler).cast(cast).value;
		}
		return compiled;
	}
}
