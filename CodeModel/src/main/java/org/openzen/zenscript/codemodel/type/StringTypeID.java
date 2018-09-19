/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type;

import java.util.List;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.storage.AnyStorageTag;
import org.openzen.zenscript.codemodel.type.storage.BorrowStorageTag;
import org.openzen.zenscript.codemodel.type.storage.SharedStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StaticStorageTag;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.storage.UniqueStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class StringTypeID implements ITypeID {
	public static final StringTypeID ANY = new StringTypeID(AnyStorageTag.INSTANCE);
	public static final StringTypeID STATIC = new StringTypeID(StaticStorageTag.INSTANCE);
	public static final StringTypeID UNIQUE = new StringTypeID(UniqueStorageTag.INSTANCE);
	public static final StringTypeID BORROW = new StringTypeID(BorrowStorageTag.INVOCATION);
	public static final StringTypeID SHARED = new StringTypeID(SharedStorageTag.INSTANCE);
	
	public final StorageTag storage;
	
	public StringTypeID(StorageTag storage) {
		this.storage = storage;
	}

	@Override
	public ITypeID getUnmodified() {
		return this;
	}

	@Override
	public ITypeID getNormalized() {
		return this;
	}

	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visitString(this);
	}

	@Override
	public <C, R> R accept(C context, TypeVisitorWithContext<C, R> visitor) {
		return visitor.visitString(context, this);
	}

	@Override
	public boolean hasDefaultValue() {
		return true;
	}

	@Override
	public boolean isObjectType() {
		return true;
	}

	@Override
	public ITypeID instance(GenericMapper mapper) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean hasInferenceBlockingTypeParameters(TypeParameter[] parameters) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void extractTypeParameters(List<TypeParameter> typeParameters) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
