package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.expression.ConstructorSuperCallExpression;
import org.openzen.zenscript.codemodel.expression.ConstructorThisCallExpression;
import org.openzen.zenscript.codemodel.identifiers.MethodID;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Optional;

public class ConstructorMember extends FunctionalMember {
	public ConstructorMember(
			CodePosition position,
			HighLevelDefinition definition,
			Modifiers modifiers,
			FunctionHeader header) {
		super(
				position,
				definition,
				modifiers,
				MethodID.staticOperator(OperatorType.CONSTRUCTOR),
				header.withReturnType(DefinitionTypeID.createThis(definition)));
	}

	public boolean isConstructorForwarded() {
		ExpressionStatement firstExpression = null;
		if (body instanceof ExpressionStatement) {
			firstExpression = (ExpressionStatement) body;
		} else if (body instanceof BlockStatement) {
			BlockStatement blockBody = (BlockStatement) body;
			if (blockBody.statements.length > 0 && blockBody.statements[0] instanceof ExpressionStatement)
				firstExpression = (ExpressionStatement) blockBody.statements[0];
		}

		return firstExpression != null && (
				firstExpression.expression instanceof ConstructorSuperCallExpression
						|| firstExpression.expression instanceof ConstructorThisCallExpression);
	}

	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":this" + header.getCanonical();
	}

	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.CONSTRUCTOR;
	}

	@Override
	public String describe() {
		return "constructor " + header.toString();
	}

	@Override
	public void registerTo(TypeID targetType, MemberSet.Builder members, GenericMapper mapper) {
		members.constructor(new MethodInstance(this, mapper.map(header), targetType));
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitConstructor(this);
	}

	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitConstructor(context, this);
	}

	@Override
	public Optional<MethodInstance> getOverrides() {
		return Optional.empty();
	}

	@Override
	public Modifiers getEffectiveModifiers() {
		Modifiers result = modifiers.withStatic();
		if (definition.isEnum())
			result = result.withPrivate();
		else if (!modifiers.hasAccessModifiers())
			result = result.withInternal();

		return result;
	}
}
