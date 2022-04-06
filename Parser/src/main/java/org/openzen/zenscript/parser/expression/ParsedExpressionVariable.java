package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.compiler.InferredType;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.expression.AbstractCompilingExpression;
import org.openzen.zenscript.compiler.expression.TypeMatch;
import org.openzen.zenscript.compiler.types.ResolvedType;
import org.openzen.zenscript.compiler.expression.CompilingExpression;
import org.openzen.zenscript.compiler.expression.ExpressionCompiler;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;

import java.util.*;

public class ParsedExpressionVariable extends ParsedExpression {
	public final String name;
	private final List<IParsedType> typeArguments;

	public ParsedExpressionVariable(CodePosition position, String name, List<IParsedType> typeArguments) {
		super(position);

		this.name = name;
		this.typeArguments = typeArguments;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		TypeID[] typeArguments = IParsedType.compileTypes(this.typeArguments, compiler.types());

		Optional<CompilingExpression> resolved = compiler.resolve(new GenericName(name, typeArguments));
		return new Compiling(compiler, position, name, resolved.orElse(null));
	}

	private static class Compiling extends AbstractCompilingExpression implements ResolvedCallable {
		private final String name;
		private final CompilingExpression resolved; // can be null

		public Compiling(ExpressionCompiler compiler, CodePosition position, String name, CompilingExpression resolved) {
			super(compiler, position);
			this.name = name;
			this.resolved = resolved;
		}

		@Override
		public Expression as(TypeID type) {
			if (resolved != null) {
				return resolved.as(type);
			} else {
				return compiler.resolve(type).getContextMember(name)
						.map(member -> member.as(type))
						.orElseGet(() -> compiler.at(position, type).invalid(
								CompileExceptionCode.NO_SUCH_MEMBER,
								"Could not find context member " + name + " in " + type));
			}
		}

		@Override
		public TypeMatch matches(TypeID returnType) {
			if (resolved != null) {
				return resolved.matches(returnType);
			} else {
				ResolvedType resolved = compiler.resolve(returnType);
				if (resolved.getContextMember(name).isPresent())
					return TypeMatch.EXACT;
				else
					return TypeMatch.NONE;
			}
		}

		@Override
		public Optional<ResolvedCallable> call() {
			if (resolved == null) {
				return Optional.of(this);
			} else {
				return resolved.call();
			}
		}

		@Override
		public Expression assign(Expression value) {
			if (resolved != null) {
				return resolved.assign(value);
			} else {
				return compiler.at(position, value.type).invalid(CompileExceptionCode.UNDEFINED_VARIABLE, "No such variable: " + name);
			}
		}

		@Override
		public InferredType inferType() {
			if (resolved != null) {
				return resolved.inferType();
			} else {
				return InferredType.failure(CompileExceptionCode.UNDEFINED_VARIABLE, generateNotFoundMessage());
			}
		}

		private String generateNotFoundMessage() {
			StringBuilder builder = new StringBuilder("No such symbol: " + name);
			List<String> possibleImports = compiler.findCandidateImports(name);
			if(!possibleImports.isEmpty()){
				builder.append("\nPossible imports:");
				possibleImports.forEach(name -> {
					builder.append("\n").append(name);
				});
			}
			return builder.toString();
		}

		// ###########################################
		// ###   ResolvedCallable implementation   ###
		// ### (only used on unresolved variables) ###
		// ###########################################

		@Override
		public Expression call(TypeID returnType, CompilingExpression... arguments) {
			ResolvedType resolvedType = compiler.resolve(returnType);
			return resolvedType.getContextMember(name)
					.map(member -> member.call()
								.map(call -> call.call(returnType, arguments))
								.orElseGet(() -> compiler.at(position, returnType).invalid(
										CompileExceptionCode.CALL_NO_VALID_METHOD,
										"Cannot call this expression"))
					)
					.orElseGet(() -> compiler.at(position, returnType).invalid(
							CompileExceptionCode.UNDEFINED_VARIABLE,
							"No such variable: " + name));
		}

		@Override
		public TypeMatch matches(TypeID returnType, CompilingExpression... arguments) {
			ResolvedType resolvedType = compiler.resolve(returnType);
			return resolvedType.getContextMember(name).isPresent() ? TypeMatch.EXACT : TypeMatch.NONE;
		}

		@Override
		public InferredType inferReturnType(CompilingExpression... arguments) {
			return InferredType.failure(CompileExceptionCode.UNDEFINED_VARIABLE, "No such variable: " + name);
		}
	}

	@Override
	public Expression compileKey(ExpressionCompiler compiler, TypeID type) {
		return new ConstantStringExpression(position, name);
	}

	@Override
	public SwitchValue compileToSwitchValue(TypeID type, ExpressionScope scope) throws CompileException {
		TypeMembers members = scope.getTypeMembers(type);
		if (type.isEnum()) {
			EnumConstantMember member = members.getEnumMember(name);
			if (member == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Enum member does not exist: " + name);

			return new EnumConstantSwitchValue(member);
		} else if (type.isVariant()) {
			VariantOptionRef option = members.getVariantOption(name);
			if (option == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant option does not exist: " + name);
			if (option.types.length > 0)
				throw new CompileException(position, CompileExceptionCode.MISSING_VARIANT_CASEPARAMETERS, "Variant case is missing parameters");

			return new VariantOptionSwitchValue(option, new String[0]);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
		}
	}

	@Override
	public ParsedFunctionHeader toLambdaHeader() {
		return new ParsedFunctionHeader(position, Collections.singletonList(toLambdaParameter()), ParsedTypeBasic.UNDETERMINED);
	}

	@Override
	public ParsedFunctionParameter toLambdaParameter() {
		return new ParsedFunctionParameter(ParsedAnnotation.NONE, name, ParsedTypeBasic.UNDETERMINED, null, false);
	}
}
