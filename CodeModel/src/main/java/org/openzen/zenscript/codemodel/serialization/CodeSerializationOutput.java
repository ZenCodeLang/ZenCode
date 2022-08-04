package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
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

	void write(TypeSymbol type);

	void write(EnumConstantMember constant);

	void write(VariantOptionInstance option);

	void write(TypeSerializationContext context, FieldSymbol field);

	void write(TypeSerializationContext context, MethodSymbol method);

	void write(AnnotationDefinition annotationType);

	void serialize(TypeSerializationContext context, IDefinitionMember member);

	void serialize(TypeSerializationContext context, TypeID type);

	void serialize(TypeSerializationContext context, TypeParameter parameter);

	void serialize(TypeSerializationContext context, TypeParameter[] parameters);

	void serialize(CodePosition position);

	void serialize(TypeSerializationContext context, FunctionHeader header);

	void serialize(StatementSerializationContext context, CallArguments arguments);

	void serialize(StatementSerializationContext context, Statement statement);

	void serialize(StatementSerializationContext context, Expression expression);

	void serialize(StatementSerializationContext context, SwitchValue value);

	void enqueueMembers(EncodingOperation operation);

	void enqueueCode(EncodingOperation operation);
}
