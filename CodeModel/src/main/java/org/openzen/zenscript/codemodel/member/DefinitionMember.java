/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class DefinitionMember extends Taggable implements IDefinitionMember {
	public final CodePosition position;
	public final HighLevelDefinition definition;
	public int modifiers;
	public MemberAnnotation[] annotations = MemberAnnotation.NONE;
	
	public DefinitionMember(CodePosition position, HighLevelDefinition definition, int modifiers) {
		this.position = position;
		this.definition = definition;
		this.modifiers = modifiers;
	}
	
	@Override
	public final CodePosition getPosition() {
		return position;
	}
	
	public MemberAnnotation[] getAnnotations() {
		return annotations;
	}
	
	public boolean isStatic() {
		return Modifiers.isStatic(modifiers);
	}
	
	public boolean isFinal() {
		return Modifiers.isFinal(modifiers);
	}
	
	public boolean isExtern() {
		return Modifiers.isExtern(modifiers);
	}
	
	public boolean isPrivate() {
		return Modifiers.isPrivate(modifiers);
	}
	
	public boolean isPublic() {
		return Modifiers.isPublic(modifiers);
	}
	
	public boolean isProtected() {
		return Modifiers.isProtected(modifiers);
	}
}
