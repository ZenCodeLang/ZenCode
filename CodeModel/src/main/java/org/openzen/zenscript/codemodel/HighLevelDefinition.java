/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitorWithContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.MemberCollector;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class HighLevelDefinition extends Taggable {
	public final CodePosition position;
	public final Module module;
	public final ZSPackage pkg;
	public final String name;
	public final int modifiers;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public TypeParameter[] typeParameters = TypeParameter.NONE;
	public DefinitionAnnotation[] annotations = DefinitionAnnotation.NONE;
	
	public HighLevelDefinition outerDefinition;
	private TypeID superType;
	
	private boolean isDestructible = false;
	
	public HighLevelDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, HighLevelDefinition outerDefinition) {
		if (module == null)
			throw new NullPointerException();
		
		this.position = position;
		this.module = module;
		this.pkg = pkg;
		this.name = name;
		this.modifiers = modifiers;
		this.outerDefinition = outerDefinition;
		
		if (pkg != null)
			pkg.register(this);
	}
	
	public String getFullName() {
		return pkg.fullName + '.' + name;
	}
	
	public TypeID getSuperType() {
		return superType;
	}
	
	public void setSuperType(TypeID superType) {
		this.superType = superType;
		if (outerDefinition != null)
			isDestructible |= outerDefinition.isDestructible;
	}
	
	public boolean isSubclassOf(HighLevelDefinition other) {
		if (superType.isDefinition(other))
			return true;
		if (superType == null || !(superType instanceof DefinitionTypeID))
			return false;
		
		DefinitionTypeID superDefinition = (DefinitionTypeID)superType;
		return superDefinition.definition.isSubclassOf(other);
	}
	
	public int getNumberOfGenericParameters() {
		return typeParameters == null ? 0 : typeParameters.length;
	}
	
	public void setOuterDefinition(HighLevelDefinition outerDefinition) {
		this.outerDefinition = outerDefinition;
	}

	public boolean isExpansion() {
		return this instanceof ExpansionDefinition;
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
	
	public void collectMembers(MemberCollector collector) {
		for (IDefinitionMember member : members)
			collector.member(member);
	}
	
	public boolean isDestructible() {
		Set<HighLevelDefinition> scanning = new HashSet<>();
		return isDestructible(scanning);
	}
	
	public boolean isDestructible(Set<HighLevelDefinition> scanning) {
		if (scanning.contains(this))
			return false;
		
		scanning.add(this);
		
		boolean isDestructible = false;
		for (IDefinitionMember member : members) {
			if (member instanceof DestructorMember)
				isDestructible = true;
			if (member instanceof FieldMember) {
				FieldMember field = (FieldMember)member;
				if (field.getType().isDestructible(scanning))
					isDestructible = true;
			}
			if ((member instanceof ImplementationMember) && ((ImplementationMember)member).type.isDestructible())
				isDestructible = true;
		}
		
		scanning.remove(this);
		return isDestructible;
	}
	
	public void setTypeParameters(TypeParameter[] typeParameters) {
		this.typeParameters = typeParameters;
	}
	
	public AccessScope getAccessScope() {
		return new AccessScope(module, this);
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
		List<FieldMember> fields = new ArrayList<>();
		
		for (IDefinitionMember member : members) {
			member.normalize(scope);
			
			if (member instanceof DestructorMember)
				destructor = (DestructorMember)member;
			if (member instanceof FieldMember)
				fields.add((FieldMember)member);
		}
		
		if (isDestructible() && destructor == null && !(this instanceof ExpansionDefinition)) {
			//System.out.println("Added destructor to " + position);
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
	
	public abstract <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor);
	
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

	public boolean isOuterOf(HighLevelDefinition definition) {
		if (definition.outerDefinition == this)
			return true;
		if (definition.outerDefinition == null)
			return false;
		
		return isOuterOf(definition.outerDefinition);
	}
}
