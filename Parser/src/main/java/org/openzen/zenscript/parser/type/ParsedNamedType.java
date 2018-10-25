/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.type;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedNamedType implements IParsedType {
	private final CodePosition position;
	public final List<ParsedNamePart> name;
	private final ParsedStorageTag storage;
	
	public ParsedNamedType(CodePosition position, List<ParsedNamePart> name, ParsedStorageTag storage) {
		this.position = position;
		this.name = name;
		this.storage = storage;
	}
	
	@Override
	public StoredType compile(TypeResolutionContext context) {
		if (name.size() == 1 && name.get(0).name.equals("Iterator"))
			return toIterator(context);
		
		List<GenericName> genericNames = new ArrayList<>();
		for (ParsedNamePart namePart : name)
			genericNames.add(namePart.compile(context));
		
		TypeID result = context.getType(position, genericNames);
		if (result == null)
			return new InvalidTypeID(position, CompileExceptionCode.NO_SUCH_TYPE, "Type not found: " + toString()).stored();
		
		return result.stored(storage.resolve(position, context));
	}
	
	@Override
	public TypeID compileUnstored(TypeResolutionContext context) {
		if (storage != ParsedStorageTag.NULL)
			return new InvalidTypeID(position, CompileExceptionCode.STORAGE_NOT_SUPPORTED, "Storage not supported here");
		
		return compile(context).type;
	}
	
	@Override
	public String toString() {
		StringBuilder nameTotal = new StringBuilder();
		for (int i = 0; i < name.size(); i++) {
			if (i > 0)
				nameTotal.append(".");
			nameTotal.append(name.get(i).toString());
		}
		return nameTotal.toString();
	}
	
	@Override
	public AnnotationDefinition compileAnnotation(BaseScope scope) {
		if (name.size() != 1)
			return null;
		
		return scope.getAnnotation(name.get(0).name);
	}
	
	@Override
	public StoredType[] compileTypeArguments(BaseScope scope) {
		ParsedNamePart last = name.get(name.size() - 1);
		return IParsedType.compileTypes(last.typeArguments, scope);
	}
	
	private StoredType toIterator(TypeResolutionContext context) {
		List<IParsedType> genericTypes = name.get(0).typeArguments;
		StoredType[] iteratorTypes = new StoredType[genericTypes.size()];
		for (int i = 0; i < genericTypes.size(); i++)
			iteratorTypes[i] = genericTypes.get(i).compile(context);

		TypeID type = context.getTypeRegistry().getIterator(iteratorTypes);
		return type.stored(storage.resolve(position, context));
	}
	
	public static class ParsedNamePart {
		public final String name;
		public final List<IParsedType> typeArguments;
		
		public ParsedNamePart(String name, List<IParsedType> genericArguments) {
			this.name = name;
			this.typeArguments = genericArguments;
		}
		
		private GenericName compile(TypeResolutionContext context) {
			return new GenericName(name, IParsedType.compileTypes(typeArguments, context));
		}
		
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder(name);
			if (typeArguments != null) {
				result.append("<");
				for (int i = 0; i < typeArguments.size(); i++) {
					if (i > 0)
						result.append(", ");
					result.append(typeArguments.get(i).toString());
				}
				result.append(">");
			}
			return result.toString();
		}
	}
}
