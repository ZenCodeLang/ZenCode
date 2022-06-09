package org.openzen.zenscript.parser.statements;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.compilation.StatementCompiler;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.StatementScope;
import org.openzen.zenscript.codemodel.statement.CatchClause;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.statement.VariableID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.parser.type.IParsedType;

import java.util.List;

public class ParsedCatchClause {
	public final CodePosition position;
	public final String exceptionName;
	public final IParsedType exceptionType;
	public final ParsedStatement content;

	public ParsedCatchClause(CodePosition position, String exceptionName, IParsedType exceptionType, ParsedStatement content) {
		this.position = position;
		this.exceptionName = exceptionName;
		this.exceptionType = exceptionType;
		this.content = content;
	}

	public CatchClause compile(StatementCompiler compiler) {
		VarStatement exceptionVariable = new VarStatement(position, new VariableID(), exceptionName, exceptionType.compile(compiler.types()), null, true);
		return new CatchClause(position, exceptionVariable, content.compile(compiler.forCatch(exceptionVariable)));
	}

	private static class CatchScope extends StatementScope {
		private final StatementScope outer;
		private final VarStatement exceptionVariable;

		public CatchScope(StatementScope outer, VarStatement exceptionVariable) {
			this.outer = outer;
			this.exceptionVariable = exceptionVariable;
		}

		@Override
		public ZSPackage getRootPackage() {
			return outer.getRootPackage();
		}

		@Override
		public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
			if (name.hasNoArguments() && exceptionVariable.name.equals(name.name))
				return new GetLocalVariableExpression(position, exceptionVariable);

			return outer.get(position, name);
		}

		@Override
		public LocalMemberCache getMemberCache() {
			return outer.getMemberCache();
		}

		@Override
		public TypeID getType(CodePosition position, List<GenericName> name) {
			return outer.getType(position, name);
		}

		@Override
		public LoopStatement getLoop(String name) {
			return outer.getLoop(name);
		}

		@Override
		public FunctionHeader getFunctionHeader() {
			return outer.getFunctionHeader();
		}

		@Override
		public TypeID getThisType() {
			return outer.getThisType();
		}

		@Override
		public DollarEvaluator getDollar() {
			return outer.getDollar();
		}

		@Override
		public IPartialExpression getOuterInstance(CodePosition position) throws CompileException {
			return outer.getOuterInstance(position);
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return outer.getAnnotation(name);
		}

		@Override
		public TypeMemberPreparer getPreparer() {
			return outer.getPreparer();
		}

		@Override
		public GenericMapper getLocalTypeParameters() {
			return outer.getLocalTypeParameters();
		}
	}
}
