package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface CodeSerializationInput {
	boolean readBool();

	int readByte();

	byte readSByte();

	short readShort();

	int readUShort();

	int readInt();

	int readUInt();

	long readLong();

	long readULong();

	float readFloat();

	double readDouble();

	char readChar();

	String readString();

	HighLevelDefinition readDefinition();

	DefinitionMemberRef readMember(TypeSerializationContext context, TypeID type);

	EnumConstantMember readEnumConstant(TypeSerializationContext context);

	VariantOptionInstance readVariantOption(TypeSerializationContext context, TypeID type);

	AnnotationDefinition readAnnotationType();

	TypeID deserializeType(TypeSerializationContext context);

	CodePosition deserializePosition();

	FunctionHeader deserializeHeader(TypeSerializationContext context);

	CallArguments deserializeArguments(StatementSerializationContext context);

	Statement deserializeStatement(StatementSerializationContext context);

	Expression deserializeExpression(StatementSerializationContext context);

	TypeParameter deserializeTypeParameter(TypeSerializationContext context);

	TypeParameter[] deserializeTypeParameters(TypeSerializationContext context);

	void enqueueMembers(DecodingOperation operation);

	public void enqueueCode(DecodingOperation operation);
}
