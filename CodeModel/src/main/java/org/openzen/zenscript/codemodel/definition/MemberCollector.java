/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public interface MemberCollector {
	void member(IDefinitionMember member);
	
	void enumConstant(EnumConstantMember member);
	
	void variantOption(VariantDefinition.Option member);
}
