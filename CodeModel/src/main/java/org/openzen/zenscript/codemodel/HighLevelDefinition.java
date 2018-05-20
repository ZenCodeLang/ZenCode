/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.Taggable;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class HighLevelDefinition extends Taggable {
	public final CodePosition position;
	public final ZSPackage pkg;
	public final String name;
	public final int modifiers;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public TypeParameter[] genericParameters = null;
	
	public HighLevelDefinition outerDefinition;
	public ITypeID superType;
	
	public HighLevelDefinition(CodePosition position, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		this.position = position;
		this.pkg = pkg;
		this.name = name;
		this.modifiers = modifiers;
		this.outerDefinition = outerDefinition;
		
		if (pkg != null)
			pkg.register(this);
	}
	
	public int getNumberOfGenericParameters() {
		return genericParameters == null ? 0 : genericParameters.length;
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
	
	public void setTypeParameters(TypeParameter[] typeParameters) {
		this.genericParameters = typeParameters;
	}
	
	public List<FieldMember> getFields() {
		List<FieldMember> fields = new ArrayList<>();
		for (IDefinitionMember member : members)
			if (member instanceof FieldMember)
				fields.add((FieldMember)member);
		
		return fields;
	}
	
	public boolean hasEmptyConstructor() {
		for (IDefinitionMember member : members) {
			if (member instanceof ConstructorMember) {
				if (((ConstructorMember) member).header.parameters.length == 0)
					return true;
			}
		}
		
		return false;
	}
	
	public boolean isStatic() {
		return (modifiers & Modifiers.STATIC) > 0;
	}
	
	public abstract <T> T accept(DefinitionVisitor<T> visitor);
}
