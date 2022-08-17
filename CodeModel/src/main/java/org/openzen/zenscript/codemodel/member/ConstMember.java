package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class ConstMember extends PropertyMember implements FieldSymbol {
	public final String name;
	public Expression value;

	public ConstMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers, String name, TypeID type) {
		super(position, definition, modifiers, type);

		this.name = name;
	}

	@Override
	public String describe() {
		return "const " + name;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.field(new FieldInstance(this, mapper.map(type)));
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
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers;
		if (definition.isInterface())
			result = result.withPublic();
		if (!result.hasAccessModifiers())
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

	/* FieldSymbol implementation */

	@Override
	public TypeSymbol getDefiningType() {
		return definition;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public Optional<CompileTimeConstant> evaluate() {
		return value.evaluate();
	}
}
