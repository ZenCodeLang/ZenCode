package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaEnumMapper {

	private final Map<EnumDefinition, Map<EnumConstantMember, String>> definitionMap;

	public JavaEnumMapper() {
		this.definitionMap = new HashMap<>();
	}

	public void merge(JavaEnumMapper mapper) {
		mapper.definitionMap.forEach((key, val) -> this.definitionMap.merge(key, val, (a, b) -> Stream.concat(a.entrySet().stream(), b.entrySet().stream()).distinct().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
	}

	public Optional<String> getMapping(EnumConstantMember member) {

		if (!(member.definition instanceof EnumDefinition)) {
			return Optional.empty();
		}

		return Optional.ofNullable(definitionMap.getOrDefault(member.definition, Collections.emptyMap()).get(member));
	}

	public Optional<Map<EnumConstantMember, String>> getMappings(EnumDefinition definition) {
		return Optional.ofNullable(definitionMap.get(definition));
	}

	public void registerMapping(EnumDefinition definition, EnumConstantMember member, String name) {
		this.definitionMap.computeIfAbsent(definition, definition1 -> new HashMap<>()).put(member, name);
	}

}
