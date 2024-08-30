package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.IteratorInstance;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExpandedResolvedType implements ResolvedType {
	public static ResolvedType of(ResolvedType base, List<ResolvedType> resolutions) {
		if (resolutions.isEmpty()) {
			return base;
		} else {
			return new ExpandedResolvedType(base, resolutions);
		}
	}

	private final ResolvedType base;
	private final List<ResolvedType> expansions;

	private ExpandedResolvedType(ResolvedType base, List<ResolvedType> expansions) {
		this.base = base;
		this.expansions = expansions;
	}

	@Override
	public StaticCallable getConstructor() {
		return base.getConstructor();
	}

	@Override
	public Optional<StaticCallable> findImplicitConstructor() {
		return base.findImplicitConstructor();
	}

	@Override
	public Optional<StaticCallable> findSuffixConstructor(String suffix) {
		return base.findSuffixConstructor(suffix);
	}

	@Override
	public Optional<InstanceCallableMethod> findCaster(TypeID toType) {
		Optional<InstanceCallableMethod> baseCaster = base.findCaster(toType);
		if (baseCaster.isPresent())
			return baseCaster;

		List<InstanceCallableMethod> resolutions = new ArrayList<>();
		for (ResolvedType expansion : expansions) {
			expansion.findCaster(toType).ifPresent(resolutions::add);
		}
		if (resolutions.isEmpty()) {
			return Optional.empty();
		} else if (resolutions.size() == 1) {
			return Optional.ofNullable(resolutions.get(0));
		} else {
			List<MethodInstance> methods = new ArrayList<>();
			resolutions.forEach(method -> method.asMethod().ifPresent(methods::add));
			return Optional.of(new AmbiguousExpansionCall(new FunctionHeader(toType), methods));
		}
	}

	@Override
	public Optional<StaticCallable> findStaticMethod(String name) {
		StaticCallable result = base.findStaticMethod(name).orElse(null);
		for (ResolvedType expansion : expansions) {
			StaticCallable expanded = expansion.findStaticMethod(name).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<StaticCallable> findStaticGetter(String name) {
		StaticCallable result = base.findStaticGetter(name).orElse(null);
		for (ResolvedType expansion : expansions) {
			StaticCallable expanded = expansion.findStaticGetter(name).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<StaticCallable> findStaticSetter(String name) {
		StaticCallable result = base.findStaticSetter(name).orElse(null);
		for (ResolvedType expansion : expansions) {
			StaticCallable expanded = expansion.findStaticSetter(name).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<InstanceCallable> findMethod(String name) {
		InstanceCallable result = base.findMethod(name).orElse(null);
		for (ResolvedType expansion : expansions) {
			InstanceCallable expanded = expansion.findMethod(name).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<InstanceCallable> findGetter(String name) {
		InstanceCallable result = base.findGetter(name).orElse(null);
		for (ResolvedType expansion : expansions) {
			InstanceCallable expanded = expansion.findGetter(name).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<InstanceCallable> findSetter(String name) {
		InstanceCallable result = base.findSetter(name).orElse(null);
		for (ResolvedType expansion : expansions) {
			InstanceCallable expanded = expansion.findSetter(name).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<InstanceCallable> findOperator(OperatorType operator) {
		InstanceCallable result = base.findOperator(operator).orElse(null);
		for (ResolvedType expansion : expansions) {
			InstanceCallable expanded = expansion.findOperator(operator).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<StaticCallable> findStaticOperator(OperatorType operator) {
		StaticCallable result = base.findStaticOperator(operator).orElse(null);
		for (ResolvedType expansion : expansions) {
			StaticCallable expanded = expansion.findStaticOperator(operator).orElse(null);
			if (expanded != null) {
				result = result == null ? expanded : result.union(expanded);
			}
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Optional<Field> findField(String name) {
		return base.findField(name);
	}

	@Override
	public Optional<TypeSymbol> findInnerType(String name) {
		return base.findInnerType(name);
	}

	@Override
	public Optional<CompilableExpression> getContextMember(String name) {
		return base.getContextMember(name);
	}

	@Override
	public Optional<SwitchMember> findSwitchMember(String name) {
		return base.findSwitchMember(name);
	}

	@Override
	public List<Comparator> comparators() {
		return base.comparators();
	}

	@Override
	public Optional<IteratorInstance> findIterator(int variables) {
		return base.findIterator(variables);
	}

	@Override
	public ResolvedType withExpansions(TypeID type, List<ExpansionSymbol> expansions) {
		List<ResolvedType> newExpansions = this.expansions.stream().map(expansion -> expansion.withExpansions(type, expansions)).collect(Collectors.toList());
		return ExpandedResolvedType.of(base.withExpansions(type, expansions), newExpansions);
	}
}
