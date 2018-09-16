/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.EncodingOperation;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.moduleserialization.TypeParameterEncoding;
import org.openzen.zenscript.moduleserializer.encoder.DefinitionMemberSerializer;
import org.openzen.zenscript.moduleserializer.encoder.DefinitionSerializer;
import org.openzen.zenscript.moduleserializer.encoder.ExpressionSerializer;
import org.openzen.zenscript.moduleserializer.encoder.MemberSerializer;
import org.openzen.zenscript.moduleserializer.encoder.StatementSerializer;
import org.openzen.zenscript.moduleserializer.encoder.SwitchValueSerializer;
import org.openzen.zenscript.moduleserializer.encoder.TypeParameterBoundSerializer;
import org.openzen.zenscript.moduleserializer.encoder.TypeSerializer;

/**
 *
 * @author Hoofdgebruiker
 */
public class TableBuilder implements CodeSerializationOutput {
	private final Map<String, Integer> strings = new HashMap<>();
	private final Set<HighLevelDefinition> definitions = new HashSet<>();
	private final Set<Module> moduleSet = new HashSet<>();
	private final Set<SourceFile> sourceFiles = new HashSet<>();
	public final List<EncodingModule> modules = new ArrayList<>();
	private final List<IDefinitionMember> members = new ArrayList<>();
	
	private final SerializationOptions options;
	
	private final DefinitionSerializer definitionSerializer;
	private final DefinitionMemberSerializer definitionMemberSerializer;
	private final MemberSerializer memberSerializer;
	private final SwitchValueSerializer switchValues;
	private final StatementSerializer statements;
	private final ExpressionSerializer expressions;
	private final TypeSerializer typeSerializer;
	private final TypeParameterBoundSerializer typeParameterBoundSerializer;
	
	public TableBuilder(SerializationOptions options) {
		this.options = options;
		
		definitionSerializer = new DefinitionSerializer(options, this);
		definitionMemberSerializer = new DefinitionMemberSerializer(options, this);
		memberSerializer = new MemberSerializer(this, options);
		switchValues = new SwitchValueSerializer(this, options.localVariableNames);
		statements = new StatementSerializer(this, options.positions, options.localVariableNames);
		expressions = new ExpressionSerializer(this, options.positions, options.localVariableNames);
		typeSerializer = new TypeSerializer(this);
		typeParameterBoundSerializer = new TypeParameterBoundSerializer(this);
	}
	
	public EncodingModule register(Module module, ModuleContext context) {
		if (moduleSet.add(module)) {
			EncodingModule encodedModule = new EncodingModule(module, context, true);
			module.setTag(EncodingModule.class, encodedModule);
			modules.add(encodedModule);
			return encodedModule;
		} else {
			return module.getTag(EncodingModule.class);
		}
	}
	
	public SourceFile[] getSourceFileList() {
		return sourceFiles.toArray(new SourceFile[sourceFiles.size()]);
	}
	
	public String[] getStrings() {
		Map.Entry<String, Integer>[] entries = strings.entrySet().toArray(new Map.Entry[strings.size()]);
		Arrays.sort(entries, (a, b) -> b.getValue() - a.getValue());
		
		String[] result = new String[entries.length];
		for (int i = 0; i < result.length; i++)
			result[i] = entries[i].getKey();
		return result;
	}
	
	public List<IDefinitionMember> getMembers() {
		return members;
	}
	
	private EncodingDefinition prepare(HighLevelDefinition definition) {
		register(definition.module, null);
		
		if (definitions.add(definition)) {
			EncodingDefinition result = new EncodingDefinition(definition);
			definition.setTag(EncodingDefinition.class, result);
			return result;
		} else {
			EncodingDefinition result = definition.getTag(EncodingDefinition.class);
			if (result == null)
				throw new IllegalStateException("Definition not prepared: " + definition.name);
			return result;
		}
	}
	
	public void serialize(ModuleContext context, HighLevelDefinition definition) {
		definition.accept(context, definitionSerializer);
		ITypeID thisType = context.registry.getForMyDefinition(definition);
		definition.accept(new TypeContext(context, definition.typeParameters, thisType), definitionMemberSerializer);
	}

	@Override
	public void writeBool(boolean value) {}

	@Override
	public void writeByte(int value) {}

	@Override
	public void writeSByte(byte value) {}

	@Override
	public void writeShort(short value) {}

	@Override
	public void writeUShort(int value) {}

	@Override
	public void writeInt(int value) {}

	@Override
	public void writeUInt(int value) {}

	@Override
	public void writeLong(long value) {}

	@Override
	public void writeULong(long value) {}

	@Override
	public void writeFloat(float value) {}

	@Override
	public void writeDouble(double value) {}

	@Override
	public void writeChar(char value) {}

	@Override
	public void writeString(String value) {
		Integer count = strings.getOrDefault(value, 0);
		strings.put(value, count);
	}

	@Override
	public void write(HighLevelDefinition definition) {
		prepare(definition);
	}

	@Override
	public void write(EnumConstantMember constant) {
		prepare(constant.definition).mark(constant);
	}

	@Override
	public void write(VariantOptionRef option) {
		prepare(option.getOption().variant).mark(option.getOption());
	}

	@Override
	public void write(TypeContext context, DefinitionMemberRef member) {
		if (member != null && member.getTarget().getBuiltin() == null) {
			if (prepare(member.getTarget().getDefinition()).mark(member.getTarget()))
				members.add(member.getTarget());
		}
	}

	@Override
	public void serialize(TypeContext context, IDefinitionMember member) {
		member.accept(context, memberSerializer);
	}

	@Override
	public void serialize(TypeContext context, ITypeID type) {
		if (type != null)
			type.accept(context, typeSerializer);
	}

	@Override
	public void serialize(TypeContext context, TypeParameter parameter) {
		int typeParameterFlags = 0;
		if (parameter.position != CodePosition.UNKNOWN && options.positions)
			typeParameterFlags |= TypeParameterEncoding.FLAG_POSITION;
		if (parameter.name != null && options.typeParameterNames)
			typeParameterFlags |= TypeParameterEncoding.FLAG_NAME;
		if (!parameter.bounds.isEmpty())
			typeParameterFlags |= TypeParameterEncoding.FLAG_BOUNDS;

		if ((typeParameterFlags & TypeParameterEncoding.FLAG_POSITION) > 0)
			serialize(parameter.position);
		if ((typeParameterFlags & TypeParameterEncoding.FLAG_NAME) > 0)
			writeString(parameter.name);
		if ((typeParameterFlags & TypeParameterEncoding.FLAG_BOUNDS) > 0) {
			for (TypeParameterBound bound : parameter.bounds)
				bound.accept(context, typeParameterBoundSerializer);
		}
	}
	
	@Override
	public void serialize(TypeContext context, TypeParameter[] parameters) {
		TypeContext inner = new TypeContext(context, context.thisType, parameters);
		for (TypeParameter parameter : parameters)
			serialize(inner, parameter);
	}

	@Override
	public void serialize(CodePosition position) {
		sourceFiles.add(position.file);
	}

	@Override
	public void serialize(TypeContext context, FunctionHeader header) {
		serialize(context, header.typeParameters);
		serialize(context, header.getReturnType());
		
		StatementContext statementContext = new StatementContext(context, header);
		for (FunctionParameter parameter : header.parameters) {
			// TODO: annotations
			serialize(context, parameter.type);
			writeString(parameter.name == null ? "" : parameter.name);
			serialize(statementContext, parameter.defaultValue);
		}
	}

	@Override
	public void serialize(StatementContext context, CallArguments arguments) {
		for (ITypeID typeArgument : arguments.typeArguments)
			serialize(context, typeArgument);
		
		for (Expression expression : arguments.arguments)
			serialize(context, expression);
	}

	@Override
	public void serialize(StatementContext context, Statement statement) {
		if (statement != null)
			statement.accept(context, statements);
	}

	@Override
	public void serialize(StatementContext context, Expression expression) {
		if (expression != null)
			expression.accept(context, expressions);
	}

	@Override
	public void serialize(StatementContext context, SwitchValue value) {
		if (value != null)
			value.accept(context, switchValues);
	}

	@Override
	public void enqueueMembers(EncodingOperation operation) {
		operation.encode(this);
	}

	@Override
	public void enqueueCode(EncodingOperation operation) {
		operation.encode(this);
	}
}
