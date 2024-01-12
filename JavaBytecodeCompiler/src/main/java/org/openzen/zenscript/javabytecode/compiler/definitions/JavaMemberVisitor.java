package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.NativeTag;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.*;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.util.*;

public class JavaMemberVisitor implements MemberVisitor<Void> {
	private final ClassWriter writer;
	private final JavaBytecodeContext context;
	private final JavaCompilingClass class_;
	private final HighLevelDefinition definition;
	private final JavaStatementVisitor clinitStatementVisitor;
	private final JavaCompiledModule javaModule;
	private EnumDefinition enumDefinition = null;

	public JavaMemberVisitor(
			JavaBytecodeContext context,
			ClassWriter writer,
			JavaCompilingClass class_,
			HighLevelDefinition definition
	) {
		this.writer = writer;
		this.class_ = class_;
		this.definition = definition;
		this.context = context;
		javaModule = context.getJavaModule(definition.module);

		JavaNativeMethod clinitMethod = new JavaNativeMethod(class_.compiled, JavaNativeMethod.Kind.STATICINIT, "<clinit>", true, "()V", Opcodes.ACC_STATIC, false);
		final JavaCompilingMethod clinitMethodCompiling = new JavaCompilingMethod(class_.compiled, clinitMethod, "()V");
		final JavaWriter javaWriter = new JavaWriter(context.logger, definition.position, writer, clinitMethodCompiling, definition);
		this.clinitStatementVisitor = new JavaStatementVisitor(context, javaModule, javaWriter);
		this.clinitStatementVisitor.start();
		CompilerUtils.writeDefaultFieldInitializers(context, javaWriter, definition, true);

		if (definition instanceof EnumDefinition) {
			this.enumDefinition = (EnumDefinition) definition;
		}
	}

	@Override
	public Void visitField(FieldMember member) {
		JavaNativeField field = class_.getField(member);
		writer.visitField(CompilerUtils.calcAccess(member.getEffectiveModifiers()), field.name, field.descriptor, field.signature, null).visitEnd();

		if (member.hasAutoGetter()) {
			member.autoGetter.accept(this);
		}

		if(member.hasAutoSetter()) {
			member.autoSetter.accept(this);
		}
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		final boolean isEnum = definition instanceof EnumDefinition;
		final JavaCompilingMethod method = class_.getMethod(member);

		final Label constructorStart = new Label();
		final Label constructorEnd = new Label();
		final JavaWriter constructorWriter = new JavaWriter(context.logger, member.position, writer, method, definition);
		constructorWriter.label(constructorStart);

		if(method.compiled.kind == JavaNativeMethod.Kind.STATIC) {
			CompilerUtils.tagMethodParameters(context, javaModule, member.header, true, Arrays.asList(this.definition.typeParameters));
		} else {
			CompilerUtils.tagConstructorParameters(context, javaModule, member.definition, member.header, isEnum);
		}
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

		forwardToSuperConstructorIfNecessary(member, isEnum, constructorWriter, method);
		initializeFieldsInConstructor(member, method, constructorWriter, statementVisitor);

		if (member.body != null) {
			member.body.accept(statementVisitor);
		}

		constructorWriter.label(constructorEnd);
		statementVisitor.end();
		return null;
	}

	private void forwardToSuperConstructorIfNecessary(ConstructorMember member, boolean isEnum, JavaWriter constructorWriter, JavaCompilingMethod method) {
		if (member.isConstructorForwarded()) {
			return;
		}

		if (isEnum) {
			context.logger.trace("Writing enum constructor");
			constructorWriter.loadObject(0);
			constructorWriter.loadObject(1);
			constructorWriter.loadInt(2);
			constructorWriter.invokeSpecial(Type.getInternalName(Enum.class), "<init>", "(Ljava/lang/String;I)V");
		} else if (method.compiled.kind == JavaNativeMethod.Kind.STATIC) {
			context.logger.trace("Writing implicit constructor");
		} else if (definition.getSuperType() == null) {
			context.logger.trace("Writing regular constructor");
			constructorWriter.loadObject(0);
			constructorWriter.invokeSpecial(Type.getInternalName(Object.class), "<init>", "()V");
		} else {
			final TypeSymbol superType = ((DefinitionTypeID) definition.getSuperType()).definition;
			constructorWriter.loadObject(0);
			constructorWriter.invokeSpecial(context.getJavaClass(superType).internalName, "<init>", "()V");
		}
	}

	private void initializeFieldsInConstructor(ConstructorMember member, JavaCompilingMethod method, JavaWriter constructorWriter, JavaStatementVisitor statementVisitor) {
		if (method.compiled.kind == JavaNativeMethod.Kind.STATIC) {
			// used by implicit constructors; these will forward to the actual constructor instead
			// of initializing fields
			return;
		}

		for (IDefinitionMember membersOfSameType : member.definition.members) {
			if (membersOfSameType instanceof FieldMember) {
				final FieldMember fieldMember = ((FieldMember) membersOfSameType);
				if (fieldMember.isStatic()) {
					continue;
				}
				final Expression initializer = fieldMember.initializer;
				if (initializer != null) {
					constructorWriter.loadObject(0);
					initializer.accept(statementVisitor.expressionVisitor);
					constructorWriter.putField(class_.getField(fieldMember));
				}
			}
		}

		for (TypeParameter typeParameter : definition.typeParameters) {
			final JavaTypeParameterInfo typeParameterInfo = javaModule.getTypeParameterInfo(typeParameter);
			final JavaNativeField field = typeParameterInfo.field;

			//Init from Constructor
			final int parameterIndex = typeParameterInfo.parameterIndex;
			constructorWriter.loadObject(0);
			constructorWriter.loadObject(parameterIndex);
			constructorWriter.putField(field);
		}
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visitFunction(member);
		return null;
	}

	private void visitFunction(FunctionalMember member) {
		CompilerUtils.tagMethodParameters(context, javaModule, member.header, member.isStatic(), Collections.emptyList());

		final boolean isAbstract = member.body == null || member.getEffectiveModifiers().isAbstract();
		final JavaCompilingMethod method = class_.getMethod(member);

		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, method, definition);

		if (!isAbstract) {
			if (method.compiled.isAbstract() || method.compiled.cls.kind == JavaClass.Kind.INTERFACE)
				throw new IllegalArgumentException();

			final Label methodStart = new Label();
			final Label methodEnd = new Label();

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);

			statementVisitor.start();
			member.body.accept(statementVisitor);
			methodWriter.label(methodEnd);
			statementVisitor.end();
		}
	}

	@Override
	public Void visitGetter(GetterMember member) {
		if (member.hasTag(NativeTag.class)) {
			return null;
		}

		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaCompilingMethod method = class_.getMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, this.writer, method, definition);

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
		final Label methodStart = new Label();
		final Label methodEnd = new Label();

		final JavaCompilingMethod javaMethod = class_.getMethod(member);
		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, javaMethod, member.definition);
		methodWriter.label(methodStart);

		//ToDo:
		// in scripts, you use $ but the parameter is named "value", which to choose?
		//final String name = member.parameter.name;
		final String name = "$";
		final int localIndex = member.isStatic() ? 0 : 1;
		methodWriter.nameVariable(localIndex, name, methodStart, methodEnd, context.getType(member.type));
		methodWriter.nameParameter(0, name);

		javaModule.setParameterInfo(member.parameter, new JavaParameterInfo(localIndex, context.getDescriptor(member.type)));

		final JavaStatementVisitor javaStatementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
		javaStatementVisitor.start();
		member.body.accept(javaStatementVisitor);
		javaStatementVisitor.end();
		methodWriter.label(methodEnd);

		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		if (member.operator == OperatorType.DESTRUCTOR) {
			int modifiers = Opcodes.ACC_PUBLIC;
			if (member.body == null)
				modifiers |= Opcodes.ACC_ABSTRACT;

			final JavaCompilingMethod method = new JavaCompilingMethod(class_.compiled, JavaNativeMethod.getVirtual(class_.compiled, "close", "()V", modifiers), "()V");
			if (member.body == null)
				return null;

			final Label constructorStart = new Label();
			final Label constructorEnd = new Label();
			final JavaWriter destructorWriter = new JavaWriter(context.logger, member.position, writer, method, definition);
			destructorWriter.label(constructorStart);

			final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, javaModule, destructorWriter);
			statementVisitor.start();

			// TODO: destruction of members (to be done when memory tags are implemented)
			member.body.accept(statementVisitor);
			destructorWriter.label(constructorEnd);
			statementVisitor.end();
			return null;
		} else {
			visitFunction(member);
			return null;
		}
	}

	@Override
	public Void visitCaster(CasterMember member) {
		final JavaCompilingMethod javaMethod = class_.getMethod(member);
		if (javaMethod == null || !javaMethod.compile) {
			return null;
		}

		final ArrayList<TypeParameter> typeParameters = new ArrayList<>(Arrays.asList(this.definition.typeParameters));

		CompilerUtils.tagMethodParameters(context, javaModule, member.getHeader(), false, typeParameters);
		member.toType.extractTypeParameters(typeParameters);

		final Label methodStart = new Label();
		final Label methodEnd = new Label();


		final JavaWriter methodWriter = new JavaWriter(context.logger, member.position, writer, javaMethod, member.definition);

		methodWriter.label(methodStart);

		int i = 1;
		for (TypeParameter typeParameter : typeParameters) {
			final String name = "typeOf" + typeParameter.name;
			methodWriter.nameVariable(i, name, methodStart, methodEnd, Type.getType(Class.class));
			methodWriter.nameParameter(0, name);
		}

		if(member.body != null) {
			final JavaStatementVisitor javaStatementVisitor = new JavaStatementVisitor(context, javaModule, methodWriter);
			javaStatementVisitor.start();
			member.body.accept(javaStatementVisitor);
			javaStatementVisitor.end();
		}
		methodWriter.label(methodEnd);
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		JavaImplementation implementation = context.getJavaImplementation(member);
		if (implementation.inline) {
			for (IDefinitionMember imember : member.members)
				imember.accept(this);
		} else {
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

				JavaNativeMethod compiled = Optional.ofNullable(class_.getMethod(constant.constructor.member.method))
								.map(method -> method.compiled)
								.orElseThrow(() -> new IllegalStateException("Cannot find constructor for enum: " + definition.name));

				clinitWriter.invokeSpecial(compiled);
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
