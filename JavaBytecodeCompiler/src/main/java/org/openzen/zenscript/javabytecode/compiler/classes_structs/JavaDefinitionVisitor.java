package org.openzen.zenscript.javabytecode.compiler.classes_structs;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.javabytecode.JavaClassInfo;
import org.openzen.zenscript.javabytecode.JavaFieldInfo;
import org.openzen.zenscript.javabytecode.compiler.JavaTypeClassVisitor;

public class JavaDefinitionVisitor implements DefinitionVisitor<byte[]> {


    @Override
    public byte[] visitClass(ClassDefinition definition) {


        Type superType;
        if (definition.superType == null)
            superType = Type.getType(Object.class);
        else
            superType = Type.getType(definition.superType.accept(JavaTypeClassVisitor.INSTANCE));

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String signature = null;
        /* TODO: Calculate signature from generic parameters
        if(!definition.genericParameters.isEmpty()) {
            StringBuilder signatureBuilder = new StringBuilder();
            for (TypeParameter genericParameter : definition.genericParameters) {

            }
        }
        */


        for (IDefinitionMember member : definition.members) {
            if (member instanceof ConstructorMember) {
                for (IDefinitionMember member2 : definition.members) {
                    if (member2 instanceof FieldMember) {
                        if (!member2.hasTag(JavaClassInfo.class))
                            member2.setTag(JavaFieldInfo.class, new JavaFieldInfo(new JavaClassInfo(definition.name), ((FieldMember) member2).name, Type.getDescriptor(((FieldMember) member2).type.accept(JavaTypeClassVisitor.INSTANCE))));
                        if (!member.hasTag(JavaInitializedVariables.class))
                            member.setTag(JavaInitializedVariables.class, new JavaInitializedVariables(definition.name));
                        member.getTag(JavaInitializedVariables.class).fields.add((FieldMember) member2);


                    }
                }
            }
        }

        writer.visit(Opcodes.V1_8, definition.modifiers, definition.name, signature, superType.getInternalName(), null);
        for (IDefinitionMember member : definition.members) {
            member.accept(new JavaMemberVisitor(writer, definition.name));
        }


        writer.visitEnd();
        return writer.toByteArray();
    }

    @Override
    public byte[] visitInterface(InterfaceDefinition definition) {
        return null;
    }

    @Override
    public byte[] visitEnum(EnumDefinition definition) {
        return null;
    }

    @Override
    public byte[] visitStruct(StructDefinition definition) {
        return null;
    }

    @Override
    public byte[] visitFunction(FunctionDefinition definition) {
        return null;
    }

    @Override
    public byte[] visitExpansion(ExpansionDefinition definition) {
        return null;
    }

    @Override
    public byte[] visitAlias(AliasDefinition definition) {
        return null;
    }
}
