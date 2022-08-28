package org.openzen.zencode.java.module.converters;

import org.openzen.zencode.java.module.JavaNativeTypeMembers;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class JavaTypeSymbol implements TypeSymbol {
	private final ModuleSymbol module;
	private final Class<?> cls;
	private final TypeParameter[] typeParameters;
	private final TypeID superclass;

	public JavaTypeSymbol(ModuleSymbol module, Class<?> cls, TypeParameter[] typeParameters, TypeID superclass) {
		this.module = module;
		this.cls = cls;
		this.typeParameters = typeParameters;
		this.superclass = superclass;
	}

	@Override
	public ModuleSymbol getModule() {
		return module;
	}

	@Override
	public String describe() {
		return cls.getName();
	}

	@Override
	public boolean isInterface() {
		return cls.isInterface();
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean isEnum() {
		return cls.isEnum();
	}

	@Override
	public String getName() {
		return cls.getName();
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		return new JavaNativeTypeMembers(cls, typeArguments);
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return TypeParameter.NONE; // TODO
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.empty();
	}

	@Override
	public Optional<TypeID> getSupertype(TypeID[] typeArguments) {
		if (typeArguments.length == 0) {
			return Optional.ofNullable(superclass);
		} else if (superclass != null) {
			GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);
			return Optional.of(superclass.instance(mapper));
		} else {
			return Optional.empty();
		}
	}
}
