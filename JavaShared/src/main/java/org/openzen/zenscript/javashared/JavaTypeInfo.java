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
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.ModifiedTypeID;
import org.openzen.zenscript.codemodel.type.RangeTypeID;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.TypeVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeInfo {
	private static final JavaTypeInfo PRIMITIVE = new JavaTypeInfo(true);
	private static final JavaTypeInfo OBJECT = new JavaTypeInfo(false);
	private static final JavaTypeInfoVisitor VISITOR = new JavaTypeInfoVisitor();
	
	public static JavaTypeInfo get(ITypeID type) {
		return type.accept(VISITOR);
	}
	
	public static boolean isPrimitive(ITypeID type) {
		return type.accept(VISITOR).primitive;
	}
	
	public final boolean primitive;
	
	private JavaTypeInfo(boolean primitive) {
		this.primitive = primitive;
	}
	
	private static class JavaTypeInfoVisitor implements TypeVisitor<JavaTypeInfo> {

		@Override
		public JavaTypeInfo visitBasic(BasicTypeID basic) {
			return basic == BasicTypeID.NULL ? OBJECT : PRIMITIVE;
		}
		
		@Override
		public JavaTypeInfo visitString(StringTypeID string) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitArray(ArrayTypeID array) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitAssoc(AssocTypeID assoc) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitGenericMap(GenericMapTypeID map) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitIterator(IteratorTypeID iterator) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitFunction(FunctionTypeID function) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitDefinition(DefinitionTypeID definition) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitGeneric(GenericTypeID generic) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitRange(RangeTypeID range) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitModified(ModifiedTypeID type) {
			return type.baseType.accept(this);
		}
	}
}
