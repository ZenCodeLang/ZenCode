/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IPropertyMember extends IDefinitionMember {
	ITypeID getType();
	
	boolean isStatic();
	
	boolean isFinal();
	
	MemberAnnotation[] getAnnotations();
}
