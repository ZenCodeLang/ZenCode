/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.NewExpression;

/**
 *
 * @author Hoofdgebruiker
 */
public class EnumConstantMember {
	public final CodePosition position;
	public final HighLevelDefinition definition;
	public final String name;
	public final int value;
	
	public NewExpression constructor;
	
	public EnumConstantMember(CodePosition position, HighLevelDefinition definition, String name, int value) {
		this.position = position;
		this.definition = definition;
		this.name = name;
		this.value = value;
	}
}
