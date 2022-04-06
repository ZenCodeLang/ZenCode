package org.openzen.zenscript.compiler.types;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.ResolvedCallable;
import org.openzen.zenscript.compiler.expression.CompilingExpression;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface ResolvedType {
	Optional<ResolvedCallable> findConstructor(Predicate<MethodInfo> predicate);

	ResolvedConstructor getConstructor();

	List<BinaryOperator> findBinaryOperators(OperatorType operator);

	Optional<Caster> findExplicitCast(TypeID target);

	Optional<Caster> findImplicitCast(TypeID target);

	Optional<ResolvedCallable> findStaticMethod(String name, Predicate<MethodInfo> member);

	Optional<MemberGroup> findMemberGroup(String name);

	/**
	 * Finds a member that can be resolved directly in this context (eg. enum member or variant option)
	 *
	 * @param name member name
	 * @return true if it's a resolved member
	 */
	Optional<CompilingExpression> getContextMember(String name);

	Optional<Comparator> compare(TypeID other);

	List<Comparator> getComparators();

	Optional<TypeID> getSuperType();
}
