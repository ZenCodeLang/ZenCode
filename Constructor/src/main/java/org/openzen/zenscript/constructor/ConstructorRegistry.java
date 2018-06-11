/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.constructor;

import java.util.HashMap;
import java.util.Map;
import org.openzen.zenscript.compiler.TargetType;
import org.openzen.zenscript.javabytecode.JavaBytecodeJarTargetType;
import org.openzen.zenscript.javabytecode.JavaBytecodeRunTargetType;
import org.openzen.zenscript.javasource.JavaSourceTargetType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ConstructorRegistry {
	private static final Map<String, TargetType> targetTypes = new HashMap<>();
	
	static {
		registerTargetType("javaSource", JavaSourceTargetType.INSTANCE);
		registerTargetType("javaBytecodeJar", JavaBytecodeJarTargetType.INSTANCE);
		registerTargetType("javaBytecodeRun", JavaBytecodeRunTargetType.INSTANCE);
	}
	
	public static void registerTargetType(String name, TargetType type) {
		targetTypes.put(name, type);
	}
	
	public static TargetType getTargetType(String name) {
		return targetTypes.get(name);
	}

	private ConstructorRegistry() {}
}
