package org.openzen.zenscript.codemodel.expression.switchvalue;

public interface SwitchValueVisitor <T> {
	T acceptInt(IntSwitchValue value);
	
	T acceptChar(CharSwitchValue value);
	
	T acceptString(StringSwitchValue value);
	
	T acceptEnumConstant(EnumConstantSwitchValue value);
	
	T acceptVariantOption(VariantOptionSwitchValue value);
}
