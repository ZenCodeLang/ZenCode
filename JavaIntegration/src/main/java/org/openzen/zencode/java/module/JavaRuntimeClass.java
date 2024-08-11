package org.openzen.zencode.java.module;

import org.objectweb.asm.Type;
import org.openzen.zencode.java.TypeVariableContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class JavaRuntimeClass implements TypeSymbol, ExpansionSymbol {
	public final JavaNativeModule module;
	public final JavaClass javaClass;
	public final Class<?> cls;
	public final String name;

	private final Modifiers modifiers;
	private boolean superclassEvaluated = false;
	private TypeID superclass;
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
		if (!superclassEvaluated) {
			superclassEvaluated = true;

			AnnotatedType superType = cls.getAnnotatedSuperclass();
			if (superType != null && module.isKnownType(cls.getSuperclass())) {
				superclass = module.getTypeConverter().getType(context, superType);
			}
		}
		return Optional.ofNullable(superclass).map(s -> {
			GenericMapper mapper = GenericMapper.create(getTypeParameters(), typeArguments);
			return mapper.map(s);
		});
	}

	protected Collection<TypeID> getInterfaces(TypeID[] typeArguments) {
		GenericMapper mapper = GenericMapper.create(getTypeParameters(), typeArguments);

		return Arrays.stream(cls.getAnnotatedInterfaces())
				.filter(annotatedType -> module.isKnownType(annotatedType.getType()))
				.map(it -> module.getTypeConverter().getType(context, it))
				.map(mapper::map)
				.collect(Collectors.toList());
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

	public void translateTypeParameters(TypeVariableContext context) {
		translateTypeParameters(cls, context);
	}

	private TypeParameter[] translateTypeParameters(Class<?> cls, TypeVariableContext context) {
		TypeVariable<?>[] javaTypeParameters = cls.getTypeParameters();

		// Early abort in case of self-referencing generics
		if(context.containsAll(javaTypeParameters)) {
			return Arrays.stream(javaTypeParameters)
					.map(context::get)
					.toArray(TypeParameter[]::new);
		}

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

	private TypeParameter[] translateTypeParameters(Class<?> cls) {
		return translateTypeParameters(cls, context);
	}
}
