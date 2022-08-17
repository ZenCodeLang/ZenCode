package org.openzen.zencode.java.module;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.ref.IteratorMemberRef;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;
import java.util.stream.Collectors;

public class JavaNativeTypeMembers implements ResolvedType {
	private final JavaNativeTypeTemplate template;
	private final TypeID type;
	private final GenericMapper mapper;

	public JavaNativeTypeMembers(JavaNativeTypeTemplate template, TypeID type, GenericMapper mapper) {
		this.template = template;
		this.type = type;
		this.mapper = mapper;
	}

	@Override
	public StaticCallable getConstructor() {
		return new StaticCallable(template.getConstructors().stream()
				.map(c -> mapper.map(type, c))
				.collect(Collectors.toList()));
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		return Optional.empty();
	}

	@Override
	public Optional<Expression> tryCastExplicit(TypeID target, ExpressionCompiler compiler, CodePosition position, Expression value, boolean optional) {
		return Optional.empty();
	}

	@Override
	public Optional<Expression> tryCastImplicit(TypeID target, ExpressionCompiler compiler, CodePosition position, Expression value, boolean optional) {
		return Optional.empty();
	}

	@Override
	public boolean canCastImplicitlyTo(ExpressionCompiler compiler, CodePosition position, TypeID target) {
		return false;
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		return Optional.empty();
	}

	@Override
	public Optional<Field> findField(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return Optional.empty();
	}

	@Override
	public Optional<Comparator> compare() {
		return Optional.empty();
	}

	@Override
	public Optional<IteratorMemberRef> findIterator(int variables) {
		return Optional.empty();
	}
}
