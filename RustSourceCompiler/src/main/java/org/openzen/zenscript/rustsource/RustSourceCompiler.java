package org.openzen.zenscript.rustsource;

import org.openzen.zencode.shared.logging.IZSLogger;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.rustsource.definitions.*;
import org.openzen.zenscript.rustsource.expressions.RustExpressionCompiler;
import org.openzen.zenscript.rustsource.statements.RustStatementCompiler;
import org.openzen.zenscript.rustsource.types.RustTypeCompiler;

import java.util.*;

public class RustSourceCompiler {
	public final RustSourceFormattingSettings settings;
	public final RustSourceModule helpers;

	public RustSourceCompiler() {
		helpers = new RustSourceModule(new ModuleSymbol("helpers"), FunctionParameter.NONE);
		settings = new RustSourceFormattingSettings.Builder().build();
	}

	public RustSourceModule compile(IZSLogger logger, SemanticModule module, RustCompileSpace space, String basePackage) {
		RustSourceContext context = new RustSourceContext(
				space,
				module.modulePackage,
				basePackage,
				logger);

		RustSourceModule result = new RustSourceModule(module.module, module.parameters);
		context.addModule(module.module, result);

		List<RustSourceFile> sourceFiles = new ArrayList<>();
		for (HighLevelDefinition definition : module.definitions.getAll()) {
			RustFile file = getFile(definition.pkg, definition.name);
			RustSourceFile sourceFile = new RustSourceFile(file);
			definition.accept(new DefinitionVisitorImpl(result, sourceFile));
		}

		for (ScriptBlock scriptBlock : module.scripts) {
			// TODO
		}

		return result;
	}

	private RustFile getFile(ZSPackage zsPackage, String name) {
		List<String> path = new ArrayList<>();
		ZSPackage current = zsPackage;
		while (current != null) {
			path.add(current.name);
			current = current.parent;
		}
		Collections.reverse(path);
		path.add(name);
		return new RustFile(path.toArray(new String[0]));
	}

	private class DefinitionVisitorImpl implements DefinitionVisitor<Void> {
		private final RustSourceModule module;
		private final RustSourceFile file;

		public DefinitionVisitorImpl(RustSourceModule module, RustSourceFile file) {
			this.module = module;
			this.file = file;
		}

		@Override
		public Void visitClass(ClassDefinition definition) {
			RustStruct struct = new RustStruct(file.file, definition.name, !Modifiers.isPrivate(definition.modifiers));

			ClassMemberVisitor visitor = new ClassMemberVisitor(file, struct);
			for (IDefinitionMember member : definition.members) {
				member.accept(visitor);
			}

			for (String part : visitor.complete()) {
				file.addDefinition(part);
			}

			return null;
		}

		@Override
		public Void visitInterface(InterfaceDefinition definition) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Void visitEnum(EnumDefinition definition) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Void visitStruct(StructDefinition definition) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Void visitFunction(FunctionDefinition definition) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Void visitExpansion(ExpansionDefinition definition) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Void visitAlias(AliasDefinition definition) {
			throw new UnsupportedOperationException("Not yet supported");
		}

		@Override
		public Void visitVariant(VariantDefinition variant) {
			throw new UnsupportedOperationException("Not yet supported");
		}
	}

	private class ClassMemberVisitor implements MemberVisitor<Void> {
		private final RustSourceFile sourceFile;
		private final RustStruct struct;
		private final RustTypeCompiler typeCompiler;
		private final RustExpressionCompiler expressionCompiler;
		private final RustStatementCompiler statementCompiler;

		private final Set<String> methodNames = new HashSet<>();
		private final List<String> implMembers = new ArrayList<>();
		private final List<String> compiledMembers = new ArrayList<>();

		public ClassMemberVisitor(RustSourceFile sourceFile, RustStruct struct) {
			this.sourceFile = sourceFile;
			this.struct = struct;

			typeCompiler = new RustTypeCompiler(sourceFile.imports, false);
			expressionCompiler = new RustExpressionCompiler(sourceFile.imports, false);
			statementCompiler = new RustStatementCompiler(sourceFile.imports, false);
		}

		@Override
		public Void visitConst(ConstMember member) {
			String expression = expressionCompiler.compile(member.value);
			sourceFile.addConst("const " + member.name + " = " + expression + ";");
			return null;
		}

		@Override
		public Void visitField(FieldMember member) {
			String type = typeCompiler.compile(member.getType());
			struct.addField(new RustField(
					sourceFile.file,
					member.name,
					type,
					Modifiers.isPublic(member.getEffectiveModifiers())));

			return null;
		}

		@Override
		public Void visitConstructor(ConstructorMember member) {
			String name = addMethod("new");
			StringBuilder content = new StringBuilder();
			content.append("  fn ").append(name);
			compileHeader(content, member.header);
			content.append("{\n");
			content.append(member.body.accept("    ", statementCompiler));
			content.append("  }\n");
			implMembers.add(content.toString());
			return null;
		}

		@Override
		public Void visitMethod(MethodMember member) {
			String name = addMethod(member.name);
			StringBuilder content = new StringBuilder();
			content.append("  fn ").append(name);
			compileMethodHeader(content, member.header);
			content.append(" {\n");
			content.append(member.body.accept("    ", statementCompiler));
			content.append("  }\n");
			implMembers.add(content.toString());
			return null;
		}

		@Override
		public Void visitGetter(GetterMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitSetter(SetterMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitOperator(OperatorMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitCaster(CasterMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitCustomIterator(IteratorMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitImplementation(ImplementationMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitInnerDefinition(InnerDefinitionMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		@Override
		public Void visitStaticInitializer(StaticInitializerMember member) {
			throw new UnsupportedOperationException("not yet supported");
		}

		public List<String> complete() {
			if (!implMembers.isEmpty()) {
				StringBuilder impl = new StringBuilder();
				impl.append("impl ").append(struct.name).append(" {\n");
				for (String member : implMembers) {
					impl.append(member);
				}
				impl.append("}\n");
				compiledMembers.add(impl.toString());
			}
			return compiledMembers;
		}

		private String addMethod(String name) {
			if (!methodNames.contains(name)) {
				methodNames.add(name);
				return name;
			}

			int index = 1;
			while (true) {
				String extendedName = name + "_" + index;
				if (!methodNames.contains(name)) {
					methodNames.add(name);
					return name;
				}

				index++;
			}
		}

		private void compileHeader(StringBuilder output, FunctionHeader header) {
			output.append("(");
			boolean first = true;
			for (FunctionParameter param : header.parameters) {
				if (first) {
					first = false;
				} else {
					output.append(", ");
				}
				output.append(param.name).append(": ").append(param.type.accept(typeCompiler));
			}
			output.append(")");
			if (header.getReturnType() != BasicTypeID.VOID) {
				output.append(" -> ");
				output.append(header.getReturnType().accept(typeCompiler));
			}
		}

		private void compileMethodHeader(StringBuilder output, FunctionHeader header) {
			output.append("(&self");
			for (FunctionParameter param : header.parameters) {
				output.append(", ");
				output.append(param.name).append(": ").append(param.type.accept(typeCompiler));
			}
			output.append(")");
			if (header.getReturnType() != BasicTypeID.VOID) {
				output.append(" -> ");
				output.append(header.getReturnType().accept(typeCompiler));
			}
		}
	}
}