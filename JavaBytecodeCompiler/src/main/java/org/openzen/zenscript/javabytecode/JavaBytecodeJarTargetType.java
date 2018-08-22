/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode;

import org.json.JSONObject;
import org.openzen.zenscript.compiler.Target;
import org.openzen.zenscript.compiler.TargetType;

import java.io.File;

/**
 * @author Hoofdgebruiker
 */
public class JavaBytecodeJarTargetType implements TargetType {
	public static final JavaBytecodeJarTargetType INSTANCE = new JavaBytecodeJarTargetType();

	private JavaBytecodeJarTargetType() {
	}

	@Override
	public Target create(File projectDir, JSONObject definition) {
		return new JavaBytecodeJarTarget(definition);
	}
}
