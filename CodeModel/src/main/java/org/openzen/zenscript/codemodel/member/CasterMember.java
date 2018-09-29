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
import org.openzen.zenscript.codemodel.member.ref.CasterMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class CasterMember extends FunctionalMember {
	public StoredType toType;
	public CasterMemberRef overrides;
	
	public CasterMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			StoredType toType,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, new FunctionHeader(toType), builtin);
		
		this.toType = toType;
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":caster:" + toType.toString();
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CASTER;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addCaster(new CasterMemberRef(this, type.type, mapper == null ? toType : toType.instance(mapper)), priority);
	}

	@Override
	public String describe() {
		return "caster to " + toType.toString();
	}
	
	public StoredType getTargetType() {
		return toType;
	}
	
	public boolean isImplicit() {
		return Modifiers.isImplicit(modifiers);
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCaster(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitCaster(context, this);
	}
	
	public void setOverrides(GlobalTypeRegistry registry, CasterMemberRef overrides) {
		this.overrides = overrides;
	}

	@Override
	public CasterMemberRef getOverrides() {
		return overrides;
	}
	
	@Override
	public void normalize(TypeScope scope) {
		super.normalize(scope);
		toType = toType.getNormalized();
	}
}
