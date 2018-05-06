/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import java.util.HashSet;
import java.util.Set;
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
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementVisitor;
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
public class StatementValidator implements StatementVisitor<Boolean> {
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
	public Boolean visitBlock(BlockStatement block) {
		boolean isValid = true;
		
		StatementValidator blockValidator = new StatementValidator(validator, scope);
		for (Statement statement : block.statements)
			isValid &= statement.accept(blockValidator);
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitBreak(BreakStatement statement) {
		firstStatement = false;
		return true;
	}

	@Override
	public Boolean visitContinue(ContinueStatement statement) {
		firstStatement = false;
		return true;
	}

	@Override
	public Boolean visitDoWhile(DoWhileStatement statement) {
		boolean isValid = true;
		
		if (statement.condition.type != BasicTypeID.BOOL) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_CONDITION_TYPE,
					statement.position,
					"condition must be a boolean expression");
			isValid = false;
		}
		
		isValid &= statement.condition.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		isValid &= statement.content.accept(this);
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitEmpty(EmptyStatement statement) {
		firstStatement = false;
		return true;
	}

	@Override
	public Boolean visitExpression(ExpressionStatement statement) {
		boolean isValid = statement.expression.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitForeach(ForeachStatement statement) {
		boolean isValid = true;
		isValid &= statement.list.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		isValid &= statement.content.accept(this);
		
		for (VarStatement var : statement.loopVariables) {
			if (variables.contains(var.name)) {
				validator.logError(ValidationLogEntry.Code.DUPLICATE_VARIABLE_NAME, var.position, "Duplicate variable name: " + var.name);
				isValid = false;
			}
		}
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitIf(IfStatement statement) {
		boolean isValid = true;
		isValid &= validateCondition(statement.condition);
		isValid &= statement.onThen.accept(this);
		if (statement.onElse != null)
			isValid &= statement.onElse.accept(this);
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitLock(LockStatement statement) {
		boolean isValid = true;
		// TODO: is the object a valid lock target?
		isValid &= statement.object.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		isValid &= statement.content.accept(this);
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitReturn(ReturnStatement statement) {
		if (scope.getFunctionHeader() == null) {
			validator.logError(ValidationLogEntry.Code.SCRIPT_CANNOT_RETURN, statement.position, "Cannot return from a script");
			return false;
		}
		
		boolean isValid = true;
		if (statement.value != null) {
			isValid &= statement.value.accept(new ExpressionValidator(
					validator,
					new StatementExpressionScope()));
			
			if (statement.value.type != scope.getFunctionHeader().returnType) {
				validator.logError(ValidationLogEntry.Code.INVALID_RETURN_TYPE, statement.position, "Invalid return type: " + statement.value.type.toString());
				isValid = false;
			}
		} else if (scope.getFunctionHeader().returnType != BasicTypeID.ANY
				&& scope.getFunctionHeader().returnType != BasicTypeID.VOID) {
			validator.logError(ValidationLogEntry.Code.INVALID_RETURN_TYPE, statement.position, "Missing return value");
			isValid = false;
		}
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitThrow(ThrowStatement statement) {
		boolean isValid = statement.value.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		// TODO: does the value type extend Exception?
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitTryCatch(TryCatchStatement statement) {
		boolean isValid = true;
		if (statement.resource != null) {
			if (variables.contains(statement.resource.name)) {
				validator.logError(
						ValidationLogEntry.Code.DUPLICATE_VARIABLE_NAME,
						statement.position,
						"Duplicate variable name: " + statement.resource.name);
				isValid = false;
			}
			if (statement.resource.initializer == null) {
				validator.logError(
						ValidationLogEntry.Code.TRY_CATCH_RESOURCE_REQUIRES_INITIALIZER,
						statement.position,
						"try with resource requires initializer");
				isValid = false;
			}
		}
		
		isValid &= statement.content.accept(this);
		for (CatchClause catchClause : statement.catchClauses) {
			isValid &= catchClause.content.accept(this);
		}
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitVar(VarStatement statement) {
		boolean isValid = true;
		if (variables.contains(statement.name)) {
			validator.logError(
						ValidationLogEntry.Code.DUPLICATE_VARIABLE_NAME,
						statement.position,
						"Duplicate variable name: " + statement.name);
			isValid = false;
		}
		variables.add(statement.name);
		if (statement.initializer != null) {
			isValid &= statement.initializer.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		}
		
		firstStatement = false;
		return isValid;
	}

	@Override
	public Boolean visitWhile(WhileStatement statement) {
		boolean isValid = true;
		isValid &= validateCondition(statement.condition);
		isValid &= statement.content.accept(this);
		
		firstStatement = false;
		return isValid;
	}
	
	private boolean validateCondition(Expression condition) {
		boolean isValid = condition.accept(new ExpressionValidator(validator, new StatementExpressionScope()));
		
		if (condition.type != BasicTypeID.BOOL) {
			validator.logError(
					ValidationLogEntry.Code.INVALID_CONDITION_TYPE,
					condition.position,
					"condition must be a boolean expression");
			isValid = false;
		}
		
		return isValid;
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
	}
}
