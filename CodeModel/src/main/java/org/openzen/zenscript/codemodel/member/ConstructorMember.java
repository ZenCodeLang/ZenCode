/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.ConstructorSuperCallExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorMember extends FunctionalMember {
	public ConstructorMember(CodePosition position, HighLevelDefinition definition, int modifiers, FunctionHeader header) {
		super(position, definition, modifiers, "this", new FunctionHeader(header.typeParameters, BasicTypeID.VOID, header.parameters));
	}
	
	public boolean isConstructorForwarded() {
		ExpressionStatement firstExpression = null;
		if (body instanceof ExpressionStatement) {
			firstExpression = (ExpressionStatement) body;
		} else if (body instanceof BlockStatement) {
			BlockStatement blockBody = (BlockStatement) body;
			if (blockBody.statements.size() > 0 && blockBody.statements.get(0) instanceof ExpressionStatement)
				firstExpression = (ExpressionStatement) blockBody.statements.get(0);
		}
		
		return firstExpression != null && (
				firstExpression.expression instanceof ConstructorSuperCallExpression
				|| firstExpression.expression instanceof ConstructorThisCallExpression);
	}
	
	@Override
	public String getInformalName() {
		return "constructor";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		if (priority == TypeMemberPriority.SPECIFIED)
			type.addConstructor(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		ConstructorMember result = new ConstructorMember(position, definition, modifiers, header.instance(registry, mapping));
		if (definition.name.equals("NFAState"))
			System.out.println("X");
		return result;
	}

	@Override
	public String describe() {
		return "constructor " + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConstructor(this);
	}
}
