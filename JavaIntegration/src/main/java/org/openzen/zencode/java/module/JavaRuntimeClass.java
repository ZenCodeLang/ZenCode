package org.openzen.zencode.java.module;

import org.objectweb.asm.Type;
import org.openzen.zencode.java.TypeVariableContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javashared.JavaClass;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Optional;

public abstract class JavaRuntimeClass implements TypeSymbol {
	public final JavaNativeModule module;
	public final JavaClass javaClass;
	public final Class<?> cls;
	public final String name;

	private final Modifiers modifiers;
	private TypeParameter[] typeParameters;
	protected final TypeVariableContext context = new TypeVariableContext();

	public JavaRuntimeClass(JavaNativeModule module, Class<?> cls, String name, JavaClass.Kind kind) {
		this.module = module;
		this.cls = cls;
		this.name = name;
		this.javaClass = JavaClass.fromInternalName(Type.getInternalName(cls), kind);
		this.modifiers = translateModifiers(cls.getModifiers());
	}

	@Override
	public ModuleSymbol getModule() {
		return module.getModule();
	}

	@Override
	public String describe() {
		return name;
	}

	@Override
	public boolean isInterface() {
		return cls.isInterface();
	}

	@Override
	public boolean isExpansion() {
		return javaClass.kind == JavaClass.Kind.EXPANSION;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isStatic() {
		return modifiers.isStatic();
	}

	@Override
	public boolean isEnum() {
		return cls.isEnum();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public abstract ResolvedType resolve(TypeID[] typeArguments);

	@Override
	public TypeParameter[] getTypeParameters() {
		if (this.typeParameters == null) {
			this.typeParameters = translateTypeParameters(cls);
		}
		return typeParameters;
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.empty();
	}

	@Override
	public Optional<TypeID> getSupertype(TypeID[] typeArguments) {
		return Optional.empty();
	}

	private static Modifiers translateModifiers(int modifiers) {
		Modifiers result = Modifiers.NONE;
		if ((modifiers & Modifier.FINAL) == 0)
			result = result.withVirtual();
		if ((modifiers & Modifier.PUBLIC) > 0)
			result = result.withPublic();
		else
			result = result.withPrivate();

		return result;
	}

	private TypeParameter[] translateTypeParameters(Class<?> cls) {
		TypeVariable<?>[] javaTypeParameters = cls.getTypeParameters();
		TypeParameter[] typeParameters = TypeParameter.NONE;
		if (javaTypeParameters.length > 0) {
			typeParameters = new TypeParameter[cls.getTypeParameters().length];
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<?> typeVariable = javaTypeParameters[i];
			typeParameters[i] = new TypeParameter(CodePosition.NATIVE, typeVariable.getName());
			context.put(typeVariable, typeParameters[i]);
		}

		for (int i = 0; i < javaTypeParameters.length; i++) {
			TypeVariable<?> typeVariable = javaTypeParameters[i];
			TypeParameter parameter = typeParameters[i];
			for (AnnotatedType bound : typeVariable.getAnnotatedBounds()) {
				if (bound.getType() == Object.class) {
					continue; //Makes the stdlib types work as they have "no" bounds for T
				}
				TypeID type = module.getTypeConverter().getType(context, bound);
				parameter.addBound(new ParameterTypeBound(CodePosition.NATIVE, type));
			}
		}

		return typeParameters;
	}
}
