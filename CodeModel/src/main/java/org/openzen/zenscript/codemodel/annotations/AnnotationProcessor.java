package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.ModuleProcessor;
import org.openzen.zenscript.codemodel.ScriptBlock;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.GetterMemberRef;
import org.openzen.zenscript.codemodel.member.ref.SetterMemberRef;
import org.openzen.zenscript.codemodel.scope.*;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnnotationProcessor implements ModuleProcessor {
	private final TypeResolutionContext context;
	private final List<ExpansionDefinition> expansions;

	public AnnotationProcessor(TypeResolutionContext context, List<ExpansionDefinition> expansions) {
		this.context = context;
		this.expansions = expansions;
	}

	@Override
	public ScriptBlock process(ScriptBlock block) {
		FileScope fileScope = new FileScope(context, expansions, new HashMap<>(), member -> {});
		StatementScope scope = new GlobalScriptScope(fileScope, block.scriptHeader);
		List<Statement> transformed = new ArrayList<>();
		boolean unchanged = true;
		for (Statement statement : block.statements) {
			Statement newStatement = process(statement, scope);
			transformed.add(newStatement);
			unchanged &= statement == newStatement;
		}
		return unchanged ? block : block.withStatements(transformed);
	}

	@Override
	public void process(HighLevelDefinition definition) {
		FileScope fileScope = new FileScope(context, expansions, new HashMap<>(), member -> {});
		DefinitionScope scope = new DefinitionScope(fileScope, definition);
		for (DefinitionAnnotation annotation : definition.annotations) {
			annotation.apply(definition);
		}

		MemberAnnotationVisitor visitor = new MemberAnnotationVisitor(scope);
		for (IDefinitionMember member : definition.members) {
			member.accept(visitor);
		}
	}

	private Statement process(Statement statement, StatementScope scope) {
		return statement.transform(s -> {
			for (StatementAnnotation annotation : s.annotations) {
				s = annotation.apply(s, scope);
			}
			return s;
		});
	}

	private class MemberAnnotationVisitor implements MemberVisitor<Void> {
		private final DefinitionScope scope;

		public MemberAnnotationVisitor(DefinitionScope scope) {
			this.scope = scope;
		}

		@Override
		public Void visitConst(ConstMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member, scope);

			return null;
		}

		@Override
		public Void visitField(FieldMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member, scope);

			return null;
		}

		@Override
		public Void visitConstructor(ConstructorMember member) {
			return functional(member);
		}

		@Override
		public Void visitDestructor(DestructorMember member) {
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
		public Void visitCaller(CallerMember member) {
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
			StatementScope scope = new FunctionScope(member.position, this.scope, new FunctionHeader(BasicTypeID.VOID));
			if (member.body == null) {
				throw new IllegalStateException("No body in static initializer @ " + member.position);
			} else {
				member.body = process(member.body, scope);
			}
			return null;
		}

		private Void functional(FunctionalMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member, scope);

			if (member.getOverrides() != null) {
				functional(member, member.getOverrides());
			}

			if (member.body == null)
				return null;

			StatementScope scope = new FunctionScope(member.position, this.scope, member.header);
			member.body = process(member.body, scope);
			return null;
		}

		private Void getter(GetterMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member, scope);

			if (member.getOverrides() != null) {
				getter(member, member.getOverrides());
			}

			if (member.body == null)
				return null;

			StatementScope scope = new FunctionScope(member.position, this.scope, new FunctionHeader(member.getType()));
			member.body = process(member.body, scope);
			return null;
		}

		private Void setter(SetterMember member) {
			for (MemberAnnotation annotation : member.annotations)
				annotation.apply(member, scope);

			if (member.getOverrides() != null) {
				setter(member, member.getOverrides());
			}

			if (member.body == null)
				return null;

			StatementScope scope = new FunctionScope(member.position, this.scope, new FunctionHeader(BasicTypeID.VOID, member.parameter));
			member.body = process(member.body, scope);
			return null;
		}

		private void functional(FunctionalMember member, MethodSymbol overrides) {
			for (MemberAnnotation annotation : overrides.getAnnotations())
				annotation.applyOnOverridingMethod(member, scope);
		}

		private void getter(GetterMember member, MethodSymbol overrides) {
			for (MemberAnnotation annotation : overrides.getAnnotations())
				annotation.applyOnOverridingGetter(member, scope);
		}

		private void setter(SetterMember member, SetterMemberRef overrides) {
			for (MemberAnnotation annotation : overrides.getAnnotations())
				annotation.applyOnOverridingSetter(member, scope);

			if (overrides.getOverrides() != null) {
				setter(member, overrides.getOverrides());
			}
		}
	}
}
