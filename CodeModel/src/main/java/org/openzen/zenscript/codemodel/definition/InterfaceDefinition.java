package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.compilation.ResolvingType;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.InterfaceResolvingType;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		if (baseInterfaces.isEmpty()) {
			return InterfaceResolvingType.of(super.resolve(typeArguments), Collections.emptyList());
		} else {
			TypeID type = DefinitionTypeID.create(this, typeArguments);

			MemberSet.Builder members = MemberSet.create(type);
			GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);
			for (IDefinitionMember member : this.members) {
				member.registerTo(type, members, mapper);
			}

			TypeID[] baseInterfaces = this.baseInterfaces.toArray(TypeID.NONE);
			return InterfaceResolvingType.of(super.resolve(typeArguments), Arrays.asList(mapper.map(baseInterfaces)));
		}
	}
}
