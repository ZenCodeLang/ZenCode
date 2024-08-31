package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.TypeMatcher;
import org.openzen.zenscript.codemodel.type.member.InterfaceResolvingType;
import org.openzen.zenscript.codemodel.type.member.SubclassResolvingType;
import org.openzen.zenscript.javashared.JavaClass;

import java.util.Map;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class JavaAnnotatedRuntimeClass extends JavaRuntimeClass {
	private JavaNativeTypeTemplate template;
	private final TypeID target;

	public JavaAnnotatedRuntimeClass(JavaNativeModule module, Class<?> cls, String name, TypeID target, JavaClass.Kind kind) {
		super(module, cls, name, kind);

		this.target = target;
	}

	@Override
	public ResolvingType resolve(TypeID[] typeArguments) {
		TypeID type = DefinitionTypeID.create(this, typeArguments);
		ResolvingType resolved = new JavaNativeTypeMembers.Resolving(getTemplate(), type, GenericMapper.create(getTypeParameters(), typeArguments));
		Optional<TypeID> superType = getSupertype(typeArguments);
		if (superType.isPresent()) {
			resolved = new SubclassResolvingType(superType.get().resolve(), resolved, superType.get());
		}

		Collection<TypeID> interfaces = getInterfaces(typeArguments);
		return InterfaceResolvingType.of(resolved, interfaces);
	}

	@Override
	public Optional<ResolvedType> resolve(TypeID expandingType) {
		if (!isExpansion()) {
			return Optional.empty();
		}

		Map<TypeParameter, TypeID> mapping = TypeMatcher.match(expandingType, target);
		if (mapping == null)
			return Optional.empty();

		TypeID[] expansionTypeArguments = Stream.of(getTypeParameters()).map(mapping::get).toArray(TypeID[]::new);
		GenericMapper mapper = new GenericMapper(mapping, expansionTypeArguments);
		ResolvedType resolved = new JavaNativeTypeMembers(getTemplate(), DefinitionTypeID.create(this, expansionTypeArguments), mapper);

		return Optional.of(resolved);
	}

	private JavaNativeTypeTemplate getTemplate() {
		if (this.template == null) {
			this.template = buildTemplate();
		}
		return template;
	}

	/**
	 * Called to build a new template to be cached
	 */
	protected JavaNativeTypeTemplate buildTemplate() {
		TypeID target = this.target;
		if (target == null)
			target = DefinitionTypeID.createThis(this);
		return new JavaNativeTypeTemplate(target, this, context, isExpansion());
	}
}
