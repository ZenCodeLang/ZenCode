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

public class SetterMember extends FunctionalMember {
	public final String name;
	public TypeID type;
	public Statement body;
	public FunctionParameter parameter;
	private MethodInstance overrides;

	public SetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			String name,
			TypeID type) {
		super(position, definition, modifiers, MethodID.getter(name), new FunctionHeader(BasicTypeID.VOID, new FunctionParameter(type, "$")));

		this.type = type;
		this.name = name;
		this.parameter = header.parameters[0];
	}

	public void setBody(Statement body) {
		this.body = body;
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":get:" + name;
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.SETTER;
	}

	@Override
	public String describe() {
		return "setter " + name;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.setter(mapper.map(targetType, this));
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
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	public void setOverrides(MethodInstance overrides) {
		this.overrides = overrides;

		if (type == BasicTypeID.UNDETERMINED) {
			this.type = overrides.getHeader().getReturnType();
			parameter = new FunctionParameter(overrides.getHeader().getReturnType(), "$");
			header = new FunctionHeader(BasicTypeID.VOID, parameter);
		}
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface() || (overrides != null && overrides.getTarget().asDefinition().map(t -> t.definition.isInterface()).orElse(false)))
			result = result.withPublic();
		if (!result.hasAccessModifiers())
			result = result.withInternal();

		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.empty();
	}
}
