/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import java.util.List;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionFormatter implements DefinitionVisitor<Void> {
	private final ScriptFormattingSettings settings;
	private final TypeFormatter typeFormatter;
	private final StringBuilder output = new StringBuilder();
	private final String indent;
	
	public DefinitionFormatter(ScriptFormattingSettings settings, TypeFormatter typeFormatter, String indent) {
		this.settings = settings;
		this.typeFormatter = typeFormatter;
		this.indent = indent;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("class ");
		output.append(definition.name);
		FormattingUtils.formatTypeParameters(output, definition.genericParameters, typeFormatter);
		output.append(" ");
		if (definition.superType != null) {
			output.append("extends ");
			output.append(definition.superType.accept(typeFormatter));
			output.append(" ");
		}
		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}
		
		MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberFormatter);
		}
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("class ");
		output.append(definition.name);
		FormattingUtils.formatTypeParameters(output, definition.genericParameters, typeFormatter);
		output.append(" ");
		
		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}
		
		MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberFormatter);
		}
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("enum ");
		output.append(definition.name);
		FormattingUtils.formatTypeParameters(output, definition.genericParameters, typeFormatter);
		output.append(" ");
		
		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}
		
		List<EnumConstantMember> enumConstants = definition.enumConstants;
		boolean first = true;
		ExpressionFormatter expressionFormatter = new ExpressionFormatter(settings, typeFormatter);
		for (EnumConstantMember enumConstant : enumConstants) {
			if (first)
				first = false;
			else
				output.append(",\n");
			
			output.append(indent).append(settings.indent).append(enumConstant.name);
			if (enumConstant.constructor != null) {
				FormattingUtils.formatCall(output, typeFormatter, expressionFormatter, enumConstant.constructor.arguments);
			}
		}
		
		if (definition.members.size() > enumConstants.size()) {
			output.append(";\n").append(indent).append(settings.indent).append("\n");

			MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
			for (IDefinitionMember member : definition.members) {
				member.accept(memberFormatter);
			}
		}
		
		output.append("}\n");
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		for (IDefinitionMember member : definition.members) {
			if (member instanceof CallerMember) {
				CallerMember caller = (CallerMember) member;
				FormattingUtils.formatModifiers(output, definition.modifiers);
				output.append("function ");
				output.append(definition.name);
				
				FormattingUtils.formatHeader(output, settings, caller.header, typeFormatter);
				FormattingUtils.formatBody(output, settings, indent, typeFormatter, caller.body);
			}
		}
		
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		return null;
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		FormattingUtils.formatModifiers(output, variant.modifiers);
		output.append("variant ");
		output.append(variant.name);
		FormattingUtils.formatTypeParameters(output, variant.genericParameters, typeFormatter);
		output.append(" ");
		
		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}
		
		List<VariantDefinition.Option> options = variant.options;
		boolean first = true;
		for (VariantDefinition.Option option : options) {
			if (first)
				first = false;
			else
				output.append(",\n");
			
			output.append(indent).append(settings.indent).append(option.name);
			if (option.types.length > 0) {
				output.append("(");
				for (int i = 0; i < option.types.length; i++) {
					if (i > 0)
						output.append(", ");
					output.append(option.types[i].accept(typeFormatter));
				}
			}
		}
		
		if (variant.members.size() > options.size()) {
			output.append(";\n").append(indent).append(settings.indent).append("\n");

			MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
			for (IDefinitionMember member : variant.members) {
				member.accept(memberFormatter);
			}
		}
		
		output.append("}\n");
		return null;
	}
	
	@Override
	public String toString() {
		return output.toString();
	}
}
