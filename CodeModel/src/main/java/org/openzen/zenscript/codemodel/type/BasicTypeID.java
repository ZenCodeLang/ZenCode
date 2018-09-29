/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.ValueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public enum BasicTypeID implements TypeID {
	VOID("void"),
	NULL("null"),
	BOOL("bool"),
	BYTE("byte"),
	SBYTE("sbyte"),
	SHORT("short"),
	USHORT("ushort"),
	INT("int"),
	UINT("uint"),
	LONG("long"),
	ULONG("ulong"),
	USIZE("usize"),
	FLOAT("float"),
	DOUBLE("double"),
	CHAR("char"),
	
	UNDETERMINED("undetermined");
	
	public static final List<StoredType> HINT_BOOL = Collections.singletonList(BOOL.stored);
	
	public final String name;
	public final StoredType stored;
	
	BasicTypeID(String name) {
		this.name = name;
		stored = new StoredType(this, ValueStorageTag.INSTANCE);
	}
	
	@Override
	public BasicTypeID getNormalizedUnstored() {
		return this;
	}
	
	@Override
	public BasicTypeID instanceUnstored(GenericMapper mapper) {
		return this;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public <R> R accept(TypeVisitor<R> visitor) {
		return visitor.visitBasic(this);
	}
	
	@Override
	public <C, R, E extends Exception> R accept(C context, TypeVisitorWithContext<C, R, E> visitor) throws E {
		return visitor.visitBasic(context, this);
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}
	
	@Override
	public boolean isDestructible() {
		return false;
	}
	
	@Override
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		return false;
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		return false;
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		
	}
}
