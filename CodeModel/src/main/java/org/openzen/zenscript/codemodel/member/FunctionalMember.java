package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

public abstract class FunctionalMember extends DefinitionMember implements MethodSymbol {
	public final BuiltinID builtin;
	public FunctionHeader header;
	public Statement body = null;

	public FunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			FunctionHeader header,
			BuiltinID builtin) {
		super(position, definition, modifiers);

		this.header = header;
		this.builtin = builtin;
	}

	public void setBody(Statement body) {
		this.body = body;
	}

	public abstract String getCanonicalName();

	public abstract FunctionalKind getKind();

	public FunctionalMemberRef ref(TypeID type) {
		return new FunctionalMemberRef(this, type, null);
	}

	@Override
	public FunctionalMemberRef ref(TypeID type, GenericMapper mapper) {
		return new FunctionalMemberRef(this, type, mapper);
	}

	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}

	@Override
	public int getEffectiveModifiers() {
		int result = modifiers;
		if (definition.isInterface())
			result |= Modifiers.PUBLIC;
		if (!Modifiers.hasAccess(result))
			result |= Modifiers.INTERNAL;

		return result;
	}

	@Override
	public boolean isAbstract() {
		return body == null && builtin == null;
	}

	/* MethodSymbol implementation */

	@Override
	public DefinitionSymbol getDefiningType() {
		return definition;
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}
}
