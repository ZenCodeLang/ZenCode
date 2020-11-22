package org.openzen.zenscript.codemodel.generic;

public interface GenericParameterBoundVisitor<T> {
	T visitSuper(ParameterSuperBound bound);
	
	T visitType(ParameterTypeBound bound);
}
