/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.scope.*;
import org.openzen.zenscript.codemodel.type.*;
import org.openzen.zenscript.codemodel.type.member.*;

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
    
    @Override
    public void normalize(TypeScope scope) {
        if (members.stream().noneMatch(m -> m instanceof ConstructorMember)) {
            ConstructorMember constructor = new ConstructorMember(position, this, Modifiers.PUBLIC | Modifiers.EXTERN, new FunctionHeader(BasicTypeID.VOID), BuiltinID.ENUM_EMPTY_CONSTRUCTOR);
            addMember(constructor);
        }
        super.normalize(scope);
    }
    
    public void addEnumConstant(EnumConstantMember constant) {
		enumConstants.add(constant);
	}
}
