/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.Tag;

/**
 *
 * @author Hoofdgebruiker
 */
public class NativeTag implements Tag {
	public final String value;
	
	public NativeTag(String value) {
		this.value = value;
	}
}
