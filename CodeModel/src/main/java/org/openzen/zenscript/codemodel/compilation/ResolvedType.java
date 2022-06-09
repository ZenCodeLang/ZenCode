package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public interface ResolvedType {
	StaticCallable getConstructor();

	Optional<StaticCallable> findImplicitConstructor();

	Optional<StaticCallable> findSuffixConstructor(String suffix);

	Optional<Expression> tryCastExplicit(TypeID target, ExpressionBuilder builder, Expression value, boolean optional);

	Optional<Expression> tryCastImplicit(TypeID target, ExpressionBuilder builder, Expression value, boolean optional);

	boolean canCastImplicitlyTo(TypeID target);

	Optional<StaticCallable> findStaticMethod(String name);

	Optional<StaticGetter> findStaticGetter(String name);

	Optional<StaticSetter> findStaticSetter(String name);

	Optional<InstanceCallable> findMethod(String name);

	Optional<InstanceGetter> findGetter(String name);

	Optional<InstanceSetter> findSetter(String name);

	Optional<InstanceCallable> findOperator(OperatorType operator);

	Optional<Field> findField(String name);

	Optional<TypeSymbol> findInnerType(String name);

	/**
	 * Finds a member that can be resolved directly in this context (eg. enum constant or variant option)
	 *
	 * @param name member name
	 * @return true if it's a resolved member
	 */
	Optional<CompilingExpression> getContextMember(String name);

	Optional<SwitchMember> findSwitchMember(String name);

	Optional<Comparator> compare();

    Optional<IteratorMemberRef> findIterator(int variables);

	interface StaticGetter {
		TypeID getType();

		Expression apply(ExpressionBuilder builder);
	}

	interface StaticSetter {
		TypeID getType();

		Expression apply(ExpressionBuilder builder, Expression value);
	}

	interface InstanceGetter {
		TypeID getType();

		Expression apply(ExpressionBuilder builder, Expression instance);

		CastedExpression cast(ExpressionBuilder builder, CastedEval cast, Expression instance);
	}

	interface InstanceSetter {
		TypeID getType();

		Expression apply(ExpressionBuilder builder, Expression instance, Expression value);
	}

	interface SwitchMember {
		SwitchValue toSwitchValue(String[] bindingNames);
	}

	interface Field {
		TypeID getType();

		Expression get(ExpressionBuilder builder);

		Expression set(ExpressionBuilder builder, Expression value);
	}

	interface Caster {
		Expression call(ExpressionBuilder builder, Expression value, boolean optional);
	}

	interface Comparator {
		Expression compare(CodePosition position, Expression left, CompilingExpression right);

		Expression compare(CodePosition position, Expression left, CompilingExpression right, CompareType type);
	}
}
