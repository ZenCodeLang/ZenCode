/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValueVisitor;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwitchValueFormatter implements SwitchValueVisitor<String> {
	private final ScriptFormattingSettings settings;
	
	public SwitchValueFormatter(ScriptFormattingSettings settings) {
		this.settings = settings;
	}

	@Override
	public String acceptInt(IntSwitchValue value) {
		return Integer.toString(value.value);
	}

	@Override
	public String acceptChar(CharSwitchValue value) {
		return StringUtils.escape(new String(new char[] { value.value }), '\'', true);
	}

	@Override
	public String acceptString(StringSwitchValue value) {
		return StringUtils.escape(value.value, settings.useSingleQuotesForStrings ? '\'' : '"', true);
	}

	@Override
	public String acceptEnumConstant(EnumConstantSwitchValue value) {
		return value.constant.name;
	}

	@Override
	public String acceptVariantOption(VariantOptionSwitchValue value) {
		StringBuilder result = new StringBuilder();
		result.append(value.option.name);
		result.append("(");
		for (int i = 0; i < value.parameters.length; i++) {
			if (i > 0)
				result.append(", ");
			result.append(value.parameters[i]);
		}
		result.append(")");
		return result.toString();
	}
}
