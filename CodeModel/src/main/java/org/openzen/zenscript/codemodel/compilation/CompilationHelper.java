package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;

import java.util.Optional;

public class CompilationHelper {
	private final ExpressionCompiler compiler;
	private final CodePosition position;

	public CompilationHelper(ExpressionCompiler compiler, CodePosition position) {
		this.compiler = compiler;
		this.position = position;
	}

	public Expression compare(Expression left, CompilingExpression right, CompareType type) {
		ResolvedType resolved = compiler.resolve(left.type);

		if (type == CompareType.EQ) {
			Optional<InstanceCallable> equals = resolved.findOperator(OperatorType.EQUALS);
			if (equals.isPresent()) {
				return equals.get().call(compiler, position, left, TypeID.NONE, right);
			}
		} else if (type == CompareType.NE) {
			Optional<InstanceCallable> notEquals = resolved.findOperator(OperatorType.NOTEQUALS);
			if (notEquals.isPresent()) {
				return notEquals.get().call(compiler, position, left, TypeID.NONE, right);
			} else {
				Optional<InstanceCallable> equals = resolved.findOperator(OperatorType.EQUALS);
				if (equals.isPresent()) {
					Expression value = equals.get().call(compiler, position, left, TypeID.NONE, right);
					return new CallExpression(position, value, new MethodInstance(BuiltinMethodSymbol.BOOL_NOT), CallArguments.EMPTY);
				}
			}
		}
		CastedExpression result = resolved.comparators()
				.stream()
				.map(comparator -> comparator.compare(compiler, position, left, right, type))
				.reduce((a, b) -> {
					if (a.isFailed()) return b;
					if (b.isFailed()) return a;

					if (a.level.compareTo(b.level) == 0) {
						return new CastedExpression(a.level, compiler.at(position).invalid(CompileErrors.ambiguousComparison(a.value.type, b.value.type)));
					} else if (a.level.compareTo(b.level) > 0) {
						return a;
					} else {
						return b;
					}
				})
				.orElseGet(() -> CastedExpression.invalid(position, CompileErrors.noOperatorInType(left.type, OperatorType.COMPARE)));
		return result.value;
	}
}
