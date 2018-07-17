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
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.draw.DDrawnText;
import static org.openzen.drawablegui.swing.SwingCanvas.prepare;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingDrawnText extends SwingDrawnElement implements DDrawnText {
	private float x;
	private float y;
	private final int width;
	private final int ascent;
	private final int descent;
	private Color awtColor;
	private final Font font;
	private final String text;
	
	public SwingDrawnText(SwingDrawSurface target, int z, float x, float y, int color, DFont font, String text, int ascent, int descent, int width) {
		super(target, z);
		prepare(font);
		
		this.x = x;
		this.y = y;
		this.awtColor = new Color(color, true);
		this.font = (Font) font.cached;
		this.text = text;
		this.ascent = ascent;
		this.descent = descent;
		this.width = width;
	}

	@Override
	public void setPosition(float x, float y) {
		invalidate();
		this.x = x;
		this.y = y;
		invalidate();
	}

	@Override
	public DIRectangle getBounds() {
		return new DIRectangle((int)x, (int)y - ascent, width, ascent + descent);
	}
	
	@Override
	public void setColor(int color) {
		this.awtColor = new Color(color, true);
		invalidate();
	}

	@Override
	public void paint(Graphics2D g, DIRectangle clip) {
		g.setColor(awtColor);
		g.setFont(font);
		g.drawString(text, x, y);
	}
}
