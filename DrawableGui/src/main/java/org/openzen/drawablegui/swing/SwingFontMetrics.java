/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.FontMetrics;
import java.awt.Graphics;
import org.openzen.drawablegui.DFontMetrics;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingFontMetrics implements DFontMetrics {
	private final FontMetrics metrics;
	private final Graphics context;
	
	public SwingFontMetrics(FontMetrics metrics, Graphics context) {
		this.metrics = metrics;
		this.context = context;
	}

	@Override
	public int getAscent() {
		return metrics.getAscent();
	}

	@Override
	public int getDescent() {
		return metrics.getDescent();
	}

	@Override
	public int getLeading() {
		return metrics.getLeading();
	}

	@Override
	public int getWidth(String str) {
		return (int) metrics.getStringBounds(str, context).getWidth();
	}

	@Override
	public int getWidth(String str, int offset, int length) {
		return (int) metrics.getStringBounds(str, offset, length, context).getWidth();
	}
}
