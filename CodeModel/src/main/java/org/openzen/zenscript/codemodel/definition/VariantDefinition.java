/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;
import org.openzen.zenscript.codemodel.type.StoredType;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantDefinition extends HighLevelDefinition {
	public final List<Option> options = new ArrayList<>();
	
	public VariantDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitVariant(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitVariant(context, this);
	}
	
	@Override
	public void collectMembers(MemberCollector collector) {
		super.collectMembers(collector);
		
		for (Option option : options)
			collector.variantOption(option);
	}
	
	public static class Option extends Taggable {
		public final CodePosition position;
		public final VariantDefinition variant;
		public final String name;
		public final int ordinal;
		public final StoredType[] types;
		
		public Option(CodePosition position, VariantDefinition variant, String name, int ordinal, StoredType[] types) {
			this.position = position;
			this.variant = variant;
			this.name = name;
			this.ordinal = ordinal;
			this.types = types;
		}
		
		public VariantOptionRef instance(StoredType variantType, GenericMapper mapper) {
			return new VariantOptionRef(this, variantType, mapper.map(types));
		}
	}
}
