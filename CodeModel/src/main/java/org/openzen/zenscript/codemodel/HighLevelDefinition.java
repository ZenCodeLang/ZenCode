package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class HighLevelDefinition extends Taggable implements TypeSymbol {
	public final CodePosition position;
	public final Module module;
	public final ZSPackage pkg;
	public final String name;
	public final int modifiers;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public TypeParameter[] typeParameters = TypeParameter.NONE;
	public DefinitionAnnotation[] annotations = DefinitionAnnotation.NONE;

	public TypeSymbol outerDefinition;
	private TypeID superType;

	public HighLevelDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, TypeSymbol outerDefinition) {
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.ofNullable(outerDefinition);
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

	public boolean isEnum() { return this instanceof EnumDefinition; }

	public Optional<EnumDefinition> asEnum() {
		return Optional.empty();
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

	public boolean isStatic() {
		return (modifiers & Modifiers.STATIC) > 0;
	}

	public abstract <T> T accept(DefinitionVisitor<T> visitor);

	public abstract <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor);

	/* TypeSymbol implementation */

	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public String describe() {
		return name;
	}

	@Override
	public Optional<TypeSymbol> getSuperclass() {
		return superType == null ? Optional.empty() : superType.asDefinition().map(t -> t.definition);
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments) {
		MemberSet members = new MemberSet();
		GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);
		for (IDefinitionMember member : this.members) {
			member.registerTo(members, mapper);
		}
		return members;
	}

	@Override
	public TypeParameter[] getTypeParameters() {
		return typeParameters;
	}
}
