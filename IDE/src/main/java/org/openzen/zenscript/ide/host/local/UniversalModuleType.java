/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.host.local;

import org.openzen.zenscript.ide.host.IDEModuleType;

/**
 *
 * @author Hoofdgebruiker
 */
public class UniversalModuleType implements IDEModuleType {
	public static final UniversalModuleType INSTANCE = new UniversalModuleType();
	
	private UniversalModuleType() {}

	@Override
	public String getName() {
		return "universal";
	}
}
