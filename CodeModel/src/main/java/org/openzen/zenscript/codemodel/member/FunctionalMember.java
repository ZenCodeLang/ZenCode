package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.statement.Statement;

public abstract class FunctionalMember extends DefinitionMember implements MethodSymbol {
	public FunctionHeader header;
	public Statement body = null;

	public FunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			FunctionHeader header) {
		super(position, definition, modifiers);
		this.header = header;
	}

	public void setBody(Statement body) {
		this.body = body;
	}

	public abstract String getCanonicalName();

	public abstract FunctionalKind getKind();

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface())
			result = result.withPublic();
		if (!result.hasAccessModifiers())
			result = result.withInternal();

		return result;
	}

	@Override
	public boolean isAbstract() {
		return body == null;
	}

	/* MethodSymbol implementation */

	@Override
	public DefinitionSymbol getDefiningType() {
		return definition;
	}

	@Override
	public TypeSymbol getTargetType() {
		return target;
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}
}
