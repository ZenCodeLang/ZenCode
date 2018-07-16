/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.draw.DDrawnText;
import static org.openzen.drawablegui.swing.SwingCanvas.prepare;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingDrawnText extends SwingDrawnElement implements DDrawnText {
	private float x;
	private float y;
	private int color;
	private final DFont font;
	private final String text;
	
	public SwingDrawnText(SwingDrawSurface target, int z, float x, float y, int color, DFont font, String text) {
		super(target, z);
		
		this.x = x;
		this.y = y;
		this.color = color;
		this.font = font;
		this.text = text;
	}

	@Override
	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public void paint(Graphics2D g) {
		prepare(font);
		g.setColor(new Color(color, true));
		g.setFont((Font) font.cached);
		g.drawString(text, x, y);
	}
}
