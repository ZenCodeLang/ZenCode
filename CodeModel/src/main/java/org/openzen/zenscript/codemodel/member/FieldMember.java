package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
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
	private final TypeID thisType;

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
		this.thisType = thisType;

		// ToDo: This is never used?
		TypeID[] parameters = null;
		if (definition.typeParameters != null) {
			parameters = new TypeID[definition.typeParameters.length];
			for (int i = 0; i < parameters.length; i++)
				parameters[i] = new GenericTypeID(definition.typeParameters[i]);
		}

		final FieldInstance fieldInstance = new FieldInstance(this, type);
		autoGetter = autoGetterAccess == null ? null : constructAutoGetter(thisType, fieldInstance);
		autoSetter = autoSetterAccess == null ? null : constructAutoSetter(thisType, fieldInstance);
	}

	private SetterMember constructAutoSetter(TypeID thisType, FieldInstance fieldInstance) {
		Modifiers autoSetterModifiers = isStatic() ? autoSetterAccess.withStatic() : autoSetterAccess;
		final SetterMember autoSetter = new SetterMember(position, definition, autoSetterModifiers, name, type);
		autoSetter.setBody(constructAutoSetterBody(thisType, fieldInstance, autoSetter.parameter));
		return autoSetter;
	}

	private Statement constructAutoSetterBody(TypeID thisType, FieldInstance fieldInstance, FunctionParameter parameter) {
		final GetFunctionParameterExpression setValue = new GetFunctionParameterExpression(position, parameter);
		final Expression setFieldExpression;
		if (isStatic()) {
			setFieldExpression = new SetStaticFieldExpression(position, fieldInstance, setValue);
		} else {
			setFieldExpression = new SetFieldExpression(position, new ThisExpression(position, thisType), fieldInstance, setValue);
		}
		return new ExpressionStatement(position, setFieldExpression);
	}

	private GetterMember constructAutoGetter(TypeID thisType, FieldInstance fieldInstance) {
		Modifiers autoGetterModifiers = isStatic() ? autoGetterAccess.withStatic() : autoGetterAccess;
		final GetterMember autoGetter = new GetterMember(position, definition, autoGetterModifiers, name, type);
		autoGetter.setBody(constructAutoGetterBody(thisType, fieldInstance));
		return autoGetter;
	}

	private Statement constructAutoGetterBody(TypeID thisType, FieldInstance fieldInstance) {
		final Expression getFieldExpression;
		if(isStatic()) {
			getFieldExpression = new GetStaticFieldExpression(position, fieldInstance);
		} else {
			getFieldExpression = new GetFieldExpression(position, new ThisExpression(position, thisType), fieldInstance);
		}

		return new ReturnStatement(position, getFieldExpression);
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
	public void setType(TypeID type) {
		super.setType(type);

		final FieldInstance fieldInstance = new FieldInstance(this, type);
		if (autoGetter != null) {
			this.autoGetter.setType(type);
			this.autoGetter.setBody(constructAutoGetterBody(thisType, fieldInstance));
		}
		if (autoSetter != null) {
			this.autoSetter.setType(type);
			this.autoSetter.setBody(constructAutoSetterBody(thisType, fieldInstance, autoSetter.parameter));
		}
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
