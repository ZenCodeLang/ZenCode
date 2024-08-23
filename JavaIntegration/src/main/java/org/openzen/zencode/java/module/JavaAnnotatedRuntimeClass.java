package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.TypeMatcher;
import org.openzen.zenscript.codemodel.type.member.ExpandedResolvedType;
import org.openzen.zenscript.codemodel.type.member.InterfaceResolvedType;
import org.openzen.zenscript.codemodel.type.member.SubclassResolvedType;
import org.openzen.zenscript.javashared.JavaClass;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaAnnotatedRuntimeClass extends JavaRuntimeClass {
	private JavaNativeTypeTemplate template;
	private final TypeID target;

	public JavaAnnotatedRuntimeClass(JavaNativeModule module, Class<?> cls, String name, TypeID target, JavaClass.Kind kind) {
		super(module, cls, name, kind);

		this.target = target;
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments, List<ExpansionSymbol> expansions) {
		TypeID type = DefinitionTypeID.create(this, typeArguments);
		ResolvedType resolved = new JavaNativeTypeMembers(getTemplate(), type, GenericMapper.create(getTypeParameters(), typeArguments));
		Optional<TypeID> superType = getSupertype(typeArguments);
		if (superType.isPresent()) {
			resolved = new SubclassResolvedType(superType.get().resolve(expansions), resolved, superType.get());
		}

		Collection<TypeID> interfaces = getInterfaces(typeArguments);
		if (!interfaces.isEmpty()) {
			resolved = new InterfaceResolvedType(resolved, interfaces, expansions);
		}

		List<ResolvedType> expansionsResolved = expansions.stream().map(expansion -> expansion.resolve(type)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return ExpandedResolvedType.of(resolved, expansionsResolved);
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
