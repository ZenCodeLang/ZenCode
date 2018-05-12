package org.openzen.zenscript.parser.statements;

import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.linker.ForeachScope;
import org.openzen.zenscript.linker.StatementScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

public class ParsedStatementForeach extends ParsedStatement {
	private final String[] varnames;
	private final ParsedExpression list;
	private final ParsedStatement body;

	public ParsedStatementForeach(CodePosition position, WhitespaceInfo whitespace, String[] varnames, ParsedExpression list, ParsedStatement body) {
		super(position, whitespace);

		this.varnames = varnames;
		this.list = list;
		this.body = body;
	}

	@Override
	public Statement compile(StatementScope scope) {
		Expression list = this.list.compile(new ExpressionScope(scope)).eval();
		
		TypeMembers members = scope.getTypeMembers(list.type);
		IIteratorMember iterator = members.getIterator(varnames.length);
		if (iterator == null)
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_ITERATOR, list.type + " doesn't have an iterator with " + varnames.length + " variables");
		
		ITypeID[] loopTypes = iterator.getLoopVariableTypes();
		VarStatement[] variables = new VarStatement[varnames.length];
		for (int i = 0; i < variables.length; i++)
			variables[i] = new VarStatement(position, varnames[i], loopTypes[i], null, true);
		
		ForeachStatement statement = new ForeachStatement(position, variables, iterator, list);
		ForeachScope innerScope = new ForeachScope(statement, scope);
		statement.content = this.body.compile(innerScope);
		return result(statement);
	}
}
