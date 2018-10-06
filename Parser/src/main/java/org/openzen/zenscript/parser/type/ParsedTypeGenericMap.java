/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.parser.definitions.ParsedTypeParameter;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedTypeGenericMap implements IParsedType {
	private final CodePosition position;
	private final ParsedTypeParameter key;
	private final IParsedType value;
	private final ParsedStorageTag storage;
	
	public ParsedTypeGenericMap(CodePosition position, ParsedTypeParameter key, IParsedType value, ParsedStorageTag storage) {
		this.position = position;
		this.key = key;
		this.value = value;
		this.storage = storage;
	}

	@Override
	public StoredType compile(TypeResolutionContext context) {
		TypeParameter cKey = key.compiled;
		StoredType valueType = this.value.compile(new GenericMapScope(context, cKey));
		
		GlobalTypeRegistry registry = context.getTypeRegistry();
		StorageTag storage = this.storage.resolve(position, context);
		return registry.getGenericMap(valueType, cKey).stored(storage);
	}

	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage tag not supported here");
		
		TypeParameter cKey = key.compiled;
		StoredType valueType = this.value.compile(new GenericMapScope(context, cKey));
		
		GlobalTypeRegistry registry = context.getTypeRegistry();
		StorageTag storage = this.storage.resolve(position, context);
		return registry.getGenericMap(valueType, cKey);
	}
	
	private class GenericMapScope implements TypeResolutionContext {
		private final TypeResolutionContext outer;
		private final TypeParameter parameter;
		
		public GenericMapScope(TypeResolutionContext outer, TypeParameter parameter) {
			this.outer = outer;
			this.parameter = parameter;
		}
		
		@Override
		public GlobalTypeRegistry getTypeRegistry() {
			return outer.getTypeRegistry();
		}

		@Override
		public TypeID getType(CodePosition position, List<GenericName> name) {
			if (name.get(0).name.equals(parameter.name) && name.size() == 1 && name.get(0).hasNoArguments())
				return getTypeRegistry().getGeneric(parameter);
			
			return outer.getType(position, name);
		}
		
		@Override
		public StorageTag getStorageTag(CodePosition position, String name, String[] arguments) {
			return outer.getStorageTag(position, name, arguments);
		}
		
		@Override
		public StoredType getThisType() {
			return outer.getThisType();
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}
	}
}
