/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValueVisitor;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSwitchKeyVisitor implements SwitchValueVisitor<Integer> {
	public static final JavaSwitchKeyVisitor INSTANCE = new JavaSwitchKeyVisitor();
	
	private JavaSwitchKeyVisitor() {}
	
	@Override
	public Integer acceptInt(IntSwitchValue value) {
		return value.value;
	}

	@Override
	public Integer acceptChar(CharSwitchValue value) {
		return (int) value.value;
	}

	@Override
	public Integer acceptString(StringSwitchValue value) {
		return value.value.hashCode();
	}

	@Override
	public Integer acceptEnumConstant(EnumConstantSwitchValue value) {
		return value.constant.ordinal;
	}

	@Override
	public Integer acceptVariantOption(VariantOptionSwitchValue value) {
		return value.option.getOrdinal();
	}
}
