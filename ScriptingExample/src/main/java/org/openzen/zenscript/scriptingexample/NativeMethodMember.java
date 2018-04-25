/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.scriptingexample;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class NativeMethodMember extends MethodMember {
	public final String className;
	public final String signature;

	public NativeMethodMember(int modifiers, String name, FunctionHeader header, String className, String signature) {
		super(CodePosition.NATIVE, modifiers, name, header);

		this.className = className;
		this.signature = signature;
	}
}
