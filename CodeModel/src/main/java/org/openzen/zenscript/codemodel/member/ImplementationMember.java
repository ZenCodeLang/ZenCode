package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ExpressionBuilder;
import org.openzen.zenscript.codemodel.compilation.InstanceCallableMethod;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.*;

public class ImplementationMember extends DefinitionMember {
	public final TypeID type;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public final Map<DefinitionMemberRef, IDefinitionMember> definitionBorrowedMembers = new HashMap<>(); // contains members from the outer definition to implement interface members

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
		ImplementationMemberInstance implementationInstance = new ImplementationMemberInstance(this, targetType, implementsType);
		members.cast(new InstanceCallableMethod() {
			@Override
			public FunctionHeader getHeader() {
				return header;
			}

			@Override
			public Optional<MethodInstance> asMethod() {
				return Optional.empty();
			}

			@Override
			public Modifiers getModifiers() {
				return Modifiers.IMPLICIT;
			}

			@Override
			public Expression call(ExpressionBuilder builder, Expression instance, CallArguments arguments) {
				return builder.interfaceCast(implementationInstance, instance);
			}
		});

		for (IDefinitionMember member : this.members) {
			member.registerTo(targetType, members, mapper);
		}
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
