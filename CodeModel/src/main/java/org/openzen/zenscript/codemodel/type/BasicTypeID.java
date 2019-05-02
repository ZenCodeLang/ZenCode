/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.ConstantBoolExpression;
import org.openzen.zenscript.codemodel.expression.ConstantByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantCharExpression;
import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.expression.ConstantIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantLongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantSByteExpression;
import org.openzen.zenscript.codemodel.expression.ConstantShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUIntExpression;
import org.openzen.zenscript.codemodel.expression.ConstantULongExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUShortExpression;
import org.openzen.zenscript.codemodel.expression.ConstantUSizeExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

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
	
	private Expression defaultValue = null;
	
	BasicTypeID(String name) {
		this.name = name;
		stored = new StoredType(this, null);
		this.defaultValue = defaultValue;
	}
	
	@Override
	public BasicTypeID getNormalized() {
		return this;
	}
	
	@Override
	public StoredType instance(GenericMapper mapper, StorageTag storage) {
		return stored(storage);
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
	public boolean isValueType() {
		return true;
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
	public Expression getDefaultValue() {
		if (defaultValue == null) // lazy init due to circular initialization in the constant expressions
			defaultValue = generateDefaultValue();
		
		return defaultValue;
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		
	}
	
	private Expression generateDefaultValue() {
		switch (this) {
			case NULL: return new NullExpression(CodePosition.UNKNOWN);
			case BOOL: return new ConstantBoolExpression(CodePosition.UNKNOWN, false);
			case BYTE: return new ConstantByteExpression(CodePosition.UNKNOWN, 0);
			case SBYTE: return new ConstantSByteExpression(CodePosition.UNKNOWN, (byte)0);
			case SHORT: return new ConstantShortExpression(CodePosition.UNKNOWN, (short)0);
			case USHORT: return new ConstantUShortExpression(CodePosition.UNKNOWN, 0);
			case INT: return new ConstantIntExpression(CodePosition.UNKNOWN, 0);
			case UINT: return new ConstantUIntExpression(CodePosition.UNKNOWN, 0);
			case LONG: return new ConstantLongExpression(CodePosition.UNKNOWN, 0);
			case ULONG: return new ConstantULongExpression(CodePosition.UNKNOWN, 0);
			case USIZE: return new ConstantUSizeExpression(CodePosition.UNKNOWN, 0);
			case FLOAT: return new ConstantFloatExpression(CodePosition.UNKNOWN, 0);
			case DOUBLE: return new ConstantDoubleExpression(CodePosition.UNKNOWN, 0);
			default: return null;
		}
	}
}
