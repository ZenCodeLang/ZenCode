package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ParsedExpressionIndex extends ParsedExpression {
	private final ParsedExpression value;
	private final List<ParsedExpression> indexes;

	public ParsedExpressionIndex(CodePosition position, ParsedExpression value, List<ParsedExpression> indexes) {
		super(position);

		this.value = value;
		this.indexes = indexes;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(
				compiler,
				position,
				value.compile(compiler),
				indexes.stream().map(ix -> ix.compile(compiler)).toArray(CompilingExpression[]::new));
	}

	private class Compiling extends AbstractCompilingExpression {
		private final CompilingExpression value;
		private final CompilingExpression[] indexes;

		public Compiling(ExpressionCompiler compiler, CodePosition position, CompilingExpression value, CompilingExpression[] indexes) {
			super(compiler, position);
			this.value = value;
			this.indexes = indexes;
		}

		@Override
		public Expression as(TypeID type) {
			InferredType valueType = value.inferType();
		}

		@Override
		public Optional<ResolvedCallable> call() {
			return Optional.empty();
		}

		@Override
		public Optional<CompilingExpression> getMember(CodePosition position, GenericName name) {
			return Optional.empty();
		}

		@Override
		public Expression assign(Expression value) {
			return null;
		}

		@Override
		public InferredType inferType() {
			return null;
		}

		@Override
		public Optional<TypeID> inferAssignType() {
			return Optional.empty();
		}
	}

	private class PartialIndexedExpression implements IPartialExpression {
		private final ExpressionScope scope;
		private final Expression target;

		private PartialIndexedExpression(ExpressionScope scope) throws CompileException {
			this.scope = scope;
			target = value.compile(scope.withoutHints()).eval();
		}

		@Override
		public Expression eval() throws CompileException {
			TypeMemberGroup members = scope.getTypeMembers(target.type).getOrCreateGroup(OperatorType.INDEXGET);
			List<TypeID>[] predictedTypes = members.predictCallTypes(position, scope, scope.hints, indexes.size());
			Expression[] arguments = new Expression[indexes.size()];
			for (int i = 0; i < arguments.length; i++)
				arguments[i] = indexes.get(i).compile(scope.createInner(predictedTypes[i], this::getLength)).eval();

			return members.call(position, scope, target, new CallArguments(arguments), false);
		}

		@Override
		public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
			return eval().predictCallTypes(position, scope, hints, arguments);
		}

		@Override
		public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) throws CompileException {
			return eval().getPossibleFunctionHeaders(scope, hints, arguments);
		}

		@Override
		public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
			return eval().getMember(position, scope, hints, name);
		}

		@Override
		public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
			return eval().call(position, scope, hints, arguments);
		}

		@Override
		public Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
			TypeMemberGroup members = scope.getTypeMembers(target.type).getOrCreateGroup(OperatorType.INDEXSET);
			List<TypeID>[] predictedTypes = members.predictCallTypes(position, scope, this.scope.hints, indexes.size() + 1);

			Expression[] arguments = new Expression[indexes.size() + 1];
			for (int i = 0; i < arguments.length - 1; i++)
				arguments[i] = indexes.get(i).compile(this.scope.createInner(predictedTypes[i], this::getLength)).eval();
			arguments[indexes.size()] = value;

			return members.call(position, scope, target, new CallArguments(arguments), false);
		}

		@Override
		public List<TypeID> getAssignHints() {
			TypeMemberGroup members = scope.getTypeMembers(target.type).getOrCreateGroup(OperatorType.INDEXSET);
			List<TypeID>[] predictedTypes = members.predictCallTypes(position, scope, scope.hints, indexes.size() + 1);
			return predictedTypes[indexes.size()];
		}

		private Expression getLength(CodePosition position) throws CompileException {
			return target.getMember(position, scope, scope.hints, new GenericName("length")).eval();
		}

		@Override
		public TypeID[] getTypeArguments() {
			return null;
		}
	}
}
