/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import compactio.CompactDataOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.moduleserialization.SwitchValueEncoding;
import org.openzen.zenscript.moduleserialization.TypeEncoding;
import org.openzen.zenscript.moduleserialization.TypeParameterEncoding;
import org.openzen.zenscript.moduleserializer.encoder.ExpressionSerializer;
import org.openzen.zenscript.moduleserializer.encoder.MemberSerializer;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.moduleserializer.encoder.StatementSerializer;
import org.openzen.zenscript.moduleserializer.encoder.SwitchValueSerializer;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.serialization.EncodingOperation;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.moduleserialization.CodePositionEncoding;
import org.openzen.zenscript.moduleserialization.FunctionHeaderEncoding;
import org.openzen.zenscript.moduleserializer.encoder.TypeParameterBoundSerializer;
import org.openzen.zenscript.moduleserializer.encoder.TypeSerializer;

/**
 *
 * @author Hoofdgebruiker
 */
public class CodeWriter implements CodeSerializationOutput {
	private final Map<String, Integer> stringMap = new HashMap<>();
	private final Map<SourceFile, Integer> sourceFileMap = new HashMap<>();
	private final Map<AnnotationDefinition, Integer> annotationMap = new HashMap<>();
	private final Map<HighLevelDefinition, Integer> definitionsMap = new HashMap<>();
	
	private final Map<IDefinitionMember, Integer> memberMap = new HashMap<>();
	
	public final EncodingStage classes = new EncodingStage();
	public final EncodingStage members = new EncodingStage();
	public final EncodingStage code = new EncodingStage();
	
	private EncodingStage currentStage = null;
	
	public final CompactDataOutput output;
	public final SerializationOptions options;
	private final TypeParameterBoundSerializer typeParameterEncoder;
	private final MemberSerializer memberSerializer;
	private final TypeSerializer typeSerializer;
	private final StatementSerializer statementSerializer;
	private final ExpressionSerializer expressionSerializer;
	private final SwitchValueSerializer switchValueSerializer;
	
	private CodePosition lastPosition = CodePosition.UNKNOWN;
	
	public CodeWriter(
			CompactDataOutput output,
			SerializationOptions options,
			String[] strings,
			SourceFile[] sourceFiles,
			HighLevelDefinition[] definitions,
			List<IDefinitionMember> members,
			AnnotationDefinition[] annotations)
	{
		this.output = output;
		this.options = options;
		typeParameterEncoder = new TypeParameterBoundSerializer(this);
		memberSerializer = new MemberSerializer(this, options);
		typeSerializer = new TypeSerializer(this);
		statementSerializer = new StatementSerializer(this, options.positions, options.localVariableNames);
		expressionSerializer = new ExpressionSerializer(this, options.expressionPositions, options.localVariableNames);
		switchValueSerializer = new SwitchValueSerializer(this, options.localVariableNames);
		
		for (String string : strings)
			stringMap.put(string, stringMap.size());
		for (SourceFile sourceFile : sourceFiles)
			sourceFileMap.put(sourceFile, sourceFileMap.size());
		for (HighLevelDefinition definition : definitions)
			definitionsMap.put(definition, definitionsMap.size());
		for (IDefinitionMember member : members)
			memberMap.put(member, memberMap.size());
		for (AnnotationDefinition annotation : annotations)
			annotationMap.put(annotation, annotationMap.size());
	}
	
	public void startClasses() {
		currentStage = classes;
	}
	
	public void startMembers() {
		currentStage = members;
	}
	
	public void startCode() {
		currentStage = code;
	}
	
	public void add(SourceFile file) {
		sourceFileMap.put(file, sourceFileMap.size());
	}
	
	public int getSourceFileId(SourceFile file) {
		return sourceFileMap.get(file);
	}
	
	@Override
	public void writeBool(boolean value) {
		output.writeBool(value);
	}
	
	@Override
	public void writeByte(int value) {
		output.writeByte(value);
	}
	
	@Override
	public void writeSByte(byte value) {
		output.writeSByte(value);
	}
	
	@Override
	public void writeShort(short value) {
		output.writeShort(value);
	}
	
	@Override
	public void writeUShort(int value) {
		output.writeUShort(value);
	}
	
	@Override
	public void writeInt(int value) {
		output.writeVarInt(value);
	}
	
	@Override
	public void writeUInt(int value) {
		output.writeVarUInt(value);
	}
	
	@Override
	public void writeLong(long value) {
		output.writeVarLong(value);
	}
	
	@Override
	public void writeULong(long value) {
		output.writeVarULong(value);
	}
	
	@Override
	public void writeFloat(float value) {
		output.writeFloat(value);
	}
	
	@Override
	public void writeDouble(double value) {
		output.writeDouble(value);
	}
	
	@Override
	public void writeChar(char value) {
		output.writeChar(value);
	}
	
	@Override
	public void writeString(String value) {
		Integer index = stringMap.get(value);
		if (index == null)
			throw new IllegalArgumentException("String missing in string table: " + value);
		
		output.writeVarUInt(index);
	}
	
	@Override
	public void write(HighLevelDefinition definition) {
		if (currentStage != members && currentStage != code)
			throw new IllegalStateException("definitions not yet available!");
		if (!definitionsMap.containsKey(definition))
			throw new IllegalStateException("Definition not yet prepared: " + definition.name);
		
		int id = definitionsMap.get(definition) + 1;
		output.writeVarUInt(id);
	}
	
	@Override
	public void write(TypeContext context, DefinitionMemberRef member) {
		if (currentStage != code)
			throw new IllegalStateException("members not yet available!");
		
		if (member == null) {
			writeInt(0);
			return;
		} else if (member.getTarget().getBuiltin() != null) {
			writeInt(-member.getTarget().getBuiltin().ordinal() - 1); // TODO: use something else?
			return;
		}
		
		IDefinitionMember member_ = member.getTarget();
		Integer index = memberMap.get(member_);
		if (index == null)
			throw new IllegalStateException("Member not registered!");
		writeInt(index + 1);
	}
	
	@Override
	public void write(EnumConstantMember constant) {
		HighLevelDefinition definition = constant.definition;
		write(definition);
		writeUInt(definition.getTag(EncodingDefinition.class).enumConstants.indexOf(constant));
	}
	
	@Override
	public void write(VariantOptionRef option) {
		HighLevelDefinition definition = option.getOption().variant;
		write(definition);
		writeUInt(definition.getTag(EncodingDefinition.class).variantOptions.indexOf(option));
	}
	
	@Override
	public void write(AnnotationDefinition annotationType) {
		writeUInt(annotationMap.get(annotationType));
	}
	
	@Override
	public void serialize(TypeContext context, TypeID type) {
		if (type == null) {
			writeUInt(TypeEncoding.TYPE_NONE);
		} else {
			type.accept(context, typeSerializer);
		}
	}
	
	@Override
	public void serialize(TypeContext context, TypeParameter parameter) {
		int flags = serializeInitial(parameter);
		serializeRemaining(flags, context, parameter);
	}
	
	@Override
	public void serialize(TypeContext context, TypeParameter[] parameters) {
		writeUInt(parameters.length);
		int[] flags = new int[parameters.length];
		for (int i = 0; i < parameters.length; i++)
			flags[i] = serializeInitial(parameters[i]);
		
		TypeContext inner = new TypeContext(context, context.thisType, parameters);
		for (int i = 0; i < parameters.length; i++)
			serializeRemaining(flags[i], inner, parameters[i]);
	}
	
	private int serializeInitial(TypeParameter parameter) {
		int flags = 0;
		if (parameter.position != CodePosition.UNKNOWN && options.positions)
			flags |= TypeParameterEncoding.FLAG_POSITION;
		if (parameter.name != null && options.typeParameterNames)
			flags |= TypeParameterEncoding.FLAG_NAME;

		output.writeVarUInt(flags);
		if ((flags & TypeParameterEncoding.FLAG_POSITION) > 0)
			serialize(parameter.position);
		if ((flags & TypeParameterEncoding.FLAG_NAME) > 0)
			writeString(parameter.name);
		
		return flags;
	}
	
	private void serializeRemaining(int flags, TypeContext context, TypeParameter parameter) {
		if (currentStage == code || currentStage == members) {
			if ((flags & TypeParameterEncoding.FLAG_BOUNDS) > 0) {
				writeUInt(parameter.bounds.size());
				for (TypeParameterBound bound : parameter.bounds)
					bound.accept(context, typeParameterEncoder);
			}
		} else {
			members.enqueue(encoder -> {
				if ((flags & TypeParameterEncoding.FLAG_BOUNDS) > 0) {
					output.writeVarUInt(parameter.bounds.size());
					for (TypeParameterBound bound : parameter.bounds)
						bound.accept(context, typeParameterEncoder);
				}
			});
		}
	}
	
	@Override
	public void serialize(TypeContext context, IDefinitionMember member) {
		if (member == null) {
			output.writeVarUInt(0);
		} else {
			member.accept(context, memberSerializer);
		}
	}
	
	@Override
	public void serialize(StatementContext context, Statement statement) {
		if (statement == null) {
			output.writeVarUInt(0);
		} else {
			statement.accept(context, statementSerializer);
		}
	}
	
	@Override
	public void serialize(StatementContext context, Expression expression) {
		if (expression == null) {
			output.writeVarUInt(0);
		} else {
			expression.accept(context, expressionSerializer);
		}
	}
	
	@Override
	public void serialize(CodePosition position) {
		int flags = 0;
		if (position.file != lastPosition.file)
			flags |= CodePositionEncoding.FLAG_FILE;
		if (position.fromLine != lastPosition.fromLine)
			flags |= CodePositionEncoding.FLAG_FROM_LINE;
		if (options.positionOffsets && position.fromLineOffset != lastPosition.fromLineOffset)
			flags |= CodePositionEncoding.FLAG_FROM_OFFSET;
		if (position.toLine != position.fromLine)
			flags |= CodePositionEncoding.FLAG_TO_LINE;
		if (options.positionOffsets && position.toLineOffset != lastPosition.fromLineOffset)
			flags |= CodePositionEncoding.FLAG_TO_OFFSET;
		
		output.writeVarUInt(flags);
		if ((flags & CodePositionEncoding.FLAG_FILE) > 0)
			output.writeVarUInt(sourceFileMap.get(position.file));
		if ((flags & CodePositionEncoding.FLAG_FROM_LINE) > 0)
			output.writeVarUInt(position.fromLine);
		if ((flags & CodePositionEncoding.FLAG_FROM_OFFSET) > 0)
			output.writeVarUInt(position.fromLineOffset);
		if ((flags & CodePositionEncoding.FLAG_TO_LINE) > 0)
			output.writeVarUInt(position.toLine - position.fromLine);
		if ((flags & CodePositionEncoding.FLAG_TO_OFFSET) > 0)
			output.writeVarUInt(position.toLineOffset);
		
		lastPosition = position;
	}
	
	@Override
	public void serialize(TypeContext context, FunctionHeader header) {
		int flags = 0;
		if (header.typeParameters.length > 0)
			flags |= FunctionHeaderEncoding.FLAG_TYPE_PARAMETERS;
		if (!header.getReturnType().isBasic(BasicTypeID.VOID))
			flags |= FunctionHeaderEncoding.FLAG_RETURN_TYPE;
		if (header.thrownType != null)
			flags |= FunctionHeaderEncoding.FLAG_THROWS;
		if (header.parameters.length > 0)
			flags |= FunctionHeaderEncoding.FLAG_PARAMETERS;
		if (header.parameters.length > 0 && header.parameters[header.parameters.length - 1].variadic)
			flags |= FunctionHeaderEncoding.FLAG_VARIADIC;
		if (header.hasAnyDefaultValues())
			flags |= FunctionHeaderEncoding.FLAG_DEFAULT_VALUES;
		
		writeUInt(flags);
		if ((flags & FunctionHeaderEncoding.FLAG_TYPE_PARAMETERS) > 0)
			serialize(context, header.typeParameters);
		if ((flags & FunctionHeaderEncoding.FLAG_RETURN_TYPE) > 0)
			serialize(context, header.getReturnType());
		if ((flags & FunctionHeaderEncoding.FLAG_THROWS) > 0)
			serialize(context, header.thrownType);
		
		if ((flags & FunctionHeaderEncoding.FLAG_PARAMETERS) > 0) {
			StatementContext statementContext = new StatementContext(context);
			writeUInt(header.parameters.length);
			for (FunctionParameter parameter : header.parameters) {
				// TODO: annotations
				serialize(context, parameter.type);
				writeString(parameter.name == null ? "" : parameter.name);
				
				if ((flags & FunctionHeaderEncoding.FLAG_DEFAULT_VALUES) > 0) {
					if (currentStage == code) {
						serialize(statementContext, parameter.defaultValue);
					} else {
						code.enqueue(encoder -> serialize(statementContext, parameter.defaultValue));
					}
				}
			}
		}
	}
	
	@Override
	public void serialize(StatementContext context, CallArguments arguments) {
		output.writeVarUInt(arguments.typeArguments.length);
		for (TypeID typeArgument : arguments.typeArguments)
			serialize(context, typeArgument);
		
		output.writeVarUInt(arguments.arguments.length);
		for (Expression expression : arguments.arguments)
			serialize(context, expression);
	}
	
	@Override
	public void serialize(StatementContext context, SwitchValue value) {
		if (value == null) {
			output.writeVarUInt(SwitchValueEncoding.TYPE_NULL);
		} else {
			value.accept(context, switchValueSerializer);
		}
	}
	
	@Override
	public void enqueueMembers(EncodingOperation operation) {
		members.enqueue(operation);
	}
	
	@Override
	public void enqueueCode(EncodingOperation operation) {
		code.enqueue(operation);
	}
}
