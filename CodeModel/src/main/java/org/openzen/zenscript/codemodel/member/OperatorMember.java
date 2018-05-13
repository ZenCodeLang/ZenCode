/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class OperatorMember extends FunctionalMember {
	public final OperatorType operator;
	
	public OperatorMember(CodePosition position, HighLevelDefinition definition, int modifiers, OperatorType operator, FunctionHeader header) {
		super(position, definition, modifiers, operator.operator, header);
		
		this.operator = operator;
	}
	
	@Override
	public String getInformalName() {
		return operator.operator + " operator";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addOperator(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new OperatorMember(position, definition, modifiers, operator, header.instance(registry, mapping));
	}

	@Override
	public String describe() {
		return operator.operator + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitOperator(this);
	}
}
