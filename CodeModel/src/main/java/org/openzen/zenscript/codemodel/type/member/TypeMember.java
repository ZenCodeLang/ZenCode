/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeMember<T extends DefinitionMemberRef> {
	public final TypeMemberPriority priority;
	public final T member;
	
	public TypeMember(TypeMemberPriority priority, T member) {
		this.priority = priority;
		this.member = member;
	}
	
	public TypeMember<T> resolve(TypeMember<T> other) {
		if (priority == other.priority)
			if (priority == TypeMemberPriority.SPECIFIED)
				throw new CompileException(other.member.getPosition(), CompileExceptionCode.MEMBER_DUPLICATE, "Duplicate " + other.member.describe());
			else
				return this;
		
		if (priority.compareTo(other.priority) < 0) {
			return other;
		} else {
			return this;
		}
	}
}
