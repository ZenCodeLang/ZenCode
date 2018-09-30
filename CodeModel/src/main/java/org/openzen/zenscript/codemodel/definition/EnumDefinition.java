/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class EnumDefinition extends HighLevelDefinition {
	public TypeID asType;
	public List<EnumConstantMember> enumConstants = new ArrayList<>();
	
	public EnumDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitEnum(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitEnum(context, this);
	}
	
	@Override
	public void collectMembers(MemberCollector collector) {
		super.collectMembers(collector);
		
		for (EnumConstantMember member : enumConstants)
			collector.enumConstant(member);
	}
	
	public void addEnumConstant(EnumConstantMember constant) {
		enumConstants.add(constant);
	}
}
