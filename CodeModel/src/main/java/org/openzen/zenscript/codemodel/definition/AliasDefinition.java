package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;

public class AliasDefinition extends HighLevelDefinition {
	public TypeID type;
	
	public AliasDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}
	
	public void setType(TypeID type) {
		if (type == null)
			throw new NullPointerException("type cannot be null!");
		
		this.type = type;
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitAlias(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitAlias(context, this);
	}

	@Override
	public void normalize(TypeScope scope) {
		// nothing to do
	}
}
