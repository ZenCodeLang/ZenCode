package org.openzen.zenscript.codemodel.definition;

public interface DefinitionVisitor<T> {
	T visitClass(ClassDefinition definition);
	
	T visitInterface(InterfaceDefinition definition);
	
	T visitEnum(EnumDefinition definition);
	
	T visitStruct(StructDefinition definition);
	
	T visitFunction(FunctionDefinition definition);
	
	T visitExpansion(ExpansionDefinition definition);
	
	T visitAlias(AliasDefinition definition);
	
	T visitVariant(VariantDefinition variant);
}
