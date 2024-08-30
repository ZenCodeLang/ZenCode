package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.TypeMatcher;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ExpansionDefinition extends HighLevelDefinition implements ExpansionSymbol {
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
		return "(expansion of " + target + ")";
	}

	@Override
	public Optional<ResolvedType> resolve(TypeID expandingType) {
		if (target == null)
			throw new RuntimeException(position.toString() + ": Missing expansion target");

		Map<TypeParameter, TypeID> mapping = TypeMatcher.match(expandingType, target);
		if (mapping == null)
			return Optional.empty();

		TypeID[] expansionTypeArguments = Stream.of(typeParameters).map(mapping::get).toArray(TypeID[]::new);
		MemberSet.Builder resolution = MemberSet.create(expandingType);
		GenericMapper mapper = new GenericMapper(mapping, expansionTypeArguments);
		for (IDefinitionMember member : members)
			member.registerTo(expandingType, resolution, mapper);

		return Optional.of(resolution.build().withExpansions(Collections.emptyList()));
	}
}
