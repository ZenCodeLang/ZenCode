/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.annotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationInput;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.StringTypeID;
import org.openzen.zenscript.codemodel.type.storage.StaticStorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class PreconditionAnnotationDefinition implements AnnotationDefinition {
	public static final PreconditionAnnotationDefinition INSTANCE = new PreconditionAnnotationDefinition();
	
	private final List<GenericName> enforcementLevelName = Arrays.asList(
			new GenericName("stdlib"),
			new GenericName("EnforcementLevel"));
	
	private PreconditionAnnotationDefinition() {}

	@Override
	public String getAnnotationName() {
		return "Precondition";
	}

	@Override
	public List<FunctionHeader> getInitializers(BaseScope scope) {
		return Collections.singletonList(new FunctionHeader(
				BasicTypeID.VOID,
				scope.getType(CodePosition.BUILTIN, enforcementLevelName, StaticStorageTag.INSTANCE),
				BasicTypeID.BOOL,
				StringTypeID.UNIQUE));
	}

	@Override
	public ExpressionScope getScopeForMember(IDefinitionMember member, BaseScope scope) {
		if (member instanceof FunctionalMember) {
			FunctionHeader header = ((FunctionalMember)member).header;
			return new ExpressionScope(new FunctionScope(scope, header));
		} else {
			throw new UnsupportedOperationException("Can only assign preconditions to methods");
		}
	}

	@Override
	public ExpressionScope getScopeForType(HighLevelDefinition definition, BaseScope scope) {
		if (definition instanceof FunctionDefinition) {
			FunctionHeader header = ((FunctionDefinition)definition).header;
			return new ExpressionScope(new FunctionScope(scope, header));
		} else {
			throw new UnsupportedOperationException("Can only assign preconditions to functions");
		}
	}

	@Override
	public ExpressionScope getScopeForStatement(Statement statement, StatementScope scope) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public ExpressionScope getScopeForParameter(FunctionHeader header, FunctionParameter parameter, BaseScope scope) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public MemberAnnotation createForMember(CodePosition position, CallArguments arguments) {
		String enforcement = arguments.arguments[0].evaluateEnumConstant().name;
		Expression condition = arguments.arguments[1];
		Expression message = arguments.arguments[2];
		return new PreconditionForMethod(position, enforcement, condition, message);
	}

	@Override
	public DefinitionAnnotation createForDefinition(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public StatementAnnotation createForStatement(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public Annotation createForParameter(CodePosition position, CallArguments arguments) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public MemberAnnotation deserializeForMember(CodeSerializationInput input, TypeContext context, IDefinitionMember member) {
		CodePosition position = input.deserializePosition();
		String enforcement = input.readString();
		StatementContext statementContext = new StatementContext(context, member.getHeader());
		Expression condition = input.deserializeExpression(statementContext);
		Expression message = input.deserializeExpression(statementContext);
		return new PreconditionForMethod(position, enforcement, condition, message);
	}

	@Override
	public DefinitionAnnotation deserializeForDefinition(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public StatementAnnotation deserializeForStatement(CodeSerializationInput input, StatementContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Annotation deserializeForParameter(CodeSerializationInput input, TypeContext context) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
