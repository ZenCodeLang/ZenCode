/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.StatementSerializationContext;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.BreakStatement;
import org.openzen.zenscript.codemodel.statement.ContinueStatement;
import org.openzen.zenscript.codemodel.statement.DoWhileStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.IfStatement;
import org.openzen.zenscript.codemodel.statement.LockStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementVisitorWithContext;
import org.openzen.zenscript.codemodel.statement.SwitchCase;
import org.openzen.zenscript.codemodel.statement.SwitchStatement;
import org.openzen.zenscript.codemodel.statement.ThrowStatement;
import org.openzen.zenscript.codemodel.statement.TryCatchStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.WhileStatement;
import org.openzen.zenscript.moduleserialization.StatementEncoding;

/**
 * @author Hoofdgebruiker
 */
public class StatementSerializer implements StatementVisitorWithContext<StatementSerializationContext, Void> {
	private final CodeSerializationOutput output;
	private final boolean positions;
	private final boolean localVariableNames;

	public StatementSerializer(
			CodeSerializationOutput output,
			boolean positions,
			boolean localVariableNames) {
		this.output = output;
		this.positions = positions;
		this.localVariableNames = localVariableNames;
	}

	private int getFlags(Statement statement) {
		int flags = 0;
		if (statement.position != CodePosition.UNKNOWN && positions)
			flags |= StatementEncoding.FLAG_POSITION;

		return flags;
	}

	private void encode(int flags, Statement statement) {
		if ((flags & StatementEncoding.FLAG_POSITION) > 0)
			output.serialize(statement.position);
	}

	@Override
	public Void visitBlock(StatementSerializationContext context, BlockStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_BLOCK);
		int flags = getFlags(statement);
		encode(flags, statement);

		output.writeUInt(statement.statements.length);
		StatementSerializationContext inner = new StatementSerializationContext(context);
		for (Statement s : statement.statements)
			output.serialize(inner, s);
		return null;
	}

	@Override
	public Void visitBreak(StatementSerializationContext context, BreakStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_BREAK);
		int flags = getFlags(statement);
		encode(flags, statement);

		output.writeUInt(context.getLoopId(statement.target));
		return null;
	}

	@Override
	public Void visitContinue(StatementSerializationContext context, ContinueStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_CONTINUE);
		int flags = getFlags(statement);
		encode(flags, statement);

		output.writeUInt(context.getLoopId(statement.target));
		return null;
	}

	@Override
	public Void visitDoWhile(StatementSerializationContext context, DoWhileStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_DO_WHILE);
		int flags = getFlags(statement);
		if (statement.label != null)
			flags |= StatementEncoding.FLAG_LABEL;
		encode(flags, statement);

		output.serialize(context, statement.condition);
		if ((flags & StatementEncoding.FLAG_LABEL) > 0)
			output.writeString(statement.label);
		StatementSerializationContext inner = context.forLoop(statement);
		output.serialize(inner, statement.content);
		return null;
	}

	@Override
	public Void visitEmpty(StatementSerializationContext context, EmptyStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_EMPTY);
		int flags = getFlags(statement);
		encode(flags, statement);
		return null;
	}

	@Override
	public Void visitExpression(StatementSerializationContext context, ExpressionStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_EXPRESSION);
		int flags = getFlags(statement);
		encode(flags, statement);
		output.serialize(context, statement.expression);
		return null;
	}

	@Override
	public Void visitForeach(StatementSerializationContext context, ForeachStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_FOREACH);
		int flags = getFlags(statement);
		if (localVariableNames)
			flags |= StatementEncoding.FLAG_NAME;
		encode(flags, statement);
		output.serialize(context, statement.list);
		output.write(context, statement.iterator);
		if ((flags & StatementEncoding.FLAG_NAME) > 0) {
			for (VarStatement loopVariable : statement.loopVariables)
				output.writeString(loopVariable.name);
		}
		StatementSerializationContext inner = context.forLoop(statement);
		for (VarStatement variable : statement.loopVariables)
			inner.add(variable);

		output.serialize(inner, statement.content);
		return null;
	}

	@Override
	public Void visitIf(StatementSerializationContext context, IfStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_IF);
		int flags = getFlags(statement);
		encode(flags, statement);
		output.serialize(context, statement.condition);
		output.serialize(context, statement.onThen);
		output.serialize(context, statement.onElse);
		return null;
	}

	@Override
	public Void visitLock(StatementSerializationContext context, LockStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_LOCK);
		int flags = getFlags(statement);
		encode(flags, statement);
		output.serialize(context, statement.object);
		output.serialize(context, statement.content);
		return null;
	}

	@Override
	public Void visitReturn(StatementSerializationContext context, ReturnStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_RETURN);
		int flags = getFlags(statement);
		encode(flags, statement);
		output.serialize(context, statement.value);
		return null;
	}

	@Override
	public Void visitSwitch(StatementSerializationContext context, SwitchStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_RETURN);
		int flags = getFlags(statement);
		if (statement.label != null)
			flags |= StatementEncoding.FLAG_LABEL;
		if (localVariableNames)
			flags |= StatementEncoding.FLAG_NAME;
		encode(flags, statement);

		output.serialize(context, statement.value);
		if ((flags & StatementEncoding.FLAG_LABEL) > 0)
			output.writeString(statement.label);
		output.writeUInt(statement.cases.size());

		StatementSerializationContext inner = context.forLoop(statement);
		for (SwitchCase case_ : statement.cases) {
			output.serialize(context, case_.value);
			output.writeUInt(case_.statements.length);
			for (Statement s : case_.statements) {
				output.serialize(inner, s);
			}
		}
		return null;
	}

	@Override
	public Void visitThrow(StatementSerializationContext context, ThrowStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_THROW);
		int flags = getFlags(statement);
		encode(flags, statement);
		output.serialize(context, statement.value);
		return null;
	}

	@Override
	public Void visitTryCatch(StatementSerializationContext context, TryCatchStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_TRY_CATCH);
		int flags = getFlags(statement);
		encode(flags, statement);

		throw new UnsupportedOperationException("Not supported yet");
	}

	@Override
	public Void visitVar(StatementSerializationContext context, VarStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_VAR);
		int flags = getFlags(statement);
		if (statement.isFinal)
			flags |= StatementEncoding.FLAG_FINAL;
		if (statement.name != null && localVariableNames)
			flags |= StatementEncoding.FLAG_NAME;
		encode(flags, statement);

		output.serialize(context, statement.type);
		if ((flags & StatementEncoding.FLAG_NAME) > 0)
			output.writeString(statement.name);
		output.serialize(context, statement.initializer);

		context.add(statement);
		return null;
	}

	@Override
	public Void visitWhile(StatementSerializationContext context, WhileStatement statement) {
		output.writeUInt(StatementEncoding.TYPE_WHILE);
		int flags = getFlags(statement);
		if (statement.label != null)
			flags |= StatementEncoding.FLAG_LABEL;
		encode(flags, statement);

		output.serialize(context, statement.condition);
		if ((flags & StatementEncoding.FLAG_LABEL) > 0)
			output.writeString(statement.label);
		output.serialize(new StatementSerializationContext(context, statement), statement.content);
		return null;
	}
}
