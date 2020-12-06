package org.openzen.zenscript.parser.definitions;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.context.CompilingType;
import org.openzen.zenscript.codemodel.context.LocalTypeResolutionContext;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.DefinitionScope;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.ParsedDefinition;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.member.ParsedDefinitionMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseParsedDefinition extends ParsedDefinition {
	protected final List<ParsedDefinitionMember> members = new ArrayList<>();
	private final Map<String, ParsedDefinition> innerTypes = new HashMap<>();
	private boolean typesCompiled = false;
	private boolean isCompiled = false;

	public BaseParsedDefinition(CodePosition position, int modifiers, CompilingPackage pkg, ParsedAnnotation[] annotations) {
		super(position, modifiers, pkg, annotations);
	}

	public void addMember(ParsedDefinitionMember member) {
		members.add(member);
		member.registerInnerTypes(innerTypes);
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		if (typesCompiled)
			return;
		typesCompiled = true;

		linkTypesLocal(context);
	}

	protected void linkTypesLocal(TypeResolutionContext localContext) {
		for (ParsedDefinitionMember member : members) {
			member.linkTypes(localContext);
			getCompiled().addMember(member.getCompiled());
		}
	}

	@Override
	public void registerMembers(BaseScope scope, PrecompilationState state) {
		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members) {
			state.register(innerScope, member);
			member.registerMembers(innerScope, state);
		}
	}

	@Override
	public void compile(BaseScope scope) throws CompileException {
		if (isCompiled)
			return;
		isCompiled = true;

		getCompiled().annotations = ParsedAnnotation.compileForDefinition(annotations, getCompiled(), scope);

		DefinitionScope innerScope = new DefinitionScope(scope, getCompiled());
		for (ParsedDefinitionMember member : members)
			member.compile(innerScope);
	}

	@Override
	public CompilingType getCompiling(TypeResolutionContext context) {
		return new Compiling(context);
	}

	private class Compiling implements CompilingType {
		private final TypeResolutionContext context;

		private Compiling(TypeResolutionContext context) {
			this.context = new LocalTypeResolutionContext(context, this, getCompiled().typeParameters);
		}

		@Override
		public HighLevelDefinition load() {
			linkTypes(context);
			return getCompiled();
		}

		@Override
		public CompilingType getInner(String name) {
			if (!innerTypes.containsKey(name))
				return null;

			return innerTypes.get(name).getCompiling(context);
		}
	}
}
