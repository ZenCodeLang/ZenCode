/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.Modifiers;

/**
 *
 * @author Hoofdgebruiker
 */
public class FormattingUtils {
	private FormattingUtils() {}
	
	public static void formatModifiers(StringBuilder output, int modifiers) {
		if (Modifiers.isPrivate(modifiers))
			output.append("private ");
		if (Modifiers.isProtected(modifiers))
			output.append("protected ");
		if (Modifiers.isPublic(modifiers))
			output.append("public ");
		if (Modifiers.isExport(modifiers))
			output.append("export ");
		if (Modifiers.isStatic(modifiers))
			output.append("static ");
		if (Modifiers.isAbstract(modifiers))
			output.append("abstract ");
		if (Modifiers.isVirtual(modifiers))
			output.append("virtual ");
		if (Modifiers.isFinal(modifiers))
			output.append("final ");
		if (Modifiers.isExtern(modifiers))
			output.append("extern ");
		if (Modifiers.isImplicit(modifiers))
			output.append("implicit ");
		if (Modifiers.isConst(modifiers))
			output.append("const ");
		if (Modifiers.isConstOptional(modifiers))
			output.append("const? ");
	}
}
