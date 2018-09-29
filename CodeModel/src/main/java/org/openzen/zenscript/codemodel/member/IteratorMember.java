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
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class IteratorMember extends FunctionalMember {
	private final StoredType[] iteratorTypes;
	public Statement body;
	public IteratorMemberRef overrides;
	
	public IteratorMember(CodePosition position, HighLevelDefinition definition, int modifiers, StoredType[] iteratorTypes, GlobalTypeRegistry registry, BuiltinID builtin) {
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
	
	public int getLoopVariableCount() {
		return iteratorTypes.length;
	}
	
	public StoredType[] getLoopVariableTypes() {
		return iteratorTypes;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addIterator(new IteratorMemberRef(this, type.type, mapper == null ? iteratorTypes : mapper.map(iteratorTypes)), priority);
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
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitIterator(context, this);
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
	
	private static FunctionHeader createIteratorHeader(GlobalTypeRegistry registry, StoredType[] iteratorTypes) {
		return new FunctionHeader(registry.getIterator(iteratorTypes).stored(UniqueStorageTag.INSTANCE));
	}
}
