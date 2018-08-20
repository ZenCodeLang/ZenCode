/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;

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
	public TypeParameter[] genericParameters = TypeParameter.NONE;
	public DefinitionAnnotation[] annotations = DefinitionAnnotation.NONE;
	
	public HighLevelDefinition outerDefinition;
	private ITypeID superType;
	
	private boolean isDestructible = false;
	
	public HighLevelDefinition(CodePosition position, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		this.position = position;
		this.pkg = pkg;
		this.name = name;
		this.modifiers = modifiers;
		this.outerDefinition = outerDefinition;
		
		if (pkg != null)
			pkg.register(this);
	}
	
	public HighLevelDefinition getOutermost() {
		HighLevelDefinition result = this;
		while (result.outerDefinition != null)
			result = result.outerDefinition;
		return result;
	}
	
	public String getFullName() {
		return pkg.fullName + '.' + name;
	}
	
	public ITypeID getSuperType() {
		return superType;
	}
	
	public void setSuperType(ITypeID superType) {
		this.superType = superType;
		if (outerDefinition != null)
			isDestructible |= outerDefinition.isDestructible;
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
	
	public boolean isInterface() {
		return this instanceof InterfaceDefinition;
	}
	
	public boolean isAlias() {
		return this instanceof AliasDefinition;
	}
	
	public void addMember(IDefinitionMember member) {
		if (!members.contains(member))
			members.add(member);
	}
	
	public boolean isDestructible() {
		boolean isDestructible = false;
		for (IDefinitionMember member : members) {
			if (member instanceof DestructorMember)
				isDestructible = true;
			if ((member instanceof FieldMember) && ((FieldMember)member).type.isDestructible())
				isDestructible = true;
			if ((member instanceof ImplementationMember) && ((ImplementationMember)member).type.isDestructible())
				isDestructible = true;
		}
		return isDestructible;
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
	
	public void normalize(TypeScope scope) {
		DestructorMember destructor = null;
		List<FieldMember> fields = new ArrayList();
		
		for (IDefinitionMember member : members) {
			member.normalize(scope);
			
			if (member instanceof DestructorMember)
				destructor = (DestructorMember)member;
			if (member instanceof FieldMember)
				fields.add((FieldMember)member);
		}
		
		if (isDestructible() && destructor == null && !(this instanceof ExpansionDefinition)) {
			System.out.println("Added destructor to " + position);
			destructor = new DestructorMember(position, this, Modifiers.PUBLIC);
			members.add(destructor);
		}
		
		for (FieldMember field : fields) {
			if (field.autoGetter != null)
				members.add(field.autoGetter);
			if (field.autoSetter != null)
				members.add(field.autoSetter);
		}
	}
	
	public abstract <T> T accept(DefinitionVisitor<T> visitor);

	public HighLevelDefinition getInnerType(String name) {
		for (IDefinitionMember member : members) {
			if (member instanceof InnerDefinitionMember) {
				InnerDefinitionMember inner = (InnerDefinitionMember)member;
				if (inner.innerDefinition.name.equals(name))
					return inner.innerDefinition;
			}
		}
		
		return null;
	}
}
