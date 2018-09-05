/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.context;

import java.util.Arrays;
import java.util.List;
import org.openzen.zenscript.codemodel.generic.TypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeContext {
	protected final TypeParameter[] parameters;
	
	public TypeContext(TypeParameter[] parameters) {
		this.parameters = parameters;
	}
	
	public TypeContext(TypeContext outer, TypeParameter[] inner) {
		parameters = concat(outer.parameters, inner);
	}
	
	public TypeContext(TypeContext outer, List<TypeParameter> inner) {
		this(outer, inner.toArray(new TypeParameter[inner.size()]));
	}
	
	public int getId(TypeParameter parameter) {
		for (int i = 0; i < parameters.length; i++)
			if (parameters[i] == parameter)
				return i;
		
		return -1;
	}
	
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
}
