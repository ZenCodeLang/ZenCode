package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.FileScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.Collections;

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

	private FunctionParameter compiled;

	public ParsedFunctionParameter(ParsedAnnotation[] annotations, String name, IParsedType type, ParsedExpression defaultValue, boolean variadic) {
		this.annotations = annotations;
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.variadic = variadic;
	}

	public FunctionParameter compile(TypeResolutionContext context) {
		if (compiled != null)
			return compiled;

		TypeID cType = type.compile(context);
		Expression cDefaultValue = null;


		this.compiled = new FunctionParameter(cType, name, null, variadic);
		compileInitializer(new FileScope(context, Collections.emptyList(), Collections.emptyMap()), new PrecompilationState());
		return compiled;
	}

	// TODO: this isn't called!
	public void compileInitializer(BaseScope scope, PrecompilationState state) {
		if (defaultValue != null) {
			try {
				compiled.defaultValue = defaultValue.compile(new ExpressionScope(scope, compiled.type)).eval();
			} catch (CompileException ex) {
				compiled.defaultValue = new InvalidExpression(compiled.type, ex);
			}
		}
	}
}
