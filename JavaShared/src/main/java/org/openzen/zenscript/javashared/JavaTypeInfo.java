/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.*;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaTypeInfo {
	private static final JavaTypeInfo PRIMITIVE = new JavaTypeInfo(true);
	private static final JavaTypeInfo OBJECT = new JavaTypeInfo(false);
	private static final JavaTypeInfoVisitor VISITOR = new JavaTypeInfoVisitor();
	
	public static JavaTypeInfo get(TypeID type) {
		return type.accept(type, VISITOR);
	}
	
	public static boolean isPrimitive(TypeID type) {
		return type.accept(type, VISITOR).primitive;
	}
	
	public final boolean primitive;
	
	private JavaTypeInfo(boolean primitive) {
		this.primitive = primitive;
	}
	
	private static class JavaTypeInfoVisitor implements TypeVisitorWithContext<TypeID, JavaTypeInfo, RuntimeException> {

		@Override
		public JavaTypeInfo visitBasic(TypeID context, BasicTypeID basic) {
			return (basic == BasicTypeID.NULL || basic == BasicTypeID.STRING) ? OBJECT : PRIMITIVE;
		}

		@Override
		public JavaTypeInfo visitArray(TypeID context, ArrayTypeID array) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitAssoc(TypeID context, AssocTypeID assoc) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitGenericMap(TypeID context, GenericMapTypeID map) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitIterator(TypeID context, IteratorTypeID iterator) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitFunction(TypeID context, FunctionTypeID function) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitDefinition(TypeID context, DefinitionTypeID definition) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitGeneric(TypeID context, GenericTypeID generic) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitRange(TypeID context, RangeTypeID range) {
			return OBJECT;
		}

		@Override
		public JavaTypeInfo visitOptional(TypeID context, OptionalTypeID type) {
			return type.baseType.accept(null, this);
		}
	}
}
