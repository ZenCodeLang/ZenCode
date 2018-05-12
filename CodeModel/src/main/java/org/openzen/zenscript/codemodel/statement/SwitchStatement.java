/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.statement;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwitchStatement extends LoopStatement {
	public final Expression value;
	public final List<SwitchCase> cases = new ArrayList<>();
	
	public SwitchStatement(CodePosition position, String label, Expression value) {
		super(position, label);
		
		this.value = value;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitSwitch(this);
	}
}
