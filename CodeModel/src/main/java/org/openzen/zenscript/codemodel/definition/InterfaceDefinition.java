package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.type.TypeID;

public class InterfaceDefinition extends HighLevelDefinition {
	public final List<TypeID> baseInterfaces = new ArrayList<>();
	
	public InterfaceDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}
	
	public void addBaseInterface(TypeID baseInterface) {
		baseInterfaces.add(baseInterface);
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitInterface(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitInterface(context, this);
	}
}
