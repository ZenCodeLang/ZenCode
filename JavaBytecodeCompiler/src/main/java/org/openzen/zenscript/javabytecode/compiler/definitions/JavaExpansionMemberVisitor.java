package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.CompilerUtils;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;

public class JavaExpansionMemberVisitor implements MemberVisitor<Void> {

	private final ClassWriter writer;
	private final JavaBytecodeContext context;
	private final JavaClass toClass;
	private final HighLevelDefinition definition;
	private final JavaCompiledModule javaModule;

	public JavaExpansionMemberVisitor(JavaBytecodeContext context, ClassWriter writer, JavaClass toClass, HighLevelDefinition definition) {
		this.writer = writer;
		this.toClass = toClass;
		this.definition = definition;
		this.context = context;
		javaModule = context.getJavaModule(definition.module);
	}

	public void end() {
	}

	@Override
	public Void visitConst(ConstMember member) {
		JavaField field = context.getJavaField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();
		return null;
	}

	@Override
	public Void visitField(FieldMember member) {
		throw new IllegalStateException("Cannot add fields via expansions");
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		throw new IllegalStateException("Cannot add constructors via expansions");
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		throw new IllegalStateException("Cannot add constructors via expansions");
	}

	@Override
	public Void visitMethod(MethodMember member) {
		final boolean isStatic = member.isStatic();
		final JavaMethod method = context.getJavaMethod(member);
		if(!method.compile)
			return null;


		CompilerUtils.tagMethodParameters(context, javaModule, member.header, member.isStatic());


		final Label methodStart = new Label();
		final Label methodEnd = new Label();
		final JavaWriter methodWriter = new JavaWriter(writer, method, definition, context.getMethodSignature(member.header), null);

		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		throw new IllegalStateException("Cannot add getters via expansions");
	}

	@Override
	public Void visitSetter(SetterMember member) {
		throw new IllegalStateException("Cannot add setters via expansions");
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		throw new IllegalStateException("Cannot add Static initializers via expansions");
	}
}
