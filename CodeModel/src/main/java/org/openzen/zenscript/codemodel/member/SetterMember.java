package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class SetterMember extends PropertyMember implements MethodSymbol {
	public final String name;
	public Statement body;
	public FunctionParameter parameter;
	private SetterMemberRef overrides;

	public SetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			TypeID type,
			BuiltinID builtin) {
		super(position,
				definition,
				modifiers,
				type,
				builtin);

		this.name = name;
		this.parameter = new FunctionParameter(type, "value");
	}

	public void setBody(Statement body) {
		this.body = body;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addSetter(new SetterMemberRef(members.type, this, mapper), priority);
	}

	@Override
	public String describe() {
		return "setter " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitSetter(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitSetter(context, this);
	}

	@Override
	public SetterMemberRef getOverrides() {
		return overrides;
	}

	public void setOverrides(SetterMemberRef overrides) {
		this.overrides = overrides;

		if (getType() == BasicTypeID.UNDETERMINED) {
			setType(overrides.getType());
			parameter = new FunctionParameter(overrides.getType(), "value");
		}
	}

	@Override
	public int getEffectiveModifiers() {
		int result = modifiers;
		if (definition.isInterface() || (overrides != null && overrides.getTarget().getDefinition().isInterface()))
			result |= Modifiers.PUBLIC;
		if (!Modifiers.hasAccess(result))
			result |= Modifiers.INTERNAL;

		return result;
	}

	@Override
	public boolean isAbstract() {
		return body == null && builtin == null;
	}

	@Override
	public DefinitionMemberRef ref(TypeID type, GenericMapper mapper) {
		return new SetterMemberRef(type, this, mapper);
	}

	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(BasicTypeID.VOID, getType());
	}
}
