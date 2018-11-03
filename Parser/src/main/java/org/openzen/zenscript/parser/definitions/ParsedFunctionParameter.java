package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
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
		
		StoredType cType = type.compile(context);
		Expression cDefaultValue = null;
		return compiled = new FunctionParameter(cType, name, null, variadic);
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
