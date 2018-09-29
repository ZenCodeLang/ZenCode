/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;

/**
 *
 * @author Hoofdgebruiker
 */
public abstract class BaseMemberCompiler implements MemberVisitor<Void> {
	protected final String indent;
	protected final StringBuilder output;
	protected final JavaSourceFileScope scope;
	protected final JavaSourceStatementScope fieldInitializerScope;
	protected final JavaSourceFormattingSettings settings;
	protected final TypeID expansionTarget;
	protected final HighLevelDefinition definition;
	
	private ElementType currentElementType = null;
	
	public BaseMemberCompiler(
			JavaSourceFormattingSettings settings,
			String indent,
			StringBuilder output,
			JavaSourceFileScope scope,
			TypeID expansionTarget,
			HighLevelDefinition definition)
	{
		this.indent = indent;
		this.output = output;
		this.scope = scope;
		this.settings = settings;
		this.expansionTarget = expansionTarget;
		this.definition = definition;
		
		fieldInitializerScope = new JavaSourceStatementScope(
				scope,
				settings,
				null,
				indent + settings.indent,
				null,
				null,
				true);
	}
	
	protected void begin(ElementType type) {
		if (currentElementType != null) {
			if (currentElementType != ElementType.FIELD || type != ElementType.FIELD)
				output.append(indent).append('\n');
		}
		
		this.currentElementType = type;
	}
	
	protected void override(boolean override) {
		if (override) {
			output.append(indent).append("@Override\n");
		}
	}
	
	protected void modifiers(int modifiers) {
		if (Modifiers.isPublic(modifiers))
			output.append("public ");
		if (Modifiers.isProtected(modifiers))
			output.append("protected ");
		if (Modifiers.isPrivate(modifiers))
			output.append("private ");
		if (Modifiers.isAbstract(modifiers))
			output.append("abstract ");
		if (Modifiers.isStatic(modifiers))
			output.append("static ");
		if (Modifiers.isFinal(modifiers))
			output.append("final ");
	}
	
	protected void compileBody(Statement body, FunctionHeader header) {
		if (body == null || body instanceof EmptyStatement) {
			output.append(";\n");
		} else {
			if (!(body instanceof BlockStatement))
				body = new BlockStatement(body.position, new Statement[] { body });
			
			JavaSourceStatementScope scope = new JavaSourceStatementScope(this.scope, settings, header, indent + settings.indent, null, null, expansionTarget != null);
			body.accept(new JavaSourceStatementCompiler(scope, output, true, false));
			output.append('\n');
		}
	}
	
	protected void formatParameters(boolean isStatic, FunctionHeader header) {
		formatParameters(isStatic, null, header);
	}
	
	protected void formatParameters(boolean isStatic, TypeParameter[] targetTypeParameters, FunctionHeader header) {
		output.append("(");
		boolean first = true;
		if (expansionTarget != null && !isStatic) {
			if (targetTypeParameters != null) {
				for (TypeParameter parameter : targetTypeParameters) {
					if (first)
						first = false;
					else
						output.append(", ");

					output.append("Class<").append(parameter.name).append("> typeOf").append(parameter.name);
				}
			}
			if (first)
				first = false;
			else
				output.append(", ");
			
			output.append(scope.type(expansionTarget));
			output.append(" self");
		}
		if (header.typeParameters != null) {
			for (TypeParameter typeParameter : header.typeParameters) {
				if (first)
					first = false;
				else
					output.append(", ");

				output.append("Class<")
						.append(typeParameter.name)
						.append(">")
						.append(" ")
						.append("typeOf")
						.append(typeParameter.name);
			}
		}
		
		for (int i = 0; i < header.parameters.length; i++) {
			if (first)
				first = false;
			else
				output.append(", ");
			
			FunctionParameter parameter = header.parameters[i];
			output.append(scope.type(parameter.type));
			output.append(" ").append(parameter.name);
			if (parameter.variadic)
				output.append("...");
		}
		output.append(")");
		
		if (header.thrownType != null) {
			output.append(" throws ");
			output.append(scope.type(header.thrownType));
		}
	}
	
	public enum ElementType {
		STATICINIT,
		FIELD,
		CONSTRUCTOR,
		METHOD,
		INNERCLASS
	}
}
