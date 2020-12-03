package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.TypeID;

public class ExpansionDefinition extends HighLevelDefinition {
	public TypeID target;
	
	public ExpansionDefinition(CodePosition position, Module module, ZSPackage pkg, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, null, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitExpansion(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitExpansion(context, this);
	}
}
