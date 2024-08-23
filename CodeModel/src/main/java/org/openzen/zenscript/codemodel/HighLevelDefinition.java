package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.annotations.DefinitionAnnotation;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.member.ExpandedResolvedType;
import org.openzen.zenscript.codemodel.type.member.MemberSet;
import org.openzen.zenscript.codemodel.type.member.SubclassResolvedType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class HighLevelDefinition extends Taggable implements TypeSymbol {
	public final CodePosition position;
	public final ModuleSymbol module;
	public final ZSPackage pkg;
	public final String name;
	public final Modifiers modifiers;
	public final List<IDefinitionMember> members = new ArrayList<>();
	public TypeParameter[] typeParameters = TypeParameter.NONE;
	public DefinitionAnnotation[] annotations = DefinitionAnnotation.NONE;

	public TypeSymbol outerDefinition;
	private TypeID superType;

	public HighLevelDefinition(CodePosition position, ModuleSymbol module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
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

	public int getNumberOfGenericParameters() {
		return typeParameters == null ? 0 : typeParameters.length;
	}

	public void addDefaultMembers() {}

	public void setOuterDefinition(HighLevelDefinition outerDefinition) {
		this.outerDefinition = outerDefinition;
	}

	public boolean isExpansion() {
		return this instanceof ExpansionDefinition;
	}

	public boolean isInnerDefinition() {
		return outerDefinition != null;
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

	public List<FieldMember> getFields() {
		List<FieldMember> fields = new ArrayList<>();
		for (IDefinitionMember member : members)
			if (member instanceof FieldMember)
				fields.add((FieldMember) member);

		return fields;
	}

	public abstract <T> T accept(DefinitionVisitor<T> visitor);

	public abstract <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor);

	/* TypeSymbol implementation */

	@Override
	public ModuleSymbol getModule() {
		return module;
	}

	@Override
	public String describe() {
		return name;
	}

	@Override
	public boolean isInterface() {
		return this instanceof InterfaceDefinition;
	}

	@Override
	public Modifiers getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isStatic() {
		return modifiers.isStatic() || outerDefinition == null;
	}

	@Override
	public boolean isEnum() { return this instanceof EnumDefinition; }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ResolvedType resolve(TypeID[] typeArguments, List<ExpansionSymbol> expansions) {
		MemberSet.Builder members = MemberSet.create();
		GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);
		TypeID type = DefinitionTypeID.create(this, typeArguments);
		for (IDefinitionMember member : this.members) {
			member.registerTo(type, members, mapper);
		}
		List<ResolvedType> interfaceExpansions = new ArrayList<>();
		for (IDefinitionMember member : this.members) {
			interfaceExpansions.addAll(member.resolveExpansions(expansions));
		}

		members.method(new MethodInstance(BuiltinMethodSymbol.OBJECT_SAME, new FunctionHeader(BasicTypeID.BOOL, type), type));
		members.method(new MethodInstance(BuiltinMethodSymbol.OBJECT_NOTSAME, new FunctionHeader(BasicTypeID.BOOL, type), type));
		resolveAdditional(type, members, mapper);
		ResolvedType resolved = members.build();

		if (superType != null) {
			TypeID instancedSuperType = mapper.map(superType);
			ResolvedType superResolved = instancedSuperType.resolve(expansions);
			resolved = new SubclassResolvedType(superResolved, resolved, superType);
		}

		return ExpandedResolvedType.of(resolved, interfaceExpansions);
	}

	protected void resolveAdditional(TypeID type, MemberSet.Builder members, GenericMapper mapper) {}

	@Override
	public TypeParameter[] getTypeParameters() {
		return typeParameters;
	}

	@Override
	public Optional<TypeSymbol> getOuter() {
		return Optional.ofNullable(outerDefinition);
	}

	@Override
	public Optional<TypeID> getSupertype(TypeID[] typeArguments) {
		return Optional.ofNullable(superType)
				.map(t -> {
					GenericMapper mapper = GenericMapper.create(typeParameters, typeArguments);
					return mapper.map(t);
				});
	}
}
