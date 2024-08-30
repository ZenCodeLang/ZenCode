package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImplementationMember extends DefinitionMember {
	public final TypeID type;
	public final List<IDefinitionMember> members = new ArrayList<>();

	public ImplementationMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers, TypeID type) {
		super(position, definition, modifiers);

		this.type = type;
	}

	public void addMember(IDefinitionMember member) {
		this.members.add(member);
	}

	@Override
	public String describe() {
		return "implementation of " + type.toString();
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		TypeID implementsType = mapper.map(type);
		FunctionHeader header = new FunctionHeader(implementsType);
		ImplementationMemberInstance implementationInstance = new ImplementationMemberInstance(implementsType);
		members.method(MethodID.caster(type), new InterfaceCaster(header, implementationInstance));

		for (IDefinitionMember member : this.members) {
			member.registerTo(targetType, members, mapper);
		}
	}

	@Override
	public Optional<TypeID> asImplementation() {
		return Optional.of(type);
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
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface())
			result = result.withPublic();
		if (!result.hasAccessModifiers())
			result = result.withPublic();

		return result;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public FunctionHeader getHeader() {
		return null;
	}
}
