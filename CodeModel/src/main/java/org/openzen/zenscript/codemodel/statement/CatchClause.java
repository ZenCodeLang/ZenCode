/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.type.ITypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class CatchClause {
	public final String exceptionName;
	public final ITypeID exceptionType;
	public final Statement content;
	
	public CatchClause(String exceptionName, ITypeID exceptionType, Statement content) {
		this.exceptionName = exceptionName;
		this.exceptionType = exceptionType;
		this.content = content;
	}
}
