package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;

import java.util.Optional;

public class ParsedStatementForeach extends ParsedStatement {
	private final String[] varnames;
	private final CompilableExpression list;
	private final ParsedStatement body;

	public ParsedStatementForeach(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String[] varnames, CompilableExpression list, ParsedStatement body) {
		super(position, annotations, whitespace);

		this.varnames = varnames;
		this.list = list;
		this.body = body;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Expression list = compiler.compile(this.list);

		ResolvedType listType = compiler.resolve(list.type);
		Optional<IteratorMemberRef> maybeIterator = listType.findIterator(varnames.length);
		if (!maybeIterator.isPresent())
			return new InvalidStatement(position, CompileErrors.noSuchIterator(list.type, varnames.length));

		IteratorMemberRef iterator = maybeIterator.get();
		TypeID[] loopTypes = iterator.types;
		VarStatement[] variables = new VarStatement[varnames.length];
		for (int i = 0; i < variables.length; i++)
			variables[i] = new VarStatement(position, new VariableID(), varnames[i], loopTypes[i], null, true);

		ForeachStatement statement = new ForeachStatement(position, variables, iterator, list);
		statement.content = this.body.compile(compiler.forForeach(statement));
		return result(statement, compiler);
	}
}
