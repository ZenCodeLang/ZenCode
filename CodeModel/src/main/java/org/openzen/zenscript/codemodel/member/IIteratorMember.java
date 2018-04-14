/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zenscript.codemodel.iterator.ForeachIteratorVisitor;
import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IIteratorMember extends IDefinitionMember {
	public int getLoopVariableCount();
	
	public ITypeID[] getLoopVariableTypes();
	
	public <T> T acceptForIterator(ForeachIteratorVisitor<T> visitor);
}
