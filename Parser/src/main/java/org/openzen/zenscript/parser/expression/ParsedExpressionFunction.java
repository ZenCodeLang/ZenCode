package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

import java.util.Optional;

public class ParsedExpressionFunction extends ParsedExpression {
	public final CompilableLambdaHeader header;
	public final ParsedFunctionBody body;

	public ParsedExpressionFunction(CodePosition position, CompilableLambdaHeader header, ParsedFunctionBody body) {
		super(position);

		this.header = header;
		this.body = body;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		FunctionHeader definedHeader = header.compile(compiler.types());
		return new Compiling(compiler, position, definedHeader, body);
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final FunctionHeader header;
		private final ParsedFunctionBody body;

		public Compiling(ExpressionCompiler compiler, CodePosition position, FunctionHeader header, ParsedFunctionBody body) {
			super(compiler, position);
			this.header = header;
			this.body = body;
		}

		@Override
		public Expression eval() {
			LambdaClosure closure = new LambdaClosure();
			StatementCompiler functionCompiler = compiler.forLambda(closure, header);
			return compiler.at(position).lambda(closure, header, body.compile(functionCompiler));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			TypeID type = cast.type.simplified();
			Optional<FunctionTypeID> maybeFunction = type.asFunction();
			if (maybeFunction.isPresent()) {
				FunctionHeader inferredHeader = maybeFunction.get().header;
				FunctionHeader header = this.header;
				if (header.canOverride(compiler, inferredHeader)) {
					header = inferredHeader.forLambda(this.header);
				}

				/*FunctionHeader genericHeader = header;
				if (!scope.genericInferenceMap.isEmpty()) {
					// prepare for type parameter inference
					header = header.forTypeParameterInference();
				}*/

				if (header.hasInvalidTypes()) {
					CompileError error = CompileErrors.invalidLambdaHeader(header);
					return CastedExpression.invalid(compiler.at(position).invalid(error), error);
				}

				LambdaClosure closure = new LambdaClosure();
				StatementCompiler functionCompiler = compiler.forLambda(closure, header);
				Statement statement = body.compile(functionCompiler);
				//StatementScope innerScope = new LambdaScope(scope, closure, header);
				//Statement statements = body.compile(innerScope, header);

				if (header.getReturnType() == BasicTypeID.UNDETERMINED) {
					Optional<TypeID> returnType = statement.getReturnType();
					/*if (returnType == null) {
						if (header.getReturnType() != BasicTypeID.UNDETERMINED) {
							returnType = genericHeader.getReturnType();
						} else {
							returnType = new InvalidTypeID(position, CompileErrors.cannotInfer());
						}
					}*/

					if (returnType.isPresent()) {
						header = header.withReturnType(returnType.get());
					}
				}

				/*if (genericHeader.typeParameters.length > 0 && !scope.genericInferenceMap.isEmpty()) {
					// perform type parameter inference
					TypeID returnType = statements.getReturnType();
					if (returnType != null) {
						Map<TypeParameter, TypeID> inferredTypes = returnType.inferTypeParameters(genericHeader.getReturnType());
						if (inferredTypes == null) {
							throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer generic type parameters");
						}

						scope.genericInferenceMap.putAll(inferredTypes);
					}

				}

				final FunctionHeader thatOtherHeader = genericHeader.withGenericArguments(new GenericMapper(position, scope.getTypeRegistry(), scope.genericInferenceMap));
				if (thatOtherHeader.getReturnType() == BasicTypeID.UNDETERMINED) {
					thatOtherHeader.setReturnType(header.getReturnType());
				}*/

				return cast.of(CastedExpression.Level.EXACT, compiler.at(position).lambda(closure, header, statement));
			} else {
				return cast.of(eval());
			}
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			// TODO - SSA doesn't yet go past lambda boundaries
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			// TODO - SSA doesn't yet go past lambda boundaries
		}
	}
}
