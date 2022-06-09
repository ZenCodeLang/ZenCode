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

	public InnerDefinitionMember(CodePosition position, HighLevelDefinition outer, int modifiers, HighLevelDefinition definition) {
		super(position, outer, definition instanceof InterfaceDefinition ? modifiers | Modifiers.STATIC : modifiers);

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
	public void registerTo(MemberSet members, GenericMapper mapper) {
		// TODO
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
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitInnerDefinition(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitInnerDefinition(context, this);
	}

	@Override
	public MethodSymbol getOverrides() {
		return null;
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
		return false;
	}

	@Override
	public DefinitionMemberRef ref(TypeID type, GenericMapper mapper) {
		throw new UnsupportedOperationException("Cannot create an inner definition reference");
	}

	@Override
	public FunctionHeader getHeader() {
		return null;
	}
}
