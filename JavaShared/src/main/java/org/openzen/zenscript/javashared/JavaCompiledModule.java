/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.DefinitionSymbol;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Hoofdgebruiker
 */
public class JavaCompiledModule {
	public final Module module;
	public final FunctionParameter[] scriptParameters;

	private final Map<DefinitionSymbol, JavaClass> classes = new HashMap<>();
	private final Map<DefinitionSymbol, JavaClass> expansionClasses = new HashMap<>();
	private final Map<DefinitionSymbol, JavaNativeClass> nativeClasses = new HashMap<>();
	private final Map<ImplementationMember, JavaImplementation> implementations = new HashMap<>();
	private final Map<FieldSymbol, JavaField> fields = new HashMap<>();
	private final Map<MethodSymbol, JavaField> fieldsAlt = new HashMap<>();
	private final Map<MethodSymbol, JavaMethod> methods = new HashMap<>();
	private final Map<TypeParameter, JavaTypeParameterInfo> typeParameters = new HashMap<>();
	private final Map<FunctionParameter, JavaParameterInfo> parameters = new IdentityHashMap<>();
	private final Map<VariantDefinition.Option, JavaVariantOption> variantOptions = new HashMap<>();
	private final JavaEnumMapper enumMapper = new JavaEnumMapper();

	public JavaCompiledModule(Module module, FunctionParameter[] scriptParameters) {
		this.module = module;
		this.scriptParameters = scriptParameters;
	}

	public String generateMappings() {
		JavaMappingWriter writer = new JavaMappingWriter(this);
		for (DefinitionSymbol definition : classes.keySet()) {
			if (definition instanceof HighLevelDefinition) {
				if (!(definition instanceof ExpansionDefinition))
					((HighLevelDefinition)definition).accept(writer);
			}
		}
		for (DefinitionSymbol definition : expansionClasses.keySet()) {
			if (definition instanceof ExpansionDefinition)
				((ExpansionDefinition)definition).accept(writer);
		}

		return writer.getOutput();
	}

	public void setClassInfo(TypeSymbol definition, JavaClass cls) {
		classes.put(definition, cls);
	}

	public void setExpansionClassInfo(TypeSymbol definition, JavaClass cls) {
		expansionClasses.put(definition, cls);
	}

	public JavaClass getClassInfo(DefinitionSymbol definition) {
		JavaClass cls = classes.get(definition);
		if (cls == null)
			throw new IllegalStateException("Missing class info for class " + definition.describe());

		return cls;
	}

	public JavaClass getExpansionClassInfo(DefinitionSymbol definition) {
		JavaClass cls = expansionClasses.get(definition);
		if (cls == null)
			return getClassInfo(definition);

		return cls;
	}

	public JavaClass optClassInfo(TypeSymbol definition) {
		return classes.get(definition);
	}

	public boolean hasClassInfo(TypeSymbol definition) {
		return classes.containsKey(definition);
	}

	public void setNativeClassInfo(TypeSymbol definition, JavaNativeClass cls) {
		nativeClasses.put(definition, cls);
	}

	public JavaNativeClass getNativeClassInfo(TypeSymbol definition) {
		return nativeClasses.get(definition);
	}

	public void setVariantOption(VariantDefinition.Option option, JavaVariantOption value) {
		variantOptions.put(option, value);
	}

	public JavaVariantOption getVariantOption(VariantDefinition.Option option) {
		JavaVariantOption result = variantOptions.get(option);
		if (result == null)
			throw new IllegalStateException("Variant option unknown: " + option.name);

		return result;
	}

	public JavaEnumMapper getEnumMapper() {
		return enumMapper;
	}

	public void setImplementationInfo(ImplementationMember member, JavaImplementation implementation) {
		implementations.put(member, implementation);
	}

	public JavaImplementation getImplementationInfo(ImplementationMember member) {
		JavaImplementation implementation = implementations.get(member);
		if (implementation == null)
			throw new IllegalStateException("Implementation unknown: " + member.type);

		return implementation;
	}

	public void setFieldInfo(FieldSymbol member, JavaField field) {
		fields.put(member, field);
	}

	public void setFieldInfo(MethodSymbol getterOrSetter, JavaField field) {
		fieldsAlt.put(getterOrSetter, field);
	}

	public JavaField optFieldInfo(FieldSymbol member) {
		return fields.get(member);
	}

	public JavaField optFieldInfo(MethodSymbol getterOrSetter) {
		return fieldsAlt.get(getterOrSetter);
	}

	public JavaField getFieldInfo(FieldSymbol member) {
		JavaField field = fields.get(member);
		if (field == null)
			throw new IllegalStateException("Missing field info for field " + member.getDefiningType().getName() + "." + member.getName());

		return field;
	}

	public void setMethodInfo(MethodSymbol member, JavaMethod method) {
		methods.put(member, method);
	}

	public JavaMethod optMethodInfo(MethodSymbol member) {
		return methods.get(member);
	}

	public JavaMethod getMethodInfo(MethodSymbol member) {
		if (member == BuiltinMethodSymbol.CLASS_DEFAULT_CONSTRUCTOR) // TODO: handle this differently
			return new JavaNativeMethod(getClassInfo(member.getDefiningType()), JavaNativeMethod.Kind.CONSTRUCTOR, "<init>", true, "()V", Modifiers.FLAG_PUBLIC, false);

		JavaMethod method = methods.get(member);
		if (method == null)
			throw new IllegalStateException("Missing method info for method " + member.getDefiningType().describe() + "." + member.getName());

		return method;
	}

	public void setTypeParameterInfo(TypeParameter parameter, JavaTypeParameterInfo info) {
		typeParameters.put(parameter, info);
	}

	public JavaTypeParameterInfo getTypeParameterInfo(TypeParameter parameter) {
		JavaTypeParameterInfo info = typeParameters.get(parameter);
		if (info == null)
			throw new IllegalStateException("Missing parameter info for type parameter " + parameter);

		return info;
	}

	public void setParameterInfo(FunctionParameter parameter, JavaParameterInfo info) {
		parameters.put(parameter, info);
	}

	public JavaParameterInfo getParameterInfo(FunctionParameter parameter) {
		JavaParameterInfo info = parameters.get(parameter);
		if (info == null)
			throw new IllegalStateException("Missing parameter info for parameter " + parameter.name);

		return info;
	}

	public void addAllFrom(JavaCompiledModule compiled) {
		this.classes.putAll(compiled.classes);
		this.expansionClasses.putAll(compiled.expansionClasses);
		this.nativeClasses.putAll(compiled.nativeClasses);
		this.implementations.putAll(compiled.implementations);
		this.fields.putAll(compiled.fields);
		this.methods.putAll(compiled.methods);
		this.typeParameters.putAll(compiled.typeParameters);
		this.parameters.putAll(compiled.parameters);
		this.variantOptions.putAll(compiled.variantOptions);
		this.enumMapper.merge(compiled.enumMapper);
	}

	public List<ExpansionDefinition> getExpansions() {
		return expansionClasses.keySet()
				.stream()
				.filter(definition -> definition instanceof ExpansionDefinition)
				.map(definition -> (ExpansionDefinition) definition)
				.collect(Collectors.toList());
	}
}
