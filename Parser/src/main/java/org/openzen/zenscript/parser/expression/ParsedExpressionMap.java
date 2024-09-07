package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.*;
import java.util.stream.Collectors;

public class ParsedExpressionMap extends ParsedExpression {
	public final List<CompilableExpression> keys;
	public final List<CompilableExpression> values;

	public ParsedExpressionMap(
			CodePosition position,
			List<CompilableExpression> keys,
			List<CompilableExpression> values) {
		super(position);

		this.keys = keys;
		this.values = values;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new Compiling(
				compiler,
				position,
				keys.stream().map(key -> key.compileKey(compiler)).collect(Collectors.toList()),
				values.stream().map(value -> value.compile(compiler)).collect(Collectors.toList()));
	}

	private static class Compiling extends AbstractCompilingExpression {
		private final List<CompilingExpression> keys;
		private final List<CompilingExpression> values;

		public Compiling(ExpressionCompiler compiler, CodePosition position, List<CompilingExpression> keys, List<CompilingExpression> values) {
			super(compiler, position);

			this.keys = keys;
			this.values = values;
		}

		@Override
		public Expression eval() {
			if (keys.isEmpty())
				return compiler.at(position).invalid(CompileErrors.cannotInferEmptyMap());

			Expression[] keys = new Expression[this.keys.size()];
			Expression[] values = new Expression[this.values.size()];

			keys[0] = this.keys.get(0).eval();
			values[0] = this.values.get(0).eval();
			TypeID keyType = keys[0].type;
			TypeID valueType = values[0].type;

			for (int i = 1; i < this.keys.size(); i++) {
				Expression key = this.keys.get(i).eval();
				Expression value = this.values.get(i).eval();

				Optional<TypeID> joinedKeyType = compiler.union(keyType, key.type);
				Optional<TypeID> joinedValueType = compiler.union(valueType, value.type);
				if (!joinedKeyType.isPresent())
					return compiler.at(position).invalid(CompileErrors.noIntersectionBetweenTypes(keyType, key.type));
				if (!joinedValueType.isPresent())
					return compiler.at(position).invalid(CompileErrors.noIntersectionBetweenTypes(valueType, value.type));

				keyType = joinedKeyType.get();
				valueType = joinedValueType.get();
				keys[i] = key;
				values[i] = value;
			}

			AssocTypeID type = compiler.types().associativeOf(keyType, valueType);
			CastedEval keyCast = cast(type.keyType);
			CastedEval valueCast = cast(type.valueType);
			return compiler.at(position).newAssoc(
					type,
					Arrays.stream(keys).map(e -> keyCast.of(e).value).collect(Collectors.toList()),
					Arrays.stream(values).map(e -> valueCast.of(e).value).collect(Collectors.toList()));
		}

		@Override
		public CastedExpression cast(CastedEval cast) {
			Optional<AssocTypeID> maybeAssoc = cast.type.simplified().asAssoc();
			if (maybeAssoc.isPresent()) {
				AssocTypeID assoc = maybeAssoc.get();
				CastedEval keyCast = cast(assoc.keyType);
				CastedEval valueCast = cast(assoc.valueType);
				List<Expression> keys = this.keys.stream().map(key -> key.cast(keyCast).value).collect(Collectors.toList());
				List<Expression> values = this.values.stream().map(value -> value.cast(valueCast).value).collect(Collectors.toList());
				return cast.of(compiler.at(position).newAssoc(assoc, keys, values));
			}

			Optional<GenericMapTypeID> maybeGenericMap = cast.type.simplified().asGenericMap();
			if (maybeGenericMap.isPresent()) {
				if (keys.isEmpty() && values.isEmpty()) {
					return cast.of(compiler.at(position).newGenericMap(maybeGenericMap.get()));
				} else {
					return cast.invalid(CompileErrors.genericMapConstructedEmpty());
				}
			}

			ResolvedType resolvedType = compiler.resolve(cast.type.simplified());
			Optional<StaticCallable> maybeImplicitConstructor = resolvedType.findImplicitConstructor();
			if (maybeImplicitConstructor.isPresent()) {
				StaticCallable implicitConstructor = maybeImplicitConstructor.get();
				CastedExpression constructed = implicitConstructor.casted(compiler, position, cast, null, this);
				if (!constructed.isFailed())
					return constructed;

				Optional<FunctionHeader> singleHeader = implicitConstructor.getSingleHeader();
				if (singleHeader.isPresent()) {
					FunctionHeader header = singleHeader.get();
					Map<String, CompilingExpression> argumentMap = new HashMap<>();
					for (int i = 0; i < keys.size(); i++) {
						Optional<String> key = keys.get(i).asStringKey();
						if (!key.isPresent())
							return cast.invalid(CompileErrors.invalidMapKey());

						argumentMap.put(key.get(), values.get(i));
					}
					CompilingExpression[] arguments = new CompilingExpression[header.parameters.length];
					Set<String> handledArguments = new HashSet<>();
					for (int i = 0; i < header.parameters.length; i++) {
						FunctionParameter parameter = header.parameters[i];
						CompilingExpression argument = argumentMap.get(parameter.name);
						if (argument == null && parameter.defaultValue == null)
							return cast.invalid(CompileErrors.missingParameter(parameter.name));

						arguments[i] = argument;
						handledArguments.add(parameter.name);
					}
					for (String key : argumentMap.keySet()) {
						if (!handledArguments.contains(key))
							return cast.invalid(CompileErrors.unknownParameter(key));
					}
					return implicitConstructor.casted(compiler, position, cast, null, arguments);
				}
			}

			return cast.invalid(CompileErrors.invalidMapType(cast.type));
		}

		@Override
		public void collect(SSAVariableCollector collector) {
			for (CompilingExpression key : keys)
				key.collect(collector);
			for (CompilingExpression value : values)
				value.collect(collector);
		}

		@Override
		public void linkVariables(CodeBlockStatement.VariableLinker linker) {
			for (CompilingExpression key : keys)
				key.linkVariables(linker);
			for (CompilingExpression value : values)
				value.linkVariables(linker);
		}
	}
}
