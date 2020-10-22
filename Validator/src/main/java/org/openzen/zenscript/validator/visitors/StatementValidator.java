/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import java.util.HashSet;
import java.util.Set;
import org.openzen.zenscript.codemodel.AccessScope;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.BreakStatement;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementVisitor;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.validator.ValidationLogEntry;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.ExpressionScope;
import org.openzen.zenscript.validator.analysis.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatementValidator implements StatementVisitor<Void> {
	private final Validator validator;
	private final StatementScope scope;
	private final Set<String> variables = new HashSet<>();
	private boolean firstStatement = true;
	
	public boolean constructorForwarded = true;
	
	public StatementValidator(Validator validator, StatementScope scope) {
		this.validator = validator;
		this.scope = scope;
	}
	
	@Override
	public Void visitBlock(BlockStatement block) {
		StatementValidator blockValidator = new StatementValidator(validator, scope);
		for (Statement statement : block.statements)
			statement.accept(blockValidator);
		
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
					ValidationLogEntry.Code.INVALID_CONDITION_TYPE,
					statement.position,
					"condition must be a boolean expression");
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
		statement.content.accept(this);
		
		for (VarStatement var : statement.loopVariables) {
			if (variables.contains(var.name)) {
				validator.logError(ValidationLogEntry.Code.DUPLICATE_VARIABLE_NAME, var.position, "Duplicate variable name: " + var.name);
			}
		}
		
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
		validator.logError(ValidationLogEntry.Code.INVALID_STATEMENT, statement.position, statement.message);
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
			validator.logError(ValidationLogEntry.Code.SCRIPT_CANNOT_RETURN, statement.position, "Cannot return from a script");
			return null;
		}
		
		if (statement.value != null) {
			statement.value.accept(new ExpressionValidator(
					validator,
					new StatementExpressionScope()));
			
			if (scope.getFunctionHeader().getReturnType() == BasicTypeID.VOID) {
				validator.logError(ValidationLogEntry.Code.INVALID_RETURN_TYPE, statement.position, "Function return type is void; cannot return a value");
			} else if (!statement.value.type.equals(scope.getFunctionHeader().getReturnType())) {
				validator.logError(ValidationLogEntry.Code.INVALID_RETURN_TYPE, statement.position, "Invalid return type: " + statement.value.type.toString());
			}
		} else if (scope.getFunctionHeader().getReturnType() != BasicTypeID.VOID) {
			validator.logError(ValidationLogEntry.Code.INVALID_RETURN_TYPE, statement.position, "Missing return value");
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
			if (variables.contains(statement.resource.name)) {
				validator.logError(
						ValidationLogEntry.Code.DUPLICATE_VARIABLE_NAME,
						statement.position,
						"Duplicate variable name: " + statement.resource.name);
			}
			if (statement.resource.initializer == null) {
				validator.logError(
						ValidationLogEntry.Code.TRY_CATCH_RESOURCE_REQUIRES_INITIALIZER,
						statement.position,
						"try with resource requires initializer");
			}
		}
		
		statement.content.accept(this);
		for (CatchClause catchClause : statement.catchClauses) {
			catchClause.content.accept(this);
		}
		
		firstStatement = false;
		return null;
	}

	@Override
	public Void visitVar(VarStatement statement) {
		if (variables.contains(statement.name)) {
			validator.logError(
						ValidationLogEntry.Code.DUPLICATE_VARIABLE_NAME,
						statement.position,
						"Duplicate variable name: " + statement.name);
		}
		variables.add(statement.name);
		if (statement.initializer != null) {
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
					ValidationLogEntry.Code.INVALID_CONDITION_TYPE,
					condition.position,
					"condition must be a boolean expression");
		}
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
		public boolean isFieldInitialized(FieldMember field) {
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
		public boolean isLocalVariableInitialized(VarStatement variable) {
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

		@Override
		public AccessScope getAccessScope() {
			return scope.getAccessScope();
		}
	}
}
