package org.openzen.zenscript.rustsource.types;

import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.rustsource.compiler.ImportSet;
import org.openzen.zenscript.rustsource.definitions.RustModule;

public class RustTypeCompiler implements TypeVisitor<String> {
	private final ImportSet imports;
	private final boolean multithreaded;

	public RustTypeCompiler(ImportSet imports, boolean multithreaded) {
		this.imports = imports;
		this.multithreaded = multithreaded;
	}

	public String compile(TypeID type) {
		return type.accept(this);
	}

	@Override
	public String visitBasic(BasicTypeID basic) {
		switch (basic) {
			case VOID: throw new UnsupportedOperationException("void is not a valid type");
			case NULL: throw new UnsupportedOperationException("null is not a valid type");
			case BOOL: return "bool";
			case BYTE: return "u8";
			case SBYTE: return "i8";
			case SHORT: return "i16";
			case USHORT: return "u16";
			case INT: return "i32";
			case UINT: return "u32";
			case LONG: return "i64";
			case ULONG: return "u64";
			case USIZE: return "usize";
			case FLOAT: return "f32";
			case DOUBLE: return "f64";
			case CHAR: return "char";
			case STRING: return "String";
			default: throw new UnsupportedOperationException("unknown basic type: " + basic);
		}
	}

	@Override
	public String visitArray(ArrayTypeID array) {
		if (array.dimension > 1)
			throw new UnsupportedOperationException("Multidimensional arrays not yet supported");

		return rc() + "<[" + array.elementType.accept(this) + "]>";
	}

	@Override
	public String visitAssoc(AssocTypeID assoc) {
		String hashMap = imports.addImport(RustModule.STD_COLLECTIONS, "HashMap");
		String key = assoc.keyType.accept(this);
		String value = assoc.valueType.accept(this);

		return rc() + "<" + hashMap + "<" + key + ", " + value + ">>";
	}

	@Override
	public String visitGenericMap(GenericMapTypeID map) {
		throw new UnsupportedOperationException("Generic maps not yet supported");
	}

	@Override
	public String visitIterator(IteratorTypeID iterator) {
		throw new UnsupportedOperationException("Iterator types not yet supported");
	}

	@Override
	public String visitFunction(FunctionTypeID function) {
		throw new UnsupportedOperationException("Function types not yet supported");
	}

	@Override
	public String visitDefinition(DefinitionTypeID definition) {
		throw new UnsupportedOperationException("Definition types not yet supported");
	}

	@Override
	public String visitGeneric(GenericTypeID generic) {
		return generic.parameter.name;
	}

	@Override
	public String visitRange(RangeTypeID range) {
		return imports.addImport(RustModule.STD_OPS, "Range") + "<" + range.baseType.accept(this) + ">";
	}

	@Override
	public String visitOptional(OptionalTypeID type) {
		return null;
	}

	private String rc() {
		return multithreaded
				? imports.addImport(RustModule.STD_SYNC, "Arc")
				: imports.addImport(RustModule.STD_RC, "Rc");
	}
}
