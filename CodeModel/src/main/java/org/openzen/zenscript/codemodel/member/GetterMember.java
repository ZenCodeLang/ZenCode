package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class GetterMember extends FunctionalMember {
	public final String name;
	public TypeID type;
	public Statement body = null;

	public GetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			String name,
			TypeID type) {
		super(position, definition, modifiers, modifiers.isStatic() ? MethodID.staticGetter(name) : MethodID.getter(name), new FunctionHeader(type));
		this.type = type;
		this.name = name;
	}

	public void setBody(Statement body) {
		this.body = body;

		if (type == BasicTypeID.UNDETERMINED) {
			body.getReturnType().ifPresent(returnType -> {
				this.type = returnType;
				this.header = new FunctionHeader(type);
			});
		}
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":get:" + name;
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.GETTER;
	}

	@Override
	public boolean isAbstract() {
		return body == null;
	}

	@Override
	public String describe() {
		return "getter " + name;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.method(mapper.map(targetType, this));
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
	protected void inferFromOverride(MethodInstance overrides) {
		if (type == BasicTypeID.UNDETERMINED)
			type = overrides.getHeader().getReturnType();
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface() || (overrides != null && overrides.getTarget().asDefinition().map(d -> d.definition.isInterface()).orElse(false)))
			result = result.withPublic();
		if (!result.hasAccessModifiers())
			result = result.withInternal();

		return result;
	}
}
