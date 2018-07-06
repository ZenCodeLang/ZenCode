/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class FieldMember extends DefinitionMember {
	public final String name;
	public final ITypeID type;
	public Expression initializer;
	public final int autoGetterAccess;
	public final int autoSetterAccess;
	public final BuiltinID builtin;
	
	public final GetterMember autoGetter;
	public final SetterMember autoSetter;
	
	public FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			GlobalTypeRegistry registry,
			int autoGetterAccess,
			int autoSetterAccess,
			BuiltinID builtin)
	{
		super(position, definition, modifiers);
		
		this.name = name;
		this.type = type;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;
		this.builtin = builtin;
		
		ITypeID[] parameters = null;
		if (definition.genericParameters != null) {
			parameters = new ITypeID[definition.genericParameters.length];
			for (int i = 0; i < parameters.length; i++)
				parameters[i] = new GenericTypeID(definition.genericParameters[i]);
		}
		
		if (autoGetterAccess != 0) {
			ITypeID myType = registry.getForDefinition(definition, parameters);
			this.autoGetter = new GetterMember(position, definition, autoGetterAccess, name, type, null);
			this.autoGetter.setBody(new ReturnStatement(position, new GetFieldExpression(position, new ThisExpression(position, myType), new FieldMemberRef(this, myType))));
		} else {
			this.autoGetter = null;
		}
		if (autoSetterAccess != 0) {
			ITypeID myType = registry.getForDefinition(definition, parameters);
			this.autoSetter = new SetterMember(position, definition, autoSetterAccess, name, type, null);
			this.autoSetter.setBody(new ExpressionStatement(position, new SetFieldExpression(
					position,
					new ThisExpression(position, myType),
					new FieldMemberRef(this, myType),
					new GetFunctionParameterExpression(position, this.autoSetter.header.parameters[0]))));
		} else {
			this.autoSetter = null;
		}
	}
	
	private FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			int autoGetterAccess,
			int autoSetterAccess,
			GetterMember autoGetter,
			SetterMember autoSetter,
			BuiltinID builtin)
	{
		super(position, definition, modifiers);
		
		this.name = name;
		this.type = type;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;
		this.autoGetter = autoGetter;
		this.autoSetter = autoSetter;
		this.builtin = builtin;
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
		members.addField(new FieldMemberRef(this, mapper.map(type)), priority);
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
}
