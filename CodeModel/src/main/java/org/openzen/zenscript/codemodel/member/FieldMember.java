package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class FieldMember extends PropertyMember implements FieldSymbol {
	public final String name;
	public final Modifiers autoGetterAccess;
	public final Modifiers autoSetterAccess;
	public final GetterMember autoGetter;
	public final SetterMember autoSetter;
	public Expression initializer;

	public FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			String name,
			TypeID thisType,
			TypeID type,
			Modifiers autoGetterAccess,
			Modifiers autoSetterAccess) {
		super(position, definition, modifiers, type);

		this.name = name;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;

		TypeID[] parameters = null;
		if (definition.typeParameters != null) {
			parameters = new TypeID[definition.typeParameters.length];
			for (int i = 0; i < parameters.length; i++)
				parameters[i] = new GenericTypeID(definition.typeParameters[i]);
		}

		boolean isStatic = modifiers.isStatic();
		if (autoGetterAccess != null) {
			Modifiers autoGetterModifiers = isStatic ? autoGetterAccess.withStatic() : autoGetterAccess;
			this.autoGetter = new GetterMember(position, definition, autoGetterModifiers, name, type);
			this.autoGetter.setBody(new ReturnStatement(position, new GetFieldExpression(
					position,
					new ThisExpression(position, thisType),
					new FieldInstance(this, type))));
		} else {
			this.autoGetter = null;
		}
		if (autoSetterAccess != null) {
			Modifiers autoSetterModifiers = isStatic ? autoSetterAccess.withStatic() : autoSetterAccess;
			this.autoSetter = new SetterMember(position, definition, autoSetterModifiers, name, type);
			this.autoSetter.setBody(new ExpressionStatement(position, new SetFieldExpression(
					position,
					new ThisExpression(position, thisType),
					new FieldInstance(this, type),
					new GetFunctionParameterExpression(position, this.autoSetter.parameter))));
		} else {
			this.autoSetter = null;
		}
	}

	public boolean hasAutoGetter() {
		return autoGetterAccess != null;
	}

	public boolean hasAutoSetter() {
		return autoSetterAccess != null;
	}

	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.field(mapper.map(this));

		if (autoGetter != null)
			autoGetter.registerTo(targetType, members, mapper);
		if (autoSetter != null)
			autoSetter.registerTo(targetType, members, mapper);
	}

	@Override
	public String describe() {
		return "field " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitField(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitField(context, this);
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		return modifiers;
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
	public TypeID getType() {
		return type;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public Optional<CompileTimeConstant> evaluate() {
		if (modifiers.isConst()) {
			return initializer.evaluate();
		} else {
			return Optional.empty();
		}
	}
}
