/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.expression.Expression;
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

/**
 *
 * @author Hoofdgebruiker
 */
public class StatementFormatter implements StatementVisitor<Void> {
	private final FormattingSettings settings;
	private final StringBuilder result;
	private final ExpressionFormatter expressionFormatter;
	
	private String indent;
	private ParentStatementType position = ParentStatementType.NONE;
	
	public StatementFormatter(String indent, FormattingSettings settings, ExpressionFormatter expressionFormatter) {
		this.indent = indent;
		this.settings = settings;
		result = new StringBuilder();
		this.expressionFormatter = expressionFormatter;
	}
	
	@Override
	public String toString() {
		return result.toString();
	}

	@Override
	public Void visitBlock(BlockStatement statement) {
		beginBlock();
		
		String oldIndent = indent;
		indent = oldIndent + settings.indent;
		for (Statement subStatement : statement.statements) {
			format(ParentStatementType.NONE, subStatement);
		}
		indent = oldIndent;
		endBlock();
		return null;
	}

	@Override
	public Void visitBreak(BreakStatement statement) {
		beginSingleLine();
		result.append("break");
		if (statement.target != null)
			result.append(' ').append(statement.target.label);
		result.append(";");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitContinue(ContinueStatement statement) {
		beginSingleLine();
		result.append("continue");
		if (statement.target != null)
			result.append(' ').append(statement.target.label);
		result.append(";");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitDoWhile(DoWhileStatement statement) {
		beginSingleLine();
		result.append("do");
		if (statement.label != null) {
			if (settings.spaceBeforeLabelColon)
				result.append(' ');
			result.append(':');
			if (settings.spaceAfterLabelColon)
				result.append(' ');
			result.append(statement.label);
		}
		format(ParentStatementType.LOOP, statement.content);
		result.append(" while ");
		appendCondition(statement.condition);
		result.append(";");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitEmpty(EmptyStatement statement) {
		beginSingleLine();
		result.append(";\n");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitExpression(ExpressionStatement statement) {
		beginSingleLine();
		result.append(statement.expression.accept(expressionFormatter).value)
			  .append(";");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitForeach(ForeachStatement statement) {
		beginSingleLine();
		result.append("for ");
		for (int i = 0; i < statement.loopVariables.length; i++) {
			if (i > 0)
				result.append(", ");
			
			result.append(statement.loopVariables[i].name);
		}
		result.append(" in ");
		result.append(statement.list.accept(expressionFormatter).value);
		format(ParentStatementType.LOOP, statement.content);
		endSingleLine();
		return null;
	}

	@Override
	public Void visitIf(IfStatement statement) {
		ParentStatementType position = this.position;
		beginSingleLine();
		result.append("if ");
		appendCondition(statement.condition);
		format(statement.onElse == null ? ParentStatementType.IF : ParentStatementType.IF_WITH_ELSE, statement.onThen);
		if (statement.onElse != null) {
			result.append("else");
			format(ParentStatementType.ELSE, statement.onElse);
		}
		endSingleLine();
		return null;
	}

	@Override
	public Void visitLock(LockStatement statement) {
		beginSingleLine();
		result.append("lock ");
		result.append(statement.object.accept(expressionFormatter).value);
		statement.content.accept(this);
		endSingleLine();
		return null;
	}

	@Override
	public Void visitReturn(ReturnStatement statement) {
		beginSingleLine();
		result.append("return");
		if (statement.value != null) {
			result.append(' ');
			result.append(statement.value.accept(expressionFormatter).value);
		}
		result.append(";");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitThrow(ThrowStatement statement) {
		beginSingleLine();
		result.append("throw ").append(statement.value.accept(expressionFormatter));
		endSingleLine();
		return null;
	}

	@Override
	public Void visitTryCatch(TryCatchStatement statement) {
		beginSingleLine();
		result.append("try");
		if (statement.resource != null) {
			result.append(' ').append(statement.resource.name);
			result.append(" = ");
			result.append(statement.resource.initializer.accept(expressionFormatter).value);
		}
		
		format(ParentStatementType.TRY, statement.content);
		
		for (CatchClause catchClause : statement.catchClauses) {
			result.append(indent).append("catch ");
			if (catchClause.exceptionName != null)
				result.append(catchClause.exceptionName);
			if (catchClause.exceptionType != BasicTypeID.ANY) {
				result.append(" as ");
				catchClause.exceptionType.accept(expressionFormatter.typeFormatter);
			}
			
			format(ParentStatementType.CATCH, catchClause.content);
		}
		if (statement.finallyClause != null) {
			result.append(indent).append("finally ");
			
			format(ParentStatementType.FINALLY, statement.finallyClause);
		}
		endSingleLine();
		return null;
	}

	@Override
	public Void visitVar(VarStatement statement) {
		beginSingleLine();
		result.append(statement.isFinal ? "val " : "var ");
		result.append(statement.name);
		
		if (statement.initializer == null || statement.initializer.type != statement.type) {
			result.append(" as ");
			result.append(statement.type.accept(expressionFormatter.typeFormatter));
		}
		if (statement.initializer != null) {
			result.append(" = ");
			result.append(statement.initializer.accept(expressionFormatter).value);
		}
		result.append(";");
		endSingleLine();
		return null;
	}

	@Override
	public Void visitWhile(WhileStatement statement) {
		beginSingleLine();
		result.append("while");
		if (statement.label != null) {
			if (settings.spaceBeforeLabelColon)
				result.append(' ');
			result.append(':');
			if (settings.spaceAfterLabelColon)
				result.append(' ');
			result.append(statement.label);
		}
		result.append(' ');
		appendCondition(statement.condition);
		
		format(ParentStatementType.LOOP, statement.content);
		endSingleLine();
		return null;
	}
	
	private void format(ParentStatementType position, Statement statement) {
		ParentStatementType oldPosition = this.position;
		this.position = position;
		statement.accept(this);
		this.position = oldPosition;
	}
	
	private void appendCondition(Expression condition) {
		if (settings.bracketsAroundConditions)
			result.append('(');
		result.append(condition.accept(expressionFormatter).value);
		if (settings.bracketsAroundConditions)
			result.append(')');
	}
	
	private void beginBlock() {
		result.append(settings.getBlockSeparator(indent, position));
	}
	
	private void endBlock() {
		result.append("\n").append(indent).append("}");
		if (position == ParentStatementType.IF_WITH_ELSE) {
			if (settings.elseBracketOnSameLine)
				result.append(" ");
			else
				result.append("\n").append(indent);
		}
	}
	
	private void beginSingleLine() {
		result.append(settings.getSingleLineSeparator(indent, position));
	}
	
	private void endSingleLine() {
		if (position == ParentStatementType.IF_WITH_ELSE)
			result.append("\n").append(indent);
	}
}
