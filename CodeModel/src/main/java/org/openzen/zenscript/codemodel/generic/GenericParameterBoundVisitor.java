/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.generic;

/**
 *
 * @author Hoofdgebruiker
 */
public interface GenericParameterBoundVisitor<T> {
	T visitSuper(ParameterSuperBound bound);
	
	T visitType(ParameterTypeBound bound);
}
