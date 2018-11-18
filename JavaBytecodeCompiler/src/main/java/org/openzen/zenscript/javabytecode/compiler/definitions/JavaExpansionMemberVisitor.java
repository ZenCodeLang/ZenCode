package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.CompilerUtils;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;

public class JavaExpansionMemberVisitor implements MemberVisitor<Void> {

	private final ClassWriter writer;
	private final JavaBytecodeContext context;
	private final StoredType expandedClass;
	private final HighLevelDefinition definition;
	private final JavaCompiledModule javaModule;

	public JavaExpansionMemberVisitor(JavaBytecodeContext context, ClassWriter writer, StoredType expandedClass, HighLevelDefinition definition) {
		this.writer = writer;
		this.expandedClass = expandedClass;
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
		if (!method.compile)
			return null;


		CompilerUtils.tagMethodParameters(context, javaModule, member.header, member.isStatic());

		final String expandedClassDescriptor = context.getDescriptor(expandedClass);
		final Label methodStart = new Label();
		final Label methodEnd = new Label();
		final String methodSignature;

		if (!isStatic) {
			methodSignature = "(" + expandedClassDescriptor + context.getMethodSignature(member.header).substring(1);
		} else {
			methodSignature = context.getMethodSignature(member.header);
		}


		final JavaWriter methodWriter = new JavaWriter(member.position, writer, true, method, definition, true, methodSignature, methodSignature, null);
		methodWriter.label(methodStart);

		if (!isStatic) {
			methodWriter.nameVariable(0, "expandedObj", methodStart, methodEnd, Type.getType(expandedClassDescriptor));
			methodWriter.nameParameter(0, "expandedObj");
			for (final FunctionParameter parameter : member.header.parameters) {
				methodWriter.nameParameter(0, parameter.name);
				methodWriter.nameVariable(javaModule.getParameterInfo(parameter).index, parameter.name, methodStart, methodEnd, context.getType(parameter.type));
			}
		} else {
			for (final FunctionParameter parameter : member.header.parameters) {
				methodWriter.nameParameter(0, parameter.name);
				methodWriter.nameVariable(javaModule.getParameterInfo(parameter).index, parameter.name, methodStart, methodEnd, context.getType(parameter.type));
			}
		}


		{
			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
			statementVisitor.start();
			member.body.accept(statementVisitor);
			methodWriter.label(methodEnd);
			statementVisitor.end();
		}

		return null;
	}


	@Override
	public Void visitGetter(GetterMember member) {
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		return null;
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
