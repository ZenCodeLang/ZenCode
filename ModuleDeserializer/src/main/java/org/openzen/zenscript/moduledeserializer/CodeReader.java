/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduledeserializer;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ref.*;
import org.openzen.zenscript.codemodel.serialization.DecodingOperation;
import compactio.CompactDataInput;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.moduleserialization.CodePositionEncoding;
import org.openzen.zenscript.moduleserialization.ExpressionEncoding;
import org.openzen.zenscript.moduleserialization.FunctionHeaderEncoding;
import org.openzen.zenscript.moduleserialization.StatementEncoding;
import org.openzen.zenscript.moduleserialization.SwitchValueEncoding;
import org.openzen.zenscript.moduleserialization.TypeEncoding;
import org.openzen.zenscript.moduleserialization.TypeParameterEncoding;

/**
 * @author Hoofdgebruiker
 */
public class CodeReader implements CodeSerializationInput {
	public final DecodingStage classes = new DecodingStage();
	public final DecodingStage members = new DecodingStage();
	public final DecodingStage code = new DecodingStage();
	private final CompactDataInput input;
	private final String[] strings;
	private final SourceFile[] sourceFiles;
	private final AnnotationDefinition[] annotations;
	private final List<HighLevelDefinition> definitions = new ArrayList<>();
	private final List<IDefinitionMember> memberList = new ArrayList<>();
	private final List<EnumConstantMember> enumConstantMembers = new ArrayList<>();
	private final List<VariantDefinition.Option> variantOptions = new ArrayList<>();
	private DecodingStage currentStage = null;
	private CodePosition lastPosition = CodePosition.UNKNOWN;

	public CodeReader(
			CompactDataInput input,
			String[] strings,
			SourceFile[] sourceFiles,
			AnnotationDefinition[] annotations) {
		this.input = input;

		this.strings = strings;
		this.sourceFiles = sourceFiles;
		this.annotations = annotations;
	}

	public void add(HighLevelDefinition definition) {
		definitions.add(definition);
	}

	public void add(IDefinitionMember member) {
		memberList.add(member);
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

	@Override
	public boolean readBool() {
		return input.readBool();
	}

	@Override
	public int readByte() {
		return input.readByte();
	}

	@Override
	public byte readSByte() {
		return input.readSByte();
	}

	@Override
	public short readShort() {
		return input.readShort();
	}

	@Override
	public int readUShort() {
		return input.readUShort();
	}

	@Override
	public int readInt() {
		return input.readVarInt();
	}

	@Override
	public int readUInt() {
		return input.readVarUInt();
	}

	@Override
	public long readLong() {
		return input.readVarLong();
	}

	@Override
	public long readULong() {
		return input.readVarULong();
	}

	@Override
	public float readFloat() {
		return input.readFloat();
	}

	@Override
	public double readDouble() {
		return input.readDouble();
	}

	@Override
	public char readChar() {
		return input.readChar();
	}

	@Override
	public String readString() {
		int index = input.readVarUInt();
		if (index >= strings.length)
			throw new RuntimeException("Invalid string id");

		return strings[index];
	}

	@Override
	public HighLevelDefinition readDefinition() {
		int id = input.readVarUInt();
		if (id == 0)
			return null;

		HighLevelDefinition definition = definitions.get(id - 1);
		return definition;
	}

	@Override
	public DefinitionMemberRef readMember(TypeSerializationContext context, TypeID type) {
		int memberId = input.readVarUInt();
		if (memberId == 0) {
			return null;
		} else {
			return memberList.get(memberId - 2).ref(type, context.getMapper());
		}
	}

	@Override
	public EnumConstantMember readEnumConstant(TypeSerializationContext context) {
		return enumConstantMembers.get(readUInt());
	}

	@Override
	public VariantOptionInstance readVariantOption(TypeSerializationContext context, TypeID type) {
		return variantOptions.get(readUInt()).instance(type, context.getMapper());
	}

	@Override
	public AnnotationDefinition readAnnotationType() {
		return annotations[readUInt()];
	}

	@Override
	public TypeID deserializeType(TypeSerializationContext context) {
		int type = input.readVarUInt();
		switch (type) {
			case TypeEncoding.TYPE_NONE:
				return null;
			case TypeEncoding.TYPE_VOID:
				return BasicTypeID.VOID;
			case TypeEncoding.TYPE_BOOL:
				return BasicTypeID.BOOL;
			case TypeEncoding.TYPE_BYTE:
				return BasicTypeID.BYTE;
			case TypeEncoding.TYPE_SBYTE:
				return BasicTypeID.SBYTE;
			case TypeEncoding.TYPE_SHORT:
				return BasicTypeID.SHORT;
			case TypeEncoding.TYPE_USHORT:
				return BasicTypeID.USHORT;
			case TypeEncoding.TYPE_INT:
				return BasicTypeID.INT;
			case TypeEncoding.TYPE_UINT:
				return BasicTypeID.UINT;
			case TypeEncoding.TYPE_LONG:
				return BasicTypeID.LONG;
			case TypeEncoding.TYPE_ULONG:
				return BasicTypeID.ULONG;
			case TypeEncoding.TYPE_USIZE:
				return BasicTypeID.USIZE;
			case TypeEncoding.TYPE_FLOAT:
				return BasicTypeID.FLOAT;
			case TypeEncoding.TYPE_DOUBLE:
				return BasicTypeID.DOUBLE;
			case TypeEncoding.TYPE_CHAR:
				return BasicTypeID.CHAR;
			case TypeEncoding.TYPE_STRING:
				return BasicTypeID.STRING;
			case TypeEncoding.TYPE_UNDETERMINED:
				return BasicTypeID.UNDETERMINED;
			case TypeEncoding.TYPE_DEFINITION: {
				HighLevelDefinition definition = readDefinition();
				TypeID[] arguments = new TypeID[definition.typeParameters.length];
				for (int i = 0; i < arguments.length; i++)
					arguments[i] = deserializeType(context);
				return registry.getForDefinition(definition, arguments);
			}
			case TypeEncoding.TYPE_DEFINITION_INNER: {
				DefinitionTypeID outer = (DefinitionTypeID) deserializeType(context);
				HighLevelDefinition definition = readDefinition();
				TypeID[] arguments = new TypeID[definition.typeParameters.length];
				for (int i = 0; i < arguments.length; i++)
					arguments[i] = deserializeType(context);
				return registry.getForDefinition(definition, arguments, outer);
			}
			case TypeEncoding.TYPE_GENERIC:
				return registry.getGeneric(context.getTypeParameter(input.readVarUInt()));
			case TypeEncoding.TYPE_FUNCTION:
				return registry.getFunction(deserializeHeader(context));
			case TypeEncoding.TYPE_ARRAY:
				return registry.getArray(deserializeType(context), 1);
			case TypeEncoding.TYPE_ARRAY_MULTIDIMENSIONAL: {
				TypeID baseType = deserializeType(context);
				int dimension = input.readVarUInt();
				return registry.getArray(baseType, dimension);
			}
			case TypeEncoding.TYPE_ASSOC: {
				TypeID key = deserializeType(context);
				TypeID value = deserializeType(context);
				return registry.getAssociative(key, value);
			}
			case TypeEncoding.TYPE_GENERIC_MAP: {
				TypeParameter parameter = deserializeTypeParameter(context);
				TypeSerializationContext inner = new TypeSerializationContext(context, context.thisType, new TypeParameter[] { parameter });
				TypeID value = deserializeType(inner);
				return registry.getGenericMap(value, parameter);
			}
			case TypeEncoding.TYPE_RANGE:
				return registry.getRange(deserializeType(context));
			case TypeEncoding.TYPE_ITERATOR: {
				TypeID[] values = new TypeID[input.readVarUInt()];
				for (int i = 0; i < values.length; i++)
					values[i] = deserializeType(context);
				return registry.getIterator(values);
			}
			case TypeEncoding.TYPE_OPTIONAL:
				return registry.getOptional(deserializeType(context));
			default:
				throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	@Override
	public CodePosition deserializePosition() {
		int flags = readUInt();
		SourceFile file = lastPosition.file;
		int fromLine = lastPosition.fromLine;
		int fromLineOffset = lastPosition.fromLineOffset;
		if ((flags & CodePositionEncoding.FLAG_FILE) > 0) {
			int fileIndex = input.readVarUInt();
			if (fileIndex >= sourceFiles.length)
				throw new IllegalArgumentException("Invalid file index: " + fileIndex);

			file = sourceFiles[fileIndex];
		}
		if ((flags & CodePositionEncoding.FLAG_FROM_LINE) > 0)
			fromLine = input.readVarUInt();
		if ((flags & CodePositionEncoding.FLAG_FROM_OFFSET) > 0)
			fromLineOffset = input.readVarUInt();
		int toLine = fromLine;
		if ((flags & CodePositionEncoding.FLAG_TO_LINE) > 0)
			toLine = fromLine + input.readVarUInt();
		int toLineOffset = lastPosition.toLineOffset;
		if ((flags & CodePositionEncoding.FLAG_TO_OFFSET) > 0)
			toLineOffset = input.readVarUInt();
		return lastPosition = new CodePosition(file, fromLine, fromLineOffset, toLine, toLineOffset);
	}

	@Override
	public FunctionHeader deserializeHeader(TypeSerializationContext context) {
		TypeParameter[] typeParameters = TypeParameter.NONE;

		int flags = input.readVarUInt();
		TypeSerializationContext inner;
		if ((flags & FunctionHeaderEncoding.FLAG_TYPE_PARAMETERS) > 0) {
			typeParameters = deserializeTypeParameters(context);
			inner = new TypeSerializationContext(context, context.thisType, typeParameters);
		} else {
			inner = context;
		}

		TypeID returnType = BasicTypeID.VOID;
		if ((flags & FunctionHeaderEncoding.FLAG_RETURN_TYPE) > 0)
			returnType = deserializeType(inner);

		TypeID thrownType = null;
		if ((flags & FunctionHeaderEncoding.FLAG_THROWS) > 0)
			thrownType = deserializeType(inner);

		FunctionParameter[] parameters = FunctionParameter.NONE;
		if ((flags & FunctionHeaderEncoding.FLAG_PARAMETERS) > 0) {
			parameters = new FunctionParameter[readUInt()];
			StatementSerializationContext statementContext = context.forMethod();
			for (int i = 0; i < parameters.length; i++) {
				TypeID type = deserializeType(inner);
				String name = readString();
				FunctionParameter parameter = new FunctionParameter(type, name, null, (i == parameters.length - 1) && ((flags & FunctionHeaderEncoding.FLAG_VARIADIC) > 0));
				parameters[i] = parameter;

				if ((flags & FunctionHeaderEncoding.FLAG_DEFAULT_VALUES) > 0) {
					if (currentStage == code) {
						parameters[i].defaultValue = deserializeExpression(statementContext);
					} else {
						enqueueCode(decoder -> parameter.defaultValue = deserializeExpression(statementContext));
					}
				}
			}
		}
		return new FunctionHeader(typeParameters, returnType, thrownType, parameters);
	}

	@Override
	public TypeParameter deserializeTypeParameter(TypeSerializationContext context) {
		int flags = readUInt();
		CodePosition position = CodePosition.UNKNOWN;
		String name = null;
		if ((flags & TypeParameterEncoding.FLAG_POSITION) > 0)
			position = deserializePosition();
		if ((flags & TypeParameterEncoding.FLAG_NAME) > 0)
			name = readString();

		TypeParameter result = new TypeParameter(position, name);
		TypeSerializationContext inner = new TypeSerializationContext(context, context.thisType, new TypeParameter[] { result });
		if ((flags & TypeParameterEncoding.FLAG_BOUNDS) > 0) {
			int bounds = readUInt();
			for (int i = 0; i < bounds; i++)
				result.bounds.add(deserializeTypeParameterBound(inner));
		}

		return result;
	}

	@Override
	public TypeParameter[] deserializeTypeParameters(TypeSerializationContext context) {
		TypeParameter[] result = new TypeParameter[readUInt()];
		int[] allflags = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			int flags = readUInt();
			allflags[i] = flags;

			CodePosition position = CodePosition.UNKNOWN;
			String name = null;
			if ((flags & TypeParameterEncoding.FLAG_POSITION) > 0)
				position = deserializePosition();
			if ((flags & TypeParameterEncoding.FLAG_NAME) > 0)
				name = readString();

			result[i] = new TypeParameter(position, name);
		}

		if (currentStage == code || currentStage == members) {
			readTypeParameterBounds(context, allflags, result);
		} else {
			members.enqueue(input -> readTypeParameterBounds(context, allflags, result));
		}

		return result;
	}

	private void readTypeParameterBounds(TypeSerializationContext context, int[] allFlags, TypeParameter[] result) {
		TypeSerializationContext inner = new TypeSerializationContext(context, context.thisType, result);
		for (int i = 0; i < result.length; i++) {
			int flags = allFlags[i];
			if ((flags & TypeParameterEncoding.FLAG_BOUNDS) > 0) {
				int bounds = readUInt();
				for (int j = 0; j < bounds; j++)
					result[i].bounds.add(deserializeTypeParameterBound(inner));
			}
		}
	}

	@Override
	public CallArguments deserializeArguments(StatementSerializationContext context) {
		TypeID[] expansionTypeArguments = new TypeID[readUInt()];
		for (int i = 0; i < expansionTypeArguments.length; i++)
			expansionTypeArguments[i] = deserializeType(context.types());

		TypeID[] typeArguments = new TypeID[readUInt()];
		for (int i = 0; i < typeArguments.length; i++)
			typeArguments[i] = deserializeType(context.types());

		Expression[] arguments = new Expression[readUInt()];
		for (int i = 0; i < arguments.length; i++)
			arguments[i] = deserializeExpression(context);

		return new CallArguments(expansionTypeArguments, typeArguments, arguments);
	}

	@Override
	public Statement deserializeStatement(StatementSerializationContext context) {
		int type = readUInt();
		int flags = type == StatementEncoding.TYPE_NULL ? 0 : readUInt();
		CodePosition position = (flags & StatementEncoding.FLAG_POSITION) > 0 ? deserializePosition() : CodePosition.UNKNOWN;
		switch (type) {
			case StatementEncoding.TYPE_NULL:
				return new EmptyStatement(position);
			case StatementEncoding.TYPE_BLOCK: {
				Statement[] statements = new Statement[readUInt()];
				StatementSerializationContext inner = context.forBlock();
				for (int i = 0; i < statements.length; i++)
					statements[i] = deserializeStatement(inner);
				return new BlockStatement(position, statements);
			}
			case StatementEncoding.TYPE_BREAK: {
				return context.getLoop(readUInt())
						.map(loop -> (Statement)new BreakStatement(position, loop))
						.orElseGet(() -> new InvalidStatement(position, CompileErrors.deserializationError("invalid loop reference")));
			}
			case StatementEncoding.TYPE_CONTINUE: {
				return context.getLoop(readUInt())
						.map(loop -> (Statement)new ContinueStatement(position, loop))
						.orElseGet(() -> new InvalidStatement(position, CompileErrors.deserializationError("invalid loop reference")));
			}
			case StatementEncoding.TYPE_DO_WHILE: {
				Expression condition = deserializeExpression(context);
				String label = null;
				if ((flags & StatementEncoding.FLAG_LABEL) > 0)
					label = readString();
				DoWhileStatement loop = new DoWhileStatement(position, label, condition);
				StatementSerializationContext inner = context.forLoop(loop);
				loop.content = deserializeStatement(inner);
				return loop;
			}
			case StatementEncoding.TYPE_EMPTY:
				return new EmptyStatement(position);
			case StatementEncoding.TYPE_EXPRESSION: {
				Expression expression = deserializeExpression(context);
				return new ExpressionStatement(position, expression);
			}
			case StatementEncoding.TYPE_FOREACH: {
				Expression list = deserializeExpression(context);
				IteratorInstance iterator = (IteratorInstance) readMember(context.types(), list.type);
				VarStatement[] loopVariables = new VarStatement[iterator.getLoopVariableCount()];
				for (int i = 0; i < loopVariables.length; i++) {
					String name = ((flags & StatementEncoding.FLAG_NAME) > 0) ? readString() : null;
					loopVariables[i] = new VarStatement(position, new VariableID(), name, iterator.types[i], null, true);
				}
				ForeachStatement loop = new ForeachStatement(position, loopVariables, iterator, list);
				StatementSerializationContext inner = context.forLoop(loop);
				for (VarStatement variable : loopVariables)
					inner.add(variable);

				loop.content = deserializeStatement(inner);
				return loop;
			}
			case StatementEncoding.TYPE_IF: {
				Expression condition = deserializeExpression(context);
				Statement onThen = deserializeStatement(context);
				Statement onElse = deserializeStatement(context);
				return new IfStatement(position, condition, onThen, onElse);
			}
			case StatementEncoding.TYPE_LOCK: {
				Expression object = deserializeExpression(context);
				Statement content = deserializeStatement(context);
				return new LockStatement(position, object, content);
			}
			case StatementEncoding.TYPE_RETURN: {
				Expression value = deserializeExpression(context);
				return new ReturnStatement(position, value);
			}
			case StatementEncoding.TYPE_SWITCH: {
				Expression value = deserializeExpression(context);
				String label = ((flags & StatementEncoding.FLAG_LABEL) > 0) ? readString() : null;
				SwitchStatement statement = new SwitchStatement(position, label, value);
				StatementSerializationContext inner = context.forLoop(statement);

				int numberOfCases = readUInt();
				for (int i = 0; i < numberOfCases; i++) {
					SwitchValue switchValue = deserializeSwitchValue(inner, (flags & StatementEncoding.FLAG_NAME) > 0);
					if (switchValue instanceof VariantOptionSwitchValue)
						context.variantOptionSwitchValue = (VariantOptionSwitchValue) switchValue;

					Statement[] statements = new Statement[readUInt()];
					for (int j = 0; j < statements.length; j++)
						statements[j] = deserializeStatement(inner);
					statement.cases.add(new SwitchCase(switchValue, statements));
				}

				return statement;
			}
			case StatementEncoding.TYPE_THROW: {
				Expression value = deserializeExpression(context);
				return new ThrowStatement(position, value);
			}
			case StatementEncoding.TYPE_TRY_CATCH:
				throw new UnsupportedOperationException("Not supported yet");
			case StatementEncoding.TYPE_VAR: {
				TypeID varType = deserializeType(context.types());
				String name = (flags & StatementEncoding.FLAG_NAME) > 0 ? readString() : null;
				Expression initializer = deserializeExpression(context);
				VarStatement result = new VarStatement(position, new VariableID(), name, varType, initializer, (flags & StatementEncoding.FLAG_FINAL) > 0);
				context.add(result);
				return result;
			}
			case StatementEncoding.TYPE_WHILE: {
				Expression condition = deserializeExpression(context);
				String label = (flags & StatementEncoding.FLAG_LABEL) > 0 ? readString() : null;
				WhileStatement loop = new WhileStatement(position, label, condition);
				StatementSerializationContext inner = context.forLoop(loop);
				loop.content = deserializeStatement(inner);
				return loop;
			}
			default:
				throw new IllegalArgumentException("Unknown statement type: " + type);
		}
	}

	@Override
	public Expression deserializeExpression(StatementSerializationContext context) {
		int kind = readUInt();
		int flags = kind == ExpressionEncoding.TYPE_NONE ? 0 : readUInt();
		CodePosition position = (flags & ExpressionEncoding.FLAG_POSITION) > 0 ? deserializePosition() : CodePosition.UNKNOWN;
		switch (kind) {
			case ExpressionEncoding.TYPE_NONE:
				return null;
			case ExpressionEncoding.TYPE_AND_AND: {
				Expression left = deserializeExpression(context);
				Expression right = deserializeExpression(context);
				return new AndAndExpression(position, left, right);
			}
			case ExpressionEncoding.TYPE_ARRAY: {
				TypeID type = deserializeType(context.types());
				Expression[] expressions = new Expression[readUInt()];
				return new ArrayExpression(position, expressions, type);
			}
			case ExpressionEncoding.TYPE_COMPARE: {
				CompareType comparison = readComparator();
				Expression left = deserializeExpression(context);
				Expression right = deserializeExpression(context);
				MethodInstance operator = (MethodInstance) readMember(context.types(), left.type);
				return new CompareExpression(position, left, right, operator, comparison);
			}
			case ExpressionEncoding.TYPE_CALL: {
				Expression target = deserializeExpression(context);
				MethodInstance member = (MethodInstance) readMember(context.types(), target.type);
				CallArguments arguments = deserializeArguments(context);
				FunctionHeader instancedHeader = member.getHeader().instanceForCall(arguments);
				return new CallExpression(position, target, member, arguments);
			}
			case ExpressionEncoding.TYPE_CALL_STATIC: {
				TypeID type = deserializeType(context.types());
				MethodInstance member = (MethodInstance) readMember(context.types(), type);
				CallArguments arguments = deserializeArguments(context);
				FunctionHeader instancedHeader = member.getHeader().instanceForCall(arguments);
				return new CallStaticExpression(position, type, member, arguments);
			}
			case ExpressionEncoding.TYPE_CAPTURED_CLOSURE: {
				Expression value = deserializeExpression(context.getLambdaOuter());
				return new CapturedClosureExpression(position, (CapturedExpression) value, context.getLambdaClosure());
			}
			case ExpressionEncoding.TYPE_CAPTURED_LOCAL_VARIABLE: {
				VarStatement var = context.getLambdaOuter().getVariable(readUInt());
				return new CapturedLocalVariableExpression(position, var, context.getLambdaClosure());
			}
			case ExpressionEncoding.TYPE_CAPTURED_PARAMETER: {
				FunctionParameter parameter = context.getLambdaOuter().getParameter(readUInt());
				return new CapturedParameterExpression(position, parameter, context.getLambdaClosure());
			}
			case ExpressionEncoding.TYPE_CAPTURED_THIS: {
				return new CapturedThisExpression(position, context.thisType, context.getLambdaClosure());
			}
			case ExpressionEncoding.TYPE_CHECKNULL: {
				Expression value = deserializeExpression(context);
				return new CheckNullExpression(position, value);
			}
			case ExpressionEncoding.TYPE_COALESCE: {
				Expression left = deserializeExpression(context);
				Expression right = deserializeExpression(context);
				return new CoalesceExpression(position, left, right);
			}
			case ExpressionEncoding.TYPE_CONDITIONAL: {
				Expression condition = deserializeExpression(context);
				TypeID type = deserializeType(context.types());
				Expression onThen = deserializeExpression(context);
				Expression onElse = deserializeExpression(context);
				return new ConditionalExpression(position, condition, onThen, onElse, type);
			}
			case ExpressionEncoding.TYPE_CONSTANT_BOOL_TRUE:
				return new ConstantBoolExpression(position, true);
			case ExpressionEncoding.TYPE_CONSTANT_BOOL_FALSE:
				return new ConstantBoolExpression(position, false);
			case ExpressionEncoding.TYPE_CONSTANT_BYTE:
				return new ConstantByteExpression(position, readByte());
			case ExpressionEncoding.TYPE_CONSTANT_CHAR:
				return new ConstantCharExpression(position, readChar());
			case ExpressionEncoding.TYPE_CONSTANT_DOUBLE:
				return new ConstantDoubleExpression(position, readDouble());
			case ExpressionEncoding.TYPE_CONSTANT_FLOAT:
				return new ConstantFloatExpression(position, readFloat());
			case ExpressionEncoding.TYPE_CONSTANT_INT:
				return new ConstantIntExpression(position, readInt());
			case ExpressionEncoding.TYPE_CONSTANT_LONG:
				return new ConstantLongExpression(position, readLong());
			case ExpressionEncoding.TYPE_CONSTANT_SBYTE:
				return new ConstantSByteExpression(position, readSByte());
			case ExpressionEncoding.TYPE_CONSTANT_SHORT:
				return new ConstantShortExpression(position, readShort());
			case ExpressionEncoding.TYPE_CONSTANT_STRING:
				return new ConstantStringExpression(position, readString());
			case ExpressionEncoding.TYPE_CONSTANT_UINT:
				return new ConstantUIntExpression(position, readUInt());
			case ExpressionEncoding.TYPE_CONSTANT_ULONG:
				return new ConstantULongExpression(position, readULong());
			case ExpressionEncoding.TYPE_CONSTANT_USHORT:
				return new ConstantUShortExpression(position, readUShort());
			case ExpressionEncoding.TYPE_CONSTANT_USIZE:
				return new ConstantUSizeExpression(position, readULong());
			case ExpressionEncoding.TYPE_CONSTRUCTOR_THIS_CALL: {
				MethodInstance constructor = (MethodInstance) readMember(context, context.thisType);
				CallArguments arguments = deserializeArguments(context);
				return new ConstructorThisCallExpression(position, context.thisType, constructor, arguments);
			}
			case ExpressionEncoding.TYPE_CONSTRUCTOR_SUPER_CALL: {
				TypeID superType = context.thisType.getSuperType();
				MethodInstance constructor = (MethodInstance) readMember(context, superType);
				CallArguments arguments = deserializeArguments(context);
				return new ConstructorSuperCallExpression(position, superType, constructor, arguments);
			}
			case ExpressionEncoding.TYPE_ENUM_CONSTANT: {
				EnumConstantMember constant = readEnumConstant(context.types());
				return new EnumConstantExpression(position, registry.getForDefinition(constant.definition), constant);
			}
			case ExpressionEncoding.TYPE_FUNCTION: {
				FunctionHeader header = deserializeHeader(context.types());
				LambdaClosure closure = new LambdaClosure();
				StatementSerializationContext inner = new StatementSerializationContext(context.types(), header, closure);
				Statement body = deserializeStatement(inner);
				return new FunctionExpression(position, closure, header, body);
			}
			case ExpressionEncoding.TYPE_GET_FIELD: {
				Expression target = deserializeExpression(context);
				FieldInstance field = (FieldInstance) readMember(context, target.type);
				return new GetFieldExpression(position, target, field);
			}
			case ExpressionEncoding.TYPE_GET_FUNCTION_PARAMETER: {
				FunctionParameter parameter = context.getParameter(readUInt());
				return new GetFunctionParameterExpression(position, parameter);
			}
			case ExpressionEncoding.TYPE_GET_LOCAL_VARIABLE: {
				VarStatement variable = context.getVariable(readUInt());
				return new GetLocalVariableExpression(position, variable);
			}
			case ExpressionEncoding.TYPE_GET_MATCHING_VARIANT_FIELD: {
				return new GetMatchingVariantField(position, context.variantOptionSwitchValue, kind);
			}
			case ExpressionEncoding.TYPE_GET_STATIC_FIELD: {
				TypeID type = deserializeType(context);
				FieldInstance field = (FieldInstance) readMember(context, type);
				return new GetStaticFieldExpression(position, field);
			}
			case ExpressionEncoding.TYPE_GLOBAL: {
				String name = readString();
				Expression resolution = deserializeExpression(context);
				return new GlobalExpression(position, name, resolution);
			}
			case ExpressionEncoding.TYPE_GLOBAL_CALL: {
				String name = readString();
				CallArguments arguments = deserializeArguments(context);
				Expression resolution = deserializeExpression(context);
				return new GlobalCallExpression(position, name, arguments, resolution);
			}
			case ExpressionEncoding.TYPE_INTERFACE_CAST: {
				Expression value = deserializeExpression(context);
				TypeID toType = deserializeType(context);

				//FIXME: I have no idea if this works?
				final ImplementationMemberInstance definitionMemberRef = (ImplementationMemberInstance) readMember(context, toType);
				return new InterfaceCastExpression(position, value, definitionMemberRef);
			}
			case ExpressionEncoding.TYPE_IS: {
				Expression value = deserializeExpression(context);
				TypeID type = deserializeType(context);
				return new IsExpression(position, value, type);
			}
			/*case ExpressionEncoding.TYPE_MAKE_CONST: {
				Expression value = deserializeExpression(context);
				StoredType constType = registry.get(OptionalTypeID.MODIFIER_CONST, value.type.type).stored(value.type.getActualStorage());
				return new MakeConstExpression(position, value, constType);
			}*/
			case ExpressionEncoding.TYPE_MAP: {
				TypeID type = deserializeType(context);
				Expression[] keys = new Expression[readUInt()];
				Expression[] values = new Expression[keys.length];
				for (int i = 0; i < keys.length; i++) {
					keys[i] = deserializeExpression(context);
					values[i] = deserializeExpression(context);
				}
				return new MapExpression(position, keys, values, type);
			}
			case ExpressionEncoding.TYPE_MATCH: {
				boolean localVariableNames = (flags & ExpressionEncoding.FLAG_NAMES) > 0;

				Expression value = deserializeExpression(context);
				TypeID type = deserializeType(context);
				MatchExpression.Case[] cases = new MatchExpression.Case[readUInt()];
				for (int i = 0; i < cases.length; i++) {
					SwitchValue key = deserializeSwitchValue(context, localVariableNames);
					Expression caseValue = deserializeExpression(context);
					cases[i] = new MatchExpression.Case(key, caseValue);
				}
				return new MatchExpression(position, value, type, cases);
			}
			case ExpressionEncoding.TYPE_NULL: {
				TypeID type = deserializeType(context);
				return new NullExpression(position, type);
			}
			case ExpressionEncoding.TYPE_OR_OR: {
				Expression left = deserializeExpression(context);
				Expression right = deserializeExpression(context);
				return new OrOrExpression(position, left, right);
			}
			case ExpressionEncoding.TYPE_PANIC: {
				Expression value = deserializeExpression(context);
				TypeID type = deserializeType(context);
				return new PanicExpression(position, type, value);
			}
			case ExpressionEncoding.TYPE_POST_CALL: {
				Expression target = deserializeExpression(context);
				MethodInstance member = (MethodInstance) readMember(context, target.type);
				return new PostCallExpression(position, target, member, member.getHeader());
			}
			case ExpressionEncoding.TYPE_RANGE: {
				Expression from = deserializeExpression(context);
				Expression to = deserializeExpression(context);
				return new RangeExpression(position, registry.getRange(from.type), from, to);
			}
			case ExpressionEncoding.TYPE_SAME_OBJECT: {
				Expression left = deserializeExpression(context);
				Expression right = deserializeExpression(context);
				boolean inverted = (flags & ExpressionEncoding.FLAG_INVERTED) > 0;
				return new SameObjectExpression(position, left, right, inverted);
			}
			case ExpressionEncoding.TYPE_SET_FIELD: {
				Expression target = deserializeExpression(context);
				FieldInstance field = (FieldInstance) readMember(context, target.type);
				Expression value = deserializeExpression(context);
				return new SetFieldExpression(position, target, field, value);
			}
			case ExpressionEncoding.TYPE_SET_FUNCTION_PARAMETER: {
				FunctionParameter parameter = context.getParameter(readUInt());
				Expression value = deserializeExpression(context);
				return new SetFunctionParameterExpression(position, parameter, value);
			}
			case ExpressionEncoding.TYPE_SET_LOCAL_VARIABLE: {
				VarStatement variable = context.getVariable(readUInt());
				Expression value = deserializeExpression(context);
				return new SetLocalVariableExpression(position, variable, value);
			}
			case ExpressionEncoding.TYPE_SET_STATIC_FIELD: {
				TypeID type = deserializeType(context);
				FieldInstance member = (FieldInstance) readMember(context, type);
				Expression value = deserializeExpression(context);
				return new SetStaticFieldExpression(position, member, value);
			}
			case ExpressionEncoding.TYPE_SUPERTYPE_CAST: {
				TypeID type = deserializeType(context);
				Expression value = deserializeExpression(context);
				return new SupertypeCastExpression(position, value, type);
			}
			case ExpressionEncoding.TYPE_SUBTYPE_CAST: {
				TypeID type = deserializeType(context);
				Expression value = deserializeExpression(context);
				return new SubtypeCastExpression(position, value, type);
			}
			case ExpressionEncoding.TYPE_THIS:
				return new ThisExpression(position, context.thisType);
			case ExpressionEncoding.TYPE_THROW: {
				TypeID type = deserializeType(context);
				Expression value = deserializeExpression(context);
				return new ThrowExpression(position, type, value);
			}
			case ExpressionEncoding.TYPE_TRY_CONVERT: {
				TypeID type = deserializeType(context);
				Expression value = deserializeExpression(context);
				return new TryConvertExpression(position, type, value);
			}
			case ExpressionEncoding.TYPE_TRY_RETHROW_AS_EXCEPTION: {
				TypeID type = deserializeType(context);
				TypeID thrownType = deserializeType(context);
				Expression value = deserializeExpression(context);
				return new TryRethrowAsExceptionExpression(position, type, value, thrownType);
			}
			case ExpressionEncoding.TYPE_TRY_RETHROW_AS_RESULT: {
				TypeID type = deserializeType(context);
				Expression value = deserializeExpression(context);
				return new TryRethrowAsResultExpression(position, type, value);
			}
			case ExpressionEncoding.TYPE_VARIANT_VALUE: {
				TypeID type = deserializeType(context);
				VariantOptionInstance option = readVariantOption(context, type);
				Expression[] arguments = new Expression[option.types.length];
				for (int i = 0; i < arguments.length; i++)
					arguments[i] = deserializeExpression(context);
				return new VariantValueExpression(position, type, option, arguments);
			}
			case ExpressionEncoding.TYPE_WRAP_OPTIONAL: {
				Expression value = deserializeExpression(context);
				TypeID optionalType = registry.getOptional(value.type);
				return new WrapOptionalExpression(position, value, optionalType);
			}
			default:
				throw new IllegalArgumentException("Not a valid expression type: " + kind);
		}
	}

	private CompareType readComparator() {
		int type = readUInt();
		switch (type) {
			case ExpressionEncoding.COMPARATOR_LT:
				return CompareType.LT;
			case ExpressionEncoding.COMPARATOR_GT:
				return CompareType.GT;
			case ExpressionEncoding.COMPARATOR_EQ:
				return CompareType.EQ;
			case ExpressionEncoding.COMPARATOR_NE:
				return CompareType.NE;
			case ExpressionEncoding.COMPARATOR_LE:
				return CompareType.LE;
			case ExpressionEncoding.COMPARATOR_GE:
				return CompareType.GE;
			default:
				throw new IllegalArgumentException("Invalid comparator: " + type);
		}
	}

	private SwitchValue deserializeSwitchValue(StatementSerializationContext context, boolean localVariableNames) {
		int type = readUInt();
		switch (type) {
			case SwitchValueEncoding.TYPE_NULL:
				return null;
			case SwitchValueEncoding.TYPE_INT:
				return new IntSwitchValue(readInt());
			case SwitchValueEncoding.TYPE_CHAR:
				return new CharSwitchValue(readChar());
			case SwitchValueEncoding.TYPE_STRING:
				return new StringSwitchValue(readString());
			case SwitchValueEncoding.TYPE_ENUM:
				return new EnumConstantSwitchValue(readEnumConstant(context));
			case SwitchValueEncoding.TYPE_VARIANT_OPTION: {
				TypeID t = deserializeType(context);
				VariantOptionInstance option = readVariantOption(context, t);
				String[] names = new String[option.types.length];
				if (localVariableNames)
					for (int i = 0; i < names.length; i++)
						names[i] = readString();
				return new VariantOptionSwitchValue(option, names);
			}
			default:
				throw new IllegalArgumentException("Unknown switch value type: " + type);
		}
	}

	@Override
	public void enqueueMembers(DecodingOperation operation) {
		members.enqueue(operation);
	}

	@Override
	public void enqueueCode(DecodingOperation operation) {
		code.enqueue(operation);
	}

	private TypeParameterBound deserializeTypeParameterBound(TypeSerializationContext context) {
		int type = readUInt();
		switch (type) {
			case TypeParameterEncoding.TYPE_SUPER_BOUND:
				return new ParameterSuperBound(deserializeType(context));
			case TypeParameterEncoding.TYPE_TYPE_BOUND:
				return new ParameterTypeBound(CodePosition.UNKNOWN, deserializeType(context));
			default:
				throw new IllegalArgumentException("Not a valid parameter bound type: " + type);
		}
	}
}
