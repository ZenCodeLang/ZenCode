package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.ArrayList;
import java.util.List;

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
	}

	public boolean isSubclassOf(HighLevelDefinition other) {
		if(superType == null){
			return false;
		}
		if (superType.isDefinition(other))
			return true;
		if (!(superType instanceof DefinitionTypeID))
			return false;

		DefinitionTypeID superDefinition = (DefinitionTypeID) superType;
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
				fields.add((FieldMember) member);

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
		List<FieldMember> fields = new ArrayList<>();

		for (IDefinitionMember member : members) {
			member.normalize(scope);

			if (member instanceof FieldMember)
				fields.add((FieldMember) member);
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
				InnerDefinitionMember inner = (InnerDefinitionMember) member;
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
