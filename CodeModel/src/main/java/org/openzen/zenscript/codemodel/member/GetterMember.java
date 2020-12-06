package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class GetterMember extends PropertyMember {
	public final String name;
	public Statement body = null;
	private GetterMemberRef overrides;

	public GetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			TypeID type,
			BuiltinID builtin) {
		super(position, definition, modifiers, type, builtin);

		this.name = name;
	}

	public void setBody(Statement body) {
		this.body = body;

		if (getType() == BasicTypeID.UNDETERMINED) {
			TypeID returnType = body.getReturnType();
			if (returnType != null)
				setType(returnType);
		}
	}

	@Override
	public boolean isAbstract() {
		return body == null && builtin == null;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addGetter(new GetterMemberRef(members.type, this, mapper), priority);
	}

	@Override
	public String describe() {
		return "getter " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitGetter(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitGetter(context, this);
	}

	@Override
	public GetterMemberRef getOverrides() {
		return overrides;
	}

	public void setOverrides(GetterMemberRef override) {
		this.overrides = override;

		if (getType() == BasicTypeID.UNDETERMINED)
			setType(override.getType());
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
	public void normalize(TypeScope scope) {
		if (body != null)
			body = body.normalize(scope, ConcatMap.empty(LoopStatement.class, LoopStatement.class));
	}

	@Override
	public GetterMemberRef ref(TypeID type, GenericMapper mapper) {
		return new GetterMemberRef(type, this, mapper);
	}

	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(getType());
	}
}
