/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.FieldSymbol;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.TypeContext;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;
import org.openzen.zenscript.validator.analysis.StatementScope;

import java.util.Arrays;

/**
 * @author Hoofdgebruiker
 */
public class StatementValidator implements StatementVisitor<Void> {
	private final Validator validator;
	private final StatementScope scope;
	public boolean constructorForwarded = false;
	private boolean firstStatement = true;

	public StatementValidator(Validator validator, StatementScope scope) {
		this.validator = validator;
		this.scope = scope;
	}

	@Override
	public Void visitBlock(BlockStatement block) {
		validateInnerBlock(block.statements);
		firstStatement = false;
		return null;
	}

	@Override
	public Void visitBreak(BreakStatement statement) {
		firstStatement = false;
		return null;
	}

	@Override
	public Void visitContinue(ContinueStatement statement) {
		firstStatement = false;
		return null;
	}

	@Override
	public Void visitDoWhile(DoWhileStatement statement) {
		if (statement.condition.type != BasicTypeID.BOOL) {
			validator.logError(
					statement.position,
					CompileErrors.typeMismatch(BasicTypeID.BOOL, statement.condition.type));
		}

		statement.condition.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		statement.content.accept(this);

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitEmpty(EmptyStatement statement) {
		firstStatement = false;
		return null;
	}

	@Override
	public Void visitExpression(ExpressionStatement statement) {
		statement.expression.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		firstStatement = false;
		return null;
	}

	@Override
	public Void visitForeach(ForeachStatement statement) {
		statement.list.accept(new ExpressionValidator(validator, new StatementExpressionScope()));

		validateInnerBlock(statement.getContent());

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitIf(IfStatement statement) {
		validateCondition(statement.condition);
		statement.onThen.accept(this);
		if (statement.onElse != null)
			statement.onElse.accept(this);

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitInvalid(InvalidStatement statement) {
		validator.logError(statement.position, statement.error);
		return null;
	}

	@Override
	public Void visitLock(LockStatement statement) {
		// TODO: is the object a valid lock target?
		statement.object.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		statement.content.accept(this);

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitReturn(ReturnStatement statement) {
		if (scope.getFunctionHeader() == null) {
			validator.logError(statement.position, CompileErrors.returnOutsideFunction());
			return null;
		}

		if (statement.value != null) {
			statement.value.accept(new ExpressionValidator(
					validator,
					new StatementExpressionScope()));

			if (scope.getFunctionHeader().getReturnType() == BasicTypeID.VOID) {
				validator.logError(statement.position, CompileErrors.returnValueInVoidFunction());
			} else if (!statement.value.type.equals(scope.getFunctionHeader().getReturnType())) {
				validator.logError(statement.position, CompileErrors.typeMismatch(scope.getFunctionHeader().getReturnType(), statement.value.type));
			}
		} else if (scope.getFunctionHeader().getReturnType() != BasicTypeID.VOID) {
			validator.logError(statement.position, CompileErrors.missingReturnValue());
		}

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitSwitch(SwitchStatement statement) {
		statement.value.accept(new ExpressionValidator(validator, new StatementExpressionScope()));

		for (SwitchCase switchCase : statement.cases) {
			for (Statement caseStatement : switchCase.statements)
				caseStatement.accept(this);

			// TODO: finish this
		}

		return null;
	}

	@Override
	public Void visitThrow(ThrowStatement statement) {
		statement.value.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		// TODO: does the value type extend Exception?

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitTryCatch(TryCatchStatement statement) {
		if (statement.resource != null) {
			if (statement.resource.initializer == null) {
				validator.logError(
						statement.position,
						CompileErrors.tryCatchResourceWithoutInitializer());
			}
		}

		statement.content.accept(this);
		for (CatchClause catchClause : statement.catchClauses) {
			validateInnerBlock(catchClause.content);
		}

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitVar(VarStatement statement) {
		new TypeValidator(validator, statement.position).validate(TypeContext.VARIABLE_TYPE, statement.type);
		if (!statement.type.isInvalid() && statement.initializer != null) {
			statement.initializer.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		}

		firstStatement = false;
		return null;
	}

	@Override
	public Void visitWhile(WhileStatement statement) {
		validateCondition(statement.condition);
		statement.content.accept(this);

		firstStatement = false;
		return null;
	}

	private void validateCondition(Expression condition) {
		condition.accept(new ExpressionValidator(validator, new StatementExpressionScope()));

		if (condition.type != BasicTypeID.BOOL) {
			validator.logError(
					condition.position,
					CompileErrors.typeMismatch(BasicTypeID.BOOL, condition.type));
		}
	}

	private void validateInnerBlock(final Statement statement) {
		validateInnerBlock(new Statement[] { statement });
	}

	private void validateInnerBlock(final Statement[] statements) {
		final StatementValidator innerValidator = new StatementValidator(this.validator, this.scope);
		Arrays.stream(statements).forEach(it -> it.accept(innerValidator));
		this.constructorForwarded |= innerValidator.constructorForwarded;
	}

	private class StatementExpressionScope implements ExpressionScope {
		@Override
		public boolean isConstructor() {
			return scope.isConstructor();
		}

		@Override
		public boolean isFirstStatement() {
			return firstStatement;
		}

		@Override
		public boolean hasThis() {
			return !scope.isStatic();
		}

		@Override
		public boolean isFieldInitialized(FieldSymbol field) {
			return true; // TODO: improve field initialization analysis
		}

		@Override
		public void markConstructorForwarded() {
			constructorForwarded = true;
		}

		@Override
		public boolean isEnumConstantInitialized(EnumConstantMember member) {
			return true;
		}

		@Override
		public boolean isLocalVariableInitialized(VariableID variable) {
			return true; // TODO
		}

		@Override
		public boolean isStaticInitializer() {
			return scope.isStaticInitializer();
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return scope.getDefinition();
		}
	}
}
