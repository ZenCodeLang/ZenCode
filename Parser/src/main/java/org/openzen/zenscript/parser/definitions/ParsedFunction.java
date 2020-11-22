package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import static org.openzen.zenscript.lexer.ZSTokenType.*;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.CompilingType;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.statements.ParsedStatement;

public class ParsedFunction extends ParsedDefinition {
	public static ParsedFunction parseFunction(
			CompilingPackage pkg,
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ZSTokenParser parser,
			HighLevelDefinition outerDefinition) throws ParseException {
		String name = parser.required(T_IDENTIFIER, "identifier expected").content;
		ParsedFunctionHeader header = ParsedFunctionHeader.parse(parser);
		ParsedFunctionBody body = ParsedStatement.parseFunctionBody(parser);
		return new ParsedFunction(pkg, position, modifiers, annotations, name, header, body, outerDefinition);
	}
	
	private final ParsedFunctionHeader header;
	private final ParsedFunctionBody body;

	private final FunctionDefinition compiled;
	
	private ParsedFunction(CompilingPackage pkg, CodePosition position, int modifiers, ParsedAnnotation[] annotations, String name, ParsedFunctionHeader header, ParsedFunctionBody body, HighLevelDefinition outerDefinition) {
		super(position, modifiers, pkg, annotations);
		
		this.header = header;
		this.body = body;
		
		compiled = new FunctionDefinition(position, pkg.module, pkg.getPackage(), name, modifiers, outerDefinition);
	}

	@Override
	public HighLevelDefinition getCompiled() {
		return compiled;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		if (compiled.header == null)
			compiled.setHeader(context.getTypeRegistry(), header.compile(context));
	}
	
	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		
	}

	@Override
	public void compile(BaseScope scope) {
		FunctionScope innerScope = new FunctionScope(position, scope, compiled.header);
		compiled.setCode(body.compile(innerScope, compiled.header));
		
		if (compiled.header.getReturnType() == BasicTypeID.UNDETERMINED)
			compiled.header.setReturnType(compiled.caller.body.getReturnType());
	}

	@Override
	public CompilingType getCompiling(TypeResolutionContext context) {
		return new Compiling(context);
	}

	private class Compiling implements CompilingType {
		private final TypeResolutionContext context;
		
		public Compiling(TypeResolutionContext context) {
			this.context = context;
		}
		
		@Override
		public CompilingType getInner(String name) {
			return null;
		}

		@Override
		public HighLevelDefinition load() {
			linkTypes(context);
			return compiled;
		}
	}
}
