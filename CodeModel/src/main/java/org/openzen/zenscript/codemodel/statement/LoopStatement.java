package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.type.TypeID;
import stdlib.EqualsComparable;

public abstract class LoopStatement extends Statement implements EqualsComparable<LoopStatement> {
	public static final LoopStatement[] NONE = new LoopStatement[0];
	
	public String label;
	
	public LoopStatement(CodePosition position, String label, TypeID thrownType) {
		super(position, thrownType);
		
		this.label = label;
	}

	@Override
	public boolean equals_(LoopStatement other) {
		return this == other;
	}
}
