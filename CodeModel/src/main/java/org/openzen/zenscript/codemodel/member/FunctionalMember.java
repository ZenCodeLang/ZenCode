package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public abstract class FunctionalMember extends DefinitionMember implements MethodSymbol {
	public FunctionHeader header;
	public Statement body = null;
	private final MethodID id;
	protected MethodInstance overrides;
	private boolean expectsOverride = false;

	public FunctionalMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			MethodID id,
			FunctionHeader header) {
		super(position, definition, modifiers);
		this.id = id;
		this.header = header;
	}

	public void setBody(Statement body) {
		this.body = body;
	}

	public abstract String getCanonicalName();

	public abstract FunctionalKind getKind();

	public boolean doesExpectOverride() {
		return expectsOverride;
	}

	@Override
	public Modifiers getModifiers() {
		return getEffectiveModifiers();
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface())
			result = result.withPublic();
		if (!result.hasAccessModifiers())
			result = result.withInternal();
		if (isImplicitlyAbstract())
			result = result.withAbstract();

		return result;
	}

	@Override
	public boolean isAbstract() {
		return body == null;
	}

	/* MethodSymbol implementation */

	@Override
	public DefinitionSymbol getDefiningType() {
		return definition;
	}

	@Override
	public TypeID getTargetType() {
		return targetType;
	}

	@Override
	public MethodID getID() {
		return id;
	}

	@Override
	public FunctionHeader getHeader() {
		return header;
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.ofNullable(overrides);
	}

	public void setOverrides(MethodInstance overrides) {
		this.overrides = overrides;
		this.expectsOverride = true;
		if (overrides != null) {
			inferFromOverride(overrides);
		}
	}

	protected abstract void inferFromOverride(MethodInstance overrides);

	private boolean isImplicitlyAbstract() {
		return definition.isInterface()
				&& !getID().getOperator().filter(op -> op == OperatorType.CONSTRUCTOR).isPresent();
	}
}
