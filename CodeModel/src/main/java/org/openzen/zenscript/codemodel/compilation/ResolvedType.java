package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ResolvedType {
	TypeID getType();

	StaticCallable getConstructor();

	Optional<StaticCallable> findImplicitConstructor();

	Optional<StaticCallable> findSuffixConstructor(String suffix);

	Optional<InstanceCallableMethod> findCaster(TypeID toType);

	default Optional<Expression> tryCastExplicit(TypeID target, ExpressionCompiler compiler, CodePosition position, Expression value, boolean optional) {
		return findCaster(target)
				.filter(caster -> !caster.getModifiers().isImplicit())
				// TODO: remember the type arguments in the caster method instead, so we don't need to infer it again
				.flatMap(caster -> MatchedCallArguments.match(compiler, position, Collections.singletonList(caster), target, TypeID.NONE)
						.getArguments()
						.map(arg -> caster.call(compiler.at(position), value, arg))
				);
	}

	default Optional<Expression> tryCastImplicit(TypeID target, ExpressionCompiler compiler, CodePosition position, Expression value, boolean optional) {
		return findCaster(target)
				.filter(caster -> caster.getModifiers().isImplicit())
				// TODO: remember the type arguments in the caster method instead, so we don't need to infer it again
				.flatMap(caster -> MatchedCallArguments.match(compiler, position, Collections.singletonList(caster), target, TypeID.NONE)
						.getArguments()
						.map(arg -> caster.call(compiler.at(position), value, arg))
				);
	}

	default boolean canCastImplicitlyTo(TypeID target) {
		return findCaster(target)
				.filter(caster -> caster.getModifiers().isImplicit())
				.isPresent();
	}
	Optional<StaticCallable> findStaticMethod(String name);

	Optional<StaticCallable> findStaticGetter(String name);

	Optional<StaticCallable> findStaticSetter(String name);

	Optional<InstanceCallable> findMethod(String name);

	Optional<InstanceCallable> findGetter(String name);

	Optional<InstanceCallable> findSetter(String name);

	Optional<InstanceCallable> findOperator(OperatorType operator);

	Optional<Field> findField(String name);

	Optional<TypeSymbol> findInnerType(String name);

	/**
	 * Finds a member that can be resolved directly in this context (eg. enum constant or variant option)
	 *
	 * @param name member name
	 * @return true if it's a resolved member
	 */
	Optional<CompilableExpression> getContextMember(String name);

	Optional<SwitchMember> findSwitchMember(String name);

	List<Comparator> comparators();

    Optional<IteratorInstance> findIterator(int variables);

	Optional<StaticCallable> findStaticOperator(OperatorType operator);

	List<MethodSymbol> getInterfaceMethodsToImplement();

	interface SwitchMember {
		SwitchValue toSwitchValue(List<CompilingVariable> bindings);
	}

	interface Field {
		TypeID getType();

		boolean isStatic();

		Expression get(ExpressionBuilder builder, Expression target);

		Expression set(ExpressionBuilder builder, Expression target, Expression value);

		Expression getStatic(ExpressionBuilder builder);

		Expression setStatic(ExpressionBuilder builder, Expression value);
	}

	interface StaticField {
		TypeID getType();

		Expression get(ExpressionBuilder builder);

		Expression set(ExpressionBuilder builder, Expression value);
	}

	@FunctionalInterface
	interface Comparator {
		CastedExpression compare(
				ExpressionCompiler compiler,
				CodePosition position,
				Expression left,
				CompilingExpression right,
				CompareType type);
	}
}
