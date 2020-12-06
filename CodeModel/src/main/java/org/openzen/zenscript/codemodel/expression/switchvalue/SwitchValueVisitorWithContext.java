package org.openzen.zenscript.codemodel.expression.switchvalue;

public interface SwitchValueVisitorWithContext<C, R> {
	R acceptInt(C context, IntSwitchValue value);

	R acceptChar(C context, CharSwitchValue value);

	R acceptString(C context, StringSwitchValue value);

	R acceptEnumConstant(C context, EnumConstantSwitchValue value);

	R acceptVariantOption(C context, VariantOptionSwitchValue value);
}
