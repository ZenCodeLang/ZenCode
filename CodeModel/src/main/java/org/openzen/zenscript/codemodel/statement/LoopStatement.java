/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class LoopStatement extends Statement {
	public String label;
	
	public LoopStatement(CodePosition position, String label, ITypeID thrownType) {
		super(position, thrownType);
		
		this.label = label;
	}
}
