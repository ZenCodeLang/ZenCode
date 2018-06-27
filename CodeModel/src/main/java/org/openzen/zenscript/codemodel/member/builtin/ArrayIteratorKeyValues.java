/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.builtin;

import java.util.Map;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public class ArrayIteratorKeyValues extends Taggable implements IIteratorMember {
	private final ArrayTypeID type;
	private final ITypeID[] loopVariableTypes;
	
	public ArrayIteratorKeyValues(ArrayTypeID type) {
		this.type = type;
		
		loopVariableTypes = new ITypeID[type.dimension + 1];
		for (int i = 0; i < type.dimension; i++)
			loopVariableTypes[i] = BasicTypeID.INT;
		loopVariableTypes[type.dimension] = type.elementType;
	}
	
	@Override
	public CodePosition getPosition() {
		return CodePosition.BUILTIN;
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public int getLoopVariableCount() {
		return loopVariableTypes.length;
	}

	@Override
	public ITypeID[] getLoopVariableTypes() {
		return loopVariableTypes;
	}

	@Override
	public String describe() {
		return "iterator for array elements with index";
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addIterator(this, priority);
	}

	@Override
	public IDefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new ArrayIteratorKeyValues(type.withGenericArguments(registry, mapping));
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		throw new UnsupportedOperationException("Not a compilable member");
	}

	@Override
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor) {
		return visitor.visitArrayKeyValueIterator();
	}
}
