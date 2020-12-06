package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
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

	DefinitionMemberRef readMember(TypeContext context, TypeID type);

	EnumConstantMember readEnumConstant(TypeContext context);

	VariantOptionRef readVariantOption(TypeContext context, TypeID type);

	AnnotationDefinition readAnnotationType();

	TypeID deserializeType(TypeContext context);

	CodePosition deserializePosition();

	FunctionHeader deserializeHeader(TypeContext context);

	CallArguments deserializeArguments(StatementContext context);

	Statement deserializeStatement(StatementContext context);

	Expression deserializeExpression(StatementContext context);

	TypeParameter deserializeTypeParameter(TypeContext context);

	TypeParameter[] deserializeTypeParameters(TypeContext context);

	void enqueueMembers(DecodingOperation operation);

	public void enqueueCode(DecodingOperation operation);
}
