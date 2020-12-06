/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.ide.ui.view.output;

/**
 * @author Hoofdgebruiker
 */
public class BasicOutputSpan implements OutputSpan {
	private final String value;

	public BasicOutputSpan(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public String getText() {
		return value;
	}

	@Override
	public int getColor() {
		return 0xFF000000;
	}
}
