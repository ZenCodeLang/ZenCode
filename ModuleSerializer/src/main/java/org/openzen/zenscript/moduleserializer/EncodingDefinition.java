/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.MemberCollector;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class EncodingDefinition {
	public static final EncodingDefinition BUILTIN = new EncodingDefinition(null);
	
	public static EncodingDefinition complete(HighLevelDefinition definition) {
		EncodingDefinition result = new EncodingDefinition(definition);
		definition.setTag(EncodingDefinition.class, result);
		definition.collectMembers(new Collector(result));
		return result;
	}
	
	public final HighLevelDefinition definition;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public final List<EnumConstantMember> enumConstants = new ArrayList<>();
	public final List<VariantDefinition.Option> variantOptions = new ArrayList<>();
	
	public final Set<IDefinitionMember> memberSet = new HashSet<>();
	public final Set<EnumConstantMember> enumConstantSet = new HashSet<>();
	public final Set<VariantDefinition.Option> variantOptionSet = new HashSet<>();
	
	
	public EncodingDefinition(HighLevelDefinition definition) {
		this.definition = definition;
	}
	
	public boolean mark(IDefinitionMember member) {
		if (memberSet.add(member)) {
			members.add(member);
			return true;
		} else {
			return false;
		}
	}
	
	public void mark(EnumConstantMember member) {
		if (enumConstantSet.add(member))
			enumConstants.add(member);
	}
	
	public void mark(VariantDefinition.Option member) {
		if (variantOptionSet.add(member))
			variantOptions.add(member);
	}
	
	private static class Collector implements MemberCollector {
		private final EncodingDefinition definition;
		
		private Collector(EncodingDefinition definition) {
			this.definition = definition;
		}

		@Override
		public void member(IDefinitionMember member) {
			definition.mark(member);
		}

		@Override
		public void enumConstant(EnumConstantMember member) {
			definition.mark(member);
		}

		@Override
		public void variantOption(VariantDefinition.Option member) {
			definition.mark(member);
		}
	}
}
