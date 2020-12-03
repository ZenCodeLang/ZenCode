package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.WhitespacePostComment;
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
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;

public class StatementFormatter implements StatementVisitor<Void> {
	private final ScriptFormattingSettings settings;
	private final StringBuilder output;
	private final ExpressionFormatter expressionFormatter;
	
	private String indent;
	private ParentStatementType position = ParentStatementType.NONE;
	
	public StatementFormatter(StringBuilder output, String indent, ScriptFormattingSettings settings, ExpressionFormatter expressionFormatter) {
		this.output = output;
		this.indent = indent;
		this.settings = settings;
		this.expressionFormatter = expressionFormatter;
	}
	
	@Override
	public String toString() {
		return output.toString();
	}

	@Override
	public Void visitBlock(BlockStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginBlock(whitespace);
		
		String oldIndent = indent;
		indent = oldIndent + settings.indent;
		for (Statement subStatement : statement.statements) {
			format(ParentStatementType.NONE, subStatement);
		}
		
		WhitespacePostComment postComment = statement.getTag(WhitespacePostComment.class);
		if (postComment != null) {
			writePostComments(postComment.comments);
		}
		
		indent = oldIndent;
		
		endBlock(whitespace);
		return null;
	}

	@Override
	public Void visitBreak(BreakStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("break");
		if (statement.target.label != null)
			output.append(' ').append(statement.target.label);
		output.append(";");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitContinue(ContinueStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("continue");
		if (statement.target.label != null)
			output.append(' ').append(statement.target.label);
		output.append(";");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitDoWhile(DoWhileStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("do");
		if (statement.label != null) {
			if (settings.spaceBeforeLabelColon)
				output.append(' ');
			output.append(':');
			if (settings.spaceAfterLabelColon)
				output.append(' ');
			output.append(statement.label);
		}
		format(ParentStatementType.LOOP, statement.content);
		output.append(" while ");
		appendCondition(statement.condition);
		output.append(";");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitEmpty(EmptyStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append(";\n");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitExpression(ExpressionStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
        String value = statement.expression.accept(expressionFormatter).value;
        output.append(value).append(";");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitForeach(ForeachStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("for ");
		for (int i = 0; i < statement.loopVariables.length; i++) {
			if (i > 0)
				output.append(", ");
			
			output.append(statement.loopVariables[i].name);
		}
		output.append(" in ");
		output.append(statement.list.accept(expressionFormatter).value);
		format(ParentStatementType.LOOP, statement.content);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitIf(IfStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		ParentStatementType position = this.position;
		beginSingleLine(whitespace);
		output.append("if ");
		appendCondition(statement.condition);
		format(statement.onElse == null ? ParentStatementType.IF : ParentStatementType.IF_WITH_ELSE, statement.onThen);
		if (statement.onElse != null) {
			output.append("else");
			format(ParentStatementType.ELSE, statement.onElse);
		}
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitLock(LockStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("lock ");
		output.append(statement.object.accept(expressionFormatter).value);
		statement.content.accept(this);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitReturn(ReturnStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("return");
		if (statement.value != null) {
			output.append(' ');
			output.append(statement.value.accept(expressionFormatter).value);
		}
		output.append(";");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitSwitch(SwitchStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("switch ");
		if (statement.label != null)
			output.append(':').append(statement.label);
		
		output.append(settings.getBlockSeparator(indent, position));
		
		StatementFormatter innerFormatter = new StatementFormatter(output, indent + settings.indent + settings.indent, settings, expressionFormatter);
		for (SwitchCase switchCase : statement.cases) {
			if (switchCase.value == null) {
				output.append(indent).append(settings.indent).append("default:\n");
			} else {
				output.append(indent)
						.append(settings.indent)
						.append("case ")
						.append(switchCase.value.accept(new SwitchValueFormatter(settings)))
						.append(":\n");
			}
			for (Statement s : switchCase.statements)
				s.accept(innerFormatter);
		}
		
		output.append("\n").append(indent).append("}");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitThrow(ThrowStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("throw ").append(statement.value.accept(expressionFormatter));
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitTryCatch(TryCatchStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("try");
		if (statement.resource != null) {
			output.append(' ').append(statement.resource.name);
			output.append(" = ");
			output.append(statement.resource.initializer.accept(expressionFormatter).value);
		}
		
		format(ParentStatementType.TRY, statement.content);
		
		for (CatchClause catchClause : statement.catchClauses) {
			output.append(indent).append("catch ");
			if (catchClause.exceptionVariable != null)
				output.append(catchClause.exceptionVariable.name).append(" as ").append(expressionFormatter.typeFormatter.format(catchClause.exceptionVariable.type));
			
			format(ParentStatementType.CATCH, catchClause.content);
		}
		if (statement.finallyClause != null) {
			output.append(indent).append("finally ");
			
			format(ParentStatementType.FINALLY, statement.finallyClause);
		}
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitVar(VarStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append(statement.isFinal ? "val " : "var ");
		output.append(statement.name);
		
		if (statement.initializer == null || statement.initializer.type != statement.type) {
			output.append(" as ");
			output.append(expressionFormatter.typeFormatter.format(statement.type));
		}
		if (statement.initializer != null) {
			output.append(" = ");
            String value = statement.initializer.accept(expressionFormatter).value;
            output.append(value);
		}
		output.append(";");
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitWhile(WhileStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		output.append("while");
		if (statement.label != null) {
			if (settings.spaceBeforeLabelColon)
				output.append(' ');
			output.append(':');
			if (settings.spaceAfterLabelColon)
				output.append(' ');
			output.append(statement.label);
		}
		output.append(' ');
		appendCondition(statement.condition);
		
		format(ParentStatementType.LOOP, statement.content);
		endSingleLine(whitespace);
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
			output.append('(');
		output.append(condition.accept(expressionFormatter).value);
		if (settings.bracketsAroundConditions)
			output.append(')');
	}
	
	private void beginBlock(WhitespaceInfo whitespace) {
		if (whitespace != null && whitespace.emptyLine) {
			output.append("\n").append(indent);
		}
		
		String separator = settings.getBlockSeparator(indent, position);
		output.append(separator);
		
		if (whitespace != null)
			writeComments(whitespace.commentsBefore);
	}
	
	private void endBlock(WhitespaceInfo whitespace) {
		if (whitespace != null && !whitespace.commentsAfter.isEmpty())
			output.append(' ').append(whitespace.commentsAfter);
		
		output.append("\n").append(indent).append("}");
		if (position == ParentStatementType.IF_WITH_ELSE) {
			if (settings.elseBracketOnSameLine)
				output.append(" ");
			else
				output.append("\n").append(indent);
		}
	}
	
	private void beginSingleLine(WhitespaceInfo whitespace) {
		String separator = settings.getSingleLineSeparator(indent, position);
		output.append(separator);
		
		if (whitespace != null) {
			if (whitespace.emptyLine) {
				output.append("\n").append(indent);
			}
			writeComments(whitespace.commentsBefore);
		}
	}
	
	private void endSingleLine(WhitespaceInfo whitespace) {
		if (whitespace != null && !whitespace.commentsAfter.isEmpty())
			output.append(' ').append(whitespace.commentsAfter);
		
		if (position == ParentStatementType.IF_WITH_ELSE)
			output.append("\n").append(indent);
	}
	
	private void writeComments(String[] comments) {
		for (String comment : CommentFormatter.format(comments)) {
			output.append(comment).append("\n").append(indent);
		}
	}
	
	private void writePostComments(String[] comments) {
		for (String comment : CommentFormatter.format(comments)) {
			output.append("\n").append(indent).append(comment);
		}
	}
}
