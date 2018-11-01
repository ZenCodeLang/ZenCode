/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaCompiledModule {
	public final Module module;
	
	private final Map<HighLevelDefinition, JavaClass> classes = new HashMap<>();
	private final Map<HighLevelDefinition, JavaClass> expansionClasses = new HashMap<>();
	private final Map<HighLevelDefinition, JavaNativeClass> nativeClasses = new HashMap<>();
	private final Map<ImplementationMember, JavaImplementation> implementations = new HashMap<>();
	private final Map<IDefinitionMember, JavaField> fields = new HashMap<>();
	private final Map<IDefinitionMember, JavaMethod> methods = new HashMap<>();
	private final Map<FunctionParameter, JavaParameterInfo> parameters = new HashMap<>();
	private final Map<VariantDefinition.Option, JavaVariantOption> variantOptions = new HashMap<>();
	
	public JavaCompiledModule(Module module) {
		this.module = module;
	}
	
	/*public Statement generateModuleDefinitionMethod() {
		List<Statement> statements = new ArrayList<>();
		
		
		return new BlockStatement(CodePosition.GENERATED, statements.toArray(new Statement[statements.size()]));
	}*/
	
	public void setClassInfo(HighLevelDefinition definition, JavaClass cls) {
		classes.put(definition, cls);
	}
	
	public void setExpansionClassInfo(HighLevelDefinition definition, JavaClass cls) {
		expansionClasses.put(definition, cls);
	}
	
	public JavaClass getClassInfo(HighLevelDefinition definition) {
		JavaClass cls = classes.get(definition);
		if (cls == null)
			throw new IllegalStateException("Missing class info for class " + definition.name);
		
		return cls;
	}
	
	public JavaClass getExpansionClassInfo(HighLevelDefinition definition) {
		JavaClass cls = expansionClasses.get(definition);
		if (cls == null)
			return getClassInfo(definition);
		
		return cls;
	}
	
	public JavaClass optClassInfo(HighLevelDefinition definition) {
		return classes.get(definition);
	}
	
	public boolean hasClassInfo(HighLevelDefinition definition) {
		return classes.containsKey(definition);
	}
	
	public void setNativeClassInfo(HighLevelDefinition definition, JavaNativeClass cls) {
		nativeClasses.put(definition, cls);
	}
	
	public JavaNativeClass getNativeClassInfo(HighLevelDefinition definition) {
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
	
	public void setImplementationInfo(ImplementationMember member, JavaImplementation implementation) {
		implementations.put(member, implementation);
	}
	
	public JavaImplementation getImplementationInfo(ImplementationMember member) {
		JavaImplementation implementation = implementations.get(member);
		if (implementation == null)
			throw new IllegalStateException("Implementation unknown: " + member.type);
		
		return implementation;
	}
	
	public void setFieldInfo(IDefinitionMember member, JavaField field) {
		fields.put(member, field);
	}
	
	public JavaField optFieldInfo(IDefinitionMember member) {
		return fields.get(member);
	}
	
	public JavaField getFieldInfo(IDefinitionMember member) {
		JavaField field = fields.get(member);
		if (field == null)
			throw new IllegalStateException("Missing field info for field " + member.getDefinition().name + "." + member.describe());
		
		return field;
	}
	
	public void setMethodInfo(IDefinitionMember member, JavaMethod method) {
		methods.put(member, method);
	}
	
	public JavaMethod getMethodInfo(DefinitionMemberRef member) {
		return getMethodInfo(member.getTarget());
	}
	
	public JavaMethod getMethodInfo(IDefinitionMember member) {
		JavaMethod method = methods.get(member);
		if (method == null)
			throw new IllegalStateException("Missing method info for method " + member.getDefinition().name + "." + member.describe());
		
		return method;
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
}
