/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValueVisitor;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSwitchValueCompiler implements SwitchValueVisitor<String> {
	private final JavaSourceStatementScope scope;
	
	public JavaSwitchValueCompiler(JavaSourceStatementScope scope) {
		this.scope = scope;
	}

	@Override
	public String acceptInt(IntSwitchValue value) {
		return Integer.toString(value.value);
	}

	@Override
	public String acceptChar(CharSwitchValue value) {
		return StringUtils.escape(Character.toString(value.value), '\'', true);
	}

	@Override
	public String acceptString(StringSwitchValue value) {
		return StringUtils.escape(value.value, '"', true);
	}

	@Override
	public String acceptEnumConstant(EnumConstantSwitchValue value) {
		return value.constant.name;
	}

	@Override
	public String acceptVariantOption(VariantOptionSwitchValue value) {
		return value.option.name;
	}
}
