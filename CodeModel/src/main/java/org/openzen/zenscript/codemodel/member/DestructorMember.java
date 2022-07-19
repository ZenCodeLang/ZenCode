package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class DestructorMember extends FunctionalMember {
	private static final FunctionHeader HEADER = new FunctionHeader(BasicTypeID.VOID);
	public FunctionalMemberRef overrides;

	public DestructorMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers) {
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
	public String describe() {
		return "destructor";
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.destructor(new MethodInstance(this, header, targetType));
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
	public String getName() {
		return "~this";
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.empty();
	}
}
