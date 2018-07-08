/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
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
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.compiler.CompileScope;
import org.openzen.zenscript.compiler.SemanticModule;
import org.openzen.zenscript.javasource.prepare.JavaNativeClass;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javasource.tags.JavaSourceClass;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaDefinitionVisitor implements DefinitionVisitor<Void> {
	private final JavaSourceCompiler compiler;
	private final JavaSourceClass cls;
	private final JavaSourceFile file;
	private final JavaSourceFormattingSettings settings;
	private final List<ExpansionDefinition> expansions;
	private final StringBuilder output;
	private final Map<HighLevelDefinition, SemanticModule> modules;
	
	public JavaDefinitionVisitor(
			JavaSourceCompiler compiler,
			JavaSourceClass cls,
			JavaSourceFile file,
			StringBuilder output,
			List<ExpansionDefinition> expansions,
			Map<HighLevelDefinition, SemanticModule> modules)
	{
		this.compiler = compiler;
		this.cls = cls;
		this.file = file;
		this.settings = compiler.settings;
		this.output = output;
		this.expansions = expansions;
		this.modules = modules;
	}
	
	private JavaSourceFileScope createScope(HighLevelDefinition definition) {
		SemanticModule module = modules.get(definition);
		CompileScope scope = new CompileScope(definition.access, module.compilationUnit.globalTypeRegistry, module.expansions, module.annotations);
		return new JavaSourceFileScope(file.importer, compiler.typeGenerator, compiler.helperGenerator, cls, scope, definition instanceof InterfaceDefinition);
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		
		convertModifiers(definition.modifiers);
		output.append("class ").append(definition.name);
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, definition.genericParameters);
		if (definition.superType != null) {
			output.append(" extends ");
			output.append(scope.type(definition.superType));
		}
		List<ImplementationMember> mergedImplementations = new ArrayList<>();
		for (IDefinitionMember member : definition.members) {
			if (member instanceof ImplementationMember) {
				if (isImplementationMergable((ImplementationMember)member))
					mergedImplementations.add((ImplementationMember)member);
			}
		}
		
		if (mergedImplementations.size() > 0) {
			output.append("implements ");
			for (int i = 0; i < mergedImplementations.size(); i++) {
				if (i > 0)
					output.append(", ");
				
				ImplementationMember implementation = mergedImplementations.get(i);
				output.append(scope.type(implementation.type));
			}
		}
		
		output.append(" {\n");
		compileMembers(scope, definition);
		compileExpansions();
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		
		convertModifiers(definition.modifiers | Modifiers.VIRTUAL); // to prevent 'final'
		output.append("interface ").append(definition.name);
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, definition.genericParameters);
		output.append(" {\n");
		compileMembers(scope, definition);
		compileExpansions();
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		
		convertModifiers(definition.modifiers | Modifiers.VIRTUAL); // to prevent 'final'
		output.append("enum ").append(definition.name);
		
		output.append(" {\n");
		boolean firstMember = true;
		for (EnumConstantMember constant : definition.enumConstants) {
			if (firstMember) {
				firstMember = false;
			} else {
				output.append(",\n");
			}
			output.append("\t").append(constant.name);
			if (constant.constructor != null) {
				// TODO: constructor
			}
		}
		
		if (definition.members.size() > 0) {
			output.append(";\n");
			compileMembers(scope, definition);
		} else {
			output.append("\n");
		}
		
		compileExpansions();
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		
		convertModifiers(definition.modifiers | Modifiers.FINAL);
		output.append("class ").append(definition.name);
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, definition.genericParameters);
		output.append(" {\n");
		compileMembers(scope, definition);
		compileExpansions();
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		
		convertModifiers(definition.modifiers | Modifiers.STATIC);
		
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		
		convertModifiers(definition.modifiers);
		output.append("class ");
		output.append(cls.name);
		output.append(" {\n");
		output.append(settings.indent).append("private ").append(cls.name).append("() {}\n");
		
		JavaExpansionMemberCompiler memberCompiler = new JavaExpansionMemberCompiler(settings, definition.target, definition.genericParameters, "\t", output, scope, definition);
		for (IDefinitionMember member : definition.members)
			member.accept(memberCompiler);
		memberCompiler.finish();
		
		compileExpansions();
		output.append("}");
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		throw new UnsupportedOperationException("Should not arrive here");
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		JavaSourceFileScope scope = createScope(variant);
		
		convertModifiers(variant.modifiers | Modifiers.VIRTUAL | Modifiers.ABSTRACT);
		output.append("class ").append(variant.name);
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, variant.genericParameters);
		output.append("{\n");
		compileMembers(scope, variant);
		output.append(settings.indent).append("public abstract Discriminant getDiscriminant();\n");
		
		output.append(settings.indent).append("\n");
		output.append(settings.indent).append("public static enum Discriminant {\n");
		for (VariantDefinition.Option option : variant.options) {
			output.append(settings.indent).append(settings.indent).append(option.name).append(",\n");
		}
		output.append(settings.indent).append("}\n");
		
		for (VariantDefinition.Option option : variant.options) {
			output.append(settings.indent).append("\n");
			output.append(settings.indent).append("public static class ").append(option.name);
			JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, variant.genericParameters);
			output.append(" extends ");
			output.append(variant.name);
			if (variant.genericParameters != null && variant.genericParameters.length > 0) {
				output.append('<');
				for (int i = 0; i < variant.genericParameters.length; i++) {
					if (i > 0)
						output.append(", ");
					output.append(variant.genericParameters[i].name);
				}
				output.append('>');
			}
			output.append(" {\n");
			
			for (int i = 0; i < option.types.length; i++) {
				String name = option.types.length == 1 ? "value" : "value" + (i + 1);
				output.append(settings.indent).append(settings.indent).append("public final ").append(scope.type(option.types[i])).append(" ").append(name).append(";\n");
			}
			output.append(settings.indent).append(settings.indent).append("\n");
			output.append(settings.indent).append(settings.indent).append("public ").append(option.name).append("(");
			for (int i = 0; i < option.types.length; i++) {
				if (i > 0)
					output.append(", ");
				String name = option.types.length == 1 ? "value" : "value" + (i + 1);
				output.append(scope.type(option.types[i])).append(' ').append(name);
			}
			output.append("){\n");
			for (int i = 0; i < option.types.length; i++) {
				if (i > 0)
					output.append(settings.indent).append(settings.indent).append(settings.indent).append(";\n");
				
				String name = option.types.length == 1 ? "value" : "value" + (i + 1);
				output.append(settings.indent).append(settings.indent).append(settings.indent).append("this.").append(name).append(" = ").append(name).append(";\n");
			}
			output.append(settings.indent).append(settings.indent).append("}\n");
			
			output.append(settings.indent).append(settings.indent).append("\n");
			output.append(settings.indent).append(settings.indent).append("@Override\n");
			output.append(settings.indent).append(settings.indent).append("public Discriminant getDiscriminant() {\n");
			output.append(settings.indent).append(settings.indent).append(settings.indent).append("return Discriminant.").append(option.name).append(";\n");
			output.append(settings.indent).append(settings.indent).append("}\n");
			output.append(settings.indent).append("}\n");
		}
		
		compileExpansions();
		output.append("}\n");
		return null;
	}
	
	private void compileExpansions() {
		for (ExpansionDefinition definition : expansions) {
			JavaSourceFileScope scope = createScope(definition);
			JavaExpansionMemberCompiler memberCompiler = new JavaExpansionMemberCompiler(settings, definition.target, definition.genericParameters, "\t", output, scope, definition);
			for (IDefinitionMember member : definition.members)
				member.accept(memberCompiler);
			memberCompiler.finish();
		}
	}
	
	private void convertModifiers(int modifiers) {
		if (Modifiers.isExport(modifiers) || Modifiers.isPublic(modifiers))
			output.append("public ");
		if (Modifiers.isPrivate(modifiers))
			output.append("private ");
		if (Modifiers.isProtected(modifiers))
			output.append("protected ");
		if (Modifiers.isStatic(modifiers))
			output.append("static ");
		if (Modifiers.isAbstract(modifiers))
			output.append("abstract ");
		if (!Modifiers.isVirtual(modifiers))
			output.append("final ");
	}
	
	private boolean isImplementationMergable(ImplementationMember implementation) {
		return true; // TODO: check merging conflicts
	}
	
	private void compileMembers(JavaSourceFileScope scope, HighLevelDefinition definition) {
		if (definition.hasTag(JavaNativeClass.class)) {
			ITypeID[] typeParameters = new ITypeID[definition.getNumberOfGenericParameters()];
			for (int i = 0; i < typeParameters.length; i++)
				typeParameters[i] = scope.semanticScope.getTypeRegistry().getGeneric(definition.genericParameters[i]);
			ITypeID targetType = new DefinitionTypeID(definition, typeParameters);
			
			JavaExpansionMemberCompiler memberCompiler = new JavaExpansionMemberCompiler(settings, targetType, definition.genericParameters, "\t", output, scope, definition);
			for (IDefinitionMember member : definition.members)
				member.accept(memberCompiler);
			memberCompiler.finish();
		} else {
			JavaMemberCompiler memberCompiler = new JavaMemberCompiler(settings, "\t", output, scope, scope.isInterface, definition);
			for (IDefinitionMember member : definition.members)
				member.accept(memberCompiler);
			memberCompiler.finish();
		}
	}
}
