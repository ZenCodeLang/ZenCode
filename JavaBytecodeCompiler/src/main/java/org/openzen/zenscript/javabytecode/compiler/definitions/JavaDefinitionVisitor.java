package org.openzen.zenscript.javabytecode.compiler.definitions;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaMethodInfo;
import org.openzen.zenscript.javabytecode.JavaModule;
import org.openzen.zenscript.javabytecode.compiler.*;


public class JavaDefinitionVisitor implements DefinitionVisitor<byte[]> {
	private static final JavaClassInfo T_CLASS = new JavaClassInfo("java/lang/Class");
	private static final JavaMethodInfo CLASS_FORNAME = new JavaMethodInfo(
			T_CLASS,
			"forName",
			"(Ljava/lang/String;)Ljava/lang/Class;",
			Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);

	private static final JavaClassInfo T_ENUM = new JavaClassInfo("java/lang/Enum");
	private static final JavaMethodInfo ENUM_VALUEOF = new JavaMethodInfo(
			T_ENUM,
			"valueOf",
			"(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;",
			Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);

	private final JavaClassWriter outerWriter;

	public JavaDefinitionVisitor(JavaClassWriter outerWriter) {

		this.outerWriter = outerWriter;
	}

	@Override
	public byte[] visitClass(ClassDefinition definition) {
		//Classes will always be created in a new File/Class

		final Type superType;
		if (definition.superType == null)
			superType = Type.getType(Object.class);
		else
			superType = Type.getType(definition.superType.accept(JavaTypeClassVisitor.INSTANCE));

		JavaClassInfo toClass = new JavaClassInfo(definition.name);
		JavaClassWriter writer = new JavaClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		//TODO: Interfaces?
		String signature = null;


		writer.visit(Opcodes.V1_8, definition.modifiers, definition.name, signature, superType.getInternalName(), null);
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitInterface(InterfaceDefinition definition) {
		JavaClassInfo toClass = new JavaClassInfo(definition.name);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		//TODO: Calculate signature from generic parameters
		//TODO: Extending Interfaces?
		String signature = null;
		writer.visit(Opcodes.V1_8, definition.modifiers | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT, definition.name, signature, Type.getInternalName(Object.class), null);
		JavaMemberVisitor memberVisitor = new JavaMemberVisitor(writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberVisitor);
		}
		memberVisitor.end();
		writer.visitEnd();
		return writer.toByteArray();
	}

	@Override
	public byte[] visitEnum(EnumDefinition definition) {
		System.out.println("Compiling enum " + definition.name + " in " + definition.position.filename);

		final Type superType;
		if (definition.superType == null)
			superType = Type.getType(Object.class);
		else
			superType = Type.getType(definition.superType.accept(JavaTypeClassVisitor.INSTANCE));

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

		writer.visit(Opcodes.V1_8, Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL, definition.name, "Ljava/lang/Enum<L" + definition.name + ";>;", superType.getInternalName(), null);

		JavaClassInfo toClass = new JavaClassInfo(definition.name, true);

		//Enum Stuff(required!)
		writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, "$VALUES", "[L" + definition.name + ";", null, null).visitEnd();

		final JavaMemberVisitor visitor = new JavaMemberVisitor(writer, toClass, definition);
		for (IDefinitionMember member : definition.members) {
			member.accept(visitor);
		}

		JavaClassInfo arrayClass = new JavaClassInfo("[L" + definition.name + ";");
		JavaMethodInfo arrayClone = new JavaMethodInfo(arrayClass, "clone", "()Ljava/lang/Object;", Opcodes.ACC_PUBLIC);

		JavaMethodInfo valuesMethodInfo = new JavaMethodInfo(toClass, "values", "()[L" + definition.name + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valuesWriter = new JavaWriter(writer, true, valuesMethodInfo, definition, null, null);
		valuesWriter.start();
		valuesWriter.getStaticField(definition.name, "$VALUES", "[L" + definition.name + ";");
		valuesWriter.invokeVirtual(arrayClone);
		valuesWriter.checkCast("[L" + definition.name + ";");
		valuesWriter.returnObject();
		valuesWriter.end();

		JavaMethodInfo valueOfMethodInfo = new JavaMethodInfo(toClass, "valueOf", "(Ljava/lang/String;)L" + definition.name + ";", Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC);
		JavaWriter valueOfWriter = new JavaWriter(writer, true, valueOfMethodInfo, definition, null, null);
		valueOfWriter.start();
		valueOfWriter.invokeStatic(CLASS_FORNAME);
		valueOfWriter.loadObject(0);
		valueOfWriter.invokeStatic(ENUM_VALUEOF);
		valueOfWriter.checkCast("L" + definition.name + ";");
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
		CompilerUtils.tagMethodParameters(definition.header, true);

		final String signature = CompilerUtils.calcSign(definition.header, false);

		final JavaClassInfo toClass = new JavaClassInfo(CompilerUtils.calcClasName(definition.position));
		final JavaMethodInfo methodInfo = new JavaMethodInfo(toClass, definition.name, CompilerUtils.calcDesc(definition.header, false), CompilerUtils.calcAccess(definition.modifiers) | Opcodes.ACC_STATIC);

		final JavaWriter writer = new JavaWriter(outerWriter, true, methodInfo, definition, signature, null);
		final JavaStatementVisitor statementVisitor = new JavaStatementVisitor(writer);
		statementVisitor.start();
		boolean returns = definition.statement.accept(statementVisitor);
		if (!returns) {
			ITypeID type = definition.header.returnType;
			if (CompilerUtils.isPrimitive(type))
				writer.iConst0();
			else if (type != BasicTypeID.VOID)
				writer.aConstNull();
			writer.returnType(type.accept(JavaTypeVisitor.INSTANCE));
		}

		statementVisitor.end();

		definition.setTag(JavaMethodInfo.class, methodInfo);
		definition.caller.setTag(JavaMethodInfo.class, methodInfo);
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
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
