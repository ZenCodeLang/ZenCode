package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

import java.util.*;

public class ImplementationMember extends DefinitionMember {
	public final TypeID type;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public final Map<DefinitionMemberRef, IDefinitionMember> definitionBorrowedMembers = new HashMap<>(); // contains members from the outer definition to implement interface members

	public ImplementationMember(CodePosition position, HighLevelDefinition definition, int modifiers, TypeID type) {
		super(position, definition, modifiers);

		this.type = type;
	}

	public void addMember(IDefinitionMember member) {
		this.members.add(member);
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		TypeID instancedType = mapper == null ? type : mapper.map(type);
		members.addImplementation(new ImplementationMemberRef(this, members.type, instancedType), priority);

		TypeMembers interfaceTypeMembers = members.getMemberCache().get(instancedType);
		interfaceTypeMembers.copyMembersTo(members, TypeMemberPriority.INTERFACE);
	}

	@Override
	public String describe() {
		return "implementation of " + type.toString();
	}

	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitImplementation(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitImplementation(context, this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public int getEffectiveModifiers() {
		int result = modifiers;
		if (definition.isInterface())
			result |= Modifiers.PUBLIC;
		if (!Modifiers.hasAccess(result))
			result |= Modifiers.PUBLIC;

		return result;
	}

	@Override
	public void normalize(TypeScope scope) {
		Set<IDefinitionMember> implemented = new HashSet<>();
		for (IDefinitionMember member : members) {
			member.normalize(scope);
			if (member.getOverrides() != null)
				implemented.add(member.getOverrides().getTarget());
		}

		TypeMembers interfaceMembers = scope.getTypeMembers(type);
		TypeMembers definitionMembers = scope.getTypeMembers(scope.getTypeRegistry().getForMyDefinition(definition));

		definitionBorrowedMembers.clear();
		definitionBorrowedMembers.putAll(interfaceMembers.borrowInterfaceMembersFromDefinition(implemented, definitionMembers));
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public DefinitionMemberRef ref(TypeID type, GenericMapper mapper) {
		throw new UnsupportedOperationException("Cannot create an implementation reference");
	}

	@Override
	public FunctionHeader getHeader() {
		return null;
	}
}
