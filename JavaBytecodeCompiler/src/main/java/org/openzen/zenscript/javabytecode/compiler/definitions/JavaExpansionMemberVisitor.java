package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.CompilerUtils;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.util.ArrayList;

public class JavaExpansionMemberVisitor implements MemberVisitor<Void> {

	private final ClassWriter writer;
	private final JavaBytecodeContext context;
	private final TypeID expandedClass;
	private final HighLevelDefinition definition;
	private final JavaCompiledModule javaModule;
	private final JavaCompilingClass class_;
	private final JavaMangler mangler;

	private final JavaStatementVisitor clinitStatementVisitor;

	public JavaExpansionMemberVisitor(JavaBytecodeContext context, JavaCompilingClass class_, ClassWriter writer, TypeID expandedClass, HighLevelDefinition definition, JavaMangler mangler) {
		this.writer = writer;
		this.class_ = class_;
		this.expandedClass = expandedClass;
		this.definition = definition;
		this.context = context;
		this.mangler = mangler;

		javaModule = context.getJavaModule(definition.module);

		JavaNativeMethod clinit = new JavaNativeMethod(context.getJavaClass(definition), JavaNativeMethod.Kind.STATICINIT, "<clinit>", true, "()V", Opcodes.ACC_STATIC, false);
		JavaCompilingMethod clinitCompiling = new JavaCompilingMethod(class_.compiled, clinit, "()V");
		final JavaWriter javaWriter = new JavaWriter(context.logger, definition.position, writer, clinitCompiling, definition);
		this.clinitStatementVisitor = new JavaStatementVisitor(context, javaModule, javaWriter, mangler);
		this.clinitStatementVisitor.start();
		CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, definition, mangler, true);
	}

	public void end() {
		clinitStatementVisitor.end();
	}

	@Override
	public Void visitField(FieldMember member) {
		if (!member.isStatic())
			throw new IllegalStateException("Cannot add fields via expansions");

		JavaNativeField field = class_.getField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		throw new IllegalStateException("Cannot add constructors via expansions");
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visitFunctional(member);
		return null;
	}

	private void visitFunctional(FunctionalMember member) {
		final boolean isStatic = member.isStatic();
		final JavaCompilingMethod method = class_.getMethod(member);
		if (!method.compile) {
			return;
		}

		if (member.body == null && member.hasTag(NativeTag.class)) {
			//Is it an error that method.compile == true then?
			return;
		}

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);

		CompilerUtils.tagMethodParameters(context, javaModule, member.header, member.isStatic(), typeParameters);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();


		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, method, definition, true);
		methodWriter.label(methodStart);

		if (!isStatic) {

			for (TypeParameter typeParameter : typeParameters) {
				methodWriter.nameParameter(0, "typeOf" + typeParameter.name);
				methodWriter.nameVariable(javaModule.getTypeParameterInfo(typeParameter).parameterIndex, "typeOf" + typeParameter.name, methodStart, methodEnd, Type.getType(Class.class));
			}

			methodWriter.nameVariable(typeParameters.size(), "expandedObj", methodStart, methodEnd, context.getType(expandedClass));
			methodWriter.nameParameter(0, "expandedObj");
		}

		for (TypeParameter typeParameter : member.header.typeParameters) {
			methodWriter.nameParameter(0, "typeOf" + typeParameter.name);
			methodWriter.nameVariable(javaModule.getTypeParameterInfo(typeParameter).parameterIndex, "typeOf" + typeParameter.name, methodStart, methodEnd, Type.getType(Class.class));
		}

		for (final FunctionParameter parameter : member.header.parameters) {
			methodWriter.nameParameter(0, parameter.name);
			methodWriter.nameVariable(javaModule.getParameterInfo(parameter).index, parameter.name, methodStart, methodEnd, context.getType(parameter.type));
		}


		{
			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter, mangler);
			statementVisitor.start();
			member.body.accept(statementVisitor);
			methodWriter.label(methodEnd);
			statementVisitor.end();
		}
	}


	@Override
	public Void visitGetter(GetterMember member) {
		final boolean isStatic = member.isStatic();
		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaCompilingMethod method = class_.getMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, this.writer, method, definition, true);

		methodWriter.label(methodStart);

		if (!isStatic) {
			methodWriter.nameVariable(0, "expandedObj", methodStart, methodEnd, context.getType(this.expandedClass));
			methodWriter.nameParameter(0, "expandedObj");
		}

		int i = isStatic ? 0 : 1;
		for (TypeParameter typeParameter : typeParameters) {
			final String name = "typeOf" + typeParameter.name;
			methodWriter.nameVariable(i, name, methodStart, methodEnd, Type.getType(Class.class));
			methodWriter.nameParameter(0, name);
		}

		{
			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter, mangler);
			statementVisitor.start();
			member.body.accept(statementVisitor);
			methodWriter.label(methodEnd);
			statementVisitor.end();
		}

		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		final boolean isStatic = member.isStatic();
		final TypeID setterType = member.parameter.type;

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);
		CompilerUtils.tagMethodParameters(context, javaModule, member.getHeader(), isStatic, typeParameters);
		setterType.extractTypeParameters(typeParameters);


		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaCompilingMethod javaMethod = class_.getMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, javaMethod, member.definition, true);


		methodWriter.label(methodStart);
		if (!isStatic) {
			methodWriter.nameVariable(0, "expandedObj", methodStart, methodEnd, context.getType(this.expandedClass));
			methodWriter.nameParameter(0, "expandedObj");
		}

		int i = isStatic ? 0 : 1;
		for (TypeParameter typeParameter : typeParameters) {
			final String name = "typeOf" + typeParameter.name;
			methodWriter.nameVariable(i, name, methodStart, methodEnd, Type.getType(Class.class));
			methodWriter.nameParameter(0, name);
			i++;
		}

		//in script you use $ but the parameter is named "value", which to choose?
		//final String name = member.parameter.name;
		final String name = "$";
		methodWriter.nameVariable(i, name, methodStart, methodEnd, context.getType(setterType));
		methodWriter.nameParameter(0, name);

		javaModule.setParameterInfo(member.parameter, new JavaParameterInfo(i, context.getDescriptor(setterType)));

		final JavaStatementVisitor javaStatementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter, mangler);
		javaStatementVisitor.start();
		member.body.accept(javaStatementVisitor);
		javaStatementVisitor.end();
		methodWriter.label(methodEnd);

		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		final JavaCompilingMethod javaMethod = class_.getMethod(member);
		if (!javaMethod.compile) {
			return null;
		}

		visitFunctional(member);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);

		CompilerUtils.tagMethodParameters(context, javaModule, member.getHeader(), false, typeParameters);
		member.toType.extractTypeParameters(typeParameters);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaCompilingMethod javaMethod = class_.getMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, javaMethod, member.definition, true);

		methodWriter.label(methodStart);
		methodWriter.nameVariable(0, "expandedObj", methodStart, methodEnd, context.getType(this.expandedClass));
		methodWriter.nameParameter(0, "expandedObj");

		int i = 1;
		for (TypeParameter typeParameter : typeParameters) {
			final String name = "typeOf" + typeParameter.name;
			methodWriter.nameVariable(i, name, methodStart, methodEnd, Type.getType(Class.class));
			methodWriter.nameParameter(0, name);
		}

		final JavaStatementVisitor javaStatementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter, mangler);
		javaStatementVisitor.start();
		member.body.accept(javaStatementVisitor);
		javaStatementVisitor.end();
		methodWriter.label(methodEnd);
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
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
		member.body.accept(clinitStatementVisitor);
		return null;
	}
}
