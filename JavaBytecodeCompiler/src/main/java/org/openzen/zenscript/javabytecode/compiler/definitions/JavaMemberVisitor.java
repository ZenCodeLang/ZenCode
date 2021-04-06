package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.CompilerUtils;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javashared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaMemberVisitor implements MemberVisitor<Void> {
	private final ClassWriter writer;
	private final JavaBytecodeContext context;
	private final JavaClass toClass;
	private final HighLevelDefinition definition;
	private final JavaStatementVisitor clinitStatementVisitor;
	private final JavaCompiledModule javaModule;
	private EnumDefinition enumDefinition = null;

	public JavaMemberVisitor(JavaBytecodeContext context, ClassWriter writer, JavaClass toClass, HighLevelDefinition definition) {
		this.writer = writer;
		this.toClass = toClass;
		this.definition = definition;
		this.context = context;
		javaModule = context.getJavaModule(definition.module);

		final JavaWriter javaWriter = new JavaWriter(context.logger, definition.position, writer, new JavaMethod(toClass, JavaMethod.Kind.STATICINIT, "<clinit>", true, "()V", Opcodes.ACC_STATIC, false), definition, null, null);
		this.clinitStatementVisitor = new JavaStatementVisitor(context, javaModule, javaWriter);
		this.clinitStatementVisitor.start();
		CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, definition, true);

		if (definition instanceof EnumDefinition) {
			this.enumDefinition = (EnumDefinition) definition;
		}
	}

	@Override
	public Void visitConst(ConstMember member) {
		JavaField field = context.getJavaField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();
		return null;
	}

	@Override
	public Void visitField(FieldMember member) {
		JavaField field = context.getJavaField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		final boolean isEnum = definition instanceof EnumDefinition;
		final JavaMethod method = context.getJavaMethod(member);

		final Label constructorStart = new Label();
		final Label constructorEnd = new Label();
		final JavaWriter constructorWriter = new JavaWriter(context.logger, member.position, writer, method, definition, context.getMethodSignature(member.header), null);
		constructorWriter.label(constructorStart);
		CompilerUtils.tagConstructorParameters(context, javaModule, member.definition, member.header, isEnum);
		if (isEnum) {
			constructorWriter.nameParameter(0, "name");
			constructorWriter.nameParameter(0, "index");
		}

		for (TypeParameter typeParameter : definition.typeParameters) {
			constructorWriter.nameParameter(0, "typeof" + typeParameter.name);
			constructorWriter.nameVariable(
					javaModule.getTypeParameterInfo(typeParameter).parameterIndex,
					"typeOf" + typeParameter.name,
					constructorStart,
					constructorEnd,
					Type.getType(Class.class)
			);
		}

		for (FunctionParameter parameter : member.header.parameters) {
			constructorWriter.nameVariable(
					javaModule.getParameterInfo(parameter).index,
					parameter.name,
					constructorStart,
					constructorEnd,
					context.getType(parameter.type));
		}

		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, constructorWriter);
		statementVisitor.start();

		if (!member.isConstructorForwarded()) {
			if (isEnum) {
				context.logger.trace("Writing enum constructor");
				constructorWriter.loadObject(0);
				constructorWriter.loadObject(1);
				constructorWriter.loadInt(2);
				constructorWriter.invokeSpecial(Type.getInternalName(Enum.class), "<init>", "(Ljava/lang/String;I)V");
			} else if (definition.getSuperType() == null) {
				context.logger.trace("Writing regular constructor");
				constructorWriter.loadObject(0);
				constructorWriter.invokeSpecial(Type.getInternalName(Object.class), "<init>", "()V");
			} else if (member.builtin == BuiltinID.CLASS_DEFAULT_CONSTRUCTOR) { //Inherited classes needs to call super()
				final HighLevelDefinition superType = ((DefinitionTypeID) definition.getSuperType()).definition;
				constructorWriter.loadObject(0);
				constructorWriter.invokeSpecial(context.getJavaClass(superType).internalName, "<init>", "()V");
			}
		}

		for (IDefinitionMember membersOfSameType : member.definition.members) {
			if (membersOfSameType instanceof FieldMember) {
				final FieldMember fieldMember = ((FieldMember) membersOfSameType);
				if(fieldMember.isStatic()){
					continue;
				}
				final Expression initializer = fieldMember.initializer;
				if (initializer != null) {
					constructorWriter.loadObject(0);
					initializer.accept(statementVisitor.expressionVisitor);
					constructorWriter.putField(context.getJavaField(fieldMember));
				}
			}
		}

		for (TypeParameter typeParameter : definition.typeParameters) {
			final JavaTypeParameterInfo typeParameterInfo = javaModule.getTypeParameterInfo(typeParameter);
			final JavaField field = typeParameterInfo.field;

			//Init from Constructor
			final int parameterIndex = typeParameterInfo.parameterIndex;
			constructorWriter.loadObject(0);
			constructorWriter.loadObject(parameterIndex);
			constructorWriter.putField(field);
		}

		if (member.body != null) {
			member.body.accept(statementVisitor);
		}

		constructorWriter.label(constructorEnd);
		statementVisitor.end();
		return null;
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		int modifiers = Opcodes.ACC_PUBLIC;
		if (member.body == null)
			modifiers |= Opcodes.ACC_ABSTRACT;

		final JavaMethod method = JavaMethod.getVirtual(toClass, "close", "()V", modifiers);
		if (member.body == null)
			return null;

		final Label constructorStart = new Label();
		final Label constructorEnd = new Label();
		final JavaWriter destructorWriter = new JavaWriter(context.logger, member.position, writer, method, definition, null, null);
		destructorWriter.label(constructorStart);

		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, destructorWriter);
		statementVisitor.start();
		// TODO: destruction of members (to be done when memory tags are implemented)
		member.body.accept(statementVisitor);
		destructorWriter.label(constructorEnd);
		statementVisitor.end();
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		CompilerUtils.tagMethodParameters(context, javaModule, member.header, member.isStatic(), Collections.emptyList());

		final boolean isAbstract = member.body == null || Modifiers.isAbstract(member.getEffectiveModifiers());
		final JavaMethod method = context.getJavaMethod(member);

		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, method, definition, context.getMethodSignature(member.header), null);

		if (!isAbstract) {
			if (method.isAbstract() || method.cls.kind == JavaClass.Kind.INTERFACE)
				throw new IllegalArgumentException();

			final Label methodStart = new Label();
			final Label methodEnd = new Label();

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
		if (member.hasTag(NativeTag.class)) {
			return null;
		}

		final String descriptor = context.getMethodDescriptor(member.getHeader());
		final String signature = context.getMethodSignature(member.getHeader(), true);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaMethod method = context.getJavaMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, this.writer, true, method, definition, false, signature, descriptor, new String[0]);

		methodWriter.label(methodStart);
		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
		statementVisitor.start();
		member.body.accept(statementVisitor);
		methodWriter.label(methodEnd);
		statementVisitor.end();

		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		final String signature = context.getMethodSignature(member.getHeader());
		final String description = context.getMethodDescriptor(member.getHeader());

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaMethod javaMethod = context.getJavaMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, true, javaMethod, member.definition, false, signature, description, new String[0]);
		methodWriter.label(methodStart);

		//in script you use $ but the parameter is named "value", which to choose?
		//final String name = member.parameter.name;
		final String name = "$";
		methodWriter.nameVariable(1, name, methodStart, methodEnd, context.getType(member.getType()));
		methodWriter.nameParameter(0, name);

		javaModule.setParameterInfo(member.parameter, new JavaParameterInfo(1, context.getDescriptor(member.getType())));

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
		final JavaMethod javaMethod = context.getJavaMethod(member);
		if (javaMethod == null || !javaMethod.compile) {
			return null;
		}

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>(Arrays.asList(this.definition.typeParameters));

		CompilerUtils.tagMethodParameters(context, javaModule, member.getHeader(), false, typeParameters);
		member.toType.extractTypeParameters(typeParameters);

		final String methodSignature = context.getMethodSignature(member.getHeader());
		final String methodDescriptor = context.getMethodDescriptor(member.getHeader());

		final Label methodStart = new Label();
		final Label methodEnd = new Label();


		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, true, javaMethod, member.definition, false, methodSignature, methodDescriptor, new String[0]);

		methodWriter.label(methodStart);

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
		JavaImplementation implementation = context.getJavaImplementation(member);
		if (implementation.inline) {
			for (IDefinitionMember imember : member.members)
				imember.accept(this);
		} else {
			//TODO: Fixme???
			// What should I do if a native class has interfaces to be visited?
			if (javaModule.getNativeClassInfo(member.definition) != null) {
				return null;
			}


			throw new UnsupportedOperationException("Non-inline interface implementations not yet available");
		}
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

	public void end() {

		if (enumDefinition != null) {
			JavaClass toClass = context.getJavaClass(enumDefinition);

			for (EnumConstantMember constant : enumDefinition.enumConstants) {
				writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM, constant.name, "L" + toClass.internalName + ";", null, null).visitEnd();
				final String internalName = context.getInternalName(constant.constructor.type);
				final JavaWriter clinitWriter = clinitStatementVisitor.getJavaWriter();
				clinitWriter.newObject(internalName);
				clinitWriter.dup();
				clinitWriter.constant(constant.name);
				clinitWriter.constant(constant.ordinal);
				for (Expression argument : constant.constructor.arguments.arguments) {
					argument.accept(clinitStatementVisitor.expressionVisitor);
				}

				clinitWriter.invokeSpecial(internalName, "<init>", context.getEnumConstructorDescriptor(constant.constructor.constructor.getHeader()));
				clinitWriter.putStaticField(internalName, constant.name, "L" + internalName + ";");
			}

			final JavaWriter clinitWriter = clinitStatementVisitor.getJavaWriter();
			final List<EnumConstantMember> enumConstants = enumDefinition.enumConstants;
			clinitWriter.constant(enumConstants.size());
			clinitWriter.newArray(Type.getType("L" + toClass.internalName + ";"));

			for (EnumConstantMember enumConstant : enumConstants) {
				clinitWriter.dup();
				clinitWriter.constant(enumConstant.ordinal);
				clinitWriter.getStaticField(toClass.internalName, enumConstant.name, "L" + toClass.internalName + ";");
				clinitWriter.arrayStore(Type.getType("L" + toClass.internalName + ";"));
			}
			clinitWriter.putStaticField(toClass.internalName, "$VALUES", "[L" + toClass.internalName + ";");
		}


		clinitStatementVisitor.end();
	}
}
