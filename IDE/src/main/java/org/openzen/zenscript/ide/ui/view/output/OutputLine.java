/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

/**
 *
 * @author Hoofdgebruiker
 */
public class OutputLine {
	public final OutputSpan[] spans;
	
	public OutputLine(OutputSpan[] spans) {
		this.spans = spans;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (OutputSpan span : spans)
			result.append(span.toString());
		return result.toString();
	}
}
