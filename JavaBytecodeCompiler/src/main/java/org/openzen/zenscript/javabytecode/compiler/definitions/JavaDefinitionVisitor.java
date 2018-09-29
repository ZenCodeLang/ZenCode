package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.openzen.zenscript.javashared.JavaTypeGenericVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericTypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.compiler.*;
import org.openzen.zenscript.javashared.JavaClass;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.javashared.JavaMethod;
import org.openzen.zenscript.javashared.JavaModifiers;
import org.openzen.zenscript.javashared.JavaVariantOption;


public class JavaDefinitionVisitor implements DefinitionVisitor<byte[]> {
	private static final JavaMethod CLASS_FORNAME
			= JavaMethod.getNativeStatic(JavaClass.CLASS, "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
	private static final JavaMethod ENUM_VALUEOF
			= JavaMethod.getNativeStatic(JavaClass.CLASS, "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");
	private static final JavaMethod ARRAY_CLONE
			= JavaMethod.getNativeVirtual(JavaClass.ARRAYS, "clone", "()Ljava/lang/Object;");

	private final JavaClassWriter outerWriter;
	private final JavaBytecodeContext context;
	final JavaTypeGenericVisitor javaTypeGenericVisitor;

    public JavaDefinitionVisitor(JavaBytecodeContext context, JavaClassWriter outerWriter) {
		this.context = context;
		this.outerWriter = outerWriter;
	    this.javaTypeGenericVisitor = new JavaTypeGenericVisitor(context);
	}

	@Override
	public byte[] visitClass(ClassDefinition definition) {
        final String superTypeInternalName = definition.getSuperType() == null ? "java/lang/Object" : context.getInternalName(definition.getSuperType());

		JavaClass toClass = definition.getTag(JavaClass.class);
		JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

        //TODO: Calculate signature from generic parameters
		List<String> interfaces = new ArrayList<>();
		for (IDefinitionMember member : definition.members) {
			if (member instanceof ImplementationMember)
				interfaces.add(context.getInternalName(((ImplementationMember) member).type));
		}
        String signature = null;

        writer.visit(Opcodes.V1_8, definition.modifiers, toClass.internalName, signature, superTypeInternalName, interfaces.toArray(new String[interfaces.size()]));
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(context, writer, toClass, definition);
        for (IDefinitionMember member : definition.members) {
            member.accept(memberVisitor);
        }
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitInterface(InterfaceDefinition definition) {
		JavaClass toClass = definition.getTag(JavaClass.class);
		ClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		String signature = null;
		String[] baseInterfaces = new String[definition.baseInterfaces.size()];
		for (int i = 0; i < baseInterfaces.length; i++)
			baseInterfaces[i] = context.getInternalName(definition.baseInterfaces.get(i));

		writer.visit(Opcodes.V1_8, definition.modifiers | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, toClass.internalName, signature, "java/lang/Object", baseInterfaces);
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(context, writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitEnum(EnumDefinition definition) {
		System.out.println("Compiling enum " + definition.name + " in " + definition.position.getFilename());

		String superTypeInternalName = definition.getSuperType() == null ? "java/lang/Object" : context.getInternalName(definition.getSuperType());

		ClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		JavaClass toClass = definition.getTag(JavaClass.class);
		writer.visit(Opcodes.V1_8, Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL, toClass.internalName, "Ljava/lang/Enum<L" + toClass.internalName + ";>;", superTypeInternalName, null);

		//Enum Stuff(required!)
		writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, "$VALUES", "[L" + toClass.internalName + ";", null, null).visitEnd();

        final JavaMemberVisitor visitor = new JavaMemberVisitor(context, writer, toClass, definition);
        for (IDefinitionMember member : definition.members) {
            member.accept(visitor);
        }

		JavaMethod valuesMethod = JavaMethod.getStatic(toClass, "values", "()[L" + toClass.internalName + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valuesWriter = new JavaWriter(writer, true, valuesMethod, definition, null, null);
		valuesWriter.start();
		valuesWriter.getStaticField(toClass.internalName, "$VALUES", "[L" + toClass.internalName + ";");
		valuesWriter.invokeVirtual(ARRAY_CLONE);
		valuesWriter.checkCast("[L" + toClass.internalName + ";");
		valuesWriter.returnObject();
		valuesWriter.end();

		JavaMethod valueOfMethod = JavaMethod.getStatic(toClass, "valueOf", "(Ljava/lang/String;)L" + toClass.internalName + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valueOfWriter = new JavaWriter(writer, true, valueOfMethod, definition, null, null);
		valueOfWriter.start();
		valueOfWriter.invokeStatic(CLASS_FORNAME);
		valueOfWriter.loadObject(0);
		valueOfWriter.invokeStatic(ENUM_VALUEOF);
		valueOfWriter.checkCast(toClass.internalName);
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
		CompilerUtils.tagMethodParameters(context, definition.header, true);

        final String signature = context.getMethodSignature(definition.header);

		final JavaMethod method = definition.caller.getTag(JavaMethod.class);

		final JavaWriter writer = new JavaWriter(outerWriter, true, method, definition, signature, null);
        final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(context, writer);
        statementVisitor.start();
		boolean returns = definition.caller.body.accept(statementVisitor);
		if (!returns) {
			StoredType type = definition.header.getReturnType();
			if (CompilerUtils.isPrimitive(type.type))
				writer.iConst0();
			else if (type.type != BasicTypeID.VOID)
				writer.aConstNull();
			writer.returnType(context.getType(type));
		}

		statementVisitor.end();
		return null;
	}

	@Override
	public byte[] visitExpansion(ExpansionDefinition definition) {
		return null;
	}

	@Override
	public byte[] visitAlias(AliasDefinition definition) {
		throw new AssertionError("Aliases shouldn't exist here...");
	}

	@Override
	public byte[] visitVariant(VariantDefinition variant) {
		final JavaClass toClass = variant.getTag(JavaClass.class);
		final JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		final String variantName = variant.name;


		final String ss = "<" + javaTypeGenericVisitor.getGenericSignature(variant.typeParameters) + ">Ljava/lang/Object;";
		JavaClassWriter.registerSuperClass(variantName, "java/lang/Object");

		writer.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, toClass.internalName, ss, "java/lang/Object", null);
		writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "getDenominator", "()I", null, null).visitEnd();

		final JavaMemberVisitor visitor = new JavaMemberVisitor(context, writer, toClass, variant);

		final List<VariantDefinition.Option> options = variant.options;
		//Each option is one of the possible child classes
		for (final VariantDefinition.Option option : options) {
			JavaVariantOption optionTag = option.getTag(JavaVariantOption.class);
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
				for (final StoredType type : option.types) {
					builder.append(javaTypeGenericVisitor.getSignatureWithBound(type.type));
				}
				builder.append(">");
				builder.append("L").append(toClass.internalName).append("<");

				for (final TypeParameter genericParameter : variant.typeParameters) {
					boolean t = true;
					for (final StoredType type : option.types)
						if (type.type instanceof GenericTypeID) {
							final GenericTypeID genericTypeID = (GenericTypeID) type.type;
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
			final JavaMemberVisitor optionVisitor = new JavaMemberVisitor(context, optionWriter, optionTag.variantOptionClass, variant);
			final StringBuilder optionInitDescBuilder = new StringBuilder("(");
			final StringBuilder optionInitSignatureBuilder = new StringBuilder("(");

			StoredType[] types = option.types;
			for (int i = 0; i < types.length; ++i) {
				final String descriptor = context.getDescriptor(types[i]);
				optionInitDescBuilder.append(descriptor);
				optionInitSignatureBuilder.append("T").append(((GenericTypeID) types[i].type).parameter.name).append(";");
				optionWriter.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "field" + i, descriptor, "T" + ((GenericTypeID) types[i].type).parameter.name + ";", null).visitEnd();
			}
			optionInitDescBuilder.append(")V");
			optionInitSignatureBuilder.append(")V");

JavaMethod constructorMethod = JavaMethod.getConstructor(optionTag.variantOptionClass, optionInitDescBuilder.toString(), JavaModifiers.PUBLIC);
			final JavaWriter initWriter = new JavaWriter(optionWriter, constructorMethod, variant, optionInitSignatureBuilder.toString(), null);
			initWriter.start();
			initWriter.loadObject(0);
			initWriter.dup();
			initWriter.invokeSpecial(toClass.internalName, "<init>", "()V");
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
			JavaMethod denominator = JavaMethod.getVirtual(optionTag.variantOptionClass, "getDenominator", "()I", JavaModifiers.PUBLIC);
			final JavaWriter getDenominator = new JavaWriter(optionWriter, denominator, null, null, null, "java/lang/Override");
			getDenominator.start();
			getDenominator.constant(option.ordinal);
			getDenominator.returnInt();
			getDenominator.end();

			optionVisitor.end();
			optionWriter.visitEnd();
			final byte[] byteArray = optionWriter.toByteArray();
			context.register(optionTag.variantOptionClass.internalName, byteArray);

			//Print the option files, won't be in production
			try (FileOutputStream out = new FileOutputStream(optionTag.variantOptionClass.internalName.replace('/', '_') + ".class")) {
				out.write(byteArray);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		for (final IDefinitionMember member : variant.members) {
			member.accept(visitor);
		}

		final JavaWriter superInitWriter = new JavaWriter(writer, JavaMethod.getConstructor(toClass, "()V", Opcodes.ACC_PUBLIC), variant, "()V", null);
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
