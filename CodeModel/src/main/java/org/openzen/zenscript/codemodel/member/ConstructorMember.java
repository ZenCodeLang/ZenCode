/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.expression.ConstructorSuperCallExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorMember extends FunctionalMember {
	public ConstructorMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			FunctionHeader header,
			BuiltinID builtin)
	{
		super(
				position,
				definition,
				modifiers,
				new FunctionHeader(header.typeParameters, BasicTypeID.VOID, header.thrownType, header.parameters),
				builtin);
	}
	
	public boolean isConstructorForwarded() {
		ExpressionStatement firstExpression = null;
		if (body instanceof ExpressionStatement) {
			firstExpression = (ExpressionStatement) body;
		} else if (body instanceof BlockStatement) {
			BlockStatement blockBody = (BlockStatement) body;
			if (blockBody.statements.length > 0 && blockBody.statements[0] instanceof ExpressionStatement)
				firstExpression = (ExpressionStatement) blockBody.statements[0];
		}
		
		return firstExpression != null && (
				firstExpression.expression instanceof ConstructorSuperCallExpression
				|| firstExpression.expression instanceof ConstructorThisCallExpression);
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":this" + header.getCanonical();
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CONSTRUCTOR;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		if (priority == TypeMemberPriority.SPECIFIED)
			type.addConstructor(ref(type.type, mapper), priority);
	}

	@Override
	public String describe() {
		return "constructor " + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConstructor(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitConstructor(context, this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}
	
	@Override
	public int getEffectiveModifiers() {
		int result = modifiers;
		if (definition instanceof EnumDefinition)
			result |= Modifiers.PRIVATE;
		else if (!Modifiers.hasAccess(result))
			result |= Modifiers.INTERNAL;
		
		return result;
	}
}
