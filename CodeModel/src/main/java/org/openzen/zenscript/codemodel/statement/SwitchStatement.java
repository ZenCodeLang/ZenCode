package org.openzen.zenscript.codemodel.statement;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.ConcatMap;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.ExpressionTransformer;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SwitchStatement extends LoopStatement {
	public final Expression value;
	public final List<SwitchCase> cases = new ArrayList<>();

	public SwitchStatement(CodePosition position, String label, Expression value) {
		super(position, label, null); // TODO: thrown type

		this.value = value;
	}

	public void addCase(SwitchCase case_) {
		cases.add(case_);
	}

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visitSwitch(this);
	}

	@Override
	public <C, R> R accept(C context, StatementVisitorWithContext<C, R> visitor) {
		return visitor.visitSwitch(context, this);
	}

	@Override
	public void forEachStatement(Consumer<Statement> consumer) {
		consumer.accept(this);
		for (SwitchCase switchCase : cases) {
			for (Statement statement : switchCase.statements)
				statement.forEachStatement(consumer);
		}
	}

	@Override
	public Statement transform(StatementTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		SwitchStatement result = new SwitchStatement(position, label, tValue);
		ConcatMap<LoopStatement, LoopStatement> tModified = modified.concat(this, result);
		for (SwitchCase case_ : cases) {
			result.cases.add(case_.transform(transformer, tModified));
		}
		return result;
	}

	@Override
	public Statement transform(ExpressionTransformer transformer, ConcatMap<LoopStatement, LoopStatement> modified) {
		Expression tValue = value.transform(transformer);
		SwitchStatement result = new SwitchStatement(position, label, tValue);
		ConcatMap<LoopStatement, LoopStatement> tModified = modified.concat(this, result);
		for (SwitchCase case_ : cases) {
			result.cases.add(case_.transform(transformer, tModified));
		}
		return result;
	}

	@Override
	public TypeID getReturnType() {
		//return super.getReturnType();
		final List<TypeID> collect = cases.stream()
				.flatMap(aCase -> Arrays.stream(aCase.statements))
				.map(Statement::getReturnType)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (collect.isEmpty())
			return null;

		if (collect.size() == 1)
			return collect.get(0);

		final long c = collect.stream().distinct().count();
		if (c == 1)
			return collect.get(0);
		else
			//TODO make this real
			throw new IllegalStateException("Too many possible types: " + c);
	}
}
