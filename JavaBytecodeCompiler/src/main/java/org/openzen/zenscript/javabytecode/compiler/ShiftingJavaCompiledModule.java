package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.*;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.*;

import java.util.List;

final class ShiftingJavaCompiledModule extends JavaCompiledModule {
	private final JavaCompiledModule delegate;
	private final LambdaClosureInfo closureInfo;
	private final JavaBytecodeContext context;

	ShiftingJavaCompiledModule(final JavaCompiledModule module, final LambdaClosureInfo closureInfo, final JavaBytecodeContext context) {
		super(module.module, module.scriptParameters);
		this.delegate = module;
		this.closureInfo = closureInfo;
		this.context = context;
	}

	@Override
	public void addExpansion(final ExpansionSymbol expansion, final JavaClass cls) {
		this.delegate.addExpansion(expansion, cls);
	}

	@Override
	public String generateMappings() {
		return this.delegate.generateMappings();
	}

	@Override
	public void setClassInfo(final DefinitionSymbol definition, final JavaClass cls) {
		this.delegate.setClassInfo(definition, cls);
	}

	@Override
	public void setExpansionClassInfo(final DefinitionSymbol definition, final JavaClass cls) {
		this.delegate.setExpansionClassInfo(definition, cls);
	}

	@Override
	public JavaClass getClassInfo(final DefinitionSymbol definition) {
		return this.delegate.getClassInfo(definition);
	}

	@Override
	public JavaClass getExpansionClassInfo(final DefinitionSymbol definition) {
		return this.delegate.getExpansionClassInfo(definition);
	}

	@Override
	public JavaClass optClassInfo(final TypeSymbol definition) {
		return this.delegate.optClassInfo(definition);
	}

	@Override
	public boolean hasClassInfo(final TypeSymbol definition) {
		return this.delegate.hasClassInfo(definition);
	}

	@Override
	public void setNativeClassInfo(final TypeSymbol definition, final JavaNativeClass cls) {
		this.delegate.setNativeClassInfo(definition, cls);
	}

	@Override
	public JavaNativeClass getNativeClassInfo(final TypeSymbol definition) {
		return this.delegate.getNativeClassInfo(definition);
	}

	@Override
	public void setVariantOption(final VariantDefinition.Option option, final JavaVariantOption value) {
		this.delegate.setVariantOption(option, value);
	}

	@Override
	public JavaVariantOption getVariantOption(final VariantDefinition.Option option) {
		return this.delegate.getVariantOption(option);
	}

	@Override
	public JavaEnumMapper getEnumMapper() {
		return this.delegate.getEnumMapper();
	}

	@Override
	public void setImplementationInfo(final ImplementationMember member, final JavaImplementation implementation) {
		this.delegate.setImplementationInfo(member, implementation);
	}

	@Override
	public JavaImplementation getImplementationInfo(final ImplementationMember member) {
		return this.delegate.getImplementationInfo(member);
	}

	@Override
	public void setFieldInfo(final FieldSymbol member, final JavaField field) {
		this.delegate.setFieldInfo(member, field);
	}

	@Override
	public void setFieldInfo(final MethodSymbol getterOrSetter, final JavaField field) {
		this.delegate.setFieldInfo(getterOrSetter, field);
	}

	@Override
	public JavaField optFieldInfo(final FieldSymbol member) {
		return this.delegate.optFieldInfo(member);
	}

	@Override
	public JavaField optFieldInfo(final MethodSymbol getterOrSetter) {
		return this.delegate.optFieldInfo(getterOrSetter);
	}

	@Override
	public JavaField getFieldInfo(final FieldSymbol member) {
		return this.delegate.getFieldInfo(member);
	}

	@Override
	public void setMethodInfo(final MethodSymbol member, final JavaMethod method) {
		this.delegate.setMethodInfo(member, method);
	}

	@Override
	public JavaMethod optMethodInfo(final MethodSymbol member) {
		return this.delegate.optMethodInfo(member);
	}

	@Override
	public JavaMethod getMethodInfo(final MethodSymbol member) {
		return this.delegate.getMethodInfo(member);
	}

	@Override
	public void setTypeParameterInfo(final TypeParameter parameter, final JavaTypeParameterInfo info) {
		this.delegate.setTypeParameterInfo(parameter, info);
	}

	@Override
	public JavaTypeParameterInfo getTypeParameterInfo(final TypeParameter parameter) {
		return this.delegate.getTypeParameterInfo(parameter);
	}

	@Override
	public void setParameterInfo(final FunctionParameter parameter, final JavaParameterInfo info) {
		this.delegate.setParameterInfo(parameter, info);
	}

	@Override
	public JavaParameterInfo getParameterInfo(final FunctionParameter parameter) {
		final JavaParameterInfo info = this.delegate.getParameterInfo(parameter);
		if (this.closureInfo.isDifferentThis()) {
			final TypeID type = this.closureInfo.thisType();
			final int size = this.context.getType(type).getSize();
			return new JavaParameterInfo(info.index + size, info.typeDescriptor);
		}
		return info;
	}

	@Override
	public void addAllFrom(final JavaCompiledModule compiled) {
		this.delegate.addAllFrom(compiled);
	}

	@Override
	public List<ExpansionSymbol> getExpansions() {
		return this.delegate.getExpansions();
	}
}
