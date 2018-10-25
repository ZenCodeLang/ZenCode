/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantOptionRef {
	private final VariantDefinition.Option option;
	public final StoredType variant;
	public final StoredType[] types;
	
	public VariantOptionRef(VariantDefinition.Option option, StoredType variant, StoredType[] types) {
		this.option = option;
		this.variant = variant;
		this.types = types;
	}
	
	public String getName() {
		return option.name;
	}
	
	public StoredType getParameterType(int index) {
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
