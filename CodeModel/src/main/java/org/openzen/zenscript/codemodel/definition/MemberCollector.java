package org.openzen.zenscript.codemodel.definition;

import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;

public interface MemberCollector {
	void member(IDefinitionMember member);

	void enumConstant(EnumConstantMember member);

	void variantOption(VariantDefinition.Option member);
}
