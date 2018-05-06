/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CustomIteratorMember extends DefinitionMember implements IIteratorMember {
	private final ITypeID[] iteratorTypes;
	private List<Statement> content;
	
	public CustomIteratorMember(CodePosition position, HighLevelDefinition definition, int modifiers, ITypeID[] iteratorTypes) {
		super(position, definition, modifiers);
		
		this.iteratorTypes = iteratorTypes;
	}
	
	public void setContent(List<Statement> content) {
		this.content = content;
	}

	@Override
	public int getLoopVariableCount() {
		return iteratorTypes.length;
	}

	@Override
	public ITypeID[] getLoopVariableTypes() {
		return iteratorTypes;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addIterator(this, priority);
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		ITypeID[] newIteratorTypes = new ITypeID[iteratorTypes.length];
		for (int i = 0; i < newIteratorTypes.length; i++)
			newIteratorTypes[i] = iteratorTypes[i].withGenericArguments(registry, mapping);
		return new CustomIteratorMember(position, definition, modifiers, newIteratorTypes);
	}

	@Override
	public String describe() {
		return "iterator with " + iteratorTypes.length + " variables";
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCustomIterator(this);
	}

	@Override
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor) {
		return visitor.visitCustomIterator();
	}
}
