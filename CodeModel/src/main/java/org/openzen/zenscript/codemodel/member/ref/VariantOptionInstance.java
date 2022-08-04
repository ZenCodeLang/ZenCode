package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.type.TypeID;

public class VariantOptionInstance {
	public final TypeID variant;
	public final TypeID[] types;
	private final VariantDefinition.Option option;

	public VariantOptionInstance(VariantDefinition.Option option, TypeID variant, TypeID[] types) {
		this.option = option;
		this.variant = variant;
		this.types = types;
	}

	public String getName() {
		return option.name;
	}

	public TypeID getParameterType(int index) {
		return types[index];
	}

	public <T extends Tag> T getTag(Class<T> type) {
		return option.getTag(type);
	}

	public int getOrdinal() {
		return option.ordinal;
	}

	public VariantDefinition.Option getOption() {
		return option;
	}
}
