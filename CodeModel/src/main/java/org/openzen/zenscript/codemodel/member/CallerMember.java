package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class CallerMember extends FunctionalMember {
	public MethodSymbol overrides;

	public CallerMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			FunctionHeader header,
			BuiltinID builtin) {
		super(position, definition, modifiers, header, builtin);
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":caller:" + header.getCanonical();
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CALLER;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addCaller(ref(type.type, mapper), priority);
	}

	@Override
	public String describe() {
		return "caller " + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCaller(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitCaller(context, this);
	}

	@Override
	public int getEffectiveModifiers() {
		int result = super.getEffectiveModifiers();
		if (overrides != null && overrides.getDefiningType().isInterface())
			result |= Modifiers.PUBLIC;

		return result;
	}

	public void setOverrides(MethodSymbol overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(overrides.getHeader());
	}

	@Override
	public MethodSymbol getOverrides() {
		return overrides;
	}
}
