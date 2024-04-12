package org.openzen.zenscript.codemodel.compilation.impl.compiler;

import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.compilation.expression.*;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.impl.capture.LocalExpression;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.identifiers.instances.FieldInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ref.ImplementationMemberInstance;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ExpressionCompilerImpl implements ExpressionCompiler {
	private final CompileContext context;
	private final LocalType localType;
	private final TypeBuilder types;
	private final TypeID thrownType;
	private final LocalSymbols locals;
	private final FunctionHeader header;

	public ExpressionCompilerImpl(
			CompileContext context,
			LocalType localType,
			TypeBuilder types,
			TypeID thrownType,
			LocalSymbols locals,
			FunctionHeader header
	) {
		this.context = context;
		this.localType = localType;
		this.types = types;
		this.thrownType = thrownType;
		this.locals = locals;
		this.header = header;
	}

	public ExpressionBuilder at(CodePosition position) {
		return new ExpressionBuilderImpl(position);
	}

	@Override
	public CompilingExpression invalid(CodePosition position, CompileError error) {
		return new InvalidCompilingExpression(this, position, error);
	}

	@Override
	public Optional<TypeID> getThisType() {
		return Optional.ofNullable(localType).map(LocalType::getThisType);
	}

	@Override
	public Optional<LocalType> getLocalType() {
		return Optional.ofNullable(localType);
	}

	@Override
	public Optional<TypeID> getThrowableType() {
		return Optional.ofNullable(thrownType);
	}

	@Override
	public TypeBuilder types() {
		return types;
	}

	@Override
	public Optional<CompilingExpression> resolve(CodePosition position, GenericName name) {
		Optional<LocalExpression> localVariable = locals.findLocalVariable(position, name.name);
		if (localVariable.isPresent()) {
			return Optional.of(localVariable.get().compile(this));
		}

		Optional<IGlobal> global = context.findGlobal(name.name);
		if (global.isPresent())
			return global.map(g -> g.getExpression(position, name.arguments).compile(this));

		return types.resolve(position, Collections.singletonList(name))
				.map(type -> new TypeCompilingExpression(this, position, type));
	}

	@Override
	public Optional<CompilingExpression> dollar() {
		return locals.getDollar().map(e -> e.compile(this));
	}

	@Override
	public List<String> findCandidateImports(String name) {
		// TODO: implement
		return Collections.emptyList();
	}

	@Override
	public Optional<TypeID> union(TypeID left, TypeID right) {
		if (left.equals(right))
			return Optional.of(right);

		ResolvedType leftResolved = resolve(left);
		ResolvedType rightResolved = resolve(right);

		if (leftResolved.canCastImplicitlyTo(right))
			return Optional.of(right);

		if (rightResolved.canCastImplicitlyTo(left))
			return Optional.of(left);

		Optional<ArrayTypeID> maybeLeftArray = left.asArray();
		Optional<ArrayTypeID> maybeRightArray = right.asArray();
		if (maybeLeftArray.isPresent() && maybeRightArray.isPresent()) {
			ArrayTypeID leftArray = maybeLeftArray.get();
			ArrayTypeID rightArray = maybeRightArray.get();

			if (leftArray.dimension == rightArray.dimension) {
				return union(leftArray.elementType, rightArray.elementType)
						.map(t -> new ArrayTypeID(t, leftArray.dimension));
			}
		}

		return Optional.empty();
	}

	@Override
	public ExpressionCompiler withLocalVariables(List<CompilingVariable> variables) {
		LocalSymbols newLocals = locals.forBlock();
		for (CompilingVariable variable : variables) {
			newLocals.add(variable);
		}
		return new ExpressionCompilerImpl(context, localType, types, thrownType, newLocals, header);
	}

	@Override
	public StatementCompiler forLambda(LambdaClosure closure, FunctionHeader header) {
		LocalSymbols newLocals = locals.forLambda(closure, header);
		return new StatementCompilerImpl(context, localType, types, header, newLocals, null);
	}

	@Override
	public ResolvedType resolve(TypeID type) {
		return context.resolve(type);
	}

	private class ExpressionBuilderImpl implements ExpressionBuilder {
		private final CodePosition position;

		public ExpressionBuilderImpl(CodePosition position) {
			this.position = position;
		}

		@Override
		public Expression andAnd(Expression left, Expression right) {
			return new AndAndExpression(position, left, right);
		}

		@Override
		public Expression callStatic(MethodInstance method, CallArguments arguments) {
			return new CallStaticExpression(position, method, arguments);
		}

		@Override
		public Expression callVirtual(MethodInstance method, Expression target, CallArguments arguments) {
			return new CallExpression(position, target, method, arguments);
		}

		@Override
		public Expression callSuper(MethodInstance method, Expression target, CallArguments arguments) {
			return new CallSuperExpression(position, target, method, arguments);
		}

		@Override
		public Expression coalesce(Expression left, Expression right) {
			return new CoalesceExpression(position, left, right);
		}

		@Override
		public Expression constant(boolean value) {
			return new ConstantBoolExpression(position, value);
		}

		@Override
		public Expression constant(String value) {
			return new ConstantStringExpression(position, value);
		}

		@Override
		public Expression constant(float value) {
			return new ConstantFloatExpression(position, value);
		}

		@Override
		public Expression constant(double value) {
			return new ConstantDoubleExpression(position, value);
		}

		@Override
		public Expression constantNull(TypeID type) {
			return new NullExpression(position, type);
		}

		@Override
		public Expression constructorSuper(TypeID type, MethodInstance constructor, CallArguments arguments) {
			return new ConstructorSuperCallExpression(position, type, constructor, arguments);
		}

		@Override
		public Expression constructorThis(TypeID type, MethodInstance constructor, CallArguments arguments) {
			return new ConstructorThisCallExpression(position, type, constructor, arguments);
		}

		@Override
		public Expression getThis(TypeID thisType) {
			return new ThisExpression(position, thisType);
		}

		@Override
		public Expression getInstanceField(Expression target, FieldInstance field) {
			return null;
		}

		@Override
		public Expression getFunctionParameter(FunctionParameter parameter) {
			return new GetFunctionParameterExpression(position, parameter);
		}

		@Override
		public Expression getLocalVariable(VariableDefinition variable) {
			return new GetLocalVariableExpression(position, variable);
		}

		@Override
		public Expression getStaticField(FieldInstance field) {
			return new GetStaticFieldExpression(position, field);
		}

		@Override
		public Expression interfaceCast(ImplementationMemberInstance implementation, Expression value) {
			return new InterfaceCastExpression(position, value, implementation);
		}

		@Override
		public Expression invalid(CompileError error) {
			return new InvalidExpression(position, BasicTypeID.INVALID, error);
		}

		@Override
		public Expression invalid(CompileError error, TypeID type) {
			return new InvalidExpression(position, type, error);
		}

		@Override
		public Expression is(Expression value, TypeID type) {
			return new IsExpression(position, value, type);
		}

		@Override
		public Expression lambda(LambdaClosure closure, FunctionHeader header, Statement body) {
			return new FunctionExpression(position, closure, header, body);
		}

		@Override
		public Expression newArray(ArrayTypeID type, Expression[] values) {
			return new ArrayExpression(position, values, type);
		}

		@Override
		public Expression newAssoc(AssocTypeID type, List<Expression> keys, List<Expression> values) {
			return new MapExpression(position, keys.toArray(Expression.NONE), values.toArray(Expression.NONE), type);
		}

		@Override
		public Expression newGenericMap(GenericMapTypeID type) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Expression newRange(Expression from, Expression to) {
			return new RangeExpression(position, types().rangeOf(from.type), from, to);
		}

		@Override
		public Expression match(Expression value, TypeID resultingType, MatchExpression.Case[] cases) {
			return new MatchExpression(position, value, resultingType, cases);
		}

		@Override
		public Expression orOr(Expression left, Expression right) {
			return new OrOrExpression(position, left, right);
		}

		@Override
		public Expression panic(TypeID type, Expression value) {
			return new PanicExpression(position, type, value);
		}

		@Override
		public Expression setFunctionParameter(FunctionParameter parameter, Expression value) {
			return new SetFunctionParameterExpression(position, parameter, value);
		}

		@Override
		public Expression setInstanceField(Expression target, FieldInstance field, Expression value) {
			return new SetFieldExpression(position, target, field, value);
		}

		@Override
		public Expression setLocalVariable(VariableDefinition variable, Expression value) {
			return new SetLocalVariableExpression(position, variable, value);
		}

		@Override
		public Expression setStaticField(FieldInstance field, Expression value) {
			return new SetStaticFieldExpression(position, field, value);
		}

		@Override
		public Expression ternary(Expression condition, Expression ifThen, Expression ifElse) {
			return new ConditionalExpression(position, condition, ifThen, ifElse, ifThen.type);
		}

		@Override
		public Expression throw_(TypeID type, Expression value) {
			return new ThrowExpression(position, type, value);
		}

		@Override
		public Expression tryConvert(Expression value, TypeID resultingType) {
			return new TryConvertExpression(position, resultingType, value);
		}

		@Override
		public Expression tryRethrowAsException(Expression value, TypeID resultingType) {
			return new TryRethrowAsExceptionExpression(position, resultingType, value, thrownType);
		}

		@Override
		public Expression tryRethrowAsResult(Expression value, TypeID resultingType) {
			return new TryRethrowAsResultExpression(position, resultingType, value);
		}
	}
}
