package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.ref.ConstMemberRef;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class ConstMember extends PropertyMember implements FieldSymbol {
	public final String name;
	public Expression value;

	public ConstMember(CodePosition position, HighLevelDefinition definition, int modifiers, String name, TypeID type, BuiltinID builtin) {
		super(position, definition, modifiers, type, builtin);

		this.name = name;
	}

	@Override
	public String describe() {
		return "const " + name;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addConst(new ConstMemberRef(members.type, this, mapper));
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConst(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitConst(context, this);
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
		return new ConstMemberRef(type, this, mapper);
	}

	@Override
	public FunctionHeader getHeader() {
		return null;
	}
}
