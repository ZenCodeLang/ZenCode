package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class DestructorMember extends FunctionalMember {
	private static final FunctionHeader HEADER = new FunctionHeader(BasicTypeID.VOID);
	public FunctionalMemberRef overrides;
	
	public DestructorMember(CodePosition position, HighLevelDefinition definition, int modifiers) {
		super(position, definition, modifiers, HEADER, null);
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":destructor";
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.DESTRUCTOR;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		if (priority == TypeMemberPriority.SPECIFIED)
			type.addDestructor(ref(type.type, mapper), priority);
	}

	@Override
	public String describe() {
		return "destructor";
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitDestructor(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitDestructor(context, this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return overrides;
	}
}
