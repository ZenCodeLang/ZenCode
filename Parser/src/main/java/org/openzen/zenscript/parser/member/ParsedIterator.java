package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

public class ParsedIterator extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedFunctionHeader header;
	private final ParsedFunctionBody body;

	private IteratorMember compiled;

	public ParsedIterator(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionHeader header,
			ParsedFunctionBody body) {
		super(definition, annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.header = header;
		this.body = body;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		TypeID[] loopVariableTypes = new TypeID[header.parameters.size()];
		for (int i = 0; i < loopVariableTypes.length; i++)
			loopVariableTypes[i] = header.parameters.get(i).type.compile(context);

		compiled = new IteratorMember(position, definition, modifiers, loopVariableTypes, context.getTypeRegistry(), null);
	}

	@Override
	public IteratorMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		FunctionHeader header = new FunctionHeader(scope.getTypeRegistry().getIterator(compiled.getLoopVariableTypes()));
		StatementScope innerScope = new FunctionScope(position, scope, header);
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);
		compiled.setContent(body.compile(innerScope, header));
	}
}
