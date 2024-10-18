package org.openzen.zencode.java.impl.conversion;

import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;

import java.util.*;

public class JdkJavaRuntimeConverter {

	private final ZSPackage rootPackage;
	private final Map<Class<?>, String> javaClassToZenCodeClassName = new HashMap<>();

	public JdkJavaRuntimeConverter(ZSPackage rootPackage) {
		this.rootPackage = rootPackage;
		fillJavaClassToZenCodeClassName();
	}

	public Optional<TypeSymbol> resolveJavaType(Class<?> javaType) {
		if (!javaClassToZenCodeClassName.containsKey(javaType)) {
			return Optional.empty();
		}

		String zenCodeName = javaClassToZenCodeClassName.get(javaType);
		TypeSymbol result = rootPackage.getImport(zenCodeName.split("\\."));
		if(result == null) {
			throw new IllegalStateException("Could not resolve zen code type " + zenCodeName + " for java type " + javaType.getName() + ", make sure the required stdlib modules are registered as dependencies");
		}

		return Optional.of(result);

	}

	private void fillJavaClassToZenCodeClassName() {
		fillMapping(List.class, "stdlib.List");
		fillMapping(Collection.class, "stdlib.List");
		fillMapping(Comparable.class, "stdlib.Comparable");
		fillMapping(Exception.class, "stdlib.Exception");
		fillMapping(IllegalArgumentException.class, "stdlib.IllegalArgumentException");
		fillMapping(Iterable.class, "stdlib.Iterable");
		fillMapping(Iterator.class, "stdlib.Iterator");
		fillMapping(StringBuilder.class, "stdlib.StringBuilder");

		fillMapping(HashSet.class, "collections.HashSet");
		fillMapping(LinkedList.class, "collections.LinkedList");
		fillMapping(ArrayList.class, "collections.ArrayList");
		fillMapping(Queue.class, "collections.Queue");
		fillMapping(Set.class, "collections.Set");
		fillMapping(Stack.class, "collections.Stack");

		fillMapping(UUID.class, "uuid.UUID");
	}

	private void fillMapping(Class<?> javaType, String zenCodeClassName) {
		javaClassToZenCodeClassName.put(javaType, zenCodeClassName);
	}


}
