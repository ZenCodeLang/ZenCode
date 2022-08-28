package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionInstance;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.List;

public class VariantDefinition extends HighLevelDefinition {
	public final List<Option> options = new ArrayList<>();

	public VariantDefinition(CodePosition position, ModuleSymbol module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
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

	@Override
	protected void resolveAdditional(TypeID type, MemberSet.Builder members, GenericMapper mapper) {
		for (Option option : options) {
			VariantOptionInstance instance = new VariantOptionInstance(option, type, mapper.map(option.types));
			members.contextMember(option.name, instance);
			members.switchValue(option.name, instance);
		}
	}

	public static class Option extends Taggable {
		public final CodePosition position;
		public final VariantDefinition variant;
		public final String name;
		public final int ordinal;
		public final TypeID[] types;

		public Option(CodePosition position, VariantDefinition variant, String name, int ordinal, TypeID[] types) {
			this.position = position;
			this.variant = variant;
			this.name = name;
			this.ordinal = ordinal;
			this.types = types;
		}

		public VariantOptionInstance instance(TypeID variantType, GenericMapper mapper) {
			return new VariantOptionInstance(this, variantType, mapper.map(types));
		}
	}
}
