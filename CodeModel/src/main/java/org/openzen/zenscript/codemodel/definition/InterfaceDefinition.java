package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.InterfaceResolvingType;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.List;

public class InterfaceDefinition extends HighLevelDefinition {
	public final List<TypeID> baseInterfaces = new ArrayList<>();

	public InterfaceDefinition(CodePosition position, ModuleSymbol module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
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

	@Override
	public ResolvingType resolve(TypeID[] typeArguments) {
		return InterfaceResolvingType.of(super.resolve(typeArguments), baseInterfaces);
	}
}
