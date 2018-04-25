/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class NativeFieldMember extends FieldMember {
	public final String className;
	
	public NativeFieldMember(int modifiers, String name, ITypeID type, boolean isFinal, String className) {
		super(CodePosition.NATIVE, modifiers, name, type, isFinal);
		
		this.className = className;
	}
}
