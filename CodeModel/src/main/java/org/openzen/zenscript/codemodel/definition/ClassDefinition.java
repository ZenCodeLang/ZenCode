package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

public class ClassDefinition extends HighLevelDefinition {
	public ClassDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers) {
		this(position, module, pkg, name, modifiers, null);
	}

	public ClassDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public void normalize(TypeScope scope) {
		super.normalize(scope);

		boolean hasConstructor = false;
		for (IDefinitionMember member : members) {
			if (member instanceof ConstructorMember) {
				hasConstructor = true;
				break;
			}
		}

		if (!hasConstructor) {
			ConstructorMember constructor = new ConstructorMember(position, this, Modifiers.PUBLIC | Modifiers.EXTERN, new FunctionHeader(BasicTypeID.VOID), BuiltinID.CLASS_DEFAULT_CONSTRUCTOR);
			addMember(constructor);
		}
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitClass(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitClass(context, this);
	}
}
