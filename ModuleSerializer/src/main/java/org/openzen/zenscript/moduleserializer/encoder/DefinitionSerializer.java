/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.moduleserialization.DefinitionEncoding;
import org.openzen.zenscript.moduleserialization.TypeParameterEncoding;
import org.openzen.zenscript.moduleserializer.SerializationOptions;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionSerializer implements DefinitionVisitor<Void> {
	private final SerializationOptions options;
	private final CodeSerializationOutput output;
	
	public DefinitionSerializer(SerializationOptions options, CodeSerializationOutput output) {
		this.options = options;
		this.output = output;
	}
	
	private void visit(HighLevelDefinition definition) {
		int flags = 0;
		if (definition.position != null && options.positions)
			flags |= DefinitionEncoding.FLAG_POSITION;
		if (definition.name != null)
			flags |= DefinitionEncoding.FLAG_NAME;
		if (definition.typeParameters.length > 0)
			flags |= DefinitionEncoding.FLAG_TYPE_PARAMETERS;
		
		output.writeUInt(flags);
		output.writeUInt(definition.modifiers);
		if (definition.position != CodePosition.UNKNOWN && options.positions)
			output.serialize(definition.position);
		output.writeString(definition.pkg.fullName);
		if (definition.name != null)
			output.writeString(definition.name);
		if (definition.typeParameters.length > 0) {
			output.writeUInt(definition.typeParameters.length);
			for (TypeParameter parameter : definition.typeParameters) {
				int typeParameterFlags = 0;
				if (parameter.position != CodePosition.UNKNOWN && options.positions)
					typeParameterFlags |= TypeParameterEncoding.FLAG_POSITION;
				if (parameter.name != null && options.typeParameterNames)
					typeParameterFlags |= TypeParameterEncoding.FLAG_NAME;
				
				output.writeUInt(typeParameterFlags);
				if ((typeParameterFlags & TypeParameterEncoding.FLAG_POSITION) > 0)
					output.serialize(parameter.position);
				if ((typeParameterFlags & TypeParameterEncoding.FLAG_NAME) > 0)
					output.writeString(parameter.name);
			}
		}
	}
	
	private void queueEncodeMembers(HighLevelDefinition definition) {
		output.enqueueMembers(output -> {
			DefinitionMemberSerializer memberEncoder = new DefinitionMemberSerializer(options, output);
			TypeContext context = new TypeContext(definition.typeParameters);
			definition.accept(context, memberEncoder);
		});
	}
	
	@Override
	public Void visitClass(ClassDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_CLASS);
		visit(definition);
		queueEncodeMembers(definition);
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_INTERFACE);
		visit(definition);
		queueEncodeMembers(definition);
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_ENUM);
		visit(definition);
		queueEncodeMembers(definition);
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_STRUCT);
		visit(definition);
		queueEncodeMembers(definition);
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_FUNCTION);
		visit(definition);
		queueEncodeMembers(definition);
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_EXPANSION);
		visit(definition);
		queueEncodeMembers(definition);
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_ALIAS);
		visit(definition);
		return null;
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		output.writeUInt(DefinitionEncoding.TYPE_VARIANT);
		visit(variant);
		queueEncodeMembers(variant);
		return null;
	}
}
