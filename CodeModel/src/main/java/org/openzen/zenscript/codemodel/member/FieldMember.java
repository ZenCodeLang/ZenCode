/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.lang.reflect.Modifier;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class FieldMember extends PropertyMember {
	public final String name;
	public Expression initializer;
	public final int autoGetterAccess;
	public final int autoSetterAccess;
	
	public final GetterMember autoGetter;
	public final SetterMember autoSetter;
	
	public FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			StoredType thisType,
			StoredType type,
			GlobalTypeRegistry registry,
			int autoGetterAccess,
			int autoSetterAccess,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, type, builtin);
		
		this.name = name;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;
		
		TypeID[] parameters = null;
		if (definition.typeParameters != null) {
			parameters = new TypeID[definition.typeParameters.length];
			for (int i = 0; i < parameters.length; i++)
				parameters[i] = registry.getGeneric(definition.typeParameters[i]);
		}
		
		if (autoGetterAccess != 0) {
			this.autoGetter = new GetterMember(position, definition, autoGetterAccess, name, type, null);
			this.autoGetter.setBody(new ReturnStatement(position, new GetFieldExpression(
					position,
					new ThisExpression(position, thisType),
					new FieldMemberRef(thisType, this, null))));
		} else {
			this.autoGetter = null;
		}
		if (autoSetterAccess != 0) {
			this.autoSetter = new SetterMember(position, definition, autoSetterAccess, name, type, null);
			this.autoSetter.setBody(new ExpressionStatement(position, new SetFieldExpression(
					position,
					new ThisExpression(position, thisType),
					new FieldMemberRef(thisType, this, null),
					new GetFunctionParameterExpression(position, this.autoSetter.parameter))));
		} else {
			this.autoSetter = null;
		}
	}
	
	private FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			StoredType type,
			int autoGetterAccess,
			int autoSetterAccess,
			GetterMember autoGetter,
			SetterMember autoSetter,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, type, builtin);
		
		this.name = name;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;
		this.autoGetter = autoGetter;
		this.autoSetter = autoSetter;
	}
	
	public boolean hasAutoGetter() {
		return autoGetterAccess != 0;
	}
	
	public boolean hasAutoSetter() {
		return autoSetterAccess != 0;
	}
	
	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addField(new FieldMemberRef(members.type, this, mapper), priority);
		
		if (autoGetter != null)
			autoGetter.registerTo(members, priority, mapper);
		if (autoSetter != null)
			autoSetter.registerTo(members, priority, mapper);
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
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
	public DefinitionMemberRef getOverrides() {
		return null;
	}
	
	@Override
	public int getEffectiveModifiers() {
		return modifiers;
	}

	@Override
	public void normalize(TypeScope scope) {
		type = type.getNormalized();
		if (initializer != null)
			initializer = initializer.normalize(scope);
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public DefinitionMemberRef ref(StoredType type, GenericMapper mapper) {
		return new FieldMemberRef(type, this, mapper);
	}
	
	@Override
	public FunctionHeader getHeader() {
		return null;
	}
}
