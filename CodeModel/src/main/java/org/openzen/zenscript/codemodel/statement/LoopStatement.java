/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.ITypeID;
import stdlib.EqualsComparable;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class LoopStatement extends Statement implements EqualsComparable<LoopStatement> {
	public static final LoopStatement[] NONE = new LoopStatement[0];
	
	public String label;
	
	public LoopStatement(CodePosition position, String label, ITypeID thrownType) {
		super(position, thrownType);
		
		this.label = label;
	}

	@Override
	public boolean equals_(LoopStatement other) {
		return this == other;
	}
}
