package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ExpansionDefinition extends HighLevelDefinition {
	public TypeID target;

	public ExpansionDefinition(CodePosition position, ModuleSymbol module, ZSPackage pkg, Modifiers modifiers) {
		super(position, module, pkg, null, modifiers, null);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitExpansion(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitExpansion(context, this);
	}

	@Override
	public String getName() {
		return null;
	}
}
