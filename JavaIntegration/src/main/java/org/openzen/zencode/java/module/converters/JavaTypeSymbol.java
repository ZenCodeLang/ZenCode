package org.openzen.zencode.java.module.converters;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class JavaTypeSymbol implements TypeSymbol {
	private final Module module;
	private final Class<?> cls;
	private final TypeParameter[] typeParameters;
	private final TypeID superclass;

	public JavaTypeSymbol(Module module, Class<?> cls, TypeParameter[] typeParameters, TypeID superclass) {
		this.module = module;
		this.cls = cls;
		this.typeParameters = typeParameters;
		this.superclass = superclass;
	}

	@Override
	public Module getModule() {
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
		return new JavaTypeMembers(cls, typeArguments);
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
