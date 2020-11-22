package org.openzen.zenscript.codemodel.generic;

public interface GenericParameterBoundVisitorWithContext<C, R> {
	R visitSuper(C context, ParameterSuperBound bound);
	
	R visitType(C context, ParameterTypeBound bound);
}
