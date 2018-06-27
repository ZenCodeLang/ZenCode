/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formattershared;

import java.util.List;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.BreakStatement;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementVisitor;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;

/**
 *
 * @author Hoofdgebruiker
 */
public class StatementFormatter implements StatementVisitor<Void>, StatementFormattingTarget {
	private final StringBuilder output;
	private final Formatter formatter;
	private final String indent;
	private final FormattingSettings settings;
	private final LoopStatement innerLoop;
	
	public StatementFormatter(StringBuilder output, FormattingSettings settings, Formatter formatter, String indent, LoopStatement innerLoop) {
		this.output = output;
		this.formatter = formatter;
		this.indent = indent;
		this.settings = settings;
		this.innerLoop = innerLoop;
	}

	@Override
	public Void visitBlock(BlockStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginBlock(whitespace);
		formatter.formatBlock(this, statement);
		endBlock(whitespace);
		return null;
	}

	@Override
	public Void visitBreak(BreakStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatBreak(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitContinue(ContinueStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatContinue(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitDoWhile(DoWhileStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatDoWhile(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitEmpty(EmptyStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatEmpty(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitExpression(ExpressionStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatExpression(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitForeach(ForeachStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatForeach(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitIf(IfStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatIf(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitLock(LockStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatLock(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitReturn(ReturnStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatReturn(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitSwitch(SwitchStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatSwitch(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitThrow(ThrowStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatThrow(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitTryCatch(TryCatchStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatTryCatch(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitVar(VarStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatVar(this, statement);
		endSingleLine(whitespace);
		return null;
	}

	@Override
	public Void visitWhile(WhileStatement statement) {
		WhitespaceInfo whitespace = statement.getTag(WhitespaceInfo.class);
		beginSingleLine(whitespace);
		formatter.formatWhile(this, statement);
		endSingleLine(whitespace);
		return null;
	}
	
	// == StatementFormattingTarget implementation ==
	
	@Override
	public LoopStatement getInnerLoop() {
		return innerLoop;
	}

	@Override
	public String getIndent() {
		return indent;
	}
	
	@Override
	public void writeLine(String line) {
		output.append('\n').append(indent).append(line);
	}

	@Override
	public void writeInner(String lineBefore, Statement contents, LoopStatement loop, String lineAfter) {
		output.append('\n').append(indent).append(lineBefore);
		contents.accept(new StatementFormatter(output, settings, formatter, indent + settings.indent, loop == null ? innerLoop : loop));
		
		if (!lineAfter.isEmpty())
			output.append('\n').append(indent).append(lineAfter);
	}

	@Override
	public void writeInnerMulti(String lineBefore, List<StatementFormattingSubBlock> contents, LoopStatement loop, String lineAfter) {
		output.append('\n').append(indent).append(lineBefore);
		
		String newIndent = indent + settings.indent + settings.indent;
		StatementFormatter inner = new StatementFormatter(output, settings, formatter, newIndent, innerLoop);
		
		for (StatementFormattingSubBlock subBlock : contents) {
			output.append('\n').append(indent).append(settings.indent).append(subBlock.header);
			for (String literal : subBlock.literalStatements)
				output.append('\n').append(newIndent).append(literal);
			for (Statement statement : subBlock.statements)
				statement.accept(inner);
		}
		
		if (!lineAfter.isEmpty())
			output.append('\n').append(indent).append(lineAfter);
	}

	@Override
	public void writeBlock(String lineBefore, BlockStatement contents, String lineAfter) {
		output.append(' ').append(lineBefore);
		
		StatementFormatter inner = new StatementFormatter(output, settings, formatter, indent, innerLoop);
		for (Statement statement : contents.statements)
			statement.accept(inner);
		
		if (!lineAfter.isEmpty())
			output.append('\n').append(indent.substring(0, indent.length() - settings.indent.length())).append(lineAfter);
	}
	
	private void beginBlock(WhitespaceInfo whitespace) {
		if (whitespace != null && whitespace.emptyLine) {
			output.append("\n").append(indent);
		}
		
		if (whitespace != null)
			writeComments(whitespace.commentsBefore);
	}
	
	private void endBlock(WhitespaceInfo whitespace) {
		if (whitespace != null && !whitespace.commentsAfter.isEmpty())
			output.append(' ').append(whitespace.commentsAfter);
	}
	
	private void beginSingleLine(WhitespaceInfo whitespace) {
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
	}
	
	private void writeComments(String[] comments) {
		for (String comment : settings.commentFormatter.format(comments)) {
			output.append(comment).append("\n").append(indent);
		}
	}
	
	private void writePostComments(String[] comments) {
		for (String comment : settings.commentFormatter.format(comments)) {
			output.append("\n").append(indent).append(comment);
		}
	}
	
	public interface Formatter {
		public void formatBlock(StatementFormattingTarget target, BlockStatement statement);
	
		public void formatBreak(StatementFormattingTarget target, BreakStatement statement);

		public void formatContinue(StatementFormattingTarget target, ContinueStatement statement);

		public void formatDoWhile(StatementFormattingTarget target, DoWhileStatement statement);

		public void formatEmpty(StatementFormattingTarget target, EmptyStatement statement);

		public void formatExpression(StatementFormattingTarget target, ExpressionStatement statement);

		public void formatForeach(StatementFormattingTarget target, ForeachStatement statement);

		public void formatIf(StatementFormattingTarget target, IfStatement statement);

		public void formatLock(StatementFormattingTarget target, LockStatement statement);

		public void formatReturn(StatementFormattingTarget target, ReturnStatement statement);

		public void formatSwitch(StatementFormattingTarget target, SwitchStatement statement);

		public void formatThrow(StatementFormattingTarget target, ThrowStatement statement);

		public void formatTryCatch(StatementFormattingTarget target, TryCatchStatement statement);

		public void formatVar(StatementFormattingTarget target, VarStatement statement);

		public void formatWhile(StatementFormattingTarget target, WhileStatement statement);
	}
}
