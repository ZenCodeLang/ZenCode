package org.openzen.zenscript.scriptingexample;

import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Expansion(value = "EntityType", genericParameters = {@ZenCodeType.GenericParameter(type = "Entity", bound = ZenCodeType.TypeParameterBoundType.SUPER, name = "T")})
public class ExpandEntityType {

//	@ZenCodeType.Method
//	public static <T extends Entity> void doThing(EntityType<T> internal, Class<T> clazz) {
//		System.out.println("did thing for " + clazz);
//	}

	@ZenCodeType.Method
	public static <T extends Entity> void doThing(EntityType<T> internal) {
		System.out.println("did thing for " + "clazz");
	}

}
