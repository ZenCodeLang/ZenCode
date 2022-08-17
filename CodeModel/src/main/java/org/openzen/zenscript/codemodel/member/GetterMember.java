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

public class GetterMember extends PropertyMember implements MethodSymbol {
	public final String name;
	public Statement body = null;
	private MethodInstance overrides;

	public GetterMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			String name,
			TypeID type) {
		super(position, definition, modifiers, type);

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
		return body == null;
	}

	@Override
	public String describe() {
		return "getter " + name;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.getter(mapper.map(targetType, this));
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
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	public void setOverrides(MethodInstance override) {
		this.overrides = override;

		if (getType() == BasicTypeID.UNDETERMINED)
			setType(override.getHeader().getReturnType());
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
		return modifiers;
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
		return new FunctionHeader(getType());
	}
}
