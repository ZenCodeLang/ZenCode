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
import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class CustomIteratorMember extends FunctionalMember implements IIteratorMember {
	private final ITypeID[] iteratorTypes;
	public Statement body;
	public IteratorMemberRef overrides;
	
	public CustomIteratorMember(CodePosition position, HighLevelDefinition definition, int modifiers, ITypeID[] iteratorTypes, GlobalTypeRegistry registry, BuiltinID builtin) {
		super(position, definition, modifiers, createIteratorHeader(registry, iteratorTypes), builtin);
		
		this.iteratorTypes = iteratorTypes;
	}
	
	public void setContent(Statement body) {
		this.body = body;
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":iterator:" + iteratorTypes.length;
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
	public BuiltinID getBuiltin() {
		return null;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addIterator(new IteratorMemberRef(this, mapper.map(iteratorTypes)), priority);
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
	
	public void setOverrides(IteratorMemberRef overrides) {
		this.overrides = overrides;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return overrides;
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.ITERATOR;
	}
	
	private static FunctionHeader createIteratorHeader(GlobalTypeRegistry registry, ITypeID[] iteratorTypes) {
		return new FunctionHeader(registry.getIterator(iteratorTypes));
	}
}
