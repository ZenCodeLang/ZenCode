package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class MethodMember extends FunctionalMember {
	public final String name;
	private MethodSymbol overrides;

	public MethodMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, FunctionHeader header, BuiltinID builtin) {
		super(position, definition, modifiers, header, builtin);

		this.name = name;
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":" + name + header.getCanonical();
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.METHOD;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addMethod(name, ref(type.type, mapper), priority);
	}

	@Override
	public void registerTo(MemberSet.Builder members, GenericMapper mapper) {
		if (isStatic()) {
			members.staticMethod(name, mapper.map(this));
		} else {
			members.method(name, mapper.map(this));
		}
	}

	@Override
	public String describe() {
		return name + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitMethod(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitMethod(context, this);
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = super.getEffectiveModifiers();
		if (overrides != null) {
			if (overrides.getModifiers().isPublic())
				result = result.withPublic();
			if (overrides.getModifiers().isProtected())
				result = result.withProtected();
		}
		return result;
	}

	@Override
	public MethodSymbol getOverrides() {
		return overrides;
	}

	public void setOverrides(MethodSymbol overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(overrides.getHeader());
	}

	@Override
	public String getName() {
		return name;
	}
}
