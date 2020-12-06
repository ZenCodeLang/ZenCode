package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;

import java.util.Map;

public class ParsedInnerDefinition extends ParsedDefinitionMember {
	private final ParsedDefinition innerDefinition;
	private final InnerDefinitionMember member;
	private boolean typesCompiled = false;

	public ParsedInnerDefinition(HighLevelDefinition outer, ParsedDefinition definition) {
		super(outer, ParsedAnnotation.NONE);

		this.innerDefinition = definition;

		member = new InnerDefinitionMember(definition.getPosition(), outer, definition.getModifiers(), definition.getCompiled());
	}

	@Override
	public void registerInnerTypes(Map<String, ParsedDefinition> inner) {
		inner.put(innerDefinition.getCompiled().name, innerDefinition);
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		if (typesCompiled)
			return;
		typesCompiled = true;

		//System.out.println("compileTypes " + definition.name + "::" + innerDefinition.getCompiled().name);
		innerDefinition.linkTypes(context);
	}

	@Override
	public InnerDefinitionMember getCompiled() {
		return member;
	}

	@Override
	public void compile(BaseScope scope) throws CompileException {
		innerDefinition.compile(scope);
	}

	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		innerDefinition.registerMembers(scope, state);
	}
}
