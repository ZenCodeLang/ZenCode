package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockStatement extends Statement {
	public final Statement[] statements;

	public BlockStatement(CodePosition position, Statement[] statements) {
		super(position, getThrownType(statements));

		this.statements = statements;
	}

	private static TypeID getThrownType(Statement[] statements) {
		TypeID result = null;
		for (Statement statement : statements)
			result = Expression.binaryThrow(statement.position, result, statement.getThrownType());
		return result;
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitBlock(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitBlock(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		for (Statement s : statements) {
			s.forEachStatement(consumer);
		}
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Statement[] tStatements = new Statement[statements.length];
		boolean unchanged = true;
		for (int i = 0; i < statements.length; i++) {
			Statement statement = statements[i];
			Statement tStatement = statement.transform(transformer, modified);
			unchanged &= statement == tStatement;
			tStatements[i] = statement;
		}
		return unchanged ? this : new BlockStatement(position, tStatements);
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Statement[] tStatements = new Statement[statements.length];
		boolean unchanged = true;
		for (int i = 0; i < tStatements.length; i++) {
			Statement tStatement = statements[i].transform(transformer, modified);
			unchanged &= statements[i] == tStatement;
			tStatements[i] = tStatement;
		}
		return unchanged ? this : new BlockStatement(position, tStatements);
	}

	@Override
	public Optional<TypeID> getReturnType() {
		final List<TypeID> collect = Arrays.stream(statements)
				.map(Statement::getReturnType)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.distinct()
				.collect(Collectors.toList());

		if (collect.isEmpty())
			return super.getReturnType();
		else if (collect.size() == 1)
			return Optional.ofNullable(collect.get(0));
		else
			//TODO make this real?
			throw new IllegalStateException("More than one possible type: " + collect.size());
	}
}
