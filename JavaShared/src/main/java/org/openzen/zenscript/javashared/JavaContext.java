/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.DefinitionMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoofdgebruiker
 */
public abstract class JavaContext {
	public final ZSPackage modulePackage;
	public final String basePackage;
	public final IZSLogger logger;
	private final GlobalTypeRegistry registry;
	private final Map<String, JavaSynthesizedFunction> functions = new HashMap<>();
	private final Map<String, JavaSynthesizedRange> ranges = new HashMap<>();
	private final JavaCompileSpace space;
	private final Map<Module, JavaCompiledModule> modules = new HashMap<>();
	private boolean useShared = false;

	public JavaContext(JavaCompileSpace space, ZSPackage modulePackage, String basePackage, IZSLogger logger) {
		this.logger = logger;
		this.space = space;
		this.registry = space.getRegistry();

		this.modulePackage = modulePackage;
		this.basePackage = basePackage;

		{
			TypeParameter t = new TypeParameter(CodePosition.BUILTIN, "T");
			TypeParameter u = new TypeParameter(CodePosition.BUILTIN, "U");
			TypeParameter v = new TypeParameter(CodePosition.BUILTIN, "V");

			functions.put("TToU", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "Function", JavaClass.Kind.INTERFACE),
					new TypeParameter[]{t, u},
					new FunctionHeader(registry.getGeneric(u), registry.getGeneric(t)),
					"apply"));

			functions.put("TUToV", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "BiFunction", JavaClass.Kind.INTERFACE),
					new TypeParameter[]{t, u, v},
					new FunctionHeader(registry.getGeneric(v), registry.getGeneric(t), registry.getGeneric(u)),
					"apply"));

			functions.put("TToVoid", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "Consumer", JavaClass.Kind.INTERFACE),
					new TypeParameter[]{t},
					new FunctionHeader(BasicTypeID.VOID, registry.getGeneric(t)),
					"accept"));

			functions.put("TUToVoid", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "BiConsumer", JavaClass.Kind.INTERFACE),
					new TypeParameter[]{t, u},
					new FunctionHeader(BasicTypeID.VOID, registry.getGeneric(t), registry.getGeneric(u)),
					"accept"));

			functions.put("TToBool", new JavaSynthesizedFunction(
					new JavaClass("java.util.function", "Predicate", JavaClass.Kind.INTERFACE),
					new TypeParameter[]{t},
					new FunctionHeader(BasicTypeID.BOOL, registry.getGeneric(t)),
					"test"));
		}
	}

	public String getPackageName(ZSPackage pkg) {
		if (pkg == null)
			throw new IllegalArgumentException("Package not part of this module");

		if (pkg == modulePackage)
			return basePackage;

		return getPackageName(pkg.parent) + "/" + pkg.name;
	}

	public JavaMethod getFunctionalInterface(TypeID type) {
		if (type instanceof JavaFunctionalInterfaceTypeID) {
			JavaFunctionalInterfaceTypeID t = (JavaFunctionalInterfaceTypeID) type;
			return t.method;
		} else {
			FunctionTypeID functionType = (FunctionTypeID) type;
			JavaSynthesizedFunctionInstance function = getFunction(functionType);

			return new JavaMethod(
					function.getCls(),
					JavaMethod.Kind.INTERFACE,
					function.getMethod(),
					false,
					getMethodDescriptor(function.getHeader()),
					JavaModifiers.PUBLIC | JavaModifiers.ABSTRACT,
					function.getHeader().getReturnType().isGeneric());
		}
	}

	protected abstract JavaSyntheticClassGenerator getTypeGenerator();

	public abstract String getDescriptor(TypeID type);

	public String getSignature(TypeID type) {
		return new JavaTypeGenericVisitor(this).getGenericSignature(type);
	}

	public void addModule(Module module, JavaCompiledModule target) {
		modules.put(module, target);

		//TODO: can we do this here?
		space.register(target);
	}

	public JavaCompiledModule getJavaModule(Module module) {
		if (modules.containsKey(module))
			return modules.get(module);

		JavaCompiledModule javaModule = space.getCompiled(module);
		if (javaModule == null)
			throw new IllegalStateException("Module not yet registered: " + module.name);

		return javaModule;
	}

	public JavaClass getJavaClass(HighLevelDefinition definition) {
		return getJavaModule(definition.module).getClassInfo(definition);
	}

	public JavaClass getJavaExpansionClass(HighLevelDefinition definition) {
		return getJavaModule(definition.module).getExpansionClassInfo(definition);
	}

	public JavaClass optJavaClass(HighLevelDefinition definition) {
		return getJavaModule(definition.module).optClassInfo(definition);
	}

	public JavaNativeClass getJavaNativeClass(HighLevelDefinition definition) {
		return getJavaModule(definition.module).getNativeClassInfo(definition);
	}

	public boolean hasJavaClass(HighLevelDefinition definition) {
		return getJavaModule(definition.module).hasClassInfo(definition);
	}

	public void setJavaClass(HighLevelDefinition definition, JavaClass cls) {
		getJavaModule(definition.module).setClassInfo(definition, cls);
	}

	public void setJavaExpansionClass(HighLevelDefinition definition, JavaClass cls) {
		getJavaModule(definition.module).setExpansionClassInfo(definition, cls);
	}

	public void setJavaNativeClass(HighLevelDefinition definition, JavaNativeClass cls) {
		getJavaModule(definition.module).setNativeClassInfo(definition, cls);
	}

	public boolean hasJavaField(DefinitionMemberRef member) {
		HighLevelDefinition definition = member.getTarget().getDefinition();
		return getJavaModule(definition.module).optFieldInfo(member.getTarget()) != null;
	}

	public JavaField getJavaField(IDefinitionMember member) {
		HighLevelDefinition definition = member.getDefinition();
		return getJavaModule(definition.module).getFieldInfo(member);
	}

	public JavaField getJavaField(DefinitionMemberRef member) {
		return getJavaField(member.getTarget());
	}

	public JavaMethod getJavaMethod(IDefinitionMember member) {
		HighLevelDefinition definition = member.getDefinition();
		return getJavaModule(definition.module).getMethodInfo(member);
	}

	public JavaMethod getJavaMethod(DefinitionMemberRef member) {
		return getJavaMethod(member.getTarget());
	}

	public JavaVariantOption getJavaVariantOption(VariantDefinition.Option option) {
		HighLevelDefinition definition = option.variant;
		return getJavaModule(definition.module).getVariantOption(option);
	}

	public JavaVariantOption getJavaVariantOption(VariantOptionRef member) {
		return getJavaVariantOption(member.getOption());
	}

	public JavaImplementation getJavaImplementation(ImplementationMember member) {
		return getJavaModule(member.definition.module).getImplementationInfo(member);
	}

	public String getMethodDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, false, "");
	}

	public String getMethodDescriptorExpansion(FunctionHeader header, TypeID expandedType) {
		StringBuilder startBuilder = new StringBuilder(getDescriptor(expandedType));
		final List<TypeParameter> typeParameters = new ArrayList<>();
		expandedType.extractTypeParameters(typeParameters);
		for (TypeParameter typeParameter : typeParameters) {
			startBuilder.append("Ljava/lang/Class;");
		}

		return getMethodDescriptor(header, false, startBuilder.toString());
	}

	public String getMethodSignatureExpansion(FunctionHeader header, TypeID expandedClass) {
		return new JavaTypeGenericVisitor(this).getMethodSignatureExpansion(header, expandedClass);
	}

	public String getMethodSignature(FunctionHeader header) {
		return getMethodSignature(header, true);
	}

	public String getMethodSignature(FunctionHeader header, boolean withGenerics) {
		return new JavaTypeGenericVisitor(this).getGenericMethodSignature(header, withGenerics);
	}

	public String getEnumConstructorDescriptor(FunctionHeader header) {
		return getMethodDescriptor(header, true, "");
	}

	public JavaSynthesizedFunctionInstance getFunction(FunctionTypeID type) {
		String id = getFunctionId(type.header);
		JavaSynthesizedFunction function;
		if (!functions.containsKey(id)) {
			JavaClass cls = new JavaClass("zsynthetic", "Function" + id, JavaClass.Kind.INTERFACE);
			List<TypeParameter> typeParameters = new ArrayList<>();
			List<FunctionParameter> parameters = new ArrayList<>();
			for (FunctionParameter parameter : type.header.parameters) {
				JavaTypeInfo typeInfo = JavaTypeInfo.get(parameter.type);
				if (typeInfo.primitive) {
					parameters.add(new FunctionParameter(parameter.type, Character.toString((char) ('a' + parameters.size()))));
				} else {
					TypeParameter typeParameter = new TypeParameter(CodePosition.BUILTIN, getTypeParameter(typeParameters.size()));
					typeParameters.add(typeParameter);
					parameters.add(new FunctionParameter(registry.getGeneric(typeParameter), Character.toString((char) ('a' + parameters.size()))));
				}
			}
			TypeID returnType;
			{
				JavaTypeInfo typeInfo = JavaTypeInfo.get(type.header.getReturnType());
				if (typeInfo.primitive) {
					returnType = type.header.getReturnType();
				} else {
					TypeParameter typeParameter = new TypeParameter(CodePosition.BUILTIN, getTypeParameter(typeParameters.size()));
					typeParameters.add(typeParameter);
					returnType = registry.getGeneric(typeParameter);
				}
			}
			function = new JavaSynthesizedFunction(
					cls,
					typeParameters.toArray(new TypeParameter[typeParameters.size()]),
					new FunctionHeader(returnType, parameters.toArray(new FunctionParameter[parameters.size()])),
					"invoke");

			functions.put(id, function);
			getTypeGenerator().synthesizeFunction(function);
		} else {
			function = functions.get(id);
		}

		List<TypeID> typeArguments = new ArrayList<>();
		for (FunctionParameter parameter : type.header.parameters) {
			JavaTypeInfo typeInfo = JavaTypeInfo.get(parameter.type);
			if (!typeInfo.primitive) {
				typeArguments.add(parameter.type);
			}
		}
		if (!JavaTypeInfo.isPrimitive(type.header.getReturnType()))
			typeArguments.add(type.header.getReturnType());

		return new JavaSynthesizedFunctionInstance(function, typeArguments.toArray(new TypeID[typeArguments.size()]));
	}

	private String getFunctionId(FunctionHeader header) {
		StringBuilder signature = new StringBuilder();
		int typeParameterIndex = 0;
		for (FunctionParameter parameter : header.parameters) {
			JavaTypeInfo typeInfo = JavaTypeInfo.get(parameter.type);
			String id = typeInfo.primitive ? parameter.type.accept(new JavaSyntheticTypeSignatureConverter()) : getTypeParameter(typeParameterIndex++);
			signature.append(id);
		}
		signature.append("To");
		{
			JavaTypeInfo typeInfo = JavaTypeInfo.get(header.getReturnType());
			String id = typeInfo.primitive ? header.getReturnType().accept(new JavaSyntheticTypeSignatureConverter()) : getTypeParameter(typeParameterIndex++);
			signature.append(id);
		}
		return signature.toString();
	}

	private String getTypeParameter(int index) {
		switch (index) {
			case 0:
				return "T";
			case 1:
				return "U";
			case 2:
				return "V";
			case 3:
				return "W";
			case 4:
				return "X";
			case 5:
				return "Y";
			case 6:
				return "Z";
			default:
				return "T" + index;
		}
	}

	public JavaSynthesizedClass getRange(RangeTypeID type) {
		JavaTypeInfo typeInfo = JavaTypeInfo.get(type.baseType);
		String id = typeInfo.primitive ? type.accept(new JavaSyntheticTypeSignatureConverter()) : "T";
		JavaSynthesizedRange range;
		if (!ranges.containsKey(id)) {
			JavaClass cls = new JavaClass("zsynthetic", id, JavaClass.Kind.CLASS);
			if (typeInfo.primitive) {
				range = new JavaSynthesizedRange(cls, TypeParameter.NONE, type.baseType);
			} else {
				TypeParameter typeParameter = new TypeParameter(CodePosition.BUILTIN, "T");
				range = new JavaSynthesizedRange(cls, new TypeParameter[]{typeParameter}, registry.getGeneric(typeParameter));
			}
			ranges.put(id, range);
			getTypeGenerator().synthesizeRange(range);
		} else {
			range = ranges.get(id);
		}

		if (typeInfo.primitive) {
			return new JavaSynthesizedClass(range.cls, TypeID.NONE);
		} else {
			return new JavaSynthesizedClass(range.cls, new TypeID[]{type.baseType});
		}
	}

	/**
	 * @param header            Function Header
	 * @param isEnumConstructor If this is an enum constructor, add String, int as parameters
	 * @param expandedType      If this is for an expanded type, add the type at the beginning.
	 *                          Can be null or an empty string if this is not an expansion method header
	 * @return Method descriptor {@code (<LClass;*No.TypeParameters><LString;I if enum><expandedType><headerTypes>)<retType> }
	 */
	public String getMethodDescriptor(FunctionHeader header, boolean isEnumConstructor, String expandedType) {
		StringBuilder descBuilder = new StringBuilder("(");
		for (int i = 0; i < header.getNumberOfTypeParameters(); i++)
			descBuilder.append("Ljava/lang/Class;");

		if (isEnumConstructor)
			descBuilder.append("Ljava/lang/String;I");

		//TODO: Put this earlier? We'd need to agree on one...
		if (expandedType != null)
			descBuilder.append(expandedType);

		for (FunctionParameter parameter : header.parameters) {
			descBuilder.append(getDescriptor(parameter.type));
		}
		descBuilder.append(")");
		descBuilder.append(getDescriptor(header.getReturnType()));
		return descBuilder.toString();
	}

	public String getMethodDescriptorConstructor(FunctionHeader header, DefinitionMember member) {
		StringBuilder startBuilder = new StringBuilder();
		for (TypeParameter typeParameter : member.definition.typeParameters) {
			startBuilder.append("Ljava/lang/Class;");
		}
		return getMethodDescriptor(header, member.definition instanceof EnumDefinition, startBuilder.toString());
	}
}
