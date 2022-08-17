package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class SetterMember extends PropertyMember implements MethodSymbol {
	public final String name;
	public Statement body;
	public FunctionParameter parameter;
	private MethodInstance overrides;

	public SetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			String name,
			TypeID type) {
		super(position,
				definition,
				modifiers,
				type);

		this.name = name;
		this.parameter = new FunctionParameter(type, "value");
	}

	public void setBody(Statement body) {
		this.body = body;
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

		if (getType() == BasicTypeID.UNDETERMINED) {
			setType(overrides.getHeader().getReturnType());
			parameter = new FunctionParameter(overrides.getHeader().getReturnType(), "value");
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
	public boolean isAbstract() {
		return body == null;
	}

	@Override
	public DefinitionSymbol getDefiningType() {
		return definition;
	}

	@Override
	public TypeSymbol getTargetType() {
		return target;
	}

	@Override
	public Modifiers getModifiers() {
		return getEffectiveModifiers();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<OperatorType> getOperator() {
		return Optional.empty();
	}

	@Override
	public FunctionHeader getHeader() {
		return new FunctionHeader(BasicTypeID.VOID, getType());
	}
}
