/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class HighLevelDefinition extends Taggable {
	public final String name;
	public final int modifiers;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public final List<TypeParameter> genericParameters = new ArrayList<>();
	
	public HighLevelDefinition outerDefinition;
	public ITypeID superType;
	
	public HighLevelDefinition(String name, int modifiers, HighLevelDefinition outerDefinition) {
		this.name = name;
		this.modifiers = modifiers;
		this.outerDefinition = outerDefinition;
	}
	
	public void setOuterDefinition(HighLevelDefinition outerDefinition) {
		this.outerDefinition = outerDefinition;
	}
	
	public boolean isInnerDefinition() {
		return outerDefinition != null;
	}
	
	public void addMember(IDefinitionMember member) {
		if (!members.contains(member))
			members.add(member);
	}
	
	public void addGenericParameter(TypeParameter parameter) {
		genericParameters.add(parameter);
	}
	
	public List<FieldMember> getFields() {
		List<FieldMember> fields = new ArrayList<>();
		for (IDefinitionMember member : members)
			if (member instanceof FieldMember)
				fields.add((FieldMember)member);
		return fields;
	}
	
	public boolean isStatic() {
		return (modifiers & Modifiers.MODIFIER_STATIC) > 0;
	}
	
	public abstract <T> T accept(DefinitionVisitor<T> visitor);
}
