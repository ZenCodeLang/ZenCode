package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompileErrors;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.statement.InvalidStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedStatementVar extends ParsedStatement {
	private final String name;
	private final IParsedType type;
	private final CompilableExpression initializer;
	private final boolean isFinal;

	public ParsedStatementVar(CodePosition position, ParsedAnnotation[] annotations, WhitespaceInfo whitespace, String name, IParsedType type, CompilableExpression initializer, boolean isFinal) {
		super(position, annotations, whitespace);

		this.name = name;
		this.type = type;
		this.initializer = initializer;
		this.isFinal = isFinal;
	}

	@Override
	public Statement compile(StatementCompiler compiler) {
		Expression initializer;
		TypeID type;
		if (this.type == null) {
			if (this.initializer == null)
				return new InvalidStatement(position, CompileErrors.varWithoutTypeOrInitializer());

			initializer = compiler.compile(this.initializer);
			type = initializer.type;
		} else {
			type = this.type.compile(compiler.types());
			initializer = this.initializer == null ? null : compiler.compile(this.initializer, type);
		}
		VarStatement result = new VarStatement(position, new VariableID(), name, type, initializer, isFinal);
		compiler.addLocalVariable(result);
		return result(result, compiler);
	}
}
