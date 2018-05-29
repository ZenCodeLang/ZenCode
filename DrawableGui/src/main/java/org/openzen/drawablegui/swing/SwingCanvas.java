/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Stack;
import org.openzen.drawablegui.DCanvas;
import org.openzen.drawablegui.DFont;
import org.openzen.drawablegui.DFontFamily;
import org.openzen.drawablegui.DTransform2D;
import org.openzen.drawablegui.DDrawingContext;
import org.openzen.drawablegui.DIRectangle;
import org.openzen.drawablegui.DPath;

/**
 *
 * @author Hoofdgebruiker
 */
public class SwingCanvas implements DCanvas {
	private final Graphics2D g;
	private final SwingGraphicsContext context;
	private final Stack<Rectangle> bounds = new Stack<>();
	private final Stack<AffineTransform> transformStack = new Stack<>();
	
	public SwingCanvas(Graphics2D g, SwingGraphicsContext context, DIRectangle bounds) {
		this.g = g;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		this.context = context;
		
		if (bounds != null)
			pushBounds(new DIRectangle(bounds.x, bounds.y, bounds.width, bounds.height));
	}
	
	@Override
	public void pushBounds(DIRectangle bounds) {
		this.bounds.push(g.getClipBounds());
		g.setClip(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	@Override
	public void popBounds() {
		Rectangle bounds = this.bounds.pop();
		g.setClip(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	@Override
	public void pushOffset(int x, int y) {
		transformStack.push(g.getTransform());
		g.transform(AffineTransform.getTranslateInstance(x, y));
	}
	
	@Override
	public void popOffset() {
		g.setTransform(transformStack.pop());
	}
	
	@Override
	public DIRectangle getBounds() {
		Rectangle bounds = g.getClipBounds();
		return new DIRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	@Override
	public void drawText(DFont font, int color, float x, float y, String text) {
		prepare(font);
		g.setColor(new Color(color, true));
		g.setFont((Font) font.cached);
		g.drawString(text, x, y);
	}

	@Override
	public float measureTextLength(DFont font, String text, int offset, int length) {
		prepare(font);
		g.setFont((Font) font.cached);
		Rectangle2D rect = g.getFontMetrics().getStringBounds(text, offset, length, g);
		return (float) rect.getWidth();
	}

	@Override
	public DDrawingContext getContext() {
		return context;
	}

	@Override
	public void strokePath(DPath path, DTransform2D transform, int color, float lineWidth) {
		AffineTransform old = g.getTransform();
		GeneralPath jPath = context.getPath(path);
		g.setColor(new Color(color, true));
		g.setStroke(new BasicStroke(lineWidth));
		g.transform(getTransform(transform));
		g.draw(jPath);
		g.setTransform(old);
	}

	@Override
	public void fillPath(DPath path, DTransform2D transform, int color) {
		AffineTransform old = g.getTransform();
		GeneralPath jPath = context.getPath(path);
		g.setColor(new Color(color, true));
		g.transform(getTransform(transform));
		g.fill(jPath);
		g.setTransform(old);
	}

	@Override
	public void shadowPath(DPath path, DTransform2D transform, int color, float dx, float dy, float radius) {
		// TODO
	}

	@Override
	public void fillRectangle(int x, int y, int width, int height, int color) {
		g.setColor(new Color(color, true));
		g.fillRect(x, y, width, height);
	}
	
	public static void prepare(DFont font) {
		if (font.cached != null && font.cached instanceof Font)
			return;
		
		String baseFontName = font.family == DFontFamily.CODE ? "Consolas" : Font.DIALOG;
		int style = 0;
		if (font.bold)
			style |= Font.BOLD;
		if (font.italic)
			style |= Font.ITALIC;
		
		font.cached = Font.decode(baseFontName).deriveFont(style, font.size);
	}
	
	private AffineTransform getTransform(DTransform2D transform) {
		return new AffineTransform(transform.xx, transform.xy, transform.yx, transform.yy, transform.dx, transform.dy);
	}
}
