package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaEnumMapper {

	private final Map<EnumDefinition, Set<EnumConstantMapping>> definitionMap;

	public JavaEnumMapper() {
		this.definitionMap = new HashMap<>();
	}

	public void merge(JavaEnumMapper mapper) {
		mapper.definitionMap.forEach((key, val) -> this.definitionMap.merge(key, val, (a, b) -> Stream.concat(a.stream(), b.stream())
				.collect(Collectors.toSet())));
	}

	public Optional<EnumConstantMapping> getMapping(EnumConstantMember member){

		if(!(member.definition instanceof EnumDefinition)){
			return Optional.empty();
		}

		return definitionMap.getOrDefault(member.definition, new HashSet<>()).stream().filter(mapping -> mapping.getMember().equals(member)).findFirst();
	}
	public Optional<Set<EnumConstantMapping>> getMappings(EnumDefinition definition) {
		return Optional.ofNullable(definitionMap.get(definition));
	}

	public void registerMapping(EnumDefinition definition, EnumConstantMapping mapping) {
		this.definitionMap.computeIfAbsent(definition, definition1 -> new HashSet<>()).add(mapping);
	}

}
