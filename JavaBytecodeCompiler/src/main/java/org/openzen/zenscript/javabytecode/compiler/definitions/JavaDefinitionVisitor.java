package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.*;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingClass;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;
import org.openzen.zenscript.javashared.compiling.JavaCompilingModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class JavaDefinitionVisitor implements DefinitionVisitor<byte[]> {
	final JavaTypeGenericVisitor javaTypeGenericVisitor;
	private final JavaNativeMethod ENUM_VALUEOF
			= JavaNativeMethod.getNativeStatic(JavaClass.CLASS, "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");
	private final JavaClassWriter outerWriter;
	private final JavaBytecodeContext context;
	private final JavaCompilingModule module;

	public JavaDefinitionVisitor(JavaBytecodeContext context, JavaCompilingModule module, JavaClassWriter outerWriter) {
		this.context = context;
		this.module = module;
		this.outerWriter = outerWriter;
		this.javaTypeGenericVisitor = new JavaTypeGenericVisitor(context);
	}

	@Override
	public byte[] visitClass(ClassDefinition definition) {
		final String superTypeInternalName = definition.getSuperType() == null ? "java/lang/Object" : context.getInternalName(definition.getSuperType());

		JavaCompilingClass compilingClass = module.getClass(definition);
		JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		List<String> interfaces = new ArrayList<>();
		for (IDefinitionMember member : definition.members) {
			if (member instanceof ImplementationMember)
				interfaces.add(context.getInternalName(((ImplementationMember) member).type));
		}
		String signature;

		{
			final StringBuilder signatureBuilder = new StringBuilder();
			if (definition.typeParameters.length != 0) {
				signatureBuilder.append("<");
				for (TypeParameter typeParameter : definition.typeParameters) {
					signatureBuilder.append(typeParameter.name);
					signatureBuilder.append(":");
					signatureBuilder.append("Ljava/lang/Object;");
				}
				signatureBuilder.append(">");
			}

			signatureBuilder.append("L").append(superTypeInternalName).append(";");
			for (IDefinitionMember member : definition.members) {
				if (member instanceof ImplementationMember) {
					signatureBuilder.append(context.getInternalName(((ImplementationMember) member).type));
				}
			}

			signature = signatureBuilder.toString();
		}

		writer.visit(Opcodes.V1_8, JavaModifiers.getJavaModifiers(definition.modifiers), compilingClass.compiled.internalName, signature, superTypeInternalName, interfaces.toArray(new String[0]));
		for (TypeParameter typeParameter : definition.typeParameters) {
			//Add it to the class
			writer.visitField(
					Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
					"typeOf" + typeParameter.name,
					"Ljava/lang/Class;",
					"Ljava/lang/Class<T" + typeParameter.name + ";>;",
					//"Ljava/lang/Class;",
					null
			);
		}

		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(context, writer, compilingClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitInterface(InterfaceDefinition definition) {
		JavaCompilingClass compilingClass = module.getClass(definition);
		ClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		String signature = null;
		String[] baseInterfaces = new String[definition.baseInterfaces.size()];
		for (int i = 0; i < baseInterfaces.length; i++)
			baseInterfaces[i] = context.getInternalName(definition.baseInterfaces.get(i));

		writer.visit(Opcodes.V1_8, JavaModifiers.getJavaModifiers(definition.modifiers) | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, compilingClass.compiled.internalName, signature, "java/lang/Object", baseInterfaces);
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(context, writer, compilingClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitEnum(EnumDefinition definition) {
		context.logger.trace("Compiling enum " + definition.name + " in " + definition.position.getFilename());

		String superTypeInternalName = definition.getSuperType() == null ? "java/lang/Enum" : context.getInternalName(definition.getSuperType());

		ClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		JavaCompilingClass class_ = module.getClass(definition);
		writer.visit(Opcodes.V1_8, Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL, class_.getInternalName(), null, superTypeInternalName, null);

		//Enum Stuff(required!)
		writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, "$VALUES", "[L" + class_.getInternalName() + ";", null, null).visitEnd();

		final JavaMemberVisitor visitor = new JavaMemberVisitor(context, writer, class_, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(visitor);
		}
		visitor.end();

		JavaNativeMethod valuesMethod = JavaNativeMethod.getStatic(class_.compiled, "values", "()[L" + class_.getInternalName() + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valuesWriter = new JavaWriter(context.logger, definition.position, writer, true, new JavaCompilingMethod(class_.compiled, valuesMethod), definition, null, null);
		valuesWriter.start();
		valuesWriter.getStaticField(class_.getInternalName(), "$VALUES", "[L" + class_.getInternalName() + ";");

		final JavaNativeMethod arrayClone = JavaNativeMethod.getNativeVirtual(JavaClass.fromInternalName("[L" + class_.getInternalName() + ";", JavaClass.Kind.ARRAY), "clone", "()Ljava/lang/Object;");
		valuesWriter.invokeVirtual(arrayClone);
		valuesWriter.checkCast("[L" + class_.getInternalName() + ";");
		valuesWriter.returnObject();
		valuesWriter.end();

		JavaNativeMethod valueOfMethod = JavaNativeMethod.getStatic(class_.compiled, "valueOf", "(Ljava/lang/String;)L" + class_.getInternalName() + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valueOfWriter = new JavaWriter(context.logger, definition.position, writer, true, new JavaCompilingMethod(class_.compiled, valueOfMethod), definition, null, null);
		valueOfWriter.start();
		valueOfWriter.constantClass(class_.compiled);
		valueOfWriter.loadObject(0);
		valueOfWriter.invokeStatic(ENUM_VALUEOF);
		valueOfWriter.checkCast(class_.getInternalName());
		valueOfWriter.returnObject();
		valueOfWriter.end();

		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitStruct(StructDefinition definition) {
		return null;
	}

	@Override
	public byte[] visitFunction(FunctionDefinition definition) {
		CompilerUtils.tagMethodParameters(context, context.getJavaModule(definition.module), definition.header, true, Collections
				.emptyList());

		final String signature = context.getMethodSignature(definition.header);
		JavaCompilingClass class_ = module.getClass(definition);
		JavaCompilingMethod method = class_.getMethod(definition.caller);

		final JavaWriter writer = new JavaWriter(context.logger, definition.position, outerWriter, true, method, definition, signature, null);
		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, context.getJavaModule(definition.module), writer);
		statementVisitor.start();
		boolean returns = definition.caller.body.accept(statementVisitor);
		if (!returns) {
			TypeID type = definition.header.getReturnType();
			if (CompilerUtils.isPrimitive(type))
				writer.iConst0();
			else if (type != BasicTypeID.VOID)
				writer.aConstNull();
			writer.returnType(context.getType(type));
		}

		statementVisitor.end();
		return null;
	}

	@Override
	public byte[] visitExpansion(ExpansionDefinition definition) {
		JavaCompilingClass class_ = module.getClass(definition);

		JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
		final JavaClass expansionClassInfo = context.getJavaModule(definition.module).getExpansionClassInfo(definition);
		final String internalName = expansionClassInfo.internalName;

		writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, internalName, null, "java/lang/Object", null);
		JavaExpansionMemberVisitor memberVisitor = new JavaExpansionMemberVisitor(context, class_, writer, definition.target, definition);

		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();


		return writer.toByteArray();
	}

	@Override
	public byte[] visitAlias(AliasDefinition definition) {
		throw new AssertionError("Aliases shouldn't exist here...");
	}

	@Override
	public byte[] visitVariant(VariantDefinition variant) {
		JavaCompilingClass class_ = module.getClass(variant);
		final JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		final String variantName = variant.name;


		final String ss = "<" + javaTypeGenericVisitor.getGenericSignature(variant.typeParameters) + ">Ljava/lang/Object;";
		JavaClassWriter.registerSuperClass(variantName, "java/lang/Object");

		writer.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, class_.getInternalName(), ss, "java/lang/Object", null);
		writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "getDenominator", "()I", null, null).visitEnd();

		final JavaMemberVisitor visitor = new JavaMemberVisitor(context, writer, class_, variant);

		final List<VariantDefinition.Option> options = variant.options;
		//Each option is one of the possible child classes
		for (final VariantDefinition.Option option : options) {
			JavaVariantOption optionTag = context.getJavaVariantOption(option);
			final JavaClassWriter optionWriter = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);
			final String optionClassName = variantName + "$" + option.name;
			JavaClassWriter.registerSuperClass(optionClassName, variantName);

			writer.visitInnerClass(optionTag.variantOptionClass.internalName, optionTag.variantClass.internalName, option.name, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL);

			//Generic option signature
			final String signature;
			{
				StringBuilder builder = new StringBuilder();
				//TODO check if this can be changed to what Stan was up to
				builder.append("<");
				for (final TypeID type : option.types) {
					builder.append(javaTypeGenericVisitor.getSignatureWithBound(type));
				}
				builder.append(">");
				builder.append("L").append(class_.getInternalName()).append("<");

				for (final TypeParameter genericParameter : variant.typeParameters) {
					boolean t = true;
					for (final TypeID type : option.types)
						if (type instanceof GenericTypeID) {
							final GenericTypeID genericTypeID = (GenericTypeID) type;
							if (genericParameter == genericTypeID.parameter) {
								builder.append("T").append(genericParameter.name).append(";");
								t = false;
							}
						}
					if (t)
						builder.append(javaTypeGenericVisitor.getGenericBounds(genericParameter.bounds));

				}


				signature = builder.append(">;").toString();
			}

			optionWriter.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, optionTag.variantOptionClass.internalName, signature, optionTag.variantClass.internalName, null);
			final StringBuilder optionInitDescBuilder = new StringBuilder("(");
			final StringBuilder optionInitSignatureBuilder = new StringBuilder("(");

			TypeID[] types = option.types;
			for (int i = 0; i < types.length; ++i) {
				final String descriptor = context.getDescriptor(types[i]);
				optionInitDescBuilder.append(descriptor);
				optionInitSignatureBuilder.append("T").append(((GenericTypeID) types[i]).parameter.name).append(";");
				optionWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "field" + i, descriptor, "T" + ((GenericTypeID) types[i]).parameter.name + ";", null).visitEnd();
			}
			optionInitDescBuilder.append(")V");
			optionInitSignatureBuilder.append(")V");

			JavaNativeMethod constructorMethod = JavaNativeMethod.getConstructor(optionTag.variantOptionClass, optionInitDescBuilder.toString(), JavaModifiers.PUBLIC);
			final JavaWriter initWriter = new JavaWriter(context.logger, option.position, optionWriter, new JavaCompilingMethod(class_.compiled, constructorMethod), variant, optionInitSignatureBuilder.toString(), null);
			initWriter.start();
			initWriter.loadObject(0);
			initWriter.dup();
			initWriter.invokeSpecial(class_.getInternalName(), "<init>", "()V");
			for (int i = 0; i < types.length; ++i) {
				initWriter.dup();
				initWriter.loadObject(i + 1);

				final String descriptor = context.getDescriptor(types[i]);
				initWriter.putField(optionTag.variantOptionClass.internalName, "field" + i, descriptor);
			}
			initWriter.pop();
			initWriter.ret();
			initWriter.end();

			//Denominator for switch-cases
			JavaNativeMethod denominator = JavaNativeMethod.getVirtual(optionTag.variantOptionClass, "getDenominator", "()I", JavaModifiers.PUBLIC);
			final JavaWriter getDenominator = new JavaWriter(context.logger, option.position, optionWriter, new JavaCompilingMethod(class_.compiled, denominator), null, null, null, "java/lang/Override");
			getDenominator.start();
			getDenominator.constant(option.ordinal);
			getDenominator.returnInt();
			getDenominator.end();

			optionWriter.visitEnd();
			final byte[] byteArray = optionWriter.toByteArray();
			context.register(optionTag.variantOptionClass.internalName, byteArray);
		}


		for (final IDefinitionMember member : variant.members) {
			member.accept(visitor);
		}

		final JavaWriter superInitWriter = new JavaWriter(
				context.logger,
				variant.position,
				writer,
				new JavaCompilingMethod(class_.compiled, JavaNativeMethod.getConstructor(class_.compiled, "()V", Opcodes.ACC_PUBLIC)),
				variant,
				"()V",
				null);
		superInitWriter.start();
		superInitWriter.loadObject(0);
		superInitWriter.invokeSpecial("java/lang/Object", "<init>", "()V");
		superInitWriter.ret();
		superInitWriter.end();

		visitor.end();
		writer.visitEnd();


		return writer.toByteArray();
	}
}
