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
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.moduleserialization.DefinitionEncoding;
import org.openzen.zenscript.moduleserializer.EncodingDefinition;
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
		
		output.serialize(context, definition.getSuperType());
		
		output.writeUInt(encoding.members.size());
		for (IDefinitionMember member : encoding.members)
			output.serialize(context, member);
		
		return encoding;
	}

	@Override
	public Void visitClass(TypeContext context, ClassDefinition definition) {
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitInterface(TypeContext context, InterfaceDefinition definition) {
		visit(context, definition);
		
		output.writeUInt(definition.baseInterfaces.size());
		for (TypeID baseInterface : definition.baseInterfaces)
			output.serialize(context, baseInterface);
		
		return null;
	}

	@Override
	public Void visitEnum(TypeContext context, EnumDefinition definition) {
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
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitFunction(TypeContext context, FunctionDefinition definition) {
		visit(context, definition);
		return null;
	}

	@Override
	public Void visitExpansion(TypeContext context, ExpansionDefinition definition) {
		visit(context, definition);
		output.serialize(context, definition.target);
		return null;
	}

	@Override
	public Void visitAlias(TypeContext context, AliasDefinition definition) {
		output.serialize(context, definition.type);
		return null;
	}

	@Override
	public Void visitVariant(TypeContext context, VariantDefinition variant) {
		visit(context, variant);
		
		output.writeUInt(variant.options.size());
		for (VariantDefinition.Option option : variant.options) {
			output.writeString(option.name);
			output.writeUInt(option.types.length);
			for (TypeID type : option.types)
				output.serialize(context, type);
		}
		return null;
	}
}
