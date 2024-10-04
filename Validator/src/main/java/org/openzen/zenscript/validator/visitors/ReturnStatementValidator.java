package org.openzen.zenscript.validator.visitors;

import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.validator.Validator;

public class ReturnStatementValidator implements StatementVisitor<ReturnStatementValidator.ReturnKind> {

	private final Validator validator;

	public ReturnStatementValidator(Validator validator) {
		this.validator = validator;
	}

	/**
	 * Validates whether a given statement appropriately returns a value as required by its return type.
	 *
	 * @param returnType the expected return type of the statement being validated
	 * @param statement  the statement to validate
	 * @param validator  the validator used to log errors
	 */
	public static void validate(TypeID returnType, Statement statement, Validator validator) {
		ReturnStatementValidator.ReturnKind returnKind = statement.accept(new ReturnStatementValidator(validator));
		if (returnType == BasicTypeID.VOID || returnType == BasicTypeID.UNDETERMINED) {
			// We don't care about void functions, since using return statements is optional in there
			// The check that return values in void functions don't return a value is done in the ExpressionValidator.
			return;
		}

		switch (returnKind) {
			case ALWAYS_RETURNS:
				return;
			case NEVER_RETURNS:
				validator.logError(statement.position, CompileErrors.missingReturn());
				return;
			case RETURNS_IN_SOME_CASES:
				validator.logError(statement.position, CompileErrors.notAllBranchesReturn());
				return;
			default:
				throw new IllegalStateException("Unhandled return kind: " + returnKind);
		}
	}

	@Override
	public ReturnKind visitBlock(BlockStatement statement) {
		return mergeInsideBlock(statement.statements);
	}

	@Override
	public ReturnKind visitBreak(BreakStatement statement) {
		return ReturnKind.NEVER_RETURNS;
	}

	@Override
	public ReturnKind visitContinue(ContinueStatement statement) {
		return ReturnKind.NEVER_RETURNS;
	}

	@Override
	public ReturnKind visitDoWhile(DoWhileStatement statement) {
		return statement.content.accept(this);
	}

	@Override
	public ReturnKind visitEmpty(EmptyStatement statement) {
		return ReturnKind.NEVER_RETURNS;
	}

	@Override
	public ReturnKind visitExpression(ExpressionStatement statement) {
		return ReturnKind.NEVER_RETURNS;
	}

	@Override
	public ReturnKind visitForeach(ForeachStatement statement) {
		return ReturnKind.branches(ReturnKind.NEVER_RETURNS, statement.getContent().accept(this));
	}

	@Override
	public ReturnKind visitIf(IfStatement statement) {
		ReturnKind onThen = statement.onThen.accept(this);
		ReturnKind onElse = statement.onElse == null ? ReturnKind.NEVER_RETURNS : statement.onElse.accept(this);

		return ReturnKind.branches(onThen, onElse);
	}

	@Override
	public ReturnKind visitLock(LockStatement statement) {
		return statement.content.accept(this);
	}

	@Override
	public ReturnKind visitReturn(ReturnStatement statement) {
		return ReturnKind.ALWAYS_RETURNS;
	}

	@Override
	public ReturnKind visitSwitch(SwitchStatement statement) {
		ReturnKind casesKind = statement.cases.stream()
				.map(c -> mergeInsideBlock(c.statements))
				.reduce(ReturnKind::branches)
				.orElse(ReturnKind.NEVER_RETURNS);
		if (statement.isExhaustive()) {
			return casesKind;
		} else {
			return ReturnKind.branches(casesKind, ReturnKind.NEVER_RETURNS);
		}
	}

	@Override
	public ReturnKind visitThrow(ThrowStatement statement) {
		return ReturnKind.NEVER_RETURNS;
	}

	@Override
	public ReturnKind visitTryCatch(TryCatchStatement statement) {
		ReturnKind content = statement.content.accept(this);
		ReturnKind catchClauses = statement.catchClauses.stream()
				.map(it -> it.content.accept(this))
				.reduce(ReturnKind::branches)
				.orElse(ReturnKind.NEVER_RETURNS);
		ReturnKind finallyClause = statement.finallyClause == null ? ReturnKind.NEVER_RETURNS : statement.finallyClause.accept(this);

		// If finally clause always returns it wins, otherwise use individual content/catch clauses
		return ReturnKind.max(finallyClause, ReturnKind.branches(content, catchClauses));
	}

	@Override
	public ReturnKind visitVar(VarStatement statement) {
		return ReturnKind.NEVER_RETURNS;
	}

	@Override
	public ReturnKind visitWhile(WhileStatement statement) {
		return ReturnKind.branches(ReturnKind.NEVER_RETURNS, statement.content.accept(this));
	}

	private ReturnKind mergeInsideBlock(Statement[] statements) {
		ReturnKind result = ReturnKind.NEVER_RETURNS;
		for (Statement statement : statements) {
			if (result == ReturnKind.ALWAYS_RETURNS) {
				validator.logError(statement.position, CompileErrors.unreachableStatement());
				break;
			}

			result = ReturnKind.max(result, statement.accept(this));
		}

		return result;
	}

	public enum ReturnKind {
		/**
		 * No return statements are present
		 */
		NEVER_RETURNS,
		/**
		 * Only some cases return (e.g. in the if-branch but not inside the else branch, or inside a try but not in the catch)
		 */
		RETURNS_IN_SOME_CASES,
		/**
		 * All possible branches return a value
		 */
		ALWAYS_RETURNS;

		/**
		 * Used to merge returnKinds of sequential statements into one (e.g. all statements inside a block)
		 * In that case, the highest kind wins.
		 */
		public static ReturnKind max(ReturnKind a, ReturnKind b) {
			return a.ordinal() > b.ordinal() ? a : b;
		}

		/**
		 * Used to merge returnKinds of different branches, e.g. inside an if/else branch or switch/cases.
		 */
		public static ReturnKind branches(ReturnKind a, ReturnKind b) {
			if (a == NEVER_RETURNS && b == NEVER_RETURNS) {
				return NEVER_RETURNS;
			}
			if (a == ALWAYS_RETURNS && b == ALWAYS_RETURNS) {
				return ALWAYS_RETURNS;
			}
			return RETURNS_IN_SOME_CASES;
		}
	}
}
