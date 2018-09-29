/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator.visitors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import static org.openzen.zenscript.codemodel.Modifiers.*;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.validator.Validator;
import org.openzen.zenscript.validator.analysis.StatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionValidator implements DefinitionVisitor<Void> {
	private final Validator validator;
	
	public DefinitionValidator(Validator validator) {
		this.validator = validator;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PRIVATE | ABSTRACT | STATIC | PROTECTED | VIRTUAL,
				definition.position,
				"Invalid class modifier");
		ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		if (definition.getSuperType() != null)
			definition.getSuperType().accept(new SupertypeValidator(validator, definition.position, definition));
		
		validateMembers(definition, DefinitionMemberContext.DEFINITION);
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid interface modifier");
		ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		validateMembers(definition, DefinitionMemberContext.DEFINITION);
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid enum modifier");
		ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		validateMembers(definition, DefinitionMemberContext.DEFINITION);
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		int validModifiers = PUBLIC | EXPORT | PROTECTED | PRIVATE;
		if (definition.outerDefinition != null)
			validModifiers |= STATIC;
		
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				validModifiers,
				definition.position,
				"Invalid struct modifier");
		ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		validateMembers(definition, DefinitionMemberContext.DEFINITION);
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid function modifier");
		ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
				
		StatementValidator statementValidator = new StatementValidator(validator, new FunctionStatementScope(definition.header));
		definition.caller.body.accept(statementValidator);
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid expansion modifier");
		
		definition.target.accept(new TypeValidator(validator, definition.position));
		validateMembers(definition, DefinitionMemberContext.EXPANSION);
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		ValidationUtils.validateModifiers(
				validator,
				definition.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				definition.position,
				"Invalid alias modifier");
		ValidationUtils.validateIdentifier(
				validator,
				definition.position,
				definition.name);
		
		return null;
	}
	
	private void validateMembers(HighLevelDefinition definition, DefinitionMemberContext context) {
		SimpleTypeScope scope = new SimpleTypeScope(validator.registry, validator.expansions, validator.annotations);
		DefinitionMemberValidator memberValidator = new DefinitionMemberValidator(validator, definition, scope, context);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberValidator);
		}
		if (definition instanceof EnumDefinition) {
			for (EnumConstantMember constant : ((EnumDefinition) definition).enumConstants) {
				memberValidator.visitEnumConstant(constant);
			}
		}
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		ValidationUtils.validateModifiers(
				validator,
				variant.modifiers,
				PUBLIC | EXPORT | PROTECTED | PRIVATE,
				variant.position,
				"Invalid variant modifier");
		ValidationUtils.validateIdentifier(
				validator,
				variant.position,
				variant.name);
		
		for (VariantDefinition.Option option : variant.options)
			validate(option);
		
		validateMembers(variant, DefinitionMemberContext.DEFINITION);
		return null;
	}
	
	private void validate(VariantDefinition.Option option) {
		ValidationUtils.validateIdentifier(validator, option.position, option.name);
		TypeValidator typeValidator = new TypeValidator(validator, option.position);
		for (StoredType type : option.types)
			typeValidator.validate(type);
	}
	
	private class SimpleTypeScope implements TypeScope {
		private final LocalMemberCache memberCache;
		private final Map<String, AnnotationDefinition> annotations = new HashMap<>();
		
		public SimpleTypeScope(GlobalTypeRegistry typeRegistry, List<ExpansionDefinition> expansions, AnnotationDefinition[] annotations) {
			memberCache = new LocalMemberCache(typeRegistry, expansions);
			
			for (AnnotationDefinition annotation : annotations)
				this.annotations.put(annotation.getAnnotationName(), annotation);
		}
		
		@Override
		public LocalMemberCache getMemberCache() {
			return memberCache;
		}

		@Override
		public AnnotationDefinition getAnnotation(String name) {
			return annotations.get(name);
		}

		@Override
		public TypeID getType(CodePosition position, List<GenericName> name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public StorageTag getStorageTag(CodePosition position, String name, String[] parameters) {
			throw new UnsupportedOperationException();
		}

		@Override
		public StoredType getThisType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public TypeMemberPreparer getPreparer() {
			return member -> {};
		}

		@Override
		public GenericMapper getLocalTypeParameters() {
			throw new UnsupportedOperationException();
		}
	}
	
	private class FunctionStatementScope implements StatementScope {
		private final FunctionHeader header;
		
		public FunctionStatementScope(FunctionHeader header) {
			this.header = header;
		}

		@Override
		public boolean isConstructor() {
			return false;
		}

		@Override
		public boolean isStatic() {
			return true;
		}

		@Override
		public FunctionHeader getFunctionHeader() {
			return header;
		}

		@Override
		public boolean isStaticInitializer() {
			return false;
		}

		@Override
		public HighLevelDefinition getDefinition() {
			return null;
		}
	}
}
