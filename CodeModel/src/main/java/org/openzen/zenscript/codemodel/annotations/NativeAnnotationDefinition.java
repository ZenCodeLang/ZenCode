package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.constant.CompileTimeConstant;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NativeAnnotationDefinition implements AnnotationDefinition {
	public static final NativeAnnotationDefinition INSTANCE = new NativeAnnotationDefinition();

	private final List<AnnotationInitializer> INITIALIZERS = Collections.singletonList(
					new AnnotationInitializer(new FunctionHeader(BasicTypeID.VOID, BasicTypeID.STRING)));

	private NativeAnnotationDefinition() {
	}

	@Override
	public String getAnnotationName() {
		return "Native";
	}

	@Override
	public List<AnnotationInitializer> getInitializers(TypeBuilder types) {
		return INITIALIZERS;
	}

	@Override
	public ExpressionCompiler getScopeForMember(IDefinitionMember member, MemberCompiler compiler) {
		return compiler.forFieldInitializers();
	}

	@Override
	public ExpressionCompiler getScopeForType(HighLevelDefinition definition, DefinitionCompiler compiler) {
		return compiler.types().getDefaultValueCompiler();
	}

	@Override
	public ExpressionCompiler getScopeForStatement(Statement statement, StatementCompiler compiler) {
		return compiler.types().getDefaultValueCompiler();
	}

	@Override
	public ExpressionCompiler getScopeForParameter(FunctionHeader header, FunctionParameter parameter, ExpressionCompiler compiler) {
		return compiler;
	}

	@Override
	public MemberAnnotation createForMember(CodePosition position, CallArguments arguments) {
		Expression value = arguments.arguments[0];
		Optional<String> constant = value.evaluate().flatMap(CompileTimeConstant::asString).map(c -> c.value);
		if (constant.isPresent()) {
			return new NativeMemberAnnotation(constant.get());
		} else {
			return new InvalidMemberAnnotation(new CompileException(position, CompileErrors.notAStringConstant()));
		}
	}

	@Override
	public DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments) {
		Expression value = arguments.arguments[0];
		Optional<String> constant = value.evaluate().flatMap(CompileTimeConstant::asString).map(c -> c.value);
		if (constant.isPresent()) {
			return new NativeDefinitionAnnotation(constant.get());
		} else {
			return new InvalidDefinitionAnnotation(new CompileException(position, CompileErrors.notAStringConstant()));
		}
	}

	@Override
	public StatementAnnotation createForStatement(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public ParameterAnnotation createForParameter(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeSerializationContext context, IDefinitionMember member) {
		String name = input.readString();
		return new NativeMemberAnnotation(name);
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeSerializationContext context) {
		String name = input.readString();
		return new NativeDefinitionAnnotation(name);
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementSerializationContext context) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public ParameterAnnotation deserializeForParameter(CodeSerializationInput input, TypeSerializationContext context) {
		throw new UnsupportedOperationException("Not supported");
	}
}
