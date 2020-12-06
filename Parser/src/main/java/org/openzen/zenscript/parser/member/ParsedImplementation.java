package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ImplementationScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.ArrayList;
import java.util.List;

public class ParsedImplementation extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final IParsedType type;
	private final List<ParsedDefinitionMember> members = new ArrayList<>();

	private ImplementationMember compiled;

	public ParsedImplementation(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			IParsedType type) {
		super(definition, annotations);

		this.position = position;
		this.modifiers = modifiers;
		this.type = type;
	}

	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new ImplementationMember(position, definition, modifiers, type.compile(context));

		for (ParsedDefinitionMember member : members) {
			member.linkTypes(context);
			compiled.addMember(member.getCompiled());
		}
	}

	@Override
	public ImplementationMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) throws CompileException {
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);

		ImplementationScope innerScope = new ImplementationScope(scope, compiled);
		for (ParsedDefinitionMember member : members) {
			member.compile(innerScope);
		}
	}

	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		ImplementationScope innerScope = new ImplementationScope(scope, compiled);
		for (ParsedDefinitionMember member : members)
			state.register(innerScope, member);
	}
}
