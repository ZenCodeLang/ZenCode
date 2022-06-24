/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitorWithContext;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.moduleserialization.DefinitionEncoding;
import org.openzen.zenscript.moduleserializer.SerializationOptions;

/**
 * @author Hoofdgebruiker
 */
public class DefinitionSerializer implements DefinitionVisitorWithContext<ModuleContext, Void> {
	private final SerializationOptions options;
	private final CodeSerializationOutput output;

	public DefinitionSerializer(SerializationOptions options, CodeSerializationOutput output) {
		this.options = options;
		this.output = output;
	}

	private void visit(ModuleContext context, HighLevelDefinition definition) {
		int flags = 0;
		if (definition.position != null && options.positions)
			flags |= DefinitionEncoding.FLAG_POSITION;
		if (definition.name != null)
			flags |= DefinitionEncoding.FLAG_NAME;
		if (definition.typeParameters.length > 0)
			flags |= DefinitionEncoding.FLAG_TYPE_PARAMETERS;
		if (definition.annotations.length > 0)
			flags |= DefinitionEncoding.FLAG_ANNOTATIONS;

		output.writeUInt(flags);
		output.writeUInt(definition.modifiers.value);
		if (definition.position != CodePosition.UNKNOWN && options.positions)
			output.serialize(definition.position);
		output.writeString(definition.pkg.fullName);
		if (definition.name != null)
			output.writeString(definition.name);

		TypeSerializationContext typeContext = new TypeSerializationContext(context, definition.typeParameters, null);
		if (definition.typeParameters.length > 0)
			output.serialize(typeContext, definition.typeParameters);

		if (definition.annotations.length > 0) {
			output.enqueueCode(output -> {
				output.writeUInt(definition.annotations.length);
				for (DefinitionAnnotation annotation : definition.annotations) {
					output.write(annotation.getDefinition());
					annotation.serialize(output, definition, typeContext);
				}
			});
		}
	}

	private void encodeMembers(ModuleContext moduleContext, HighLevelDefinition definition) {
		List<InnerDefinitionMember> innerDefinitions = new ArrayList<>();
		for (IDefinitionMember member : definition.members)
			if ((member instanceof InnerDefinitionMember))
				innerDefinitions.add((InnerDefinitionMember) member);

		output.writeUInt(innerDefinitions.size());
		for (InnerDefinitionMember innerDefinition : innerDefinitions) {
			System.out.println("Inner definition: " + innerDefinition.definition.name);
			output.serialize(innerDefinition.position);
			output.writeUInt(innerDefinition.getSpecifiedModifiers());
			innerDefinition.innerDefinition.accept(moduleContext, this);
		}

		output.enqueueMembers(output -> {
			DefinitionMemberSerializer memberEncoder = new DefinitionMemberSerializer(options, output);
			TypeContext context = new TypeContext(moduleContext, definition.typeParameters, moduleContext.registry.getForMyDefinition(definition));
			definition.accept(context, memberEncoder);
		});
	}

	@Override
	public Void visitClass(ModuleContext context, ClassDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_CLASS);
		visit(context, definition);
		encodeMembers(context, definition);
		return null;
	}

	@Override
	public Void visitInterface(ModuleContext context, InterfaceDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_INTERFACE);
		visit(context, definition);
		encodeMembers(context, definition);
		return null;
	}

	@Override
	public Void visitEnum(ModuleContext context, EnumDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_ENUM);
		visit(context, definition);
		encodeMembers(context, definition);
		return null;
	}

	@Override
	public Void visitStruct(ModuleContext context, StructDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_STRUCT);
		visit(context, definition);
		encodeMembers(context, definition);
		return null;
	}

	@Override
	public Void visitFunction(ModuleContext context, FunctionDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_FUNCTION);
		visit(context, definition);
		encodeMembers(context, definition);
		return null;
	}

	@Override
	public Void visitExpansion(ModuleContext context, ExpansionDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_EXPANSION);
		visit(context, definition);
		encodeMembers(context, definition);
		return null;
	}

	@Override
	public Void visitAlias(ModuleContext context, AliasDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_ALIAS);
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitVariant(ModuleContext context, VariantDefinition variant) {
		output.writeUInt(DefinitionEncoding.TYPE_VARIANT);
		visit(context, variant);
		encodeMembers(context, variant);
		return null;
	}
}
