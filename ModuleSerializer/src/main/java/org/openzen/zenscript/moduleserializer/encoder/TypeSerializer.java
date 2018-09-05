/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
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
import org.openzen.zenscript.moduleserialization.TypeEncoding;
import org.openzen.zenscript.codemodel.type.TypeVisitorWithContext;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeSerializer implements TypeVisitorWithContext<TypeContext, Void> {
	private final CodeSerializationOutput output;
	
	public TypeSerializer(CodeSerializationOutput output) {
		this.output = output;
	}

	@Override
	public Void visitBasic(TypeContext context, BasicTypeID basic) {
		switch (basic) {
			case VOID: output.writeUInt(TypeEncoding.TYPE_VOID); break;
			case BOOL: output.writeUInt(TypeEncoding.TYPE_BOOL); break;
			case BYTE: output.writeUInt(TypeEncoding.TYPE_BYTE); break;
			case SBYTE: output.writeUInt(TypeEncoding.TYPE_SBYTE); break;
			case SHORT: output.writeUInt(TypeEncoding.TYPE_SHORT); break;
			case USHORT: output.writeUInt(TypeEncoding.TYPE_USHORT); break;
			case INT: output.writeUInt(TypeEncoding.TYPE_INT); break;
			case UINT: output.writeUInt(TypeEncoding.TYPE_UINT); break;
			case LONG: output.writeUInt(TypeEncoding.TYPE_LONG); break;
			case ULONG: output.writeUInt(TypeEncoding.TYPE_ULONG); break;
			case USIZE: output.writeUInt(TypeEncoding.TYPE_USIZE); break;
			case FLOAT: output.writeUInt(TypeEncoding.TYPE_FLOAT); break;
			case DOUBLE: output.writeUInt(TypeEncoding.TYPE_DOUBLE); break;
			case CHAR: output.writeUInt(TypeEncoding.TYPE_CHAR); break;
			case STRING: output.writeUInt(TypeEncoding.TYPE_STRING); break;
			case UNDETERMINED: output.writeUInt(TypeEncoding.TYPE_UNDETERMINED); break;
			default: throw new IllegalArgumentException("Unknown basic type: " + basic);
		}
		return null;
	}

	@Override
	public Void visitArray(TypeContext context, ArrayTypeID array) {
		if (array.dimension == 1) {
			output.writeUInt(TypeEncoding.TYPE_ARRAY);
		} else {
			output.writeUInt(TypeEncoding.TYPE_ARRAY_MULTIDIMENSIONAL);
			output.writeUInt(array.dimension);
		}
		array.elementType.accept(context, this);
		return null;
	}

	@Override
	public Void visitAssoc(TypeContext context, AssocTypeID assoc) {
		output.writeUInt(TypeEncoding.TYPE_ASSOC);
		assoc.keyType.accept(context, this);
		assoc.valueType.accept(context, this);
		return null;
	}

	@Override
	public Void visitGenericMap(TypeContext context, GenericMapTypeID map) {
		output.writeUInt(TypeEncoding.TYPE_GENERIC_MAP);
		output.serialize(context, map.key);
		map.value.accept(new TypeContext(context, new TypeParameter[] { map.key }), this);
		return null;
	}

	@Override
	public Void visitIterator(TypeContext context, IteratorTypeID iterator) {
		output.writeUInt(TypeEncoding.TYPE_ITERATOR);
		output.writeUInt(iterator.iteratorTypes.length);
		for (ITypeID type : iterator.iteratorTypes)
			type.accept(context, this);
		return null;
	}

	@Override
	public Void visitFunction(TypeContext context, FunctionTypeID function) {
		output.writeUInt(TypeEncoding.TYPE_FUNCTION);
		output.serialize(context, function.header);
		return null;
	}

	@Override
	public Void visitDefinition(TypeContext context, DefinitionTypeID definition) {
		output.writeUInt(TypeEncoding.TYPE_DEFINITION);
		output.write(definition.definition);
		for (ITypeID type : definition.typeParameters)
			type.accept(context, this);
		return null;
	}

	@Override
	public Void visitGeneric(TypeContext context, GenericTypeID generic) {
		output.writeUInt(TypeEncoding.TYPE_GENERIC);
		output.writeUInt(context.getId(generic.parameter));
		return null;
	}

	@Override
	public Void visitRange(TypeContext context, RangeTypeID range) {
		output.writeUInt(TypeEncoding.TYPE_RANGE);
		range.baseType.accept(context, this);
		return null;
	}

	@Override
	public Void visitModified(TypeContext context, ModifiedTypeID type) {
		if (type.isOptional()) {
			output.writeUInt(TypeEncoding.TYPE_OPTIONAL);
			type.withoutOptional().accept(context, this);
		} else if (type.isConst()) {
			output.writeUInt(TypeEncoding.TYPE_CONST);
			type.withoutConst().accept(context, this);
		} else if (type.isImmutable()) {
			output.writeUInt(TypeEncoding.TYPE_IMMUTABLE);
			type.withoutImmutable().accept(context, this);
		} else {
			throw new IllegalArgumentException("modified type is not modified");
		}
		
		return null;
	}
}
