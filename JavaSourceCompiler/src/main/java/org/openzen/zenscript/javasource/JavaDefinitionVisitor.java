/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.SemanticModule;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.compiler.CompileScope;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaImplementation;

/**
 * @author Hoofdgebruiker
 */
public class JavaDefinitionVisitor implements DefinitionVisitor<Void> {
	private final String indent;
	private final JavaSourceCompiler compiler;
	private final JavaClass cls;
	private final JavaSourceFile file;
	private final JavaSourceFormattingSettings settings;
	private final List<ExpansionDefinition> expansions;
	private final StringBuilder output;
	private final JavaSourceContext context;
	private final JavaCompiledModule module;
	private final SemanticModule semanticModule;

	public JavaDefinitionVisitor(
			String indent,
			JavaSourceCompiler compiler,
			JavaSourceContext context,
			JavaCompiledModule module,
			JavaClass cls,
			JavaSourceFile file,
			StringBuilder output,
			List<ExpansionDefinition> expansions,
			SemanticModule semanticModule) {
		this.indent = indent;
		this.compiler = compiler;
		this.cls = cls;
		this.file = file;
		this.settings = compiler.settings;
		this.output = output;
		this.expansions = expansions;
		this.context = context;
		this.module = module;
		this.semanticModule = semanticModule;
	}

	private JavaSourceFileScope createScope(HighLevelDefinition definition) {
		GlobalTypeRegistry typeRegistry = semanticModule.registry;
		DefinitionTypeID thisType = typeRegistry.getForMyDefinition(definition);

		CompileScope scope = new CompileScope(
				semanticModule.registry,
				semanticModule.rootPackage,
				semanticModule.expansions,
				semanticModule.annotations);
		return new JavaSourceFileScope(file.importer, context, cls, scope, definition instanceof InterfaceDefinition, thisType);
	}

	private List<ImplementationMember> getMergedImplementations(HighLevelDefinition definition) {
		List<ImplementationMember> result = new ArrayList<>();
		for (IDefinitionMember member : definition.members) {
			if (member instanceof ImplementationMember) {
				JavaImplementation implementation = module.getImplementationInfo((ImplementationMember) member);
				if (implementation.inline)
					result.add((ImplementationMember) member);
			}
		}
		return result;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		JavaClass cls = context.getJavaExpansionClass(definition);

		output.append(indent);
		convertModifiers(definition.modifiers);
		output.append("class ").append(cls.getName());
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, definition.typeParameters, false);
		if (definition.getSuperType() != null) {
			output.append(" extends ");
			output.append(scope.type(definition.getSuperType()));
		}

		List<ImplementationMember> mergedImplementations = getMergedImplementations(definition);
		if (mergedImplementations.size() > 0 /*|| cls.destructible*/) {
			output.append(" implements ");
			boolean first = true;
			for (int i = 0; i < mergedImplementations.size(); i++) {
				if (first)
					first = false;
				else
					output.append(", ");

				ImplementationMember implementation = mergedImplementations.get(i);
				output.append(scope.type(implementation.type));
			}

			/*
			if (cls.destructible) {
				if (first)
					first = false;
				else
					output.append(", ");

				output.append(scope.importer.importType(JavaClass.CLOSEABLE));
			}
			 */
		}

		output.append(" {\n");
		compileMembers(scope, definition);
		compileExpansions();
		output.append(indent).append("}\n");
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);
		JavaClass cls = context.getJavaClass(definition);

		output.append(indent);
		convertModifiers(definition.modifiers.withVirtual()); // to prevent 'final'
		output.append("interface ").append(cls.getName());
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, definition.typeParameters, false);

		boolean firstExtends = true;
		/*
		if (definition.isDestructible()) {
			output.append(" extends ");
			output.append(scope.importer.importType(JavaClass.CLOSEABLE));
			firstExtends = false;
		}
		 */

		for (TypeID base : definition.baseInterfaces) {
			if (firstExtends) {
				firstExtends = false;
				output.append(" extends ");
			} else {
				output.append(", ");
			}
			output.append(scope.type(base));
		}

		List<ImplementationMember> mergedImplementations = getMergedImplementations(definition);
		for (ImplementationMember member : mergedImplementations) {
			if (firstExtends) {
				firstExtends = false;
				output.append(" extends ");
			} else {
				output.append(", ");
			}
			output.append(scope.type(member.type));
		}

		output.append(" {\n");
		compileMembers(scope, definition);
		compileExpansions();
		output.append(indent).append("}\n");
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);

		JavaSourceStatementScope fieldInitializerScope = new JavaSourceStatementScope(
				scope,
				settings,
				null,
				indent + settings.indent,
				null,
				null,
				true);

		output.append(indent);
		convertModifiers(definition.modifiers.withVirtual()); // to prevent 'final'
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
			if (constant.constructor != null && constant.constructor.arguments.arguments.length > 0) {
				output.append("(");
				boolean first = true;
				for (Expression argument : constant.constructor.arguments.arguments) {
					if (first)
						first = false;
					else
						output.append(", ");

					output.append(fieldInitializerScope.expression(null, argument));
				}
				output.append(")");
			}
		}

		if (definition.members.size() > 0) {
			output.append(";\n");
			compileMembers(scope, definition);
		} else {
			output.append("\n");
		}

		compileExpansions();
		output.append(indent).append("}\n");
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);

		output.append(indent);
		convertModifiers(definition.modifiers.withFinal());
		output.append("class ").append(definition.name);
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, definition.typeParameters, false);
		output.append(" {\n");
		compileMembers(scope, definition);
		compileExpansions();
		output.append(indent).append("}\n");
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);

		convertModifiers(definition.modifiers.withStatic());

		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		JavaSourceFileScope scope = createScope(definition);

		output.append(indent);
		convertModifiers(definition.modifiers);
		output.append("class ");
		output.append(cls.getName());
		output.append(" {\n");
		output.append(indent).append(settings.indent).append("private ").append(cls.getName()).append("() {}\n");

		JavaExpansionMemberCompiler memberCompiler = new JavaExpansionMemberCompiler(settings, definition.target, definition.typeParameters, "\t", output, scope, definition);
		for (IDefinitionMember member : definition.members)
			member.accept(memberCompiler);
		memberCompiler.finish();

		compileExpansions();
		output.append("}");
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		// nothing to do
		return null;
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		JavaSourceFileScope scope = createScope(variant);

		convertModifiers(variant.modifiers.withVirtual().withAbstract());
		output.append(indent).append("class ").append(variant.name);
		JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, variant.typeParameters, false);
		output.append(" {\n");
		compileMembers(scope, variant);
		output.append(indent).append(settings.indent).append("public abstract Discriminant getDiscriminant();\n");

		output.append(indent).append(settings.indent).append("\n");
		output.append(indent).append(settings.indent).append("public static enum Discriminant {\n");
		for (VariantDefinition.Option option : variant.options) {
			output.append(indent).append(settings.indent).append(settings.indent).append(option.name).append(",\n");
		}
		output.append(indent).append(settings.indent).append("}\n");

		for (VariantDefinition.Option option : variant.options) {
			output.append(indent).append(settings.indent).append("\n");
			output.append(indent).append(settings.indent).append("public static class ").append(option.name);
			JavaSourceUtils.formatTypeParameters(scope.typeVisitor, output, variant.typeParameters, false);
			output.append(" extends ");
			output.append(variant.name);
			if (variant.typeParameters != null && variant.typeParameters.length > 0) {
				output.append('<');
				for (int i = 0; i < variant.typeParameters.length; i++) {
					if (i > 0)
						output.append(", ");
					output.append(variant.typeParameters[i].name);
				}
				output.append('>');
			}
			output.append(" {\n");

			for (int i = 0; i < option.types.length; i++) {
				String name = option.types.length == 1 ? "value" : "value" + (i + 1);
				output.append(indent).append(settings.indent).append(settings.indent).append("public final ").append(scope.type(option.types[i])).append(" ").append(name).append(";\n");
			}
			output.append(indent).append(settings.indent).append(settings.indent).append("\n");
			output.append(indent).append(settings.indent).append(settings.indent).append("public ").append(option.name).append("(");
			for (int i = 0; i < option.types.length; i++) {
				if (i > 0)
					output.append(", ");
				String name = option.types.length == 1 ? "value" : "value" + (i + 1);
				output.append(scope.type(option.types[i])).append(' ').append(name);
			}
			output.append(") {\n");
			for (int i = 0; i < option.types.length; i++) {
				if (i > 0)
					output.append(indent).append(settings.indent).append(settings.indent).append(settings.indent).append(";\n");

				String name = option.types.length == 1 ? "value" : "value" + (i + 1);
				output.append(indent).append(settings.indent).append(settings.indent).append(settings.indent).append("this.").append(name).append(" = ").append(name).append(";\n");
			}
			output.append(indent).append(settings.indent).append(settings.indent).append("}\n");

			output.append(indent).append(settings.indent).append(settings.indent).append("\n");
			output.append(indent).append(settings.indent).append(settings.indent).append("@Override\n");
			output.append(indent).append(settings.indent).append(settings.indent).append("public Discriminant getDiscriminant() {\n");
			output.append(indent).append(settings.indent).append(settings.indent).append(settings.indent).append("return Discriminant.").append(option.name).append(";\n");
			output.append(indent).append(settings.indent).append(settings.indent).append("}\n");
			output.append(indent).append(settings.indent).append("}\n");
		}

		compileExpansions();
		output.append(indent).append("}\n");
		return null;
	}

	private void compileExpansions() {
		for (ExpansionDefinition definition : expansions) {
			JavaSourceFileScope scope = createScope(definition);
			JavaExpansionMemberCompiler memberCompiler = new JavaExpansionMemberCompiler(settings, definition.target, definition.typeParameters, indent + settings.indent, output, scope, definition);
			for (IDefinitionMember member : definition.members)
				member.accept(memberCompiler);
			memberCompiler.finish();
		}
	}

	private void convertModifiers(Modifiers modifiers) {
		if (modifiers.isPublic() || modifiers.isInternal())
			output.append("public ");
		if (modifiers.isPrivate())
			output.append("private ");
		if (modifiers.isProtected())
			output.append("protected ");
		if (modifiers.isStatic())
			output.append("static ");
		if (modifiers.isAbstract())
			output.append("abstract ");
		if (!modifiers.isVirtual() && !modifiers.isAbstract())
			output.append("final ");
	}

	private void compileMembers(JavaSourceFileScope scope, HighLevelDefinition definition) {
		if (context.getJavaNativeClass(definition) != null) {
			TypeID[] typeParameters = new TypeID[definition.getNumberOfGenericParameters()];
			for (int i = 0; i < typeParameters.length; i++)
				typeParameters[i] = scope.semanticScope.getTypeRegistry().getGeneric(definition.typeParameters[i]);
			TypeID targetType = scope.semanticScope.getTypeRegistry().getForDefinition(definition, typeParameters);

			JavaExpansionMemberCompiler memberCompiler = new JavaExpansionMemberCompiler(settings, targetType, definition.typeParameters, indent + settings.indent, output, scope, definition);
			for (IDefinitionMember member : definition.members)
				member.accept(memberCompiler);
			memberCompiler.finish();
		} else {
			JavaMemberCompiler memberCompiler = new JavaMemberCompiler(compiler, context, module, file, settings, indent + settings.indent, output, scope, scope.isInterface, definition, semanticModule);
			for (IDefinitionMember member : definition.members)
				member.accept(memberCompiler);

			/*
			if (definition.isDestructible() && !memberCompiler.hasDestructor) {
				DestructorMember emptyDestructor = new DestructorMember(CodePosition.BUILTIN, definition, 0);
				emptyDestructor.body = new BlockStatement(CodePosition.BUILTIN, new Statement[0]);
				memberCompiler.visitDestructor(emptyDestructor);
			}
			 */

			memberCompiler.finish();
		}
	}
}
