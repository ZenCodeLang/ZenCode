package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.statement.ForeachStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.ForeachScope;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.expression.ParsedExpression;

public class ParsedStatementForeach extends ParsedStatement {
	private final String[] varnames;
	private final ParsedExpression list;
	private final ParsedStatement body;

	public ParsedStatementForeach(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String[] varnames, ParsedExpression list, ParsedStatement body) {
		super(position, annotations, whitespace);

		this.varnames = varnames;
		this.list = list;
		this.body = body;
	}

	@Override
	public Statement compile(StatementScope scope) {
		try {
			Expression list = this.list.compile(new ExpressionScope(scope)).eval();

			TypeMembers members = scope.getTypeMembers(list.type);
			IteratorMemberRef iterator = members.getIterator(varnames.length);
			if (iterator == null)
				return new InvalidStatement(position, CompileExceptionCode.NO_SUCH_ITERATOR, list.type + " doesn't have an iterator with " + varnames.length + " variables");

			TypeID[] loopTypes = iterator.types;
			VarStatement[] variables = new VarStatement[varnames.length];
			for (int i = 0; i < variables.length; i++)
				variables[i] = new VarStatement(position, new VariableID(), varnames[i], loopTypes[i], null, true);

			ForeachStatement statement = new ForeachStatement(position, variables, iterator, list);
			ForeachScope innerScope = new ForeachScope(statement, scope);
			statement.content = this.body.compile(innerScope);
			return result(statement, scope);
		} catch (CompileException ex) {
			return result(new InvalidStatement(ex), scope);
		}
	}
}
