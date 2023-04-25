package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.WhitespaceInfo;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.statement.CompilingStatement;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.ssa.CodeBlock;
import org.openzen.zenscript.codemodel.ssa.VarBlockStatement;
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
	public CompilingStatement compile(StatementCompiler compiler, CodeBlock lastBlock) {
		CompilingExpression initializer = this.initializer == null ? null : this.initializer.compile(compiler.expressions());
		lastBlock.add(new VarBlockStatement(initializer));
		CompilingVariable compilingVariable = new CompilingVariable(new VariableID(), name, type == null ? null : type.compile(compiler.types()), isFinal);
		compiler.addLocalVariable(compilingVariable);
		return new Compiling(compiler, compilingVariable, initializer, lastBlock);
	}

	private class Compiling implements CompilingStatement {
		private final StatementCompiler compiler;
		private final CompilingVariable compilingVariable;
		private final CompilingExpression initializer;
		private final CodeBlock block;

		public Compiling(StatementCompiler compiler, CompilingVariable compilingVariable, CompilingExpression initializer, CodeBlock block) {
			this.compiler = compiler;
			this.compilingVariable = compilingVariable;
			this.initializer = initializer;
			this.block = block;
		}

		@Override
		public Statement complete() {
			Expression initializer;
			TypeID ctype;
			if (type == null) {
				if (this.initializer == null)
					return new InvalidStatement(position, CompileErrors.varWithoutTypeOrInitializer());

				initializer = this.initializer.eval();
				ctype = initializer.type;
			} else {
				ctype = type.compile(compiler.types());
				initializer = this.initializer == null ? null : this.initializer.as(ctype);
			}
			VarStatement result = new VarStatement(position, compilingVariable.id, compilingVariable.name, ctype, initializer, isFinal);
			return result(result, compiler);
		}

		@Override
		public CodeBlock getTail() {
			return block;
		}
	}
}
