package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class MethodMember extends FunctionalMember {
	public final String name;
	private FunctionalMemberRef overrides;
	
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
	public int getEffectiveModifiers() {
		int result = super.getEffectiveModifiers();
		if (overrides != null) {
			if (overrides.getTarget().isPublic())
				result |= Modifiers.PUBLIC;
			if (overrides.getTarget().isProtected())
				result |= Modifiers.PROTECTED;
		}
		return result;
	}

	@Override
	public FunctionalMemberRef getOverrides() {
		return overrides;
	}
	
	public void setOverrides(GlobalTypeRegistry registry, FunctionalMemberRef overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(registry, overrides.getHeader());
	}
}
