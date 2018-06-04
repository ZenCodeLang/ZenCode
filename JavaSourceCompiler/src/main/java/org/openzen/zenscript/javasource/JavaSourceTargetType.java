/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.io.File;
import org.json.JSONObject;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.TargetType;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceTargetType implements TargetType {
	public static final JavaSourceTargetType INSTANCE = new JavaSourceTargetType();
	
	private JavaSourceTargetType() {}

	@Override
	public Target create(File projectDir, JSONObject definition) {
		return new JavaSourceTarget(projectDir, definition);
	}
}
