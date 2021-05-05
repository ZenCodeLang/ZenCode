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
import org.openzen.zenscript.javabytecode.compiler.CompilerUtils;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaField;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class JavaExpansionMemberVisitor implements MemberVisitor<Void> {

	private final ClassWriter writer;
	private final JavaBytecodeContext context;
	private final TypeID expandedClass;
	private final HighLevelDefinition definition;
	private final JavaCompiledModule javaModule;

	private final JavaStatementVisitor clinitStatementVisitor;

	public JavaExpansionMemberVisitor(JavaBytecodeContext context, ClassWriter writer, TypeID expandedClass, HighLevelDefinition definition) {
		this.writer = writer;
		this.expandedClass = expandedClass;
		this.definition = definition;
		this.context = context;
		javaModule = context.getJavaModule(definition.module);

		final JavaWriter javaWriter = new JavaWriter(context.logger, definition.position, writer, new JavaMethod(context.getJavaClass(definition), JavaMethod.Kind.STATICINIT, "<clinit>", true, "()V", Opcodes.ACC_STATIC, false), definition, null, null);
		this.clinitStatementVisitor = new JavaStatementVisitor(context, javaModule, javaWriter);
		this.clinitStatementVisitor.start();
		CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, definition, true);
	}

	public void end() {
		clinitStatementVisitor.end();
	}

	@Override
	public Void visitConst(ConstMember member) {
		JavaField field = context.getJavaField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();
		return null;
	}

	@Override
	public Void visitField(FieldMember member) {
		if (!member.isStatic())
			throw new IllegalStateException("Cannot add fields via expansions");

		JavaField field = context.getJavaField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();

		return null;


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
		if (!method.compile) {
			return null;
		}

		if (member.body == null && member.hasTag(NativeTag.class)) {
			//Is it an error that method.compile == true then?
			return null;
		}

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);

		CompilerUtils.tagMethodParameters(context, javaModule, member.header, member.isStatic(), typeParameters);

		final String expandedClassDescriptor = context.getDescriptor(expandedClass);
		final String expandedClassSignature = context.getSignature(expandedClass);
		final Label methodStart = new Label();
		final Label methodEnd = new Label();
		final String methodSignature;
		final String methodDescriptor;


		if (!isStatic) {
			String methodSignature1 = context.getMethodSignature(member.header);

			//Add the expanded type as first generic parameter to the list.
			if (!typeParameters.isEmpty()) {
				final String collect = typeParameters.stream()
						.map(t -> t.name + ":" + "Ljava/lang/Object;")
						.collect(Collectors.joining("", "<", ""));
				if (methodSignature1.startsWith("<")) {
					methodSignature1 = collect + methodSignature1.substring(1);
				} else {
					methodSignature1 = collect + ">" + methodSignature1;
				}
			}

			final StringBuilder typeParamSigBuilder = new StringBuilder();
			final StringBuilder typeParamDescBuilder = new StringBuilder();
			int i = 1;
			for (TypeParameter typeParameter : typeParameters) {
				typeParamSigBuilder.append("Ljava/lang/Class<T").append(typeParameter.name).append(";>;");
				typeParamDescBuilder.append("Ljava/lang/Class;");
			}


			final int index = methodSignature1.lastIndexOf('(') + 1;
			methodSignature = methodSignature1.substring(0, index) + expandedClassSignature + typeParamSigBuilder.toString() + methodSignature1.substring(index);
			methodDescriptor = "(" + expandedClassDescriptor + typeParamDescBuilder.toString() + context.getMethodDescriptor(member.header).substring(1);
		} else {
			methodSignature = context.getMethodSignature(member.header);
			methodDescriptor = context.getMethodDescriptor(member.header);
		}


		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, true, method, definition, true, methodSignature, methodDescriptor, null);
		methodWriter.label(methodStart);

		if (!isStatic) {
			methodWriter.nameVariable(0, "expandedObj", methodStart, methodEnd, Type.getType(expandedClassDescriptor));
			methodWriter.nameParameter(0, "expandedObj");

			for (TypeParameter typeParameter : typeParameters) {
				methodWriter.nameParameter(0, "typeOf" + typeParameter.name);
				methodWriter.nameVariable(javaModule.getTypeParameterInfo(typeParameter).parameterIndex, "typeOf" + typeParameter.name, methodStart, methodEnd, Type.getType(Class.class));
			}
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
		final boolean isStatic = member.isStatic();
		final TypeID returnType = member.getType();
		final String descriptor;
		final String signature;

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);
		{

			final String descMiddle, signatureMiddle, signatureStart;
			if (typeParameters.isEmpty()) {
				descMiddle = signatureMiddle = signatureStart = "";
			} else {
				final StringBuilder descMiddleBuilder = new StringBuilder();
				final StringBuilder signatureMiddleBuilder = new StringBuilder();
				final StringBuilder signatureStartBuilder = new StringBuilder("<");

				for (TypeParameter typeParameter : typeParameters) {
					descMiddleBuilder.append("Ljava/lang/Class;");
					signatureMiddleBuilder.append("Ljava/lang/Class<T").append(typeParameter.name).append(";>;");
					signatureStartBuilder.append(typeParameter.name).append(":Ljava/lang/Object;");
				}

				descMiddle = descMiddleBuilder.toString();
				signatureMiddle = signatureMiddleBuilder.toString();
				signatureStart = signatureStartBuilder.append(">").toString();
			}


			if (isStatic) {
				descriptor = "(" + descMiddle + ")" + context.getDescriptor(returnType);
				signature = signatureStart + "(" + signatureMiddle + ")" + context.getSignature(returnType);
			} else {
				descriptor = "(" + context.getDescriptor(expandedClass) + descMiddle + ")" + context.getDescriptor(returnType);
				signature = signatureStart + "(" + context.getSignature(expandedClass) + signatureMiddle + ")" + context
						.getSignature(returnType);
			}
		}

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaMethod method = context.getJavaMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, this.writer, true, method, definition, true, signature, descriptor, new String[0]);

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
			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
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


		final String signature = context.getMethodSignatureExpansion(member.getHeader(), expandedClass);
		final String description = context.getMethodDescriptorExpansion(member.getHeader(), expandedClass);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaMethod javaMethod = context.getJavaMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, true, javaMethod, member.definition, true, signature, description, new String[0]);


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

		final JavaStatementVisitor javaStatementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
		javaStatementVisitor.start();
		member.body.accept(javaStatementVisitor);
		javaStatementVisitor.end();
		methodWriter.label(methodEnd);

		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		final JavaMethod javaMethod = context.getJavaMethod(member);
		final MethodMember methodMember = new MethodMember(member.position, member.definition, member.getEffectiveModifiers(), javaMethod.name, member.header, member.builtin);
		methodMember.body = member.body;
		methodMember.annotations = member.annotations;
		javaModule.setMethodInfo(methodMember, javaMethod);

		return methodMember.accept(this);
	}

	@Override
	public Void visitCaster(CasterMember member) {

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>();
		expandedClass.extractTypeParameters(typeParameters);

		CompilerUtils.tagMethodParameters(context, javaModule, member.getHeader(), false, typeParameters);
		member.toType.extractTypeParameters(typeParameters);

		final String methodSignature = context.getMethodSignatureExpansion(member.getHeader(), expandedClass);
		final String methodDescriptor = context.getMethodDescriptorExpansion(member.getHeader(), expandedClass);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaMethod javaMethod = context.getJavaMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, true, javaMethod, member.definition, true, methodSignature, methodDescriptor, new String[0]);

		methodWriter.label(methodStart);
		methodWriter.nameVariable(0, "expandedObj", methodStart, methodEnd, context.getType(this.expandedClass));
		methodWriter.nameParameter(0, "expandedObj");

		int i = 1;
		for (TypeParameter typeParameter : typeParameters) {
			final String name = "typeOf" + typeParameter.name;
			methodWriter.nameVariable(i, name, methodStart, methodEnd, Type.getType(Class.class));
			methodWriter.nameParameter(0, name);
		}

		final JavaStatementVisitor javaStatementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
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
	public Void visitCaller(CallerMember member) {
		//It's gonna be a method anyways, so why not reuse the code ^^
		final JavaMethod javaMethod = context.getJavaMethod(member);
		final MethodMember call = new MethodMember(member.position, member.definition, member.getEffectiveModifiers(), javaMethod.name, member.header, member.builtin);
		call.body = member.body;
		call.annotations = member.annotations;

		javaModule.setMethodInfo(call, javaMethod);
		return call.accept(this);
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
