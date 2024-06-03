package org.openzen.zenscript.javabytecode;

import org.openzen.zencode.shared.SourceFile;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.IteratorTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.nio.file.FileSystems;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JavaMangler {
	private static final class MangleCounter<K> {
		private final Map<K, Integer> counters;

		MangleCounter() {
			this.counters = new HashMap<>();
		}

		int get(final K key) {
			return this.counters.compute(key, (k, v) -> v == null? 0 : ++v);
		}
	}

	private static final String EXP_TAR_MANGLE_ARRAY_ID = "_l";
	private static final String EXP_TAR_MANGLE_ASSOC_ID = "_m";
	private static final String EXP_TAR_MANGLE_BASIC_ID = "_b";
	private static final String EXP_TAR_MANGLE_DEFINITION_ID = "_c";
	private static final String EXP_TAR_MANGLE_FUNCTION_ID = "_f";
	private static final String EXP_TAR_MANGLE_GENERIC_ID = "_g";
	private static final String EXP_TAR_MANGLE_GENERIC_MAP_ID = "_h";
	private static final String EXP_TAR_MANGLE_ITERATOR_ID = "_i";
	private static final String EXP_TAR_MANGLE_OPTIONAL_ID = "_o";
	private static final String EXP_TAR_MANGLE_RANGE_ID = "_r";

	private final MangleCounter<Class<?>> genericCounters;
	private final MangleCounter<String> expansionCounters;

	public JavaMangler() {
		this.genericCounters = new MangleCounter<>();
		this.expansionCounters = new MangleCounter<>();
	}

	public String mangleScriptName(final SourceFile sourceFile) {
		return this.mangleScriptName(sourceFile == null? null : sourceFile.getFilename());
	}

	public String mangleScriptBodyMethod(final int methodsAmount) {
		return "script-body" + (methodsAmount == 0? "" : ('-' + Integer.toString(methodsAmount)));
	}

	public String mangleSourceFileName(final HighLevelDefinition definition) {
		final SourceFile file = definition.position.file;
		if (file != null) {
			final List<String> path = file.getFilePath();
			assert !path.isEmpty();
			return this.mangleScriptName(path.get(path.size() - 1));
		}

		// Only expansions have a null name
		// TODO: Why is this mangled too?
		return this.mangleScriptName(definition.name == null? "Expansion" : definition.name);
	}

	public String mangleDefinitionName(final HighLevelDefinition definition) {
		final String mangledSource = this.mangleSourceFileName(definition);
		if (definition instanceof FunctionDefinition) {
			// We want function definitions to be in the same place as the script, so we will not mangle them further
			return mangledSource;
		}

		final Class<? extends HighLevelDefinition> clazz = definition.getClass();
		final StringBuilder builder = new StringBuilder(mangledSource);
		builder.append('$');
		builder.append(clazz.getSimpleName().replace("Definition", ""));
		builder.append('$');

		if (definition instanceof ExpansionDefinition) {
			// Expansions not only do not have a name, but they can be for different types: for ease of debugging, we
			// want to include information on the type that is expanded
			final String target = this.mangleExpansionTarget(((ExpansionDefinition) definition).target);
			builder.append(target);
			builder.append('$');
			builder.append(this.expansionCounters.get(target));
		} else {
			builder.append(definition.name);
			builder.append('$');
			builder.append(this.genericCounters.get(clazz));
		}

		return builder.toString();
	}

	private String mangleScriptName(final String rawName) {
		if (rawName == null) {
			class GeneratedBlock {}
			return "$GeneratedBlock" + this.genericCounters.get(GeneratedBlock.class);
		}
		final String separator = FileSystems.getDefault().getSeparator();
		final String specialCharRegex = Stream.of("/", "\\", ".", ";", "\\[")
				.filter(character -> !character.equals(separator))
				.collect(Collectors.joining("", "[", "]"));
		return rawName.substring(0, rawName.lastIndexOf('.')) // remove .zs/.zc
				.replaceAll(specialCharRegex, "_")
				.replace(separator, "/")
				.concat("$");
	}

	private String mangleExpansionTarget(final TypeID target) {
		return this.oneOf(
				() -> this.mangleIf(EXP_TAR_MANGLE_ARRAY_ID, target::asArray, it -> Character.toString('D') + it.dimension + 'C' + this.mangleExpansionTarget(it.elementType)),
				() -> this.mangleIf(EXP_TAR_MANGLE_ASSOC_ID, target::asAssoc, it -> this.mangleKv(this.mangleExpansionTarget(it.keyType), this.mangleExpansionTarget(it.valueType))),
				() -> this.mangleIf(EXP_TAR_MANGLE_BASIC_ID, target, BasicTypeID.class, it -> it.name),
				() -> this.mangleIf(EXP_TAR_MANGLE_DEFINITION_ID, target::asDefinition, it -> {
					final String name = it.definition.getName();
					final int simpleNameBegin = name.lastIndexOf('.');
					return name.substring(simpleNameBegin + 1);
				}),
				() -> this.mangleIf(EXP_TAR_MANGLE_FUNCTION_ID, target::asFunction, it -> this.mangleFunctionHeader(it.header)),
				() -> this.mangleIf(EXP_TAR_MANGLE_GENERIC_ID, target::asGeneric, it -> this.mangleGenericTypeParameter(it.parameter)),
				() -> this.mangleIf(
						EXP_TAR_MANGLE_GENERIC_MAP_ID,
						target::asGenericMap,
						it -> this.mangleKv(this.mangleGenericTypeParameter(it.key), this.mangleExpansionTarget(it.value))
				),
				() -> this.mangleIf(
						EXP_TAR_MANGLE_ITERATOR_ID,
						target,
						IteratorTypeID.class,
						it -> Arrays.stream(it.iteratorTypes).map(this::mangleExpansionTarget).map("I"::concat).collect(Collectors.joining())
				),
				() -> this.mangleIf(EXP_TAR_MANGLE_OPTIONAL_ID, target::asOptional, it -> this.mangleExpansionTarget(it.baseType)),
				() -> this.mangleIf(EXP_TAR_MANGLE_RANGE_ID, target::asRange, it -> this.mangleExpansionTarget(it.baseType))
		).orElseThrow(() -> new IllegalStateException("Unable to mangle target type " + target));
	}

	private <I extends TypeID> Optional<String> mangleIf(final String type, final TypeID id, final Class<? extends I> clazz, final Function<I, String> extractor) {
		return this.mangleIf(type, () -> Optional.of(id).filter(clazz::isInstance).map(clazz::cast), extractor);
	}

	private <I extends TypeID> Optional<String> mangleIf(final String type, final Supplier<Optional<I>> supplier, final Function<I, String> extractor) {
		return supplier.get().map(it -> this.mangleExpansionWithType(type, () -> extractor.apply(it)));
	}

	private String mangleKv(final String mangledKey, final String mangledValue) {
		return 'K' + mangledKey + 'V' + mangledValue;
	}

	private String mangleExpansionWithType(final String type, final Supplier<String> inner) {
		return type + (inner == null? "" : this.encodeLengthNameFormat(inner.get()));
	}

	private String mangleFunctionHeader(final FunctionHeader header) {
		final StringBuilder builder = new StringBuilder("t");
		builder.append(header.typeParameters.length);
		for (final TypeParameter typeParameter : header.typeParameters) {
			builder.append('T').append(this.encodeLengthNameFormat(this.mangleGenericTypeParameter(typeParameter)));
		}
		builder.append('R');
		builder.append(this.encodeLengthNameFormat(this.mangleExpansionTarget(header.getReturnType())));
		builder.append('p');
		builder.append(header.parameters.length);
		for (final FunctionParameter parameter : header.parameters) {
			builder.append('P').append(this.encodeLengthNameFormat(this.mangleExpansionTarget(parameter.type)));
		}
		return builder.toString();
	}

	private String mangleGenericTypeParameter(final TypeParameter parameter) {
		return parameter.name; // TODO("Verify")
	}

	private String encodeLengthNameFormat(final String string) {
		return string.length() + string;
	}

	@SafeVarargs // It's private, so it's already final...
	private final <O> Optional<? extends O> oneOf(final Supplier<? extends Optional<? extends O>>... suppliers) {
		return Arrays.stream(suppliers)
				.map(Supplier::get)
				.filter(Optional::isPresent)
				.findFirst()
				.flatMap(Function.identity());
	}
}
