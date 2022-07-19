package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class InnerDefinitionMember extends DefinitionMember {
	public final HighLevelDefinition innerDefinition;

	public InnerDefinitionMember(CodePosition position, HighLevelDefinition outer, Modifiers modifiers, HighLevelDefinition definition) {
		super(position, outer, definition instanceof InterfaceDefinition ? modifiers.withStatic() : modifiers);

		this.innerDefinition = definition;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		if (isStatic() || mapper == null || mapper.getMapping().isEmpty()) {
			type.addInnerType(innerDefinition.name, new InnerDefinition(innerDefinition));
		} else {
			type.addInnerType(innerDefinition.name, new InnerDefinition(innerDefinition, mapper.getMapping()));
		}
	}

	@Override
	public String describe() {
		return "inner type " + innerDefinition.name;
	}

	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.inner(innerDefinition);
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitInnerDefinition(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitInnerDefinition(context, this);
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface())
			result = result.withPublic();
		if (!modifiers.hasAccessModifiers())
			result = result.withInternal();

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
