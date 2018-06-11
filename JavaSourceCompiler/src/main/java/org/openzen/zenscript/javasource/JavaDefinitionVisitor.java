/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaDefinitionVisitor implements DefinitionVisitor<Void> {
	private final JavaSourceFile file;
	private final StringBuilder output;
	
	public JavaDefinitionVisitor(JavaSourceFile file, StringBuilder output) {
		this.file = file;
		this.output = output;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		convertModifiers(definition.modifiers);
		output.append("class ").append(definition.name);
		if (definition.superType != null) {
			output.append(" extends ");
			output.append(definition.superType.accept(new JavaSourceTypeVisitor(file)));
		}
		List<ImplementationMember> mergedImplementations = new ArrayList<>();
		for (IDefinitionMember member : definition.members) {
			if (member instanceof ImplementationMember) {
				if (isImplementationMergable((ImplementationMember)member))
					mergedImplementations.add((ImplementationMember)member);
			}
		}
		
		if (mergedImplementations.size() > 0) {
			output.append("implements ");
			for (int i = 0; i < mergedImplementations.size(); i++) {
				if (i > 0)
					output.append(", ");
				
				ImplementationMember implementation = mergedImplementations.get(i);
				output.append(implementation.type.accept(new JavaSourceTypeVisitor(file)));
			}
		}
		
		output.append(" {\n");
		
		
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		convertModifiers(definition.modifiers);
		output.append("interface ").append(definition.name);
		output.append(" {\n");
		
		
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		convertModifiers(definition.modifiers);
		output.append("enum ").append(definition.name);
		
		output.append(" {\n");
		
		
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		convertModifiers(definition.modifiers | Modifiers.FINAL);
		output.append("class ").append(definition.name);
		output.append(" {\n");
		
		
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		convertModifiers(definition.modifiers | Modifiers.STATIC);
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		convertModifiers(definition.modifiers);
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		throw new UnsupportedOperationException("Should not arrive here");
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		return null;
	}
	
	private void convertModifiers(int modifiers) {
		if (Modifiers.isExport(modifiers) || Modifiers.isPublic(modifiers))
			output.append("public ");
		if (Modifiers.isPrivate(modifiers))
			output.append("private ");
		if (Modifiers.isProtected(modifiers))
			output.append("protected ");
		if (Modifiers.isStatic(modifiers))
			output.append("static ");
		if (Modifiers.isAbstract(modifiers))
			output.append("abstract ");
		if (!Modifiers.isVirtual(modifiers))
			output.append("final ");
	}
	
	private boolean isImplementationMergable(ImplementationMember implementation) {
		return true; // TODO: check merging conflicts
	}
}
