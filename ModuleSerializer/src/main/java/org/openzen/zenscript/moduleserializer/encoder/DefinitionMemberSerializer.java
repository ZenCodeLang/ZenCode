/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitorWithContext;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.moduleserialization.DefinitionEncoding;
import org.openzen.zenscript.moduleserializer.EncodingDefinition;
import org.openzen.zenscript.moduleserializer.EncodingModule;
import org.openzen.zenscript.moduleserializer.ModuleEncoder;
import org.openzen.zenscript.moduleserializer.SerializationOptions;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionMemberSerializer implements DefinitionVisitorWithContext<TypeContext, Void> {
	private final SerializationOptions options;
	private final CodeSerializationOutput output;
	
	public DefinitionMemberSerializer(SerializationOptions options, CodeSerializationOutput output) {
		this.options = options;
		this.output = output;
	}
	
	private EncodingDefinition visit(TypeContext context, HighLevelDefinition definition) {
		EncodingDefinition encoding = definition.getTag(EncodingDefinition.class);
		
		for (DefinitionAnnotation annotation : definition.annotations) {
			// TODO: how to serialize annotations?
		}
		
		output.serialize(context, definition.getSuperType());
		
		output.writeUInt(encoding.members.size());
		for (IDefinitionMember member : encoding.members)
			output.serialize(context, member);
		
		return encoding;
	}

	@Override
	public Void visitClass(TypeContext context, ClassDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_CLASS);
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitInterface(TypeContext context, InterfaceDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_INTERFACE);
		visit(context, definition);
		
		output.writeUInt(definition.baseInterfaces.size());
		for (ITypeID baseInterface : definition.baseInterfaces)
			output.serialize(context, baseInterface);
		
		return null;
	}

	@Override
	public Void visitEnum(TypeContext context, EnumDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_ENUM);
		visit(context, definition);
		
		output.writeUInt(definition.enumConstants.size());
		StatementContext initContext = new StatementContext(context);
		for (EnumConstantMember constant : definition.enumConstants) {
			int flags = 0;
			if (constant.position != CodePosition.UNKNOWN && options.positions)
				flags |= DefinitionEncoding.FLAG_POSITION;
			
			output.writeUInt(flags);
			output.writeString(constant.name);
			if ((flags & DefinitionEncoding.FLAG_POSITION) > 0)
				output.serialize(constant.position);
			
			output.enqueueCode(output -> {
				output.write(context, constant.constructor.constructor);
				output.serialize(initContext, constant.constructor.arguments);
			});
		}
		return null;
	}

	@Override
	public Void visitStruct(TypeContext context, StructDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_STRUCT);
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitFunction(TypeContext context, FunctionDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_FUNCTION);
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitExpansion(TypeContext context, ExpansionDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_EXPANSION);
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitAlias(TypeContext context, AliasDefinition definition) {
		output.writeUInt(DefinitionEncoding.TYPE_ALIAS);
		
		EncodingDefinition encoding = definition.getTag(EncodingDefinition.class);
		for (DefinitionAnnotation annotation : definition.annotations) {
			// TODO: how to serialize annotations?
		}
		
		output.serialize(context, definition.type);
		return null;
	}

	@Override
	public Void visitVariant(TypeContext context, VariantDefinition variant) {
		output.writeUInt(DefinitionEncoding.TYPE_VARIANT);
		visit(context, variant);
		
		output.writeUInt(variant.options.size());
		for (VariantDefinition.Option option : variant.options) {
			output.writeString(option.name);
			output.writeUInt(option.types.length);
			for (ITypeID type : option.types)
				output.serialize(context, type);
		}
		return null;
	}
}
