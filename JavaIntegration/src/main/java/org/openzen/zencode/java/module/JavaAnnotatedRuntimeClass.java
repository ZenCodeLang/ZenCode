package org.openzen.zencode.java.module;

import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.InterfaceResolvedType;
import org.openzen.zenscript.codemodel.type.member.SubclassResolvedType;
import org.openzen.zenscript.javashared.JavaClass;

import java.util.Collection;
import java.util.Optional;

public class JavaAnnotatedRuntimeClass extends JavaRuntimeClass {
	private JavaNativeTypeTemplate template;
	private final TypeID target;

	public JavaAnnotatedRuntimeClass(JavaNativeModule module, Class<?> cls, String name, TypeID target, JavaClass.Kind kind) {
		super(module, cls, name, kind);

		this.target = target;
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		if (this.template == null) {
			TypeID target = this.target;
			if (target == null)
				target = DefinitionTypeID.createThis(this);
			this.template = new JavaNativeTypeTemplate(target, this, context, isExpansion());
		}
		ResolvedType resolved = new JavaNativeTypeMembers(template, DefinitionTypeID.create(this, typeArguments), GenericMapper.create(getTypeParameters(), typeArguments));
		Optional<TypeID> superType = getSupertype(typeArguments);
		if (superType.isPresent()) {
			resolved = new SubclassResolvedType(superType.get().resolve(), resolved, superType.get());
		}

		Collection<TypeID> interfaces = getInterfaces(typeArguments);
		if (!interfaces.isEmpty()) {
			resolved = new InterfaceResolvedType(resolved, interfaces);
		}


		return resolved;
	}
}
