package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface CodeSerializationOutput {
	void writeBool(boolean value);
	
	void writeByte(int value);
	
	void writeSByte(byte value);
	
	void writeShort(short value);
	
	void writeUShort(int value);
	
	void writeInt(int value);
	
	void writeUInt(int value);
	
	void writeLong(long value);
	
	void writeULong(long value);
	
	void writeFloat(float value);
	
	void writeDouble(double value);
	
	void writeChar(char value);
	
	void writeString(String value);
	
	void write(HighLevelDefinition definition);
	
	void write(EnumConstantMember constant);
	
	void write(VariantOptionRef option);
	
	void write(TypeContext context, DefinitionMemberRef member);
	
	void write(AnnotationDefinition annotationType);
	
	void serialize(TypeContext context, IDefinitionMember member);
	
	void serialize(TypeContext context, TypeID type);
	
	void serialize(TypeContext context, TypeParameter parameter);
	
	void serialize(TypeContext context, TypeParameter[] parameters);
	
	void serialize(CodePosition position);
	
	void serialize(TypeContext context, FunctionHeader header);
	
	void serialize(StatementContext context, CallArguments arguments);
	
	void serialize(StatementContext context, Statement statement);
	
	void serialize(StatementContext context, Expression expression);
	
	void serialize(StatementContext context, SwitchValue value);
	
	void enqueueMembers(EncodingOperation operation);
	
	void enqueueCode(EncodingOperation operation);
}
