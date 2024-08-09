package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ModuleProcessor;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.identifiers.ExpansionSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class AnnotationProcessor implements ModuleProcessor {
	private final List<ExpansionSymbol> expansions;

	public AnnotationProcessor(List<ExpansionSymbol> expansions) {
		this.expansions = expansions;
	}

	@Override
	public ScriptBlock process(ScriptBlock block) {
		List<Statement> transformed = new ArrayList<>();
		boolean unchanged = true;
		for (Statement statement : block.statements) {
			Statement newStatement = process(statement);
			transformed.add(newStatement);
			unchanged &= statement == newStatement;
		}
		return unchanged ? block : block.withStatements(transformed);
	}

	@Override
	public void process(HighLevelDefinition definition) {
		for (DefinitionAnnotation annotation : definition.annotations) {
			annotation.apply(definition);
		}

		MemberAnnotationVisitor visitor = new MemberAnnotationVisitor();
		for (IDefinitionMember member : definition.members) {
			member.accept(visitor);
		}
	}

	private Statement process(Statement statement) {
		return statement.transform(s -> {
			for (StatementAnnotation annotation : s.annotations) {
				s = annotation.apply(s);
			}
			return s;
		});
	}

	private class MemberAnnotationVisitor implements MemberVisitor<Void> {

		@Override
		public Void visitField(FieldMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member);

			return null;
		}

		@Override
		public Void visitConstructor(ConstructorMember member) {
			return functional(member);
		}

		@Override
		public Void visitMethod(MethodMember member) {
			return functional(member);
		}

		@Override
		public Void visitGetter(GetterMember member) {
			return getter(member);
		}

		@Override
		public Void visitSetter(SetterMember member) {
			return setter(member);
		}

		@Override
		public Void visitOperator(OperatorMember member) {
			return functional(member);
		}

		@Override
		public Void visitCaster(CasterMember member) {
			return functional(member);
		}

		@Override
		public Void visitCustomIterator(IteratorMember member) {
			return functional(member);
		}

		@Override
		public Void visitImplementation(ImplementationMember implementation) {
			for (IDefinitionMember member : implementation.members) {
				member.accept(this);
			}
			return null;
		}

		@Override
		public Void visitInnerDefinition(InnerDefinitionMember member) {
			process(member.innerDefinition);
			return null;
		}

		@Override
		public Void visitStaticInitializer(StaticInitializerMember member) {
			if (member.body == null) {
				throw new IllegalStateException("No body in static initializer @ " + member.position);
			} else {
				member.body = process(member.body);
			}
			return null;
		}

		private Void functional(FunctionalMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member);

			member.getOverrides().ifPresent(overrides -> functional(member, overrides));

			if (member.body == null)
				return null;

			member.body = process(member.body);
			return null;
		}

		private Void getter(GetterMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member);

			member.getOverrides().ifPresent(overrides -> getter(member, overrides));

			if (member.body == null)
				return null;

			member.body = process(member.body);
			return null;
		}

		private Void setter(SetterMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member);

			member.getOverrides().ifPresent(overrides -> setter(member, overrides));

			if (member.body == null)
				return null;

			member.body = process(member.body);
			return null;
		}

		private void functional(FunctionalMember member, MethodInstance overrides) {
			for (MemberAnnotation annotation : overrides.method.getAnnotations())
				annotation.applyOnOverridingMethod(member);

			overrides.method.getOverrides().ifPresent(overrides2 -> functional(member, overrides2));
		}

		private void getter(GetterMember member, MethodInstance overrides) {
			for (MemberAnnotation annotation : overrides.method.getAnnotations())
				annotation.applyOnOverridingGetter(member);

			overrides.method.getOverrides().ifPresent(overrides2 -> getter(member, overrides2));
		}

		private void setter(SetterMember member, MethodInstance overrides) {
			for (MemberAnnotation annotation : overrides.method.getAnnotations())
				annotation.applyOnOverridingSetter(member);

			overrides.method.getOverrides().ifPresent(overrides2 -> setter(member, overrides2));
		}
	}
}
