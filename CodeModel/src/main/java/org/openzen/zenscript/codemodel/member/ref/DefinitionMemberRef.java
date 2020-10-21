/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public interface DefinitionMemberRef {
	CodePosition getPosition();
	
	String describe();
	
	default <T extends Tag> T getTag(Class<T> type) {
		return getTarget().getTag(type);
	}
	
	default <T extends Tag> boolean hasTag(Class<T> type) {
		return getTarget().hasTag(type);
	}
	
	TypeID getOwnerType();
	
	DefinitionMemberRef getOverrides();
	
	FunctionHeader getHeader();
	
	MemberAnnotation[] getAnnotations();
	
	IDefinitionMember getTarget();
}
