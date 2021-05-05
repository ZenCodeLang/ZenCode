package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.ClassWriter;

import java.util.HashMap;
import java.util.Map;

public class JavaClassWriter extends ClassWriter {

	private static final Map<String, String> super_classes = new HashMap<>();

	public JavaClassWriter(int flags) {
		super(flags);
	}

	public static void registerSuperClass(String child, String superClass) {
		super_classes.put(child, superClass);
	}

	public static Map<String, String> getSuper_classes() {
		return super_classes;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {

		//FIXME big TODO, make this more efficient!!!
		try {
			return super.getCommonSuperClass(type1, type2);
		} catch (Exception ignored) {
		}


		if (type1.equals(type2))
			return type1;

		String newType2 = type2;
		while (super_classes.containsKey(newType2)) {
			newType2 = super_classes.get(newType2);
			if (type1.equals(newType2))
				return type1;
		}

		String newType1 = type1;
		while (super_classes.containsKey(newType1)) {
			newType1 = super_classes.get(newType1);
			if (type2.equals(newType1))
				return type2;
		}

		return super_classes.containsKey(type1) ? getCommonSuperClass(super_classes.get(type1), type2) : super_classes.containsKey(type2) ? getCommonSuperClass(type1, super_classes.get(type2)) : "java/lang/Object";

	}


}
