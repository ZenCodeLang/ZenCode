/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.iterator;

/**
 *
 * @author Hoofdgebruiker
 */
public interface ForeachIteratorVisitor<T> {
	T visitIntRange();
	
	T visitArrayValueIterator();
	
	T visitArrayKeyValueIterator();
	
	T visitCustomIterator();
}
