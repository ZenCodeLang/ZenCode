/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeVisitorWithContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeInfo {
	private static final JavaTypeInfo PRIMITIVE = new JavaTypeInfo(true);
	private static final JavaTypeInfo OBJECT = new JavaTypeInfo(false);
	private static final JavaTypeInfoVisitor VISITOR = new JavaTypeInfoVisitor();
	
	public static JavaTypeInfo get(StoredType type) {
		return type.type.accept(type, VISITOR);
	}
	
	public static boolean isPrimitive(StoredType type) {
		return type.type.accept(type, VISITOR).primitive;
	}
	
	public final boolean primitive;
	
	private JavaTypeInfo(boolean primitive) {
		this.primitive = primitive;
	}
	
	private static class JavaTypeInfoVisitor implements TypeVisitorWithContext<StoredType, JavaTypeInfo, RuntimeException> {

		@Override
		public JavaTypeInfo visitBasic(StoredType context, BasicTypeID basic) {
			return basic == BasicTypeID.NULL ? OBJECT : PRIMITIVE;
		}
		
		@Override
		public JavaTypeInfo visitString(StoredType context, StringTypeID string) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitArray(StoredType context, ArrayTypeID array) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitAssoc(StoredType context, AssocTypeID assoc) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitGenericMap(StoredType context, GenericMapTypeID map) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitIterator(StoredType context, IteratorTypeID iterator) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitFunction(StoredType context, FunctionTypeID function) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitDefinition(StoredType context, DefinitionTypeID definition) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitGeneric(StoredType context, GenericTypeID generic) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitRange(StoredType context, RangeTypeID range) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitModified(StoredType context, ModifiedTypeID type) {
			return type.baseType.accept(null, this);
		}
	}
}
