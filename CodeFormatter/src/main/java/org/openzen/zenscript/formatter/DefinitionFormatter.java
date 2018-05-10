/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;

/**
 *
 * @author Hoofdgebruiker
 */
public class DefinitionFormatter implements DefinitionVisitor<Void> {
	private final FormattingSettings settings;
	private final TypeFormatter typeFormatter;
	private final StringBuilder builder = new StringBuilder();
	private final String indent;
	
	public DefinitionFormatter(FormattingSettings settings, TypeFormatter typeFormatter, String indent) {
		this.settings = settings;
		this.typeFormatter = typeFormatter;
		this.indent = indent;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
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
	
	public String toString() {
		return builder.toString();
	}
}
